from TreeNode import TreeNode

if __name__ == "__main__":
	print("Test #8")
	root = TreeNode(1)
	root.left = TreeNode(0)
	root.left.left = TreeNode(0)
#	root.left.right = TreeNode(0)
	root.right = TreeNode(0)
	root.right.right = TreeNode(0)
	root.right.left = TreeNode(0)
	print(isCompleteTree(root))
