package server.model.customer;

public class VipCustomer extends Customer {
    public VipCustomer(String fullName, String id, String phone) {
        super(fullName, id, phone);
    }

    @Override
    public String getType() { return "VIP"; }

    @Override
    public int effectiveQtyToSell(int requestedQty) {
        int free = requestedQty / 2;
        return requestedQty + free;
    }

    @Override
    public double finalPrice(double unitPrice, int requestedQty) {
        return unitPrice * requestedQty;
    }
}
