package updateClosedSetLattice;

import utils.transaction_database_closedset.ItemUtility;
import utils.transaction_database_closedset.TransactionTP;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class Node {
    public ModifiedBitSet bitPresent;
    public int utility = 0;
    public HashMap<Integer, Integer> utilityMap;
    public HashSet<ModifiedBitSet> parents = new HashSet<>();
    public HashSet<ModifiedBitSet> children;
    public int support = 1;

    private void setChildren(int size) {
        if (size < 8) {
            this.children = new HashSet<>(size, 1);
        } else {
            this.children = new HashSet<>();
        }
    }

    public Node() {
        this.bitPresent = null;
        this.support = 0;
        this.setChildren(8); // just need greater than 7 
    }

    public Node(ModifiedBitSet bitPresent, int size) {
        this.bitPresent = bitPresent;
        this.utilityMap = new HashMap<>(size, 1);
        this.setChildren(size);
    }

    public Node(TransactionTP transaction) {
        this.bitPresent = new ModifiedBitSet(transaction.maxItemID);
        this.utilityMap = new HashMap<>(transaction.size(), 1);
        this.setChildren(transaction.size());

        for (ItemUtility t : transaction.getItems()) {
            this.utilityMap.put(t.item, t.utility);
            this.bitPresent.set(t.item);
            this.utility += t.utility;
        }
    }

    // create a common child - node, got info (quantitySeq, transactionId) from newNode
    public Node getCommonChild(ModifiedBitSet intersect) {
        int size = intersect.cardinality();
        Node commonChild = new Node(intersect, size);
        for (int i = intersect.nextSetBit(0); i != -1; i = intersect.nextSetBit(i + 1)) {
            Integer quantity = this.utilityMap.get(i);
            commonChild.utilityMap.put(i, quantity);
            commonChild.utility += quantity;
        }
        return commonChild;
    }

    // Update quantitySeq and utility based on quantitySeq and utility of otherNode
    public void updateUsing(Node otherNode) {
        Integer inheritedQuantity;

        for (int item : this.utilityMap.keySet()) {
            inheritedQuantity = otherNode.utilityMap.get(item);
            this.utilityMap.replace(item, this.utilityMap.get(item) + inheritedQuantity);
            this.utility += inheritedQuantity;
        }
        this.support += otherNode.support;
    }

    public void removeTransaction(TransactionTP transaction) {
        for (var item : transaction.getItems()) {
            Integer quantity = this.utilityMap.get(item.item);
            if (quantity == null) {
                continue;
            }
            this.utilityMap.replace(item.item, quantity - item.utility);
            this.utility -= item.utility;
        }

        this.support -= 1;
    }

    public boolean shouldBeDeleted() {
        if (this.utility == 0) {
            // System.out.printf("Delete node %s - utility is 0%n", this.bitPresent);
            return true;
        }

        for (ModifiedBitSet parent : this.parents) {
            Node parentNode = ClosedSetLattice.closedSet.get(parent);
            if (parentNode == null) {
                continue;
            }
            if (parentNode.support == this.support) {
                // System.out.printf("Delete node %s - parent same SUP%n", node.bitPresent);
                return true;
            }
        }
        return false;
    }

    public String toString() {
        List<String> output = new ArrayList<>();
        for (Map.Entry<Integer, Integer> item : utilityMap.entrySet()) {
            output.add(ItemIdMapper.getOriginalItemId(item.getKey()) + " : " + item.getValue());
        }

        return output.toString();
    }
}