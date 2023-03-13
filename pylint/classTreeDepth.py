#from TreeNode import TreeNode


def invertTree(root):
    node = root

    def invert(root):
        if root:
            interval = root.left
            root.left = root.right
            root.right = interval
            invert(root.left)
            invert(root.right)
        return 0

    invert(root)
    return node


from sys import stdin

if __name__ == "__main__": exec(stdin.read())