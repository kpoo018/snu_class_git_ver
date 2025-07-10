import java.io.*;
import java.util.Hashtable;
import java.util.LinkedList;

public class Matching
{
	private static int lineNumber;
	private static Hashtable<Integer,myAVLtree> table;
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			try
			{
				String input = br.readLine();
				if (input.compareTo("QUIT") == 0)
					break;

				command(input);
			}
			catch (IOException e)
			{
				System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
			}
		}
	}

	private static void command(String input)
	{
		// TODO : 아래 문장을 삭제하고 구현해라.
		String[] newinput= input.split(" ",2);

		switch (newinput[0])
		{
			case "<":
				readData(newinput[1]);
				break;
			case "@":
				printData(Integer.parseInt(newinput[1]));
				break;
			case "?":
				findData(newinput[1]);
				break;
			default:
				System.out.println("잘못된 입력입니다.");
				break;

		}

	}

	private static void readData(String input)
	{
		table=new Hashtable<Integer, myAVLtree>(100);
		try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
			String line;
			lineNumber = 1;
			while ((line = reader.readLine()) != null) {
				if (line.length() >= 6) {
					for (int i = 0; i <= line.length() - 6; i++) {
						String substring = line.substring(i, i + 6);
						int hashValue = hashing(substring);
						if (!table.containsKey(hashValue)) {
							table.put(hashValue, new myAVLtree<String, Loc>());
						}
						table.get(hashValue).insert(substring, new Loc(lineNumber, i + 1));
					}
				}
				lineNumber++;
			}
		} catch (IOException e) {
			System.out.println("파일을 읽는 도중 오류가 발생했습니다: " + e.getMessage());
		}


	}

	private static void printData(Integer input)
	{
		if(table.containsKey(input)){
			table.get(input).traversal();
		}
		else System.out.println("EMPTY");
	}

	private static void findData(String pattern) {
		if (pattern.length() < 6) {
			System.out.println("패턴의 길이는 6 이상이어야 합니다.");
			return;
		}

		StringBuilder result = new StringBuilder();
		boolean found = false;


		String subPattern = pattern.substring(0, 6);
		int hashValue = hashing(subPattern);

		if (table.containsKey(hashValue)) {
			myAVLNode<String, Loc> node = table.get(hashValue).search(subPattern);
			if (node.item != null) {
				for (Loc loc : node.locList) {
					if (checkFullPattern(loc, pattern)) {
						if (found) result.append(" ");
						result.append("(").append(loc.line).append(", ").append(loc.idx).append(")");
						found = true;
					}
				}
			}
		}


		if (!found) {
			System.out.println("(0, 0)");
		} else {
			System.out.println(result);
		}
	}

	private static boolean checkFullPattern(Loc startLoc, String pattern) {
		int patternLength = pattern.length();
		int currentIdx = startLoc.idx;
		int hashValue;
		String subString;

		for (int i = 6; i < patternLength; i += 6) {
			currentIdx += 6;
			if (i + 6 <= patternLength) {
				subString = pattern.substring(i, i + 6);
			} else {
				currentIdx = startLoc.idx + patternLength -6;
				subString = pattern.substring(patternLength-6, patternLength);
			}
			hashValue = hashing(subString);

			if (!table.containsKey(hashValue) ||
					table.get(hashValue).search(subString) == null ||
					!containsLocation(table.get(hashValue).search(subString).locList, startLoc.line, currentIdx)) {
				return false;
			}
		}
		return true;
	}

	private static boolean containsLocation(LinkedList<Loc> locList, int line, int idx) {
		for (Loc loc : locList) {
			if (loc.line == line && loc.idx == idx) {
				return true;
			}
		}
		return false;
	}



	private static int hashing(String input){
		int sum=0;
		if(input.length()!=6) System.err.println("잘못된 입력입니다.");
		for (int i=0; i<6;i++){
			sum+=input.charAt(i);
		}
		return sum%100;
	}


}


class myAVLtree<T extends Comparable<T>,J>
{
	private myAVLNode<T,J> root;
	private boolean flag = false;
	static final myAVLNode NIL = new myAVLNode(null,null,null,null,0);
	public myAVLtree(){
		root=NIL;
	}
	public myAVLtree(T x,J y){
		root=new myAVLNode<>(x,y);
	}

	//색인 메소드
	public myAVLNode<T,J> search(T x){
		return searchItem(root,x);
	}
	private myAVLNode<T,J> searchItem(myAVLNode<T,J> node, T x){
		if(node==NIL) return NIL;
		if(x.compareTo(node.item)<0) return searchItem(node.left,x);
		if(x.compareTo(node.item)>0) return searchItem(node.right,x);
		return node;
	}

	// 삽입 메소드
	public void insert(T x,J y) {
		root = insertItem(root, x,y);
	}

	private myAVLNode<T,J> insertItem(myAVLNode<T,J> node, T x, J y) {
		if (node == NIL) {
			return new myAVLNode<>(x,y);
		}

		if (x.compareTo(node.item) < 0) {
			node.left = insertItem(node.left, x, y);
		} else if (x.compareTo(node.item) > 0) {
			node.right = insertItem(node.right, x, y);
		} else {
			node.locList.add(y);
			return node;
		}

		return balance(node);
	}

	// 삭제 메소드
	public void delete(T x) {
		root = deleteItem(root, x);
	}

	private myAVLNode<T,J> deleteItem(myAVLNode<T,J> node, T x) {
		if (node == NIL) return NIL;

		if (x.compareTo(node.item) < 0) {
			node.left = deleteItem(node.left, x);
		} else if (x.compareTo(node.item) > 0) {
			node.right = deleteItem(node.right, x);
		} else {
			if (node.left == NIL) return node.right;
			if (node.right == NIL) return node.left;

			myAVLNode<T,J> minNode = findMin(node.right);
			node.item = minNode.item;
			node.locList=minNode.locList;
			node.right = deleteItem(node.right, minNode.item);
		}

		return balance(node);
	}

	private myAVLNode<T,J> findMin(myAVLNode<T,J> node) {
		while (node.left != NIL) node = node.left;
		return node;
	}

	//작은 것 부터 순회하며 print
	public void traversal(){
		StringBuilder result = new StringBuilder();
		preOrder(root,result);
		flag=false;
		System.out.println(result);
	}

	private void preOrder(myAVLNode<T,J> node,StringBuilder result){
		if (node==NIL) return;

		if (flag) result.append(" ");
		result.append(node.item);
		flag=true;

		preOrder(node.left,result);
		preOrder(node.right,result);
	}


	// 높이 갱신
	private void updateHeight(myAVLNode<T,J> node) {
		node.height = 1 + Math.max(node.left.height, node.right.height);
	}

	// 균형 인수 계산
	private int balanceFactor(myAVLNode<T,J> node) {
		return node.left.height - node.right.height;
	}

	// 균형 조정
	private myAVLNode<T,J> balance(myAVLNode<T,J> node) {
		updateHeight(node);
		int balance = balanceFactor(node);

		if (balance > 1) {
			if (balanceFactor(node.left) < 0) {
				node.left = rotateLeft(node.left);
			}
			return rotateRight(node);
		}
		if (balance < -1) {
			if (balanceFactor(node.right) > 0) {
				node.right = rotateRight(node.right);
			}
			return rotateLeft(node);
		}
		return node;
	}

	// 왼쪽 회전
	private myAVLNode<T,J> rotateLeft(myAVLNode<T,J> node) {
		myAVLNode<T,J> rightChild = node.right;
		node.right = rightChild.left;
		rightChild.left = node;
		updateHeight(node);
		updateHeight(rightChild);
		return rightChild;
	}

	// 오른쪽 회전
	private myAVLNode<T,J> rotateRight(myAVLNode<T,J> node) {
		myAVLNode<T,J> leftChild = node.left;
		node.left = leftChild.right;
		leftChild.right = node;
		updateHeight(node);
		updateHeight(leftChild);
		return leftChild;
	}

}

class myAVLNode<T,J> {
	public T item;
	public LinkedList<J> locList;
	public myAVLNode<T,J> left,right;
	public int height;
	public myAVLNode(T x) {
		item = x;
		height = 1;
		left = right = myAVLtree.NIL;
		locList=new LinkedList<>();
	}
	public myAVLNode(T x, J y) {
		item = x;
		height = 1;
		left = right = myAVLtree.NIL;
		locList=new LinkedList<>();
		locList.add(y);
	}
	public myAVLNode(T x, myAVLNode l, myAVLNode r,J y, int h){
		item=x;
		left=l;
		right=r;
		locList=new LinkedList<>();
		locList.add(y);
		height=h;
	}
}

class Loc {
	Integer line;
	Integer idx;

	public Loc(Integer line, Integer idx) {
		this.line = line;
		this.idx = idx;
	}

	@Override
	public String toString() {
		return "("+line.toString()+","+idx.toString()+")";
	}
}
