package servertests;

import org.junit.jupiter.api.Test;
import server.core.PurchaseService;

import static org.junit.jupiter.api.Assertions.*;

public class PurchaseServiceTest {

    @Test
    void purchaseFailsWhenCustomerNotFound() {
        PurchaseService p = new PurchaseService();
        assertThrows(Exception.class, () -> p.purchase(1, "000000000", 1, 1));
    }
}
