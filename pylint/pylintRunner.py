import sys
import os
from shutil import copyfile
import subprocess

myStyleConvension = "--module-naming-style=any --const-naming-style=any --class-naming-style=PascalCase --function-naming-style=camelCase --method-naming-style=camelCase --attr-naming-style=camelCase --argument-naming-style=camelCase --variable-naming-style=camelCase --class-attribute-naming-style=camelCase --inlinevar-naming-style=camelCase"
rcPath = ""
i = 0
print("Plint started:", " ".join(sys.argv))

while not os.path.isfile(os.path.dirname(sys.path[i]) + r"\python.exe"):
    # print("--- " + os.path.dirname(sys.path[i]) + r"\python.exe")
    i += 1
scriptsFolder = os.path.dirname(sys.path[i]) + r"\Scripts"
# print(scriptsFolder)
if not os.path.isfile(scriptsFolder + r"\pylint.exe"):
    copyfile("pylint.exe", scriptsFolder + r"\pylint.exe")

if len(sys.argv) == 2:
    realCfg = sys.argv[1].split()
# else:
#     realCfg = ["", "config.file"]

if len(realCfg) == 2:
    rcPath = "--rcfile=" + realCfg[1]
    # stream = os.popen(scriptsFolder + r"\pylint.exe " + sys.argv[1])  # + " >" + sys.argv[1] + "_errorReport.txt")
    proc = subprocess.Popen(scriptsFolder + r"\pylint.exe " + rcPath + " " + myStyleConvension + " " + realCfg[0],
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    str1 = proc.stdout.read().decode('utf8', "ignore").replace("\r\n", "\n")
    # print (str1)
    with open(realCfg[0] + "_errorReport.txt", "w+", encoding="utf-8") as outFile:
        outFile.write(str1)
