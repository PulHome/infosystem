class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

    def printTree(root):
        if root.left:
            TreeNode.printTree(root.left)
        if root.right:
            TreeNode.printTree(root.right)
        print(root.val, end=" ")
