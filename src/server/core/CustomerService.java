package server.core;

import server.model.customer.Customer;
import server.model.customer.NewCustomer;
import server.model.customer.ReturningCustomer;
import server.model.customer.VipCustomer;

public class CustomerService {

    private final ServerDataStore store = ServerDataStore.getInstance();

    public void addCustomer(String fullName, String id, String phone, String type) throws Exception {
        if (store.getCustomer(id) != null) throw new Exception("CustomerAlreadyExists");

        Customer c;
        String t = type.toUpperCase();

        if (t.equals("NEW")) c = new NewCustomer(fullName, id, phone);
        else if (t.equals("RETURNING")) c = new ReturningCustomer(fullName, id, phone);
        else if (t.equals("VIP")) c = new VipCustomer(fullName, id, phone);
        else throw new Exception("InvalidCustomerType");

        store.addCustomer(c);
    }
}
