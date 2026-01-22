package server.core;

import server.model.Employee;
import server.model.Role;
import server.model.Product;
import server.model.customer.Customer;
import server.model.customer.NewCustomer;
import server.model.customer.ReturningCustomer;
import server.model.customer.VipCustomer;

import java.util.*;

public class ServerDataStore {

    private static ServerDataStore instance;

    private final Map<String, Employee> employeesByUsername = new HashMap<>();
    private final Set<String> loggedInUsers = new HashSet<>();

    private final Map<Integer, Product> productsById = new HashMap<>();
    private final Map<Integer, Map<Integer, Integer>> inventoryByBranch = new HashMap<>();

    private final Map<String, Customer> customersById = new HashMap<>();

    private final Map<Integer, Map<Integer, Integer>> soldByBranch = new HashMap<>();

    private ServerDataStore() {

        employeesByUsername.put("admin",
                new Employee("admin", "admin123", Role.ADMIN, 0,
                        "Admin User", "000000000", "0500000000", "000-000", "EMP-000"));

        employeesByUsername.put("dan",
                new Employee("dan", "1234", Role.CASHIER, 1,
                        "Dan Worker", "111111111", "0501111111", "111-111", "EMP-101"));

        employeesByUsername.put("shahar",
                new Employee("shahar", "1234", Role.SELLER, 2,
                        "Shahar Worker", "222222222", "0502222222", "222-222", "EMP-202"));

        productsById.put(1, new Product(1, "T-Shirt", "Shirts", 59.90));
        productsById.put(2, new Product(2, "Jeans", "Pants", 199.90));
        productsById.put(3, new Product(3, "Jacket", "Outerwear", 299.90));

        inventoryByBranch.put(1, new HashMap<>());
        inventoryByBranch.put(2, new HashMap<>());

        inventoryByBranch.get(1).put(1, 10);
        inventoryByBranch.get(1).put(2, 5);

        inventoryByBranch.get(2).put(1, 3);
        inventoryByBranch.get(2).put(3, 7);

        customersById.put("123456789", new NewCustomer("Noa Levi", "123456789", "0503333333"));
        customersById.put("987654321", new ReturningCustomer("Avi Cohen", "987654321", "0504444444"));
        customersById.put("555555555", new VipCustomer("Maya VIP", "555555555", "0505555555"));
    }

    public static synchronized ServerDataStore getInstance() {
        if (instance == null) instance = new ServerDataStore();
        return instance;
    }

    public Employee findEmployee(String username) { return employeesByUsername.get(username); }
    public Collection<Employee> listEmployees() { return employeesByUsername.values(); }
    public void addEmployee(Employee e) { employeesByUsername.put(e.getUsername(), e); }

    public boolean isLoggedIn(String username) { return loggedInUsers.contains(username); }
    public void markLoggedIn(String username) { loggedInUsers.add(username); }
    public void markLoggedOut(String username) { loggedInUsers.remove(username); }

    public Product getProduct(int productId) { return productsById.get(productId); }
    public Collection<Product> listProducts() { return productsById.values(); }

    public Map<Integer, Integer> getBranchInventory(int branchId) {
        inventoryByBranch.putIfAbsent(branchId, new HashMap<>());
        return inventoryByBranch.get(branchId);
    }

    public void addToInventory(int branchId, int productId, int deltaQty) {
        Map<Integer, Integer> inv = getBranchInventory(branchId);
        int current = inv.getOrDefault(productId, 0);
        int updated = Math.max(0, current + deltaQty);
        inv.put(productId, updated);
    }

    public Customer getCustomer(String id) { return customersById.get(id); }
    public Collection<Customer> listCustomers() { return customersById.values(); }
    public void addCustomer(Customer c) { customersById.put(c.getId(), c); }

    public Map<Integer, Integer> getBranchSales(int branchId) {
        soldByBranch.putIfAbsent(branchId, new HashMap<>());
        return soldByBranch.get(branchId);
    }

    public void addSold(int branchId, int productId, int qty) {
        Map<Integer, Integer> m = getBranchSales(branchId);
        m.put(productId, m.getOrDefault(productId, 0) + qty);
    }

    public java.util.Set<Integer> listBranchIds() {
    return new java.util.HashSet<>(inventoryByBranch.keySet());
}

}
