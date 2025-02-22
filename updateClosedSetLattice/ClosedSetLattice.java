package updateClosedSetLattice;

import java.util.HashSet;

public class ClosedSetLattice {
    public static IndexHashMap closedSet;
    public static Node rootNode = new Node();

    public ClosedSetLattice(int size) {
        closedSet = new IndexHashMap(size);
    }

    public static void addChild(Node parent, Node child) {
        parent.children.add(child.bitPresent);
        child.parents.add(parent.bitPresent);
    }

    public static void add(Node newNode) {
        closedSet.put(newNode.bitPresent, newNode);
    }

    
}
