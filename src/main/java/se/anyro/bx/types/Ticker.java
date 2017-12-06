package se.anyro.bx.types;

import java.math.BigDecimal;


/**
 * Market data of a currency pairing
 */
public class Ticker {
    public int pairing_id;
    public String primary_currency;
    public String secondary_currency;
    public BigDecimal change;
    public BigDecimal last_price;
    public BigDecimal volume_24hours;
    public TickerOrderBook orderbook;

    public static class TickerOrderBook {
        public TickerOrders bids;
        public TickerOrders asks;
    }

    public static class TickerOrders {
        public int total;
        public BigDecimal volume;
        public BigDecimal highbid; // Or low ask
    }
}