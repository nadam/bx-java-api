package se.anyro.bx.types;

import java.math.BigDecimal;

/**
 * Possible currencies and the balance etc of each
 */
public class Balance {
    public BigDecimal total;
    public BigDecimal available;
    public BigDecimal orders;
    public BigDecimal withdrawals;
    public BigDecimal deposits;
}