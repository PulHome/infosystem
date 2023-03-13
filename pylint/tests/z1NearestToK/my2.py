def transpose(matr):
    retVal = list()
    for i in range(len(matr)):
        retVal.append([0] * n)

    for i in range(len(matr)):
        for j in range(len(matr)):
            retVal[j][i] = matr[i][j]
    return retVal


n, k = map(int, input().split())

matrix = list()

for i in range(n):
    matrix.append(list(map(int, input().split())))

transposedM = transpose(matrix)

for i in range(n):
    transposedM[i] = sorted(transposedM[i], key=lambda item: (abs(item - k), item))

res = transpose(transposedM)
for i in range(n):
    print(*res[i])
