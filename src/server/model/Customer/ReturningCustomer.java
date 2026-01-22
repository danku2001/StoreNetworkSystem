package server.model.customer;

public class ReturningCustomer extends Customer {
    public ReturningCustomer(String fullName, String id, String phone) {
        super(fullName, id, phone);
    }

    @Override
    public String getType() { return "RETURNING"; }

    @Override
    public int effectiveQtyToSell(int requestedQty) {
        return requestedQty;
    }

    @Override
    public double finalPrice(double unitPrice, int requestedQty) {
        double total = unitPrice * requestedQty;
        return total * 0.95; 
    }
}
