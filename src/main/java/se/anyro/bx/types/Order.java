package se.anyro.bx.types;

import java.math.BigDecimal;
import java.util.Date;

/**
 * A buy or sell order
 */
public class Order {
    public int pairing_id;
    public int order_id;
    public String order_type; // "sell" or "buy"
    public BigDecimal amount;
    public BigDecimal rate;
    public Date date;

    /**
     * Get the order type field as an enum value.
     */
    public OrderType getOrderType() {
        try {
            return OrderType.valueOf(order_type.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}