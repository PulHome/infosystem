from TreeNode import TreeNode
from classTreeDepth import invertTree

if __name__ == "__main__":
	print("Test #5")
	root = TreeNode(0)
	root.left = TreeNode(0)
	root.left.left = TreeNode(0)
	root.left.right = TreeNode(0)
	root.right = TreeNode(0)
	root.right.right = TreeNode(0)
	root.right.left = TreeNode(0)
	
	TreeNode.printTree(invertTree(root))
	