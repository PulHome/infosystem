from TreeNode import TreeNode
from classTreeDepth import invertTree

if __name__ == "__main__":
	print("Test #4")
	root = TreeNode(0)
	root.left = TreeNode(10)
	root.left.left = TreeNode(11)
	root.left.right = TreeNode(12)
	root.right = TreeNode(20)
	root.right.right = TreeNode(22)
	root.right.left = TreeNode(23)
	
	TreeNode.printTree(invertTree(root))
	