import sys


def fillParents(aTree, el):
    parents = [el]
    currentParent = aTree.get(el)
    while currentParent is not None:
        parents.append(currentParent)
        currentParent = aTree.get(currentParent)

    return parents


if __name__ == "__main__":
    tree = {"1": None}
    n = int(input())
    for line in sys.stdin:
        command, parent, child = line.split()
        if command == "ADD":
            tree[child] = parent
            continue
        # getPath to Root
        first = parent
        second = child
        firstParents = fillParents(tree, first)
        secondParents = fillParents(tree, second)

        firstParents.reverse()
        secondParents.reverse()

        #задача сведена к поиску последнего одинакового элемента
        step = 0
        commonVertex = firstParents[0]  # root Vertex
        while step < min(len(firstParents), len(secondParents)):
            if firstParents[step] == secondParents[step]:
                commonVertex = firstParents[step]
                step += 1
            else:
                break

        print(commonVertex)
