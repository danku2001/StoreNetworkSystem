package server.model.customer;

public abstract class Customer {
    protected final String fullName;
    protected final String id;
    protected final String phone;

    public Customer(String fullName, String id, String phone) {
        this.fullName = fullName;
        this.id = id;
        this.phone = phone;
    }

    public String getFullName() { return fullName; }
    public String getId() { return id; }
    public String getPhone() { return phone; }

    public abstract String getType();

    public abstract int effectiveQtyToSell(int requestedQty);

    public abstract double finalPrice(double unitPrice, int requestedQty);
}