def GenerateKGram(tokensStr):
    kGram = set()
    for i in range(0, len(tokensStr)):
        kGram.add(" ".join(tokensStr[i: i + 4]))
    return kGram


def rateHeckel(set1: set, set2: set):
    return len(set1.intersection(set2)) / len(set1.union(set2))