from TreeNode import TreeNode
from classTreeDepth import invertTree

if __name__ == "__main__":
	print("Test #4")
	root = TreeNode(0)
	root.right = TreeNode(20)
	root.right.right = TreeNode(22)
	root.right.right.right = TreeNode(22)
	print(maxDepth(root))
