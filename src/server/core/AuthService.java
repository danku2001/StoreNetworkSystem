package server.core;

import server.model.Employee;

public class AuthService {
    private final ServerDataStore store = ServerDataStore.getInstance();

    public synchronized Employee login(String username, String password) throws Exception {
        if (username == null || username.isEmpty()) {
            throw new Exception("InvalidUsername");
        }

        if (store.isLoggedIn(username)) {
            throw new Exception("DuplicateLogin");
        }

        Employee emp = store.findEmployee(username);
        if (emp == null) {
            throw new Exception("UserNotFound");
        }
        if (!emp.getPassword().equals(password)) {
            throw new Exception("WrongPassword");
        }

        store.markLoggedIn(username);
        return emp;
    }

    public synchronized void logout(String username) {
        if (username != null) {
            store.markLoggedOut(username);
        }
    }
}
