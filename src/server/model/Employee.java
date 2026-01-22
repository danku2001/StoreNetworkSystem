package server.model;

public class Employee {
    private final String username;
    private final String password;
    private final Role role;
    private final int branchId;

    private final String fullName;
    private final String id;
    private final String phone;
    private final String bankAccount;
    private final String employeeNumber;

    public Employee(
            String username, String password, Role role, int branchId,
            String fullName, String id, String phone,
            String bankAccount, String employeeNumber
    ) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.branchId = branchId;
        this.fullName = fullName;
        this.id = id;
        this.phone = phone;
        this.bankAccount = bankAccount;
        this.employeeNumber = employeeNumber;
    }

    public Employee(String username, String password, Role role, int branchId) {
        this(username, password, role, branchId,
                username, "N/A", "N/A", "N/A", "N/A");
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public int getBranchId() { return branchId; }

    public String getFullName() { return fullName; }
    public String getId() { return id; }
    public String getPhone() { return phone; }
    public String getBankAccount() { return bankAccount; }
    public String getEmployeeNumber() { return employeeNumber; }
}
