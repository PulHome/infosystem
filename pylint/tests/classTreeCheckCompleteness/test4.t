from TreeNode import TreeNode

if __name__ == "__main__":
	print("Test #4")
	root = TreeNode(0)
	root.right = TreeNode(0)
	root.right.right = TreeNode(9)
	root.right.right.right = TreeNode(9)
	print(isCompleteTree(root))
