package servertests;

import org.junit.jupiter.api.Test;
import server.core.AuthService;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    void duplicateLoginBlocked() throws Exception {
        AuthService auth = new AuthService();
        auth.login("dan", "1234");
        assertThrows(Exception.class, () -> auth.login("dan", "1234"));
    }
}
