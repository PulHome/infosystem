# код проверяльщика задач, версия 2024.33
import json
import os
import subprocess
import sys
import re

from shutil import copy2
from typing import List

from pep9 import funcCheck
from localization import Locale
from testReport import TestReport

import xmlSortChecker


# add non default complex checks here
def executeChecker(checker, cmd, input, correctAnswers):
    if checker == "xmlSortChecker":
        return xmlSortChecker.check(cmd, input, correctAnswers[0])

    return False


# read the correct answer for test## from .a file
def readAnswerFile(pathToFile):
    if os.path.isfile(pathToFile):
        return open(pathToFile, "r", encoding="utf-8").read()


def readConfing(pathTocfg):
    dictOfConfigs = dict()
    if os.path.isfile(pathTocfg):
        fileContent = open(pathTocfg, "r", encoding="utf-8").read()
        keypairs = fileContent.split("\t")
        dictOfConfigs = {k: v for k, v in map(lambda x: x.split("=", 1), keypairs)}
    # В словарь записан полный конфиг. В поле func функция проверки
    if "func" not in dictOfConfigs:
        dictOfConfigs["func"] = "contains"

    if dictOfConfigs["func"] == "contains":
        dictOfConfigs["func"] = lambda x, y: x.lower() in y.lower()
    elif dictOfConfigs["func"] == "any":
        dictOfConfigs["func"] = None
    elif "lambda" in dictOfConfigs["func"]:
        lambdaStr = dictOfConfigs["func"]
        dictOfConfigs["func"] = lambda x, y: eval(lambdaStr)(x, y)

    return dictOfConfigs


def checkCrashExists(userAnswer):
    return "traceback (most recent call last):" in userAnswer.lower()


def cutPrivateData(userAnswer):
    result = list()
    for line in userAnswer.split("\n"):
        if r'if __name__ == "__main__": exec(stdin.read())' in line:
            continue

        tempArray = line.split("\"", 2)
        if len(tempArray) > 1:
            tempArray[1] = "\"" + tempArray[1][tempArray[1].rfind("\\") + 1:] + "\""
            result.append("".join(tempArray))
        else:
            result.append(line)

        result.append("\n")

    return "".join(result)


def processAndTrimAnswer(answer):
    return answer.strip().replace(" \n", "\n") if answer else None


def cleanMainFromFileToCheck(fileToCheck):
    isMainFound = False
    sourceFileWithoutMain = ""
    for line in open(fileToCheck, "r", encoding="utf-8").readlines():
        if line.strip().startswith("if __name__"):
            isMainFound = True
            break
        sourceFileWithoutMain += line

    if isMainFound:
        text_file = open(fileToCheck, "w+", encoding="utf-8")
        text_file.write(sourceFileWithoutMain)
        text_file.close()

    return isMainFound


def addExecStdIntoTheEndOfFile(fileToCheck):
    text_file = open(fileToCheck, "a+", encoding="utf-8")
    text_file.write("\n\nfrom sys import stdin\n\nif __name__ == \"__main__\": exec(stdin.read())")
    text_file.close()


def addCustomMain(fileToCheck, funcToCall):
    text_file = open(fileToCheck, "a+", encoding="utf-8")
    text_file.write(f"\n\nif __name__ == \"__main__\": {funcToCall}()")
    text_file.close()


def prettyPrintRetArray(retArray: List[TestReport]):
    i = 0
    for reportLine in retArray:
        if reportLine[0] == TestReport.TEST:
            print(f"test{i + 1} - ", end="")
            if str(reportLine[1]) == "True":
                print(Locale.Passed)
            else:
                print(Locale.Failed)
            i += 1
        else:
            print(reportLine[1])


def checkConfigurationAndRestrictions(fileToCheck, testConfiguration):
    if "functional" in testConfiguration:
        sourceFileWithoutHeader = ""
        for line in open(fileToCheck, "r", encoding="utf-8").readlines():
            if line.strip().startswith("#"): continue
            sourceFileWithoutHeader += line + "\n"

        if not funcCheck(sourceFileWithoutHeader):
            print(f"{Locale.NotInFunctional}\n{Locale.Failed}")
            exit()

    if "deny" in testConfiguration:
        denyValues = list(map(lambda x: x.strip(), str(testConfiguration["deny"]).split(',')))
        sourceCode = open(fileToCheck, "r", encoding="utf-8").readlines()
        cleanSourceCode = map(lambda line:
                              line[:len(line) + 1 + line.find("#")], sourceCode)
        for value in denyValues:
            if value in cleanSourceCode:
                print(f"{Locale.HaveRestricted}\n{Locale.Failed}")
                exit()

    if "must" in testConfiguration:
        mustValues = set(map(lambda x: x.strip(), str(testConfiguration["must"]).split(',')))
        foundValues = set()
        sourceCode = open(fileToCheck, "r", encoding="utf-8").readlines()
        cleanSourceCode = "".join(map(
            lambda codeLine:
            (" " + codeLine) if ("#" not in codeLine) else codeLine.split("#")[0], sourceCode))

        for value in mustValues:
            if value in cleanSourceCode:
                foundValues.add(value)

        if len(foundValues) != len(mustValues):
            print(Locale.DoesntHaveMandatory)
            print(Locale.Failed)
            exit()


def getCorrectAnswers(dirWithTests, fileWithTests):
    correctAnswers = list()
    # как минимум 1 правильный ответ должен быть, иначе считаем любой ответ правильным
    readAnswer = readAnswerFile(dirWithTests + fileWithTests[:fileWithTests.rfind(".")] + ".a")
    if readAnswer is None:
        readAnswer = ' '

    correctAnswers.append(readAnswer)

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


def main():
    ################
    # Manual Config Section
    ################
    maxExecutionTimeDelay = 2  # max timeout for a task
    easyMode = False  # в этом режиме показываются входные данные для упавших тестов.
    fileToCheck = "_poisk.py"
    dirToCheck = "regGameSettings"
    # dirToCheck = "regFindReplaceRepeated"
    retArray = list()

    extraDataForEasyMode = ""
    if len(sys.argv) > 2:
        fileToCheck = sys.argv[1]
        dirToCheck = sys.argv[2]
        if len(sys.argv) > 3:
            easyMode = True if sys.argv[3] else easyMode
            # easyMode = False

    dirWithTests = ".\\tests\\" + dirToCheck + "\\"
    testConfiguration = readConfing(dirWithTests + "config.conf")

    # для функционального программирования еще ограничение: одна инструкция языка.
    checkConfigurationAndRestrictions(fileToCheck, testConfiguration)

    # для всех файлов .t с входными данными
    testFiles = sorted(filter(lambda x: x.endswith(".t"), os.listdir(dirWithTests)), key=lambda x: int(x[4:-2]))

    for file in testFiles:
        if os.path.isfile(dirWithTests + file) and file.endswith(".t"):
            needToCompileTestData = testConfiguration.get("needToCompileTestData", None)
            needToAddMain = testConfiguration.get("needToAddMain", None)
            funcToCallInMain = testConfiguration.get("funcToCallInMain", None)
            inputDataFile = testConfiguration.get("input", "input.txt")
            maxExecutionTimeDelay = float(testConfiguration.get("timeout", maxExecutionTimeDelay))

            copy2(dirWithTests + file, inputDataFile)
            if needToCompileTestData:
                cleanMainFromFileToCheck(fileToCheck)
                addExecStdIntoTheEndOfFile(fileToCheck)

            if needToAddMain and funcToCallInMain:
                cleanMainFromFileToCheck(fileToCheck)
                addCustomMain(fileToCheck, funcToCallInMain)

            # надо проверить .a файлы с ответами
            correctAnswers = getCorrectAnswers(dirWithTests, file)

            if testConfiguration.get("doUseStandAloneTests"):
                cmd = f"python -u {fileToCheck}"
                if executeChecker(testConfiguration.get("func"), cmd, inputDataFile, correctAnswers):
                    print(Locale.Passed)
                else:
                    print(Locale.Failed)

                return

            ### Костылик - когда не надо ничего запускать, все что здано - верно.
            if not testConfiguration.get("func"):
                print(Locale.Passed)
                return

            proc = subprocess.Popen(["python", "-u", fileToCheck],
                                    stdout=open("output", "w+", encoding="utf-8"),
                                    stderr=subprocess.STDOUT,
                                    stdin=open(inputDataFile, encoding="utf-8"),
                                    )

            # ждем отклика в течение таймаута, в outs - результат работы программы
            try:
                outs, errs = proc.communicate(timeout=maxExecutionTimeDelay)
            except subprocess.TimeoutExpired:
                proc.kill()
                outs, errs = proc.communicate()
                retArray.append(TestReport(TestReport.OTHER, Locale.Timeout))
                break
            finally:
                if "input" in testConfiguration and os.path.exists(str(testConfiguration["input"])):
                    os.remove(str(testConfiguration["input"]))

            # функция проверки правильного ответа - пока единственный обязательный параметр конфига
            funcToCheckAnswer = testConfiguration.get("func", None)
            if funcToCheckAnswer is None:
                break

            # кодировочный костыль, иногда приходит в кодировке ansii
            try:
                userAnswer = open("output", "r", encoding="utf-8").read().replace("\r\n", "\n")
            except UnicodeDecodeError:
                userAnswer = open("output", "r", encoding="cp1251").read().replace("\r\n", "\n")

            if checkCrashExists(userAnswer):
                userAnswer = cutPrivateData(userAnswer)
                extraDataForEasyMode = open(dirWithTests + file, encoding="utf-8").read()
                if easyMode and extraDataForEasyMode:
                    retArray.append(TestReport(TestReport.OTHER, Locale.EasyModeHelp % extraDataForEasyMode))

                retArray.append(TestReport(TestReport.OTHER, Locale.CrashFound))
                retArray.append(TestReport(TestReport.OTHER, userAnswer))
                break  # программа пользователя упала, дальше не надо.

            userAnswer = processAndTrimAnswer(userAnswer)
            correctAnswers = list(map(processAndTrimAnswer, correctAnswers))

            # Костыль для случаев, когда любой ответ - не падение верный
            if len(correctAnswers) > 0 and \
                    (correctAnswers[0] is None or
                     correctAnswers[0].strip() == ""):
                retArray.append(TestReport(TestReport.TEST, "True"))
                continue

            # tricky check for random tasks - if answer could be divided into 23
            isAnswerCorrect = False
            if "answer_code" in testConfiguration:
                if testConfiguration["answer_code"] == "mod23":
                    value = re.search(r"\d+", userAnswer)
                    if value is None:
                        print("Your answer was not found!")
                        print(Locale.Failed)
                        return
                    clearAnswer = int(value.group(0))
                    isAnswerCorrect = (clearAnswer % 23 == 0)
            elif userAnswer is not None and correctAnswers[0] is not None:
                for aCorrectAnswer in correctAnswers:
                    isAnswerCorrect |= funcToCheckAnswer(aCorrectAnswer, userAnswer)

            retArray.append(TestReport(TestReport.TEST, isAnswerCorrect))

            if not isAnswerCorrect:
                extraDataForEasyMode = open(dirWithTests + file, encoding="utf-8").read()
                if easyMode:
                    retArray.append(TestReport(TestReport.OTHER, Locale.ReceivedAnswer))
                    retArray.append(TestReport(TestReport.OTHER, userAnswer))
                    retArray.append(TestReport(TestReport.OTHER, Locale.EasyModeHelp % extraDataForEasyMode))
                if "ContinueIfTestFailed" not in testConfiguration:  # для толстых программ
                    break  # программа пользователя выдала неверный результат, дальше не надо.

    if len(retArray) == 0:
        print(Locale.GeneralError)
    else:
        prettyPrintRetArray(retArray)

    # костыль для Java-стороны Locale.Passed в последней строке вывода значит, что задача принята.
    if retArray and all(map(lambda x: str(x[1]) == "True", retArray)):
        print(Locale.Passed)
    else:
        print(Locale.Total, Locale.Failed, sep="\n")


if __name__ == "__main__":
    main()
