package servertests;

import org.junit.jupiter.api.Test;
import server.core.InventoryService;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceTest {

    @Test
    void sellingTooMuchFails() {
        InventoryService inv = new InventoryService();
        assertThrows(Exception.class, () -> inv.sell(1, 1, 9999));
    }
}
