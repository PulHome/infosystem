from TreeNode import TreeNode

if __name__ == "__main__":
	print("Test #2")
	root = TreeNode(1)
	root.left = TreeNode(1)
	root.left.left = TreeNode(1)
	root.right = TreeNode(2)
	root.right.right = TreeNode(2)
	print(sumDeepestLeafs(root))