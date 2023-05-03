from TreeNode import TreeNode

if __name__ == "__main__":
	print("Test #6")
	root = TreeNode(6)
	root.left = TreeNode(7)
	root.left.left = TreeNode(2)
	root.left.right = TreeNode(7)
	root.left.left.left = TreeNode(9)
	root.left.right.left = TreeNode(1)
	root.left.right.right = TreeNode(4)
	
	root.right = TreeNode(8)
	root.right.left = TreeNode(1)
	root.right.right = TreeNode(3)
	root.right.right.right = TreeNode(5)
	
	print(sumDeepestLeafs(root))
