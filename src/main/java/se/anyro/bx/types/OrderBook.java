package se.anyro.bx.types;

import java.math.BigDecimal;

/**
 * Compact version of the full order book of a currency pairing. Each row is an array with the two amounts (one for each
 * currency).
 */
public class OrderBook {
    public BigDecimal[][] bids;
    public BigDecimal[][] asks;
}