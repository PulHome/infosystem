import os
import xml.etree.ElementTree as ET
from xml.etree.ElementTree import tostring
import subprocess

maxExecutionTimeDelay = 3

def removeSpaces(binStr):
    return binStr.decode('utf-8').replace("\n", "").replace("\t", "").replace(" ", "")


def checkCorrrectAnswer(correctAnswer):
    etalonRoot = ET.fromstring(correctAnswer)
    userReplyFile = etalonRoot.find(".//outPutFile")
    userReply = userReplyFile.text

    fileName = userReplyFile.attrib['name']
    if not os.path.isfile(fileName):
        print(f"Your reply was not found. Was looking for .\\{fileName}")
        return False
    userAnswer = ET.parse(fileName)
    userRoot = userAnswer.getroot()
    userReplyString = tostring(userRoot, encoding='utf8', method='xml')

    etalonParsed = ET.fromstring(userReply)
    etalonString = tostring(etalonParsed, encoding='utf8', method='xml')

    if removeSpaces(userReplyString) == removeSpaces(etalonString):
        os.remove(fileName)
        return True
    else:
        print("Recieved:\n", userReplyString)
        print("Expected:\n", etalonString)
        return False


def createInputFiles(inputFile):
    fileNames = list()
    xml = ET.parse(inputFile)
    listOfInputFiles = xml.findall(".//inputFile")

    for inputFile in listOfInputFiles:
        fileNames.append(inputFile.attrib['name'])
        open(fileNames[-1], "w+", encoding="utf-8").write(inputFile.text)
    return fileNames


def check(cmd, inputDataFile, correctAnswer):
    if not os.path.isfile(inputDataFile):
        print(os.path.basename(__file__), inputDataFile, " not exist")
        return False
    inputFiles = createInputFiles(inputDataFile)
    proc = subprocess.Popen(cmd + " " + " ".join(inputFiles),
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
        retArray.append("Timeout")

    # finally:
    #     os.remove(inputFiles)

    return checkCorrrectAnswer(correctAnswer)
