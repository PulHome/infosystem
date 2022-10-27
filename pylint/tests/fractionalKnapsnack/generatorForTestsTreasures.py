import copy

from solution import solveDistanceTask
import random

from treasure_bag_1 import solveLootTask


class InputData:
    bagAndLootSizes: str
    loot: str
    result: str

    def __init__(self, _lootSize, _bagSize, _loot, answer):
        self.bagAndLootSizes = f"{_lootSize} {_bagSize}"
        self.loot = "\n".join(_loot)
        self.result = answer


def saveResults(dataList, taskName):
    i = 1
    for inputData in dataList:
        result = inputData.result
        inputData.__dict__.pop("result")
        with open(f"{taskName}/test{i}.t", "w+", encoding="utf8") as f:
            f.writelines("\n".join(map(str, inputData.__dict__.values())))
        with open(f"{taskName}/test{i}.a", "w+", encoding="utf8") as f:
            f.write(str(result))
        i += 1

        inputData.__dict__["result"] = result


if __name__ == '__main__':
    maxStops = 0
    numberOfTests = 99
    data = list()
    res = 0
    for i in range(numberOfTests):
        bagSize = random.randrange(10, 500)
        lootSize = random.randrange(1, 50)
        loot = set()
        while len(loot) < lootSize:
            loot.add(f"{random.randrange(1, 500)} {random.randrange(1, 500)}")

        res = solveLootTask(lootSize, bagSize, list(loot))
        print(res)
        data.append(InputData(lootSize, bagSize, loot, res))

    taskName = "fractionalKnapsnaсk"
    saveResults(data, taskName)

