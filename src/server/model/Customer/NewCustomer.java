package server.model.customer;

public class NewCustomer extends Customer {
    public NewCustomer(String fullName, String id, String phone) {
        super(fullName, id, phone);
    }

    @Override
    public String getType() { return "NEW"; }

    @Override
    public int effectiveQtyToSell(int requestedQty) {
        return requestedQty; 
    }

    @Override
    public double finalPrice(double unitPrice, int requestedQty) {
        return unitPrice * requestedQty;
    }
}
