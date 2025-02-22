package updateClosedSetLattice;

import hui_miner.AlgoCHUIMiner;
import hui_miner.Itemset;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

public class MainClosedSetLattice {
    public static void main(String[] args) throws Exception {

        String input = "./foodmart.txt";
        int size = 2000;

        // String input = "./ecommerce.txt";
        // int size = 500000;

        // String input = ".//data/mushroom.txt";
        // int size = 300000;

        // String input = ".//data/chess.txt";
        // int size = 300000;

        String output = ".//output.txt";
        String inputPartial = ".//foodmart_partial";
        String inputAfterDelete = ".//foodmart_after_delete";

        int minUtility = 1;
        int maxTransactions = -1; // default = -1
        int deleteTransactions = 0; // default = 0
        boolean runTests = true;
        if (args.length != 0) {
            maxTransactions = Integer.parseInt(args[0]);
        }

        // Write first maxTransactions from input to tmpInput
        List<String> lines = Files.readAllLines(Path.of(input)).stream().toList();
        if (maxTransactions != -1) {
            lines = lines.subList(0, maxTransactions);
        }
        Files.write(Path.of(inputPartial), lines);
        if (deleteTransactions != 0) {
            lines = lines.subList(deleteTransactions, lines.size());
        }
        Files.write(Path.of(inputAfterDelete), lines);

        // System.out.println("============ Running Algorithm ============");
        AlgoClosedSetLattice algorithm = new AlgoClosedSetLattice(size);
        algorithm.runAlgorithm(inputPartial);
        System.out.println("Done adding transactions");
        ArrayList<Integer> deletingTransactions = new ArrayList<>();
        for (int i = 1; i <= deleteTransactions; i++) {
            deletingTransactions.add(i);
        }
        long startTime = System.currentTimeMillis();
        algorithm.deleteTransactions(deletingTransactions);
        long endTime = System.currentTimeMillis();
        algorithm.printStats(minUtility);
        System.out.println("Delete time: " + (endTime - startTime) + " ms");

        algorithm.writeToOutputFile(output);

        if (runTests) {
            System.out.println("\n\n============ Running compare algorithm ============");
            AlgoCHUIMiner chuiMinerAlgorithm = new AlgoCHUIMiner(true);
            chuiMinerAlgorithm.runAlgorithm(inputAfterDelete, minUtility, null);
            chuiMinerAlgorithm.printStats();
            List<Itemset> chuiMinerItems = chuiMinerAlgorithm.chuis;

            System.out.println("\n\n============ Checking results ============");

            // Check same number of CHUIs from 2 algorithms
            int closedSetLatticeItemsCount = ClosedSetLattice.closedSet.size();
            int chuiMinerItemsCount = chuiMinerItems.size();
            if (closedSetLatticeItemsCount != chuiMinerItemsCount) {
                System.out.printf("Expected %d items in closed set, got %d%n", chuiMinerItemsCount,
                        closedSetLatticeItemsCount);
                if (chuiMinerItemsCount < closedSetLatticeItemsCount) {
                    var differentItems = ClosedSetLattice.closedSet.values().stream().filter(i -> {
                        for (var chui : chuiMinerItems) {
                            if (ModifiedBitSet.newSetFromOriginal(chui.itemset).equals(i.bitPresent)) {
                                return false;
                            }
                        }
                        return true;
                    }).map(i -> i.bitPresent).toList();
                    System.out.printf("Items only in closed set lattice: %s%n", differentItems);
                } else {
                    var missingItems = chuiMinerItems.stream()
                            .map(i -> i.itemset)
                            .map(ModifiedBitSet::newSetFromOriginal)
                            .filter(i -> !ClosedSetLattice.closedSet.containsKey(i))
                            .toList();
                    System.out.printf("Items only in their: %s%n", missingItems);
                }
                return;
            }

            // Check each CHUI from chuiMinerAlgorithm is in algorithm
            for (var chui : chuiMinerItems) {
                ModifiedBitSet bitPresent = new ModifiedBitSet();
                IntStream.of(chui.itemset).map(ItemIdMapper::getItemId).forEach(bitPresent::set);

                Node node = ClosedSetLattice.closedSet.get(bitPresent);
                if (node == null) {
                    System.out.printf("Node %s not found in closed set%n", bitPresent);
                    return;
                }

                int nodeSupport = node.support;
                if (nodeSupport != chui.support) {
                    System.out.printf("Support of node %s is not correct! Expected %d, got %d%n", bitPresent,
                            chui.support, nodeSupport);
                    System.out.printf("Transactions: %s%n", nodeSupport);
                    return;
                }

                if (node.utility != chui.utility) {
                    System.out.printf("Utility of node %s is not correct! Expected %d, got %d%n", bitPresent,
                            chui.utility, node.utility);
                    return;
                }
            }

            // Check algorithm traversal
            Queue<ModifiedBitSet> queue = new ArrayDeque<>(ClosedSetLattice.rootNode.children);
            HashSet<ModifiedBitSet> alreadyVisited = new HashSet<>();

            while (!queue.isEmpty()) {
                ModifiedBitSet bitPresent = queue.poll();
                Node node = ClosedSetLattice.closedSet.get(bitPresent);
                if (node == null) {
                    System.out.printf("Node %s not found in closed set%n", bitPresent);
                    return;
                }
                if (alreadyVisited.contains(node.bitPresent)) {
                    continue;
                }
                alreadyVisited.add(node.bitPresent);

                // Check if node has parents but is in rootNodes
                if (!node.parents.isEmpty() && ClosedSetLattice.rootNode.children.contains(node.bitPresent)) {
                    System.out.printf("Node %s has parents but is in rootNodes%n", node.bitPresent);
                    return;
                }

                // Check if node has no parents but is not in rootNodes
                if (node.parents.isEmpty() && !ClosedSetLattice.rootNode.children.contains(node.bitPresent)) {
                    System.out.printf("Node %s has no parents but is not in rootNodes%n", node.bitPresent);
                    return;
                }

                // Check if all parents of this node have this node as child
                List<ModifiedBitSet> parentsButNotHaveNodeAsChildren = node.parents.stream()
                        .map(p -> ClosedSetLattice.closedSet.get(p))
                        .filter(Objects::nonNull)
                        .filter(p -> !p.children.contains(node.bitPresent))
                        .map(p -> p.bitPresent)
                        .toList();
                if (!parentsButNotHaveNodeAsChildren.isEmpty()) {
                    System.out.printf("Node %s has parents but some of them dont have this node as children%n",
                            node.bitPresent);
                    System.out.printf("Invalid parents %s%n", parentsButNotHaveNodeAsChildren);
                    return;
                }

                // Check if all children of this node have this node as parent
                List<ModifiedBitSet> childrenButNotHaveNodeAsParents = node.children.stream()
                        .map(c -> ClosedSetLattice.closedSet.get(c))
                        .filter(Objects::nonNull)
                        .filter(c -> !c.parents.contains(node.bitPresent))
                        .map(c -> c.bitPresent)
                        .toList();
                if (!childrenButNotHaveNodeAsParents.isEmpty()) {
                    System.out.printf("Node %s has children but some of them dont have this node as parents%n",
                            node.bitPresent);
                    System.out.printf("Invalid children %s%n", childrenButNotHaveNodeAsParents);
                    return;
                }

                // Check if all parents of this node are in closed set
                List<ModifiedBitSet> invalidParents = node.parents.stream()
                        .filter(p -> !ClosedSetLattice.closedSet.containsKey(p))
                        .toList();
                if (!invalidParents.isEmpty()) {
                    System.out.printf("Node %s has parents but some of them are not in closed set%n", node.bitPresent);
                    System.out.printf("Missing %s%n", invalidParents);
                    return;
                }

                // Check if all children of this node are in closed set
                List<ModifiedBitSet> invalidChildren = node.children.stream()
                        .filter(c -> !ClosedSetLattice.closedSet.containsKey(c))
                        .toList();
                if (!invalidChildren.isEmpty()) {
                    System.out.printf("Node %s has children but some of them are not in closed set%n", node.bitPresent);
                    System.out.printf("Missing %s%n", invalidChildren);
                    return;
                }

                queue.addAll(node.children);
            }

            // Check if number of items in closed set is equal to number of items visited by
            // traversal
            if (alreadyVisited.size() != ClosedSetLattice.closedSet.size()) {
                System.out.printf("Expected %d items in closed set, got %d by traversal%n",
                        ClosedSetLattice.closedSet.size(), alreadyVisited.size());
                return;
            }
        }

        System.out.println("Done!");
        System.exit(3005);
    }
}