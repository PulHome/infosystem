if __name__ == "__main__":
	print("Test #1")
	root = TreeNode(0)
	root.left = TreeNode(10)
	root.right = TreeNode(2)
	invertTree(root)
	TreeNode.printTree(root)
	