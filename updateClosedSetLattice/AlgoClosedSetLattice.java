package updateClosedSetLattice;

import utils.transaction_database_closedset.TransactionTP;
import utils.transaction_database_closedset.UtilityTransactionDatabaseTP;
import utils.MemoryLogger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.io.FileWriter;


public class AlgoClosedSetLattice {
    public UtilityTransactionDatabaseTP database = new UtilityTransactionDatabaseTP();

    long startTimestamp;
    long endTimestamp;

    HashSet<ModifiedBitSet> addedTransaction = new HashSet<>();

    public AlgoClosedSetLattice(int HashTableSize) {
        ClosedSetLattice.closedSet = new IndexHashMap(HashTableSize);
    }

    public void runAlgorithm(String inputFilePath) throws Exception {
        MemoryLogger.getInstance().reset();
        MemoryLogger.getInstance().checkMemory();

        // Get starting time
        this.startTimestamp = System.currentTimeMillis();

        // Read file
        this.database.loadFile(inputFilePath);
        MemoryLogger.getInstance().checkMemory();

        // Create a bitset to take intersect (const length)
        intersect = new ModifiedBitSet(ItemIdMapper.originalItemIds.size());
        

        // Get each transaction in database
        // long startTimestamp = System.currentTimeMillis();
        Node newNode;
        for (TransactionTP transaction : this.database.getTransactions()) {
            // if (transactionId % 100 == 0) {
            // System.out.printf("Transaction %s - %sms%n", transactionId,
            // System.currentTimeMillis() - startTimestamp);
            // startTimestamp = System.currentTimeMillis();
            // }

            newNode = new Node(transaction);

            if (ClosedSetLattice.closedSet.get(newNode.bitPresent) != null) {
                this.updateRecursive(newNode, newNode.bitPresent);
            } else {
                ClosedSetLattice.closedSet.put(newNode.bitPresent, newNode);
                this.addNodeToTree(newNode, null);
            }
            this.addedTransaction.clear();
        }
        MemoryLogger.getInstance().checkMemory();

        // Get end time
        this.endTimestamp = System.currentTimeMillis();
    }

    public static ModifiedBitSet intersect;
    public static Node oldNode;
    public static ModifiedBitSet oldNodeBitPresent;

    // Function add node to tree
    void addNodeToTree(Node newNode, ModifiedBitSet root) {

        this.addedTransaction.add(newNode.bitPresent);

        Queue<ModifiedBitSet> queue = new LinkedList<>();

        
        if (root == null) {

            for (ModifiedBitSet rootBitPresent : ClosedSetLattice.rootNode.children) {

                intersect.getIntersection(newNode.bitPresent, rootBitPresent);

                // Just consider the node has intersection with newNode
                if (intersect.wordsInUse != 0) {

                    // Case belong: only consider this rootBitPresent's branch
                    if (intersect.equals(newNode.bitPresent)) {
                        queue.clear();
                        // for case belong
                        root = rootBitPresent;
                        break;
                    } else if (!this.addedTransaction.contains(intersect)) {
                        if ((oldNode = ClosedSetLattice.closedSet.get(intersect)) != null) {
                            // There is an existing node corresponding to the intersection
                            // Case contain or partial overlap
                            this.updateRecursive(newNode, oldNode.bitPresent);
                            queue.add(oldNode.bitPresent);
                        } else {
                            this.addedTransaction.add((ModifiedBitSet) intersect.clone());
                            queue.add(rootBitPresent);
                        }
                    }
                }
            }

        } 
        
        // Case belong
        while (root != null) {

            boolean shouldStop = true;

            for (ModifiedBitSet childBitPresent : ClosedSetLattice.closedSet.get(root).children) {
                if (!childBitPresent.intersects(newNode.bitPresent)) {
                    continue;
                }

                if (childBitPresent.isSuperset(newNode.bitPresent)) {
                    root = childBitPresent;
                    queue.clear();
                    shouldStop = false;
                    break;
                }

                queue.add(childBitPresent);
            }

            if (shouldStop) {
                break;
            }
        }



        // Retrieve all node in queue (lattice tree) 
        while (!queue.isEmpty()) {

            oldNodeBitPresent = queue.poll();
            intersect.getIntersection(newNode.bitPresent, oldNodeBitPresent);
            
            // if intersect is already meet before
            if (root != null && newNode.children.contains(intersect)) {
                continue;
            }


            if (!shouldBeAdded(newNode, intersect)) {
                continue;
            }

            
            // Case contain - newNode contains oldNode
            if (oldNodeBitPresent.equals(intersect)) {
                oldNode = ClosedSetLattice.closedSet.get(oldNodeBitPresent);

                if (root != null) {
                    Node parentNode = ClosedSetLattice.closedSet.get(root);
                    parentNode.children.remove(oldNode.bitPresent);
                    oldNode.parents.remove(parentNode.bitPresent);
                } else {
                    ClosedSetLattice.rootNode.children.remove(oldNodeBitPresent);
                }    

                ClosedSetLattice.addChild(newNode, oldNode);
                this.updateRecursive(newNode, oldNode.bitPresent);
                continue;
            } 
            
            
            // Case partial overlap - newNode and oldNode have a common child
            if ((oldNode = ClosedSetLattice.closedSet.get(intersect)) == null) {
                Node commonChild = newNode.getCommonChild((ModifiedBitSet) intersect.clone());
                ClosedSetLattice.closedSet.put(commonChild.bitPresent, commonChild);

                ClosedSetLattice.addChild(newNode, commonChild);
                this.addNodeToTree(commonChild, oldNodeBitPresent);
            } else {
                ClosedSetLattice.addChild(newNode, oldNode);
                this.updateRecursive(newNode, oldNode.bitPresent);
            }

            
        }

        if (root != null) {
            oldNode = ClosedSetLattice.closedSet.get(root);
            ClosedSetLattice.addChild(oldNode, newNode);
            newNode.updateUsing(oldNode);
        } else {
            ClosedSetLattice.rootNode.children.add(newNode.bitPresent);
        }
    }

