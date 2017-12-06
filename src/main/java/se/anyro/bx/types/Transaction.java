package se.anyro.bx.types;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Data of one part of a transaction (trade, withdraw, deposit or fee)
 */
public class Transaction {
    public int transaction_id;
    public String currency;
    public BigDecimal amount; // May be negative
    public Date date;
    public String type; // "trade", "fee", "deposit" or "withdraw"

    /**
     * Get the type field as an enum value.
     */
    public TransactionType getType() {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}