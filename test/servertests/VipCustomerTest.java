package servertests;

import org.junit.jupiter.api.Test;
import server.model.customer.VipCustomer;

import static org.junit.jupiter.api.Assertions.*;

public class VipCustomerTest {

    @Test
    void vipGetsBuy2Get1LikeQtyEffect() {
        VipCustomer c = new VipCustomer("vip", "1", "050");
        assertEquals(3, c.effectiveQtyToSell(2));
    }

    @Test
    void vipFinalPriceNoDiscount() {
        VipCustomer c = new VipCustomer("vip", "1", "050");
        assertEquals(200.0, c.finalPrice(100.0, 2), 0.0001);
    }
}
