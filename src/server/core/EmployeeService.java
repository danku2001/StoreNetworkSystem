package server.core;

import server.model.Employee;
import server.model.Role;

public class EmployeeService {

    private final ServerDataStore store = ServerDataStore.getInstance();

    public void addEmployee(
            String username,
            String password,
            String roleStr,
            int branchId,
            String fullName,
            String id,
            String phone,
            String bankAccount,
            String employeeNumber
    ) {
        validateUsername(username);
        validatePassword(password); 
        Role role = parseRole(roleStr);

        store.addEmployee(new Employee(
                username,
                password,
                role,
                branchId,
                fullName,
                id,
                phone,
                bankAccount,
                employeeNumber
        ));
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("InvalidUsername");
        }
        if (username.contains(" ") || username.length() < 3) {
            throw new IllegalArgumentException("InvalidUsername");
        }
    }

    private void validatePassword(String password) {
        if (password == null) throw new IllegalArgumentException("WeakPassword");
        if (password.length() < 6) throw new IllegalArgumentException("WeakPassword");

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        if (!hasLetter || !hasDigit) throw new IllegalArgumentException("WeakPassword");
    }

    private Role parseRole(String roleStr) {
        if (roleStr == null) throw new IllegalArgumentException("InvalidRole");
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("InvalidRole");
        }
    }
}
