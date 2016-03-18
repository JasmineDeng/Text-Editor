package editor;

import javafx.scene.text.Text;

import java.util.HashMap;

public class TextLinkedList {
    public class Node {
        public Text i;
        public Node next;
        public Node prev;
        public Node(Text i, Node prev, Node next) {
            this.i = i;
            this.prev = prev;
            this.next = next;
        }
    }
    private Node sentinel;
    private Node rel;
    private int rel_ind;
    private int size;
    private TextCursor tc;
    private HashMap<Integer, Node> lines;
    public TextLinkedList(TextCursor tc) {
        size = 0;
        sentinel = new Node(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        rel = sentinel;
        rel_ind = 0;
        this.tc = tc;
        lines = new HashMap<>();
    }
    public void addFirst(Text i){
        Node oldFront = sentinel.next;
        Node newFront = new Node(i, sentinel, oldFront);
        sentinel.next = newFront;
        sentinel.next.next.prev = newFront;
        rel = sentinel.next;
        size+=1;
    }
    public void addLast(Text i){
        Node oldBack = sentinel.prev;
        Node newBack = new Node(i, oldBack, sentinel);
        sentinel.prev = newBack;
        sentinel.prev.prev.next = newBack;
        size+=1;
        if (size == 1) {
            rel = sentinel.prev;
        }
    }
    //Moves the Text to the side and creates a new instance in that place, rel is in same index (points to added)
    public void add(Text i) {
        Node newList = new Node(i, rel.prev, rel);
        rel.prev.next = newList;
        rel.prev = newList;
        rel = rel.prev;
        size += 1;
        if (size == 1) {
            rel = sentinel.next;
        }
    }

    public Text removeFirst(){
        if(size == 0) {
            return null;
        }
        size-=1;
        Node remove = sentinel.next;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;;
        return remove.i;
    }

    public Text removeLast(){
        if(size == 0) {
            return null;
        }
        size-=1;
        Node remove = sentinel.prev;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        return remove.i;
    }

    //removes the Text at current (should be previous) place, rel is forward shifted from before.
    public Text remove() {
        if (size < 2) {
            return null;
        }
        Text remove = rel.prev.i;
        rel.prev.prev.next = rel;
        rel.prev = rel.prev.prev;
        size -= 1;
        rel = rel.next;
        return remove;
    }

    //moves rel right
    public void moveRight() {
        if (rel.next != sentinel) {
            rel = rel.next;
            rel_ind += 1;
        }
    }

    //move rel left
    public void moveLeft() {
        if (rel.prev != sentinel) {
            rel = rel.prev;
            rel_ind -= 1;
        }
    }

    public void pushLine(int y, int index) {
        if (index < size && index >= 0) {
            if (lines.containsKey(y)) {
                lines.remove(y);
            }
            Node p = sentinel.next;
            while (index > 0) {
                p = p.next;
                index -= 1;
            }
            lines.put(y, p);
        }
    }

    public void changeLine(int x, int y) {
        if (!lines.containsKey(y)) {
            return;
        }
        Node line_pos = lines.get(y);
        boolean move_up = (y < tc.getY());
        boolean same_line = (y == tc.getY());
        boolean move_left = (x < tc.getX());
        tc.setY(y);
        int x_diff = 10000;
        while (line_pos.next != sentinel && line_pos.i.getY() == y && x_diff >= Math.abs(x - line_pos.i.getX())) {
            tc.setX(line_pos.i.getX());
            x_diff = Math.abs(x - (int) tc.getX());
            line_pos = line_pos.next;
        }
        line_pos = line_pos.prev;
        tc.setX(line_pos.i.getX());

        if (line_pos.i.getText().equals("") && line_pos.next.i.getY() == y) {
            line_pos = line_pos.next;
        }

        int x_temp = (int) (line_pos.i.getX() + Math.round(line_pos.i.getLayoutBounds().getWidth()));
        if (x_diff >= Math.abs(x - x_temp)) {
            tc.setX(x_temp);
            line_pos = line_pos.next;
        }

        while (!rel.equals(line_pos)) {
            if (!same_line && move_up) {
                rel = rel.prev;
                rel_ind -= 1;
            } else if (!same_line && !move_up) {
                rel = rel.next;
                rel_ind += 1;
            } else if (same_line && move_left) {
                rel = rel.prev;
                rel_ind -= 1;
            } else if (same_line && !move_left) {
                rel = rel.next;
                rel_ind += 1;
            }
        }
    }

    public int roundY(int approx) {
        int y_pos = (int) rel.i.getLayoutBounds().getHeight();
        int lower_bound = y_pos * (approx / y_pos);
        return lower_bound;
    }

    public Text getRel() {
        return rel.i;
    }

    public Node getRelNode() {
        return rel;
    }

    public Node getLast() {
        return sentinel.prev;
    }

    public int getRelInd() {
        return rel_ind;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Node getNode(int index) {
        if (index > size - 1 || index < 0) {
            return null;
        }
        Node p = sentinel.next;
        while (index > 0) {
            p = p.next;
            index-=1;
        }
        return p;
    }

    //get Text
    public Text get(int index) {
        if (index > size - 1 || index < 0) {
            return null;
        }
        Node p = sentinel.next;
        while (index > 0) {
            p = p.next;
            index-=1;
        }
        return p.i;
    }

    //prints list
    public void print() {
        int index = 0;
        Node p = sentinel;
        while (index < size) {
            p = p.next;
            System.out.println(p.i);
            index += 1;
        }
    }
}
