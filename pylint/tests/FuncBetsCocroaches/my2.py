from functools import reduce
from itertools import permutations
print(*
    (lambda num:
        (lambda myList:
            next(
                filter(
                    lambda perm:
                        reduce(
                            lambda x,y:
                                x * ((perm.index(int(y[0])) < perm.index(int(y[1]))) ^ (perm.index(int(y[2])) < perm.index(int(y[3])))),
                            myList
                        ),
                    permutations(
                        range(1,int(num[0])+1)
                    )
                ),
                [0]
            )
        )(
            [1] + list(
                map(
                    lambda t: 
                        input().split(),
                    range(int(num[1]))
                )
            )
        )
    )(
        input().split()
    )
)