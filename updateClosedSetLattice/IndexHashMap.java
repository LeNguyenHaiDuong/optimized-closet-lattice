package updateClosedSetLattice;

import java.util.ArrayList;
import java.util.List;

public class IndexHashMap {
    public ArrayList<Node>[] mapValues;

    public IndexHashMap(int size) {
        mapValues = new ArrayList[size];
        for (int i = 0; i < this.mapValues.length; i++) {
            this.mapValues[i] = new ArrayList<>();
        }
    }

    public void put(ModifiedBitSet key, Node value) {
        int insertIndex = this.keyHashCode(key);
        this.mapValues[insertIndex].add(value);
    }

    public boolean containsKey(ModifiedBitSet key) {
        return this.get(key) != null;
    }

    public Node get(ModifiedBitSet key) {
        int index = this.keyHashCode(key);
        for (Node value : this.mapValues[index]) {
            if (value.bitPresent.equals(key)) {
                return value;
            }
        }
        return null;
    }

    public void remove(ModifiedBitSet key) {
        int index = this.keyHashCode(key);
        for (int i = 0; i < this.mapValues[index].size(); i++) {
            if (this.mapValues[index].get(i).bitPresent.equals(key)) {
                this.mapValues[index].remove(i);
                break;
            }
        }
    }

    public List<Node> values() {
        List<Node> values = new ArrayList<>();
        for (List<Node> mapValue : this.mapValues) {
            values.addAll(mapValue);
        }
        return values;
    }

    public int size() {
        int size = 0;
        for (List<Node> mapValue : this.mapValues) {
            size += mapValue.size();
        }
        return size;
    }

    int keyHashCode(ModifiedBitSet key) {
        return Math.abs(key.hashCode()) % this.mapValues.length;
    }
}