def getNumberOfMaxElements():
    i = -1
    l = list()
    while i != 0:
        i= int(input())
        l.append(i)
    print(l.count(max(l)))