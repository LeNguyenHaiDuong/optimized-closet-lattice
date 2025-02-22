package updateClosedSetLattice;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemIdMapper {
    public static final Hashtable<Integer, Integer> itemIds = new Hashtable<>();
    // public static final Hashtable<Integer, Integer> originalItemIds = new Hashtable<>();
    public static final List<Integer> originalItemIds = new ArrayList<>();
    public static AtomicInteger lastId = new AtomicInteger(-1);

    public static int getItemId(Integer item) {
        Integer id = itemIds.get(item);
        if (id == null) {
            id = lastId.addAndGet(1);
            itemIds.put(item, id);
            // originalItemIds.put(id, item);
            originalItemIds.add(item);
        }
        return id;
    }

    public static Integer getOriginalItemId(Integer itemId) {
        return originalItemIds.get(itemId);
    }
}