if __name__ == "__main__":
	def printTree(root):
		if root.left:
			printTree(root.left)
		if root.right:
			printTree(root.right)
		print(root.val, end=" ")
	

	print("Test #1")
	root = TreeNode(0)
	root.left = TreeNode(10)
	root.right = TreeNode(2)
	TreeNode.invertTree(root)
	printTree(root)
	