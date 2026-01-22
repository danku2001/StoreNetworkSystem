package servertests;

import org.junit.jupiter.api.Test;
import server.model.customer.ReturningCustomer;

import static org.junit.jupiter.api.Assertions.*;

public class ReturningCustomerTest {

    @Test
    void returningGets5PercentDiscount() {
        ReturningCustomer c = new ReturningCustomer("ret", "2", "050");
        assertEquals(190.0, c.finalPrice(100.0, 2), 0.0001);
    }
}
