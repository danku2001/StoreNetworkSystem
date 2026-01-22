package server.core;

import server.model.Product;
import server.model.customer.Customer;

public class PurchaseService {

    private final ServerDataStore store = ServerDataStore.getInstance();
    private final InventoryService inventoryService = new InventoryService();

    public synchronized double purchase(int branchId, String customerId, int productId, int qty) throws Exception {
        if (qty <= 0) throw new Exception("InvalidQty");

        Customer c = store.getCustomer(customerId);
        if (c == null) throw new Exception("CustomerNotFound");

        Product p = store.getProduct(productId);
        if (p == null) throw new Exception("ProductNotFound");

        int effectiveSell = c.effectiveQtyToSell(qty);

        inventoryService.sell(branchId, productId, effectiveSell);

        store.addSold(branchId, productId, qty);

        return c.finalPrice(p.getPrice(), qty);
    }
}

