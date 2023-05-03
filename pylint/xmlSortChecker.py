import os
import xml.etree.ElementTree as ET
from xml.etree.ElementTree import tostring
import re
import subprocess
import sys

maxExecutionTimeDelay = 3


def checkCorrrectAnswer(correctAnswer):
    etalonRoot = ET.fromstring(correctAnswer)
    etalonReplay = etalonRoot.find(".//outPutFile")
    etalonStrRaw = etalonReplay.text

    fileName = etalonReplay.attrib['name']
    if not os.path.isfile(fileName):
        print(f"Reply was not found. Was looking for {fileName}")
        return False
    userAnswer = ET.parse(fileName)
    userRoot = userAnswer.getroot()
    userReplyString = tostring(userRoot, encoding='utf8', method='xml')

    etalonParsed = ET.fromstring(etalonStrRaw)
    etalonString = tostring(etalonParsed, encoding='utf8', method='xml')
    pattern = r"\s"
    users = re.sub(pattern, "", userReplyString.decode('utf-8'))
    etalons = re.sub(pattern, "", etalonString.decode('utf-8'))
    if users == etalons:
        #os.remove(fileName)
        return True
    else:
        print("Recieved:\n", userRoot)
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
        # retArray.append("Timeout")

    # finally:
    #     os.remove(inputFiles)

    return checkCorrrectAnswer(correctAnswer)
