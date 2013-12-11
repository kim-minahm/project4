

import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.text.DecimalFormat;

//Each internal holds 4 byte reference each to it's 2 children and an internal Indicator
//Empties: 
//MemManage should read the disc Data then apply attributes to overarching Node class?
public class bintree {
	/**
	 * Singleton instance for the empty leaf;
	 */
	public emptyLeaf e; // internalNodes should hold left/right
						// internalNodes should hold left/right, e should be the
						// emptyCase or -1
						//
	binNode root;
	long node;
	double maxX;
	double maxY;
	double precision = 1;
	DecimalFormat df = new DecimalFormat("#.0");
	public MemManager memManage; // pass this to the nodes so they can access
							// alternately replace the nodes with

	/**
	 * Instantiates the empty leaf
	 */
	public bintree(int bufNum, int bufSize) {
		memManage = new MemManager(bufNum, bufSize);
		e = new emptyLeaf(memManage); // Sets the empty Flyweight
		// for this case: maxX is 360, maxY is 180
		maxX = 360;
		maxY = 180;
		// e = new MemHandle(0);
		// root = new internalNode(e, e);
		root = e;
	}

	/**
	 * Converts the given coordinates into the standard (x+180, y+90)
	 * 
	 * @param coords
	 *            Coordinates to convert
	 * @return The converted Coordinates
	 */
	public double[] convert(double[] coords) {
		double[] ret = { coords[0] + 180, coords[1] + 90 };
		return ret;
	}

	private binNode split(binNode n, LeafNode A, LeafNode B, double[] splits,
			double[] prevSplits, int level) {
		// check if they're the same.
		if ((A.getData(memManage)[0] == B.getData(memManage)[0])
				&& (A.getData(memManage)[1] == B.getData(memManage)[1]))
			return A;
		int check = withinSameSplit(A.getData(memManage), B.getData(memManage),
				splits, level);
		if (check == 0) {
			internalNode temp = new internalNode(A, B);
			return temp;
		} else if (check == -1) {
			internalNode temp = new internalNode(B, A);
			return temp;
		} else if (check == 1) {
			internalNode inNode = new internalNode(e, e);
			double currSplit = splits[level]
					- findMidLeft(splits, prevSplits, level);
			prevSplits[level + 2] = splits[level];
			splits[level] = currSplit;
			inNode.setLeft(split(n, A, B, splits, prevSplits, 1 - level),
					memManage);
			// split(n, A, B, splits, 1 - level);
			return inNode;
		} else {
			internalNode inNode = new internalNode(e, e);
			double currSplit = splits[level]
					+ findMidRight(splits, prevSplits, level);
			prevSplits[level] = splits[level];
			splits[level] = currSplit;
			// inNode.setLeft(split(n, A, B, splits, prevSplits, 1 - level));
			inNode.setRight(split(n, A, B, splits, prevSplits, 1 - level),
					memManage);
			// split(n, A, B, splits, 1 - level);
			return inNode;
		}
	}

	private int withinSameSplit(double[] acoords, double[] bcoords,
			double[] splits, int level) {
		if ((acoords[level] < splits[level])
				&& (bcoords[level] < splits[level])) // If both are left
			return 1;
		else if ((acoords[level] >= splits[level])
				&& (bcoords[level] >= splits[level])) // if both are right
			return 2;
		else if ((acoords[level] < splits[level]) // if a is less
				&& (bcoords[level] >= splits[level]))
			return 0;
		else
			return -1; // if b is less
	}

	public binNode insert(double[] coords, String key) {
		if (find(coords)) {
			return null;
			// don't insert
		}
		double[] splits = { maxX / 2, maxY / 2 };
		double[] prevSplits = { 0, 0, maxX, maxY };
		root = inserter(root, convert(coords), key, splits, prevSplits, 0);
		removeEmpties(root);
		return root;
	}

	private binNode inserter(binNode n, double[] coords, String key,
			double[] splits, double[] prevSplits, int level) {
		if (n == e) { // if Empty
			return new LeafNode(coords, key, memManage);
		} else if (n instanceof LeafNode) { // if leaf Node
			LeafNode A = (LeafNode) n;
			LeafNode B = new LeafNode(coords, key, memManage);
			n = split(n, A, B, splits, prevSplits, level); // splits it up

		} else
		// internalNode (used for searching)
		{
			internalNode temp = (internalNode) n;
			// level = 0: x
			// level = 1: y
			// Check to see if it falls in bounds.
			if (coords[level] < splits[level]) // coordinates are less than
												// split.
			{
				double currSplit = splits[level]
						- findMidLeft(splits, prevSplits, level);
				prevSplits[level + 2] = splits[level];
				splits[level] = currSplit;
				// splits[level] = currSplit;
				// if(temp.getRight() == e)
				// n = inserter(temp.getLeft(), coords, key, splits, level);
				temp.setLeft(
						inserter(temp.getLeft(), coords, key, splits,
								prevSplits, 1 - level), memManage);
			} else {
				double currSplit = splits[level]
						+ findMidRight(splits, prevSplits, level);
				prevSplits[level] = splits[level];
				splits[level] = currSplit;
				temp.setRight(
						inserter(temp.getRight(), coords, key, splits,
								prevSplits, 1 - level), memManage);
			}
		}
		return n;

	}

	private double findMidRight(double[] A, double[] B, int level) {
		return Math.abs((B[level] - A[level])) / 2;
	}

	private double findMidLeft(double[] A, double[] B, int level) {
		return Math.abs((B[level + 2] - A[level])) / 2;
	}

	public boolean find(double[] coords) {
		double[] splits = { maxX / 2, maxY / 2 };
		double[] prevSplits = { 0, 0, maxX, maxY };
		coords = convert(coords);
		binNode n = finder(root, coords, splits, prevSplits, 0);
		if (n == e)
			return false;
		LeafNode temp = (LeafNode) n;
		return (temp.getData(memManage)[0] == coords[0])
				&& (temp.getData(memManage)[1] == coords[1]);
	}

	public binNode finder(binNode n, double[] coords, double[] splits,
			double[] prevSplits, int level) {
		if (n instanceof internalNode) {
			internalNode temp = (internalNode) n;
			// level = 0: x
			// level = 1: y
			// Check to see if it falls in bounds.
			if (coords[level] - splits[level] < 0) // coordinates are less than
													// split.
			{
				double currSplit = splits[level]
						- findMidLeft(splits, prevSplits, level);
				prevSplits[level + 2] = splits[level];
				splits[level] = currSplit;
				return finder(temp.getLeft(), coords, splits, prevSplits,
						1 - level);
			} else {
				double currSplit = splits[level]
						+ findMidRight(splits, prevSplits, level);
				prevSplits[level] = splits[level];
				splits[level] = currSplit;
				return finder(temp.getRight(), coords, splits, prevSplits,
						1 - level);
			}

		} else
			return n;

	}

	/**
	 * Removes a specific coordinate from the tree
	 * 
	 * @param coords
	 *            The coordinates
	 * @return
	 */
	public void remove(double[] coords) {
		double[] splits = { maxX / 2, maxY / 2 };
		double[] prevSplits = { 0, 0, maxX, maxY };
		coords = convert(coords);
		root = remover(root, coords, splits, prevSplits, 0);
		root = removeEmpties(root);
	}

	private binNode removeEmpties(binNode n) {
		if (n instanceof internalNode) {
			internalNode temp = (internalNode) n;
			temp.setLeft(removeEmpties(temp.getLeft()), memManage);
			temp.setRight(removeEmpties(temp.getRight()), memManage);
			if ((temp.getLeft()) == e && (temp.getRight() == e)) // if both
																	// children
																	// are
																	// emptyLeafs,
																	// n =
																	// emptyLeaf
			{
				n = e;
				return n;
			}
			if (((internalNode) n).getLeft() instanceof LeafNode
					&& ((internalNode) n).getRight() == e)
				n = ((internalNode) n).getLeft();
			else if (((internalNode) n).getRight() instanceof LeafNode
					&& ((internalNode) n).getLeft() == e)
				n = ((internalNode) n).getRight();
		}
		// or if
		return n;

	}

	private binNode remover(binNode n, double[] coords, double[] splits,
			double[] prevSplits, int level) {
		if (root instanceof LeafNode
				&& decimalEquals((((LeafNode) root).getData(memManage)[0]),
						coords[0])
				&& decimalEquals(((LeafNode) root).getData(memManage)[1],
						coords[1])) {
			root = e;
			return e;
		}
		if (n instanceof internalNode) {
			internalNode inNode = (internalNode) n;
			binNode left = inNode.getLeft();
			binNode right = inNode.getRight();

			boolean leftIsLeaf = false;
			boolean rightIsLeaf = false;

			leftIsLeaf = left instanceof LeafNode;
			rightIsLeaf = right instanceof LeafNode;
			if (leftIsLeaf) {
				LeafNode lLeaf = (LeafNode) left;
				// getData becomes ping memHandler.getDataByHandle();
				if ((lLeaf.getData(memManage)[0] == coords[0])
						&& (lLeaf.getData(memManage)[1] == coords[1])) {
					memManage.releaseWatcher(lLeaf.getHandleToWatcher());
					memManage.releaseNode(lLeaf.handle);
					inNode.setLeft(e, memManage);
				}

			}
			if (rightIsLeaf) {
				LeafNode rLeaf = (LeafNode) right;
				if ((rLeaf.getData(memManage)[0] == coords[0])
						&& (rLeaf.getData(memManage)[1] == coords[1])) {
					memManage.releaseWatcher(rLeaf.getHandleToWatcher());
					memManage.releaseNode(rLeaf.handle);
					inNode.setRight(e, memManage);
				}
			}

			// TRAVERSE DOWN

			if ((left == e) && (right == e)) {
				return e;
			}

			if (coords[level] - splits[level] < 0) // coordinates are less than
			// split.
			{
				double currSplit = splits[level]
						- findMidLeft(splits, prevSplits, level);
				prevSplits[level + 2] = splits[level];
				splits[level] = currSplit;
				inNode.setLeft(
						remover(inNode.getLeft(), coords, splits, prevSplits,
								1 - level), memManage);
			} else {
				double currSplit = splits[level]
						+ findMidRight(splits, prevSplits, level);
				prevSplits[level] = splits[level];
				splits[level] = currSplit;
				inNode.setRight(
						remover(inNode.getRight(), coords, splits, prevSplits,
								1 - level), memManage);
			}

			return inNode; // if internalNode has one leaf and other empty
		}
		return n;

	}

	public int regionSearch(double[] coord, double d) {
		// Calc d from distance
		coord = convert(coord);
		// Top left = {0,0}, bottom right = {360, 180}
		double[] bounds = { coord[0] - d, coord[1] - d, coord[0] + d,
				coord[1] + d };
		double[] baseBounds = { 0, 0, maxX, maxY };
		int nodesVisited = regionSearcher(root, bounds, baseBounds, 0, coord, d);
		System.out.println("Watcher search caused " + nodesVisited
				+ " bintree nodes to be visited.");
		return nodesVisited;
	}

	public int regionSearcher(binNode n, double[] eqBox, double[] wBox,
			int level, double[] eqCoord, double mag) {
		// Check node type for "visit"
		if (n == e) {
			return 1;
		} else if (n instanceof LeafNode) {
			LeafNode lNode = (LeafNode) n;
			if (inRange(eqCoord, mag, lNode.getData(memManage)))
				System.out.println(lNode.getKey(memManage) + " "
						+ df.format((lNode.getData(memManage)[0] - 180)) + " "
						+ df.format((lNode.getData(memManage)[1] - 90))); // or
																			// whatever
																			// else
			return 1;
		}
		// else if is internalNode
		if (intersect(eqBox, wBox)) {
			internalNode inNode = (internalNode) n;
			double[] leftBox = { wBox[0], wBox[1], wBox[2], wBox[3] };
			double[] rightBox = { wBox[0], wBox[1], wBox[2], wBox[3] };
			double dist = (wBox[level + 2] - wBox[level]); // distance from
															// point to point
			leftBox[level + 2] = leftBox[level + 2] - dist / 2; // Farthest
																// bounds halves
																// itself
			rightBox[level] = rightBox[level] + dist / 2;
			// leftBox[level + 2] = leftBox[level + 2] - (leftBox[level + 2] /
			// 2);

			if (intersect(eqBox, rightBox) && intersect(eqBox, leftBox)) {
				return 1
						+ regionSearcher(inNode.getLeft(), eqBox, leftBox,
								1 - level, eqCoord, mag)
						+ regionSearcher(inNode.getRight(), eqBox, rightBox,
								1 - level, eqCoord, mag);
			} else if (intersect(eqBox, rightBox)) {
				return 1 + regionSearcher(inNode.getRight(), eqBox, rightBox,
						1 - level, eqCoord, mag); // Go
			} else if (intersect(eqBox, leftBox)) {
				return 1 + regionSearcher(inNode.getLeft(), eqBox, leftBox,
						1 - level, eqCoord, mag);
			}
			return 1;

		} else {
			return 1;
		}
	}

	private boolean intersect(double[] eqBox, double[] wBox) {
		Rectangle2D eq = new Rectangle2D.Double(eqBox[0], eqBox[1], eqBox[2]
				- eqBox[0], eqBox[3] - eqBox[1]);
		Rectangle2D w = new Rectangle2D.Double(wBox[0], wBox[1], wBox[2]
				- wBox[0], wBox[3] - wBox[1]);
		return eq.intersects(w);
	}

	public void inOrder() {
		inOrderTraversal(root);
	}

	private void inOrderTraversal(binNode _node) {

		if (_node instanceof internalNode) {
			typePrinter(_node);

			// System.out.println("Left Child:");
			// typePrinter(((internalNode)_node).getLeft());
			// System.out.println("Right Child:");
			// typePrinter(((internalNode)_node).getRight());

			inOrderTraversal(((internalNode) _node).getLeft());
			inOrderTraversal(((internalNode) _node).getRight());
		} else {
			typePrinter(_node);
		}

	}

	private void typePrinter(binNode _node) {
		if (_node == e) {
			System.out.println("E");
		} else if (_node instanceof LeafNode) {
			LeafNode lNode = (LeafNode) _node;

			String out = "%s %.1f %.1f";
			out = String.format(out, lNode.getKey(memManage),
					(lNode.getData(memManage)[0] - 180.0),
					(lNode.getData(memManage)[1] - 90.0));
			System.out.println(out);
		} else {
			System.out.println("I");
		}
	}

	/**
	 * Checks to see if inRange (calcs x^2 +y^2 < r^2
	 * 
	 * @param coord1
	 * @param r
	 * @param coord2
	 * @return
	 */
	private boolean inRange(double[] coord1, double r, double[] coord2) {
		int sum = 0;
		for (int i = 0; i < coord1.length; i++) // Calculates x^2 + y^2 < r^2
		{
			sum += (coord1[i] - coord2[i]) * (coord1[i] - coord2[i]);
		}
		return sum < (r * r);
	}

	private boolean decimalEquals(double x, double y) {
		return (Math.abs(y - x) < precision);
	}
}
