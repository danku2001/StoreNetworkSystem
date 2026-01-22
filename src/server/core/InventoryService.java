package server.core;

import server.model.Product;

import java.util.HashMap;
import java.util.Map;

public class InventoryService {

    private final ServerDataStore store = ServerDataStore.getInstance();

    public synchronized Map<Integer, Integer> getInventorySnapshot(int branchId) {
        return new HashMap<>(store.getBranchInventory(branchId));
    }

    public synchronized void buy(int branchId, int productId, int qty) throws Exception {
        if (qty <= 0) throw new Exception("InvalidQty");
        store.addToInventory(branchId, productId, qty);
    }

    public synchronized void sell(int branchId, int productId, int qty)
            throws OutOfStockException, Exception {

        if (qty <= 0) throw new Exception("InvalidQty");

        int current = store.getBranchInventory(branchId)
                .getOrDefault(productId, 0);

        if (current < qty) {
            throw new OutOfStockException(
                    "NotEnoughStock current=" + current);
        }

        store.addToInventory(branchId, productId, -qty);
    }

    public Product getProduct(int productId) {
        return store.getProduct(productId);
    }
}