    boolean shouldBeAdded(Node newNode, ModifiedBitSet intersect) {
        Iterator<ModifiedBitSet> ite = newNode.children.iterator();
        boolean shouldAdd = false;
        ModifiedBitSet temp;
        while (ite.hasNext()) {
            temp = ite.next();
            if (!shouldAdd && temp.isSuperset(intersect)) {
                return false;
            } else if (intersect.isSuperset(temp)) {
                shouldAdd = true;
                ClosedSetLattice.closedSet.get(temp).parents.remove(newNode.bitPresent);
                ite.remove();
            }
        }
        return true;
    }
                                                                                                                                                         
    static void filterHighestChild(Node newNode) {

        List<ModifiedBitSet> children = new ArrayList<>(newNode.children);
        newNode.children.clear();

        int index = 0;
        while (index < children.size()) {
            for (int i = index + 1; i < children.size(); i++) {
                if (children.get(index).isSuperset(children.get(i))) {
                    ClosedSetLattice.closedSet.get(children.get(i)).parents.remove(newNode.bitPresent);
                    children.remove(i--);
                } else if (children.get(i).isSuperset(children.get(index))) {
                    ClosedSetLattice.closedSet.get(children.get(index)).parents.remove(newNode.bitPresent);
                    children.remove(index);
                    i = index + 1;
                }
            }
            index += 1;
        }

        newNode.children.addAll(children);
    }



    void updateRecursive(Node inputNode, ModifiedBitSet startBitPresent) {
        if (!this.addedTransaction.contains(startBitPresent)) {
            Node node = ClosedSetLattice.closedSet.get(startBitPresent);
            node.updateUsing(inputNode);
            this.addedTransaction.add(node.bitPresent);

            for (ModifiedBitSet childBitPresent : node.children) {
                this.updateRecursive(inputNode, childBitPresent);
            }
        }
    }


    public void printStats(int minUtility) {
        System.out.println("=============  ClosedSetLattice ALGORITHM - STATS =============");
        System.out.println(" Total time ~ " + (this.endTimestamp - this.startTimestamp) + " ms");
        System.out.println(" Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println(" Closed High-utility itemsets count : " + ClosedSetLattice.closedSet.values()
                .stream()
                .filter(i -> i.utility >= minUtility).count());
        System.out.println(" Candidate count : " + ClosedSetLattice.closedSet.values().size());
        System.out.println("=====================================================");
    }

    public void writeToOutputFile(String outputFilePath)
            throws IOException {
        FileWriter fileWriter = new FileWriter(outputFilePath);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        ClosedSetLattice.closedSet.values().forEach(i -> printWriter.println(i + " #UTIL " + i.utility + " #SUPPORT " + i.support));
        printWriter.close();
    }

    public void deleteTransactions(List<Integer> transactionIds) {
        HashSet<ModifiedBitSet> affectedNodesByAllTids = new HashSet<>();
        HashSet<ModifiedBitSet> affectedNodesByThisTid = new HashSet<>();
        Queue<ModifiedBitSet> queue = new LinkedList<>();

        for (int tid : transactionIds) {
            TransactionTP transaction = this.database.getTransactions().get(tid - 1);
            // Add bitPresent of an itemset corresponding to this transaction to queue
            queue.add(ModifiedBitSet.fromTransaction(transaction));
            while (!queue.isEmpty()) {
                Node node = ClosedSetLattice.closedSet.get(queue.poll());
                if (!affectedNodesByThisTid.contains(node.bitPresent)) {

                    // Remove this transaction from the node
                    node.removeTransaction(transaction);
                    affectedNodesByThisTid.add(node.bitPresent);
                    affectedNodesByAllTids.add(node.bitPresent);
                    queue.addAll(node.children);
                }
            }
            affectedNodesByThisTid.clear();
        }

        for (ModifiedBitSet nodeBitPresent : affectedNodesByAllTids) {
            Node affectedNode = ClosedSetLattice.closedSet.get(nodeBitPresent);
            if (affectedNode.shouldBeDeleted()) {
                deleteNode(affectedNode);
            }
        }
    }

    public static void deleteNode(Node node) {
        node.children.forEach(c -> {
            Node child = ClosedSetLattice.closedSet.get(c);
            child.parents.addAll(node.parents);
            child.parents.remove(node.bitPresent);

            if (child.parents.isEmpty()) {
                ClosedSetLattice.rootNode.children.add(child.bitPresent);
            }
        });

        node.parents.forEach(p -> {
            Node parent = ClosedSetLattice.closedSet.get(p);
            parent.children.addAll(node.children);
            parent.children.remove(node.bitPresent);
            filterHighestChild(parent);
        });

        
        ClosedSetLattice.rootNode.children.remove(node.bitPresent);
        ClosedSetLattice.closedSet.remove(node.bitPresent);
    }
}
