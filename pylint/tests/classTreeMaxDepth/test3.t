from TreeNode import TreeNode
from classTreeDepth import invertTree

if __name__ == "__main__":
	print("Test #3")
	root = TreeNode(0)
	root.left = TreeNode(10)
	root.left.left = TreeNode(11)
	root.right = TreeNode(20)
	root.right.right = TreeNode(22)
	print(maxDepth(root))
