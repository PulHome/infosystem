import json
import sys
import os
from shutil import copyfile
import subprocess

crunchForLintCrash = "Unhandled Exception: System.InvalidOperationException: Sequence contains no matching element"

def loadList(exclusionsFile):
    exclusionsList = list()
    with open(exclusionsFile) as file:
        for line in file.readlines():
            if not line.startswith("#"):
                exclusionsList = line.split(",")

    exclusionsList = list(map(lambda el: el.strip(), exclusionsList))
    return exclusionsList


def main():
    listOfFoundErrors = list()
    csLintPath = os.path.abspath(r".\csharpLint\CSharpLint.exe")
    rcPath = ""
    i = 0

    if len(sys.argv) < 2:
        return

    fileToCheckPath = os.path.abspath(sys.argv[1])
    proc = subprocess.Popen(csLintPath + " " + fileToCheckPath,
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    outStr = proc.stdout.read().decode('utf8', "ignore").replace("\r\n", "\n")
    
    if crunchForLintCrash in outStr:
        listOfFoundErrors.append("[Lint hates this] Please correct the line like \"variable1 = (variable2)\". Fix and retry lint.")
    else:
        listOfFoundErrors = json.loads(outStr)  # list of dicts
        listOfIgnoredErrors = loadList(os.path.abspath(r".\csharpLint\exclusions.cfg"))
        listOfFoundErrors = list(filter(lambda er: er.get("Id") not in listOfIgnoredErrors, listOfFoundErrors))

    # print (str1)
    print("\n".join(map(str, listOfFoundErrors)))
    print(f"Errors: {len(listOfFoundErrors)}\n")


if __name__ == "__main__":
    main()
