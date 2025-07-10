import java.util.LinkedList;

public class myAVLNode<T> {
    public T item;
    public LinkedList<Loc> locList;
    public myAVLNode<T> left,right;
    public int height;
    public myAVLNode(T x) {
        item = x;
        height = 1;
        left = right = myAVLtree.NIL;
        locList=new LinkedList<>();
    }
    public myAVLNode(T x, Loc y) {
        item = x;
        height = 1;
        left = right = myAVLtree.NIL;
        locList=new LinkedList<>();
        locList.add(y);
    }
    public myAVLNode(T x, myAVLNode l, myAVLNode r,Loc y, int h){
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