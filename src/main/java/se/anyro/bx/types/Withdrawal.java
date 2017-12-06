package se.anyro.bx.types;

import java.math.BigDecimal;
import java.util.Date;

public class Withdrawal {
    public int withdrawal_id;
    public Date date_requested;
    public BigDecimal amount; // May be negative
    public String currency;
    public String address;
    public String withdrawal_status; // "Pending", "Canceled", "Completed"
    public String transaction_id; // Optional. Format depending on the currency.
}