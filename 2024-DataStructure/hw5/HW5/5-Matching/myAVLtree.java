
public class myAVLtree<T extends Comparable<T>>
{
    private myAVLNode<T> root;
    static final myAVLNode NIL = new myAVLNode(null,null,null,null,0);
    public myAVLtree(){
        root=NIL;
    }
    public myAVLtree(T x,Loc y){
        root=new myAVLNode<>(x,y);
    }

    //색인 메소드
    public myAVLNode<T> search(T x){
        return searchItem(root,x);
    }
    private myAVLNode<T> searchItem(myAVLNode<T> node, T x){
        if(node==NIL) return NIL;
        if(x.compareTo(node.item)<0) return searchItem(node.left,x);
        if(x.compareTo(node.item)>0) return searchItem(node.right,x);
        return node;
    }

    // 삽입 메소드
    public void insert(T x,Loc y) {
        root = insertItem(root, x,y);
    }

    private myAVLNode<T> insertItem(myAVLNode<T> node, T x, Loc y) {
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

    private myAVLNode<T> deleteItem(myAVLNode<T> node, T x) {
        if (node == NIL) return NIL;

        if (x.compareTo(node.item) < 0) {
            node.left = deleteItem(node.left, x);
        } else if (x.compareTo(node.item) > 0) {
            node.right = deleteItem(node.right, x);
        } else {
            if (node.left == NIL) return node.right;
            if (node.right == NIL) return node.left;

            myAVLNode<T> minNode = findMin(node.right);
            node.item = minNode.item;
            node.locList=minNode.locList;
            node.right = deleteItem(node.right, minNode.item);
        }

        return balance(node);
    }

    private myAVLNode<T> findMin(myAVLNode<T> node) {
        while (node.left != NIL) node = node.left;
        return node;
    }

    //작은 것 부터 순회하며 print
    public void traversal(){
        inOrder(root);
    }

    private void inOrder(myAVLNode<T> node){
        if (node==NIL) return;
        inOrder(node.left);
        if(findMin(this.root)!=node) System.out.print(" "+node.item);
        else System.out.print(node.item);
        inOrder(node.right);
    }


    // 높이 갱신
    private void updateHeight(myAVLNode<T> node) {
        node.height = 1 + Math.max(node.left.height, node.right.height);
    }

    // 균형 인수 계산
    private int balanceFactor(myAVLNode<T> node) {
        return node.left.height - node.right.height;
    }

    // 균형 조정
    private myAVLNode<T> balance(myAVLNode<T> node) {
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
    private myAVLNode<T> rotateLeft(myAVLNode<T> node) {
        myAVLNode<T> rightChild = node.right;
        node.right = rightChild.left;
        rightChild.left = node;
        updateHeight(node);
        updateHeight(rightChild);
        return rightChild;
    }

    // 오른쪽 회전
    private myAVLNode<T> rotateRight(myAVLNode<T> node) {
        myAVLNode<T> leftChild = node.left;
        node.left = leftChild.right;
        leftChild.right = node;
        updateHeight(node);
        updateHeight(leftChild);
        return leftChild;
    }

}
