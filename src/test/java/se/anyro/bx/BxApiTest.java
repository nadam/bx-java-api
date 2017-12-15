package se.anyro.bx;

import static se.anyro.bx.BuildVars.API_KEY;
import static se.anyro.bx.BuildVars.API_SECRET;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;
import se.anyro.bx.types.Balance;
import se.anyro.bx.types.BillPaymentGroup;
import se.anyro.bx.types.BillPaymentServiceProvider;
import se.anyro.bx.types.Order;
import se.anyro.bx.types.OrderBook;
import se.anyro.bx.types.OrderType;
import se.anyro.bx.types.Pairing;
import se.anyro.bx.types.RecentTrades;
import se.anyro.bx.types.TradeData;
import se.anyro.bx.types.Transaction;
import se.anyro.bx.types.TransactionType;
import se.anyro.bx.types.Withdrawal;

/**
 * More examples than tests. Some tests starting with underscore to prevent them from running since they require API
 * keys with more permissions.
 */
public class BxApiTest extends TestCase {

    private static BxApi bx = new BxApi(API_KEY, API_SECRET);

    public void testMarketData() throws IOException {
        // Ticker[] tickers = bx.getMarketData();
        // assertTrue(tickers.length > 0);
        // assertNotNull(tickers[0].orderbook.asks);
    }

    public void _testCurrencyPairings() throws IOException {
        Pairing[] pairings = bx.getCurrencyPairings();
        assertTrue(pairings.length > 0);
    }

    public void _testRecentTrades() throws IOException {
        RecentTrades recentTrades = bx.getRecentTrades(1);
        assertEquals(10, recentTrades.trades.length);
        assertEquals(10, recentTrades.highbid.length);
        assertEquals(10, recentTrades.lowask.length);
    }

    public void _testOrderBook() throws IOException {
        OrderBook orderBook = bx.getOrderBook(1);
        assertTrue(orderBook.asks.length > 0);
        assertTrue(orderBook.bids.length > 0);
    }

    public void _testHistoricalTradeData() throws IOException {
        TradeData tradeData = bx.getHistoricalTradeData(1, "2017-10-19");
        assertEquals(420, tradeData.volume.intValue());
    }

    public void _testOrder() throws IOException {
        BigDecimal amount = new BigDecimal("200");
        BigDecimal rate = new BigDecimal("10000");
        int orderId = bx.createOrder(1, OrderType.BUY, amount, rate, null);
        assertTrue(orderId > 0);
        bx.cancelOrder(1, orderId);
    }

    public void _testBalances() throws IOException {
        Map<String, Balance> balances = bx.getBalances(null);
        assertNotNull(balances.get("BTC"));
    }

    public void _testGetOrders() throws IOException {
        Order[] orders = bx.getOrders(1, null, null);
        assertTrue(orders.length > 0);
        assertTrue(orders[0].order_id > 0);
        assertNotNull(orders[0].date);
    }

    public void _testTransactionHistory() throws IOException {
        Transaction[] transactions = bx.getTransactionHistory(null);
        assertNotNull(transactions[0].currency);
        assertNotNull(transactions[0].type);

        transactions = bx.getTransactionHistory("BTC", TransactionType.TRADE, null, new Date(), null);
        assertEquals("BTC", transactions[0].currency);
        assertNotNull(transactions[0].type);
    }

    public void _testGetDepositAddress() throws IOException {
        String address = bx.getDepositAddress("BTC", false, null);
        assertNotNull(address);
    }

    public void _testRequestWithdrawal() throws IOException {
        BigDecimal amount = new BigDecimal("1");
        int withdrawalId = bx.requestWithdrawal("THB", amount, null, "123", null);
        assertTrue(withdrawalId > 0);
    }

    public void _testWithdrawalHistory() throws IOException {
        Withdrawal[] result = bx.getWithdrawalHistory(null);
        assertNotNull(result[0].currency);
        assertNotNull(result[0].withdrawal_status);
    }

    public void _testBillPaymentGroupTypes() throws IOException {
        BillPaymentGroup[] groups = bx.getBillPaymentGroupTypes(null);
        assertTrue(groups[0].id > 0);
        assertNotNull(groups[0].name);
    }

    public void _testBillPaymentServiceProviders() throws IOException {
        BillPaymentServiceProvider[] providers = bx.getBillPaymentServiceProviders(1, null);
        assertTrue(providers[0].id > 0);
        assertNotNull(providers[0].name);
    }

    public void _testBillPayment() throws IOException {
        BigDecimal amount = new BigDecimal("1234");
        String account = "0123456789"; // Typically a phone number
        int withdrawalId = bx.createBillPayment(1, amount, account, null);
        assertTrue(withdrawalId > 0);
    }
}