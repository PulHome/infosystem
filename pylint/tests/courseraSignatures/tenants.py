# import chess
# import chess.svg
#
# board = chess.Board()
# a = "АО "
# print(board)
#
#
import copy

#print(any([1,2,2]))


input1 = """
2
1 3
1 5
"""

output = """
1
1
"""


check = lambda x, y: len(xList := x.strip().split("\n")) == len(yList := y.strip().split("\n")) and len(xList) > 1 and len(yList) > 1 and int(xList[0]) == int(yList[0]) and set(map(int,yList[1].split())) == set(map(int, xList[1].split()))

livers = list()
liversN = int(input())
for i in range(liversN):
    livers.append(list(map(int, input().split())))

maxI = livers[0][0]
for i in range(liversN):
    maxI = max(maxI, *livers[i])
#print(livers)
dictOfInterceptions = {}
reply = list()
while livers:
    dictOfInterceptions = {}
    for hour in range(maxI+1):
        for j in range(len(livers)):
            if livers[j][0] <= hour <= livers[j][1]:
                dictOfInterceptions[hour] = dictOfInterceptions.setdefault(hour, 0) + 1

    maxInterceptionMinPoint = sorted(dictOfInterceptions.items(), key=lambda item: (item[1], -item[0]), reverse=True)[0][0]
    reply.append(maxInterceptionMinPoint)
    #print(maxInterceptionMinPoint)

    #delete from dict all with maxInterceptionMinPoint
    newLivers = list()
    for i in range(len(livers)):
        if maxInterceptionMinPoint < livers[i][0] or maxInterceptionMinPoint > livers[i][1]:
            newLivers.append(livers[i])

    livers = copy.deepcopy(newLivers)
    #print("Left livers\n", livers)

print(len(reply))
print(*reply)
