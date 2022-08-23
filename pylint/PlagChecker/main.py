import os
import sys
import tokenize
import TokenDictionary
import AlgorithmHeckel


def getKGram(pathToFile):
    with open(pathToFile, 'rb') as file:
        tokens2 = TokenDictionary.FormatTokenStr(tokenize.tokenize(file.readline))
        return AlgorithmHeckel.GenerateKGram(tokens2)


####
#
# checkPlagiat
####
def checkPlagiat(path1, path2):
    if not os.path.isfile(path1) or not os.path.isfile(path2):
        return -1

    # both files exist
    kGram1 = getKGram(path1)
    kGram2 = getKGram(path2)
    return 1 - round(AlgorithmHeckel.rateHeckel(kGram1, kGram2), 2)


if __name__ == '__main__':
    # with open(file2, 'rb') as f2:
    #     tokens2 = TokenDictionary.FormatTokenStr(tokenize.tokenize(f2.readline))
    #     kGram2 = AlgorithmHeckel.GenerateKGram(tokens2)
    #
    # print("Приблизительная оценка плагиата: " + str(round(AlgorithmHeckel.rateHeckel(kGram1, kGram2), 2)))
    if len(sys.argv) < 3:
        print("-2")
        exit(-2)
    print(checkPlagiat(sys.argv[1], sys.argv[2]))

