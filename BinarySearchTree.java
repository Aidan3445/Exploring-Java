package src;

import java.util.ArrayList;
import java.util.Comparator;

//node or leaf of generic tree
abstract class ABST<T> implements ITree<T> {
	Comparator<T> order;

	ABST(Comparator<T> order) {
		this.order = order;
	}
}

class IntSort implements Comparator<Integer> {
	@Override
	public int compare(Integer a, Integer b) {
		if (a < b) {
			return 1;
		} else if (a > b) {
			return -1;
		} else {
			return 0;
		}
	}
}

interface ITree<T> {

	ArrayList<T> buildList();

	T getLeftmost();

	T getLeftmostHelper(T t);

	ABST<T> getRight();

	ABST<T> getRightHelper();

	ABST<T> insert(T t);

	boolean present(T t);

	boolean present(T t, Comparator<T> comp);

	ABST<T> remove(T t);

	ABST<T> remove(T t, Comparator<T> comp);

	boolean sameData(ABST<T> other);

	boolean sameDataHelper(T other);

	boolean sameLeaf(Leaf<T> other);

	boolean sameTree(ABST<T> other);

	boolean sameTreeHelper(T other, ABST<T> left, ABST<T> right);

	int size();
}

class Leaf<T> extends ABST<T> {
	Leaf(Comparator<T> order) {
		super(order);
	}

	@Override
	public ArrayList<T> buildList() {
		return new ArrayList<>();
	}

	// throws exception, "No leftmost item of an empty tree"
	@Override
	public T getLeftmost() {
		throw new RuntimeException("No leftmost item of an empty tree");
	}

	// returns given T, leftmost has been reached
	@Override
	public T getLeftmostHelper(T t) {
		return t;
	}

	// throws exception, "No right of an empty tree"
	@Override
	public ABST<T> getRight() {
		throw new RuntimeException("No right of an empty tree");
	}

	// returns leaf, leafs remain in tree when getRight() is called
	@Override
	public ABST<T> getRightHelper() {
		return this;
	}

	// creates a new node with no left and right using given data, where the actual
	// insertion happens
	@Override
	public ABST<T> insert(T t) {
		return new Val<>(t, this.order);
	}

	// returns false, no data is present in a leaf
	@Override
	public boolean present(T t) {
		return false;
	}

	// returns false, no data is present in a leaf
	@Override
	public boolean present(T t, Comparator<T> comp) {
		return false;
	}

	@Override
	public ABST<T> remove(T t) {
		return this;
	}

	@Override
	public ABST<T> remove(T t, Comparator<T> comp) {
		return this;
	}

	@Override
	public boolean sameData(ABST<T> other) {
		return other.sameLeaf(this);
	}

	@Override
	public boolean sameDataHelper(T other) {
		return false;
	}

	// returns true, two leafs are the same
	@Override
	public boolean sameLeaf(Leaf<T> other) {
		return true;
	}

	// returns true if other is a leaf, makes sure that leafs have
	@Override
	public boolean sameTree(ABST<T> other) {
		return other.sameLeaf(this);
	}

	@Override
	public boolean sameTreeHelper(T other, ABST<T> left, ABST<T> right) {
		return false;
	}

	// returns 0, leaves have no size
	@Override
	public int size() {
		return 0;
	}
}

class StringSort implements Comparator<String> {
	@Override
	public int compare(String a, String b) {
		return a.compareTo(b);
	}
}

//generic node
class Val<T> extends ABST<T> {
	T data;
	ABST<T> left;
	ABST<T> right;

	Val(Comparator<T> order, T data, ABST<T> left, ABST<T> right) {
		super(order);
		this.data = data;
		this.left = left;
		this.right = right;
	}

	// convenience constructor
	Val(T data, Comparator<T> order) {
		super(order);
		this.data = data;
		this.left = new Leaf<>(this.order);
		this.right = new Leaf<>(this.order);
	}

	// builds a list with first being the leftmost and rest being a recursive call
	// on the getRight()
	// uses getRightHelper to avoid RuntimeException thrown on leaf
	@Override
	public ArrayList<T> buildList() {
		ArrayList<T> list = this.getRightHelper().buildList();
		list.add(this.getLeftmost());
		return list;
	}

	// returns the leftmost item of a tree a.k.a the smallest item according to
	// order comparator
	@Override
	public T getLeftmost() {
		return this.left.getLeftmostHelper(this.data);
	}

	// allows for recursive call to avoid throw error when getLeftMost() is called
	// on a node
	// passes current data to be returned when this.left is a leaf
	@Override
	public T getLeftmostHelper(T t) {
		return this.left.getLeftmostHelper(this.data);
	}

// returns new tree with all but the leftMost/smallest item in the tree
	@Override
	public ABST<T> getRight() {
		return this.getRightHelper();
	}

	// allows for recursive call to avoid throw error when getRight() is called on a
	// node
	// if this is the leftmost then return the right, otherwise keep this data point
	// and
	// call recursively on this.left
	@Override
	public ABST<T> getRightHelper() {
		if (this.order.compare(this.data, this.getLeftmost()) == 0) {
			return this.right;
		} else {
			return new Val<>(this.order, this.data, this.left.getRightHelper(), this.right);
		}
	}

	// inserts item of type T into this tree of type T following branch order rules
	@Override
	public ABST<T> insert(T t) {
		if (this.order.compare(this.data, t) <= 0) {
			return new Val<>(this.order, this.data, this.left, this.right.insert(t));
		} else {
			return new Val<>(this.order, this.data, this.left.insert(t), this.right);
		}
	}

	// returns true if a given item of type T is in this tree
	@Override
	public boolean present(T t) {
		if (this.order.compare(this.data, t) > 0) {
			return this.right.present(t);
		} else if (this.order.compare(this.data, t) < 0) {
			return this.left.present(t);
		} else {
			return true;
		}
	}

	// present with different comparator
	@Override
	public boolean present(T t, Comparator<T> comp) {
		if (comp.compare(this.data, t) == 0) {
			return true;
		} else {
			return this.right.present(t, comp) || this.left.present(t, comp);
		}
	}

	// removes the first instance of an item in a node
	@Override
	public ABST<T> remove(T t) {
		if (this.order.compare(this.data, t) < 0) {
			return new Val<>(this.order, this.data, this.left, this.right.remove(t));
		} else if (this.order.compare(this.data, t) > 0) {
			return new Val<>(this.order, this.data, this.left.remove(t), this.right);
		} else {
			ABST<T> newBranch = new Leaf<>(this.order);
			ArrayList<T> below = this.left.buildList();
			below.addAll(this.right.buildList());
			for (T item : below) {
				newBranch = newBranch.insert(item);
			}
			return newBranch;
		}
	}

	//remove with different comparator
	@Override
	public ABST<T> remove(T t, Comparator<T> comp) {
		if (this.order.compare(this.data, t) == 0) {
			ABST<T> newBranch = new Leaf<>(this.order);
			ArrayList<T> below = this.left.buildList();
			below.addAll(this.right.buildList());
			for (T item : below) {
				newBranch = newBranch.insert(item);
			}
			return newBranch;
		} else {
			return new Val<>(this.order, this.data, this.left.remove(t, comp), this.right.remove(t, comp));
		}
	}

	// gets the leftmost and passes through to helper to compare with other tree
	// calls recursively with the getRight() if both trees
	@Override
	public boolean sameData(ABST<T> other) {
		return other.sameDataHelper(this.getLeftmost()) && this.getRight().sameData(other.getRight());
	}

	// compares this leftmost with other leftmost
	@Override
	public boolean sameDataHelper(T other) {
		return this.order.compare(this.getLeftmost(), other) == 0;
	}

	// returns false, a leaf and a node are not the same
	@Override
	public boolean sameLeaf(Leaf<T> other) {
		return false;
	}

	// passes this.data, left, and right into helper to be compared to the other
	// given tree
	@Override
	public boolean sameTree(ABST<T> other) {
		return other.sameTreeHelper(this.data, this.left, this.right);
	}

	// compares this data to the other data and calls sameTree() recursively
	// on left and right of this tree using the fed left and right
	@Override
	public boolean sameTreeHelper(T other, ABST<T> left, ABST<T> right) {
		return this.order.compare(this.data, other) == 0 && this.left.sameTree(left) && this.right.sameTree(right);
	}

	// gets the size of the tree
	@Override
	public int size() {
		return 1 + this.left.size() + this.right.size();
	}
}
