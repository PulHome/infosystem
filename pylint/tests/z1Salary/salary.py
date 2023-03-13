class MyComparator(str):
    def __lt__(self, other):
        return int(self + other) < int(other + self)


if __name__ == "__main__":
    print("".join(map(lambda el: "".join(el), sorted(input().split(), key=MyComparator, reverse=True))))
