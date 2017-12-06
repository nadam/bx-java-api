package se.anyro.bx.types;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 10 most recent trades, and top 10 asks and bids in the order book.
 */
public class RecentTrades {
    public Trade[] trades;
    public Order[] lowask;
    public Order[] highbid;

    public static class Trade {
        public int trade_id;
        public BigDecimal rate;
        public BigDecimal amount;
        public Date trade_date;
        public int order_id;
        public String trade_type; // "sell" or "buy"
        public int seconds;
    }

    public static class Order {
        public int order_id;
        public BigDecimal rate;
        public BigDecimal amount;
        public Date date_added;
        public String order_type; // "sell" or "buy"
        public String display_vol1; // Example: "30,000.00 THB"
        public String display_vol2;
    }
}