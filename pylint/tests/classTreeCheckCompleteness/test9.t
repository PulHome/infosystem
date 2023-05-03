from TreeNode import TreeNode

if __name__ == "__main__":
	print("Test #9")
	root = TreeNode(1)
	root.left = TreeNode(2)
	root.left.left = TreeNode(5)
	root.right = TreeNode(3)
	root.right.right = TreeNode(7)
	root.right.left = TreeNode(8)
	print(isCompleteTree(root))
