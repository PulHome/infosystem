from itertools import permutations

print(
    *(list(list(
        *map(lambda a: list(
            map(lambda z: next(filter(lambda x: all(map(lambda y: ((x.index(z[y][0]) < x.index(z[y][1])) ^
                                                                   (x.index(z[y][2]) < x.index(
                                                                       z[y][3]))), range(len(list(z))))),
                                      permutations(range(1, int(a[0]) + 1)))),
                [list(map(lambda inp: list(map(int, inp.split())),
                          map(lambda i: input(), range(int(a[1])))))])), [input().split()]))) + [
          [0]])[0])