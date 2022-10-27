from solution import solveDistanceTask
import random


class InputData:
    distance: int
    tankSize: int
    numberOfStops: int
    stops: str
    result: str

    def __init__(self, d, t, n, stopsInput, answer):
        self.distance = d
        self.tankSize = t
        self.numberOfStops = n
        self.stops = stopsInput
        self.result = answer


def saveResults(dataList):
    i = 1
    for inputData in dataList:
        result = inputData.result
        inputData.__dict__.pop("result")
        with open(f"carFuelling/test{i}.t", "w+", encoding="utf8") as f:
            f.writelines("\n".join(map(str, inputData.__dict__.values())))
        with open(f"carFuelling/test{i}.a", "w+", encoding="utf8") as f:
            f.write(str(result))
        i += 1

        inputData.__dict__["result"] = result


if __name__ == '__main__':
    maxStops = 0
    while maxStops < 7:
        numberOfTests = 99
        data = list()

        for i in range(numberOfTests):
            stops = set()
            distance = random.randrange(10, 100)
            tankSize = random.randrange(1, 30)
            numberOfStops = random.randrange(1, 10)
            while len(stops) < numberOfStops:
                stops.add(random.randrange(1, distance))
            listOfStops = sorted(stops)

            stopsStr = " ".join(map(str, listOfStops))
            res = solveDistanceTask(distance, tankSize, numberOfStops, stopsStr)

            data.append(InputData(distance, tankSize, numberOfStops, stopsStr, res))
        saveResults(data)

        maxStops = max(map(lambda dataItem: dataItem.result, data))
        print(maxStops)
