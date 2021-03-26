# код проверяльщика задач, версия 2019.23

import os
import subprocess
import sys
from localization import Locale

from shutil import copy2


# read the correct answer for test## from .a file
def readAnswerFile(pathToFile):
    if os.path.isfile(pathToFile):
        return open(pathToFile, "r", encoding="utf-8").read()


def readConfing(pathTocfg):
    dictOfConfigs = dict()
    if os.path.isfile(pathTocfg):
        fileContent = open(pathTocfg, "r", encoding="utf-8").read()
        keypairs = fileContent.split("\t")
        dictOfConfigs = {k: v for k, v in map(lambda x: x.split("="), keypairs)}
    # В словарь записан полный конфиг. В поле func функция проверки
    if "func" in dictOfConfigs:
        if dictOfConfigs["func"] == "contains":
            dictOfConfigs["func"] = lambda x, y: x.lower() in y.lower()
        else:
            dictOfConfigs["func"] = lambda x, y: (x.strip() == y.strip())
    return dictOfConfigs


def checkCrashExists(userAnswer):
    return "exception" in userAnswer.lower()


def processAndTrimAnswer(answer):
    return answer.strip().replace(" \n", "\n")


def prettyPrintRetArray(retArray):
    i = 0
    while i < len(retArray):
        print(f"test{i + 1} - ", end="")
        if str(retArray[i]) == "True":
            print(Locale.Passed)
            i += 1
        else:
            print(Locale.Failed)
            i += 1
            break
    while i < len(retArray):
        print(retArray[i])
        i += 1


def getCorrectAnswers(dirWithTests, fileWithTests):
    correctAnswers = list()
    # как минимум 1 правильный ответ должен быть, иначе считаем любой ответ правильным
    correctAnswer = readAnswerFile(dirWithTests + fileWithTests[:fileWithTests.rfind(".")] + ".a")
    if correctAnswer is None:
        correctAnswer = ""
    correctAnswers.append(correctAnswer)

    # вдруг будут тесты с более чем 1 правильными ответами
    i = 1
    otherAnswerExists = os.path.isfile(dirWithTests + fileWithTests[:fileWithTests.rfind(".")] + ".a" + str(i))

    while otherAnswerExists:
        correctAnswer2 = readAnswerFile(dirWithTests + fileWithTests[:fileWithTests.rfind(".")] + ".a" + str(i))
        if correctAnswer2 is None:
            break
        correctAnswers.append(correctAnswer2)
        i += 1
    return correctAnswers


################
# Manual Config Section

easyMode = False  # в этом режиме показываются входные данные для упавших тестов.
maxExecutionTimeDelay = 2  # max timeout for a task

################


if __name__ == "__main__":
    fileToCheck = "Desks.class"
    dirToCheck = "buyingNewDesks"
    # dirToCheck = "regFindReplaceRepeated"
    retArray = list()

    extraDataForEasyMode = ""

    if len(sys.argv) > 2:
        fileToCheck = sys.argv[1]
        dirToCheck = sys.argv[2]
        if len(sys.argv) > 3:
            easyMode = True if sys.argv[3] else easyMode
    dirWithTests = ".\\tests\\" + dirToCheck + "\\"
    if not os.path.exists(dirWithTests):
        dirWithTests = "..\\pylint\\tests\\" + dirToCheck + "\\"
    testConfiguration = readConfing(dirWithTests + "config.conf")
    # print(os.path.abspath(dirWithTests))

    # для всех файлов .t с входными данными
    testFiles = sorted(filter(lambda x: x.endswith(".t"), os.listdir(dirWithTests)), key=lambda x: int(x[4:-2]))
    for file in testFiles:
        if os.path.isfile(dirWithTests + file) and file.endswith(".t"):
            inputDataFile = testConfiguration.get("input", "input.txt")
            copy2(dirWithTests + file, inputDataFile)
            proc = subprocess.Popen(["java", "-Dfile.encoding=utf-8", fileToCheck.replace(".class", "")],
                                    stdout=open("output", "w+", encoding="utf-8"),
                                    stderr=subprocess.STDOUT,
                                    stdin=open(inputDataFile, encoding="utf-8"),
                                    )
            # вычитываем исходный .t файл и помещаем всё содержимое во входящий поток к тестируемой программе
            # if "input" in testConfiguration:
            #     copy2(myDir + file, str(testConfiguration["input"]))
            #     inputDataForUsersProgram = open(str(testConfiguration["input"]), "rb").read()
            # else:
            #     inputDataForUsersProgram = open(myDir + file, "rb").read()
            #     proc.stdin.write(inputDataForUsersProgram)

            # ждем отклика в течение таймаута, в outs - результат работы программы
            try:
                outs, errs = proc.communicate(timeout=maxExecutionTimeDelay)
            except subprocess.TimeoutExpired:
                proc.kill()
                outs, errs = proc.communicate()
                retArray.append("Timeout")
                break
            finally:
                if "input" in testConfiguration and os.path.exists(str(testConfiguration["input"])):
                    os.remove(str(testConfiguration["input"]))

                # надо проверить .a файлы с ответами
                correctAnswers = getCorrectAnswers(dirWithTests, file)

                # функция проверки правильного ответа - пока единственный обязательный параметр конфига
            funcToCheckAnswer = testConfiguration.get("func", None)
            if funcToCheckAnswer is None:
                break

            # кодировочный костыль, иногда приходит в кодировке анси
            try:
                userAnswer = open("output", "r", encoding="utf-8").read().replace("\r\n", "\n")
            except UnicodeDecodeError:
                userAnswer = open("output", "r", encoding="cp1251").read().replace("\r\n", "\n")

        if checkCrashExists(userAnswer):
            extraDataForEasyMode = open(dirWithTests + file, encoding="utf-8").read()
            if easyMode and extraDataForEasyMode:
                retArray.append(Locale.EasyModeHelp % extraDataForEasyMode)

            retArray.append(Locale.CrashFound)
            retArray.append(userAnswer)
            break  # программа пользователя упала, дальше не надо.

        userAnswer = processAndTrimAnswer(userAnswer)
        correctAnswers = list(map(processAndTrimAnswer, correctAnswers))

        # Костыль для случаев, когда любой ответ - не падение верный
        if len(correctAnswers) > 0 and correctAnswers[0].strip() == "":
            retArray.append(True)
            continue

        # tricky check for random tasks - if answer could be divided into 23
        isAnswerCorrect = False
        if "answer_code" in testConfiguration:
            if testConfiguration["answer_code"] == "mod23":
                isAnswerCorrect = (int(userAnswer) % 23 == 0)
        elif userAnswer is not None:
            for aCorrectAnswer in correctAnswers:
                isAnswerCorrect |= funcToCheckAnswer(aCorrectAnswer, userAnswer)

        retArray.append(isAnswerCorrect)

        if not isAnswerCorrect:
            extraDataForEasyMode = open(dirWithTests + file, encoding="utf-8").read()
            # print(correctAnswer)
            # if easyMode:
            #    retArray.append(Locale.YourAnswer);
            #    retArray.append(userAnswer)
            if "ContinueIfTestFailed" not in testConfiguration:  # для толстых программ
                break  # программа пользователя выдала неверный результат, дальше не надо.

    if len(retArray) == 0:
        print(Locale.GeneralError)
    else:
        prettyPrintRetArray(retArray)

    if len(set(retArray)) == 1 and str(retArray[0]) == "True":
        print(Locale.Passed)

    else:
        if easyMode and extraDataForEasyMode:
            print(Locale.EasyModeHelp % extraDataForEasyMode)
        print(Locale.Failed)
