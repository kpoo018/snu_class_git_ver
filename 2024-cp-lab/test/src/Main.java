import java.util .*;
public class Main {
    public static void main(String[] args) {
        Collection<Integer> collection =
                new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5));
        System.out.println(collection);
        Integer[] morelnts = {6, 7, 8, 9, 10};
        System.out.println(morelnts);
        collection.addAll(Arrays.asList(morelnts));
        System.out.println(collection);
        List<Integer> list = Arrays.asList(16, 17, 18, 19, 20);
        System.out.println(list);
        list.set(1, 99); // OK -- modify an element
        System.out.println(list);
    }
}