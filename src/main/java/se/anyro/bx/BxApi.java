package se.anyro.bx;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import se.anyro.bx.types.Balance;
import se.anyro.bx.types.BillPaymentGroup;
import se.anyro.bx.types.BillPaymentServiceProvider;
import se.anyro.bx.types.Order;
import se.anyro.bx.types.OrderBook;
import se.anyro.bx.types.OrderType;
import se.anyro.bx.types.Pairing;
import se.anyro.bx.types.RecentTrades;
import se.anyro.bx.types.Ticker;
import se.anyro.bx.types.TradeData;
import se.anyro.bx.types.Transaction;
import se.anyro.bx.types.TransactionType;
import se.anyro.bx.types.Withdrawal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Full implementation of the API at https://bx.in.th/info/api/ except the options APIs, which are not relevant at the
 * moment.
 */
@SuppressWarnings("serial")
public class BxApi extends JsonApi {

    private final String BASE_URL = "https://bx.in.th/api/";

    private final String MARKET_DATA = BASE_URL;
    private final String CURRENCY_PAIRINGS = BASE_URL + "pairing/";
    private final String ORDER_BOOK = BASE_URL + "orderbook/?";
    private final String RECENT_TRADES = BASE_URL + "trade/?";
    private final String TRADE_HISTORY = BASE_URL + "tradehistory/?";

    private final String ORDER = BASE_URL + "order/";
    private final String CANCEL = BASE_URL + "cancel/";
    private final String BALANCE = BASE_URL + "balance/";
    private final String GET_ORDERS = BASE_URL + "getorders/";
    private final String TRANSACTION_HISTORY = BASE_URL + "history/";
    private final String DEPOSIT = BASE_URL + "deposit/";
    private final String WITHDRAWAL = BASE_URL + "withdrawal/";
    private final String WITHDRAWAL_HISTORY = BASE_URL + "withdrawal-history/";
    private final String BILLGROUP = BASE_URL + "billgroup/";
    private final String BILLER = BASE_URL + "biller/";
    private final String BILLPAY = BASE_URL + "billpay/";

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String apiKey;
    private String apiSecret;
    private long nonceCounter = System.currentTimeMillis() / 1000 - 1500000000;

    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 3000;

    /**
     * Constructor for the public API only. Accessible without api key and api secret.
     */
    public BxApi() {
        super(CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Constructor for using all methods including the private API. You can create your API key and secret at
     * https://bx.in.th/account/
     */
    public BxApi(String apiKey, String apiSecret) {
        this();
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    /**
     * Fetch available currency pairings and additional data for each. The map returned from BX is converted into an
     * array for convenience.
     * 
     * @return ticker data of all currency pairings
     */
    public Ticker[] getMarketData() throws IOException {
        MarketData marketData = callMethod(MARKET_DATA, MarketData.class);
        return marketData.values().toArray(new Ticker[marketData.size()]);
    }

    /**
     * Fetch available currency pairings. The map returned from BX is converted into an array for convenience.
     * 
     * @return all available currency pairings, including their pairing_id which is required for some other API calls
     */
    public Pairing[] getCurrencyPairings() throws IOException {
        Pairings pairings = callMethod(CURRENCY_PAIRINGS, Pairings.class);
        return pairings.values().toArray(new Pairing[pairings.size()]);
    }

    /**
     * @param pairingId
     *            Value returned in the methods above
     * @return a list of all buy and sell orders in the order book for the selected pairing market
     */
    public OrderBook getOrderBook(int pairingId) throws IOException {
        return callMethod(ORDER_BOOK + "pairing=" + pairingId, OrderBook.class);
    }

    /**
     * @param pairingId
     *            Value returned in the methods above
     * @return a list of 10 most recent trades, and top 10 asks and bids in orderbook
     */
    public RecentTrades getRecentTrades(int pairingId) throws IOException {
        return callMethod(RECENT_TRADES + "pairing=" + pairingId, RecentTrades.class);
    }

    /**
     * @param pairingId
     *            Value returned in the methods above
     * @param date
     *            Day to get data from in the format "YYYY-MM-DD"
     * @return Weighted Average, Volume, Open, Close, Low and High prices for the specified date
     */
    public TradeData getHistoricalTradeData(int pairingId, String date) throws IOException {
        HistoricalTradeData response = callMethod(TRADE_HISTORY + "pairing=" + pairingId + "&date=" + date,
                HistoricalTradeData.class);
        return response.data;
    }

    /**
     * Creates a buy or sell order on the market. For instance if you want to buy 0.001 BTC for 300 THB, type would be
     * BUY, amount would be 300 and rate would be 300000.
     * 
     * @param pairingId
     *            Value returned in the methods above
     * @param type
     *            BUY or SELL
     * @param amount
     *            Total amount of primary currency (typically THB or BTC) that you want to spend or receive
     * @param rate
     *            Amount of primary currency per secondary currency
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return order-id (or zero if the order is immediately executed)
     */
    public int createOrder(int pairingId, OrderType type, BigDecimal amount, BigDecimal rate, String twofa)
            throws IOException {
        Order order = callMethod(ORDER, createSecurityParams(twofa) + "&pairing=" + pairingId + "&type=" + type
                + "&amount=" + amount + "&rate=" + rate, Order.class);
        return order.order_id;
    }

    /**
     * @param pairingId
     *            Value returned in the methods above
     * @param orderId
     *            One or more IDs of orders to cancel
     */
    public void cancelOrder(int pairingId, int... orderId) throws IOException {
        cancelOrder(pairingId, null, orderId);
    }

    /**
     * Cancel orders with Two Factor Authentication.
     * 
     * @param pairingId
     *            Value returned in the methods above
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @param orderId
     *            One or more IDs of orders to cancel
     */
    public void cancelOrder(int pairingId, String twofa, int... orderId) throws IOException {
        if (orderId.length == 0) {
            throw new IllegalArgumentException("Missing orderId");
        }
        String orderIds = String.valueOf(orderId[0]);
        if (orderId.length > 1) {
            for (int i = 1; i < orderId.length; ++i) {
                orderIds += ",";
                orderIds += orderId[i];
            }
        }
        callMethod(CANCEL, createSecurityParams(twofa) + "&pairing=" + pairingId + "&order_id=" + orderIds,
                Order.class);
    }

    /**
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return A map of the available currencies and their balances etc.
     */
    public Map<String, Balance> getBalances(String twofa) throws IOException {
        Balances response = callMethod(BALANCE, createSecurityParams(twofa).toString(), Balances.class);
        return response.balance;
    }

    /**
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return A map of the available currencies and their balances etc.
     */
    public Order[] getOrders(String twofa) throws IOException {
        return getOrders(null, null, twofa);
    }

    /**
     * Get orders filtered by pairingId and/or type.
     * 
     * @param pairingId
     *            Optional pairing-id
     * @param type
     *            Optional order type: BUY or SELL
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return A map of the available currencies and their balances etc.
     */
    public Order[] getOrders(Integer pairingId, OrderType type, String twofa) throws IOException {
        StringBuilder parameters = createSecurityParams(twofa);
        if (pairingId != null) {
            parameters.append("&pairing=").append(pairingId);
        }
        if (type != null) {
            parameters.append("&type=").append(type);
        }

        Orders orders = callMethod(GET_ORDERS, parameters.toString(), Orders.class);
        return orders.orders;
    }

    /**
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return a list of all transactions including trades, fees, deposits and withdrawals.
     */
    public Transaction[] getTransactionHistory(String twofa) throws IOException {
        return getTransactionHistory(null, null, null, null, twofa);
    }

    /**
     * @param currency
     * @param type
     * @param startDate
     * @param endDate
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return an array of all transactions including trades, fees, deposits and withdrawals.
     */
    public Transaction[] getTransactionHistory(String currency, TransactionType type, Date startDate, Date endDate,
            String twofa) throws IOException {
        StringBuilder parameters = createSecurityParams(twofa);
        if (currency != null) {
            parameters.append("&currency=").append(currency);
        }
        if (type != null) {
            parameters.append("&type=").append(type);
        }
        if (startDate != null) {
            parameters.append("&start_date=").append(DATE_FORMATTER.format(startDate));
        }
        if (endDate != null) {
            parameters.append("&end_date=").append(DATE_FORMATTER.format(endDate));
        }
        TransactionHistory response = callMethod(TRANSACTION_HISTORY, parameters.toString(), TransactionHistory.class);
        return response.transactions;
    }

    /**
     * @param currency
     *            The currency to deposit, for instance THB or BTC
     * @param generateNew
     *            Set to true if you want a brand new address
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return the current deposit address of the currency or a new one
     */
    public String getDepositAddress(String currency, boolean generateNew, String twofa) throws IOException {
        StringBuilder parameters = createSecurityParams(twofa);
        parameters.append("&currency=").append(currency);
        if (generateNew) {
            parameters.append("&new=true");
        }
        DepositAddress response = callMethod(DEPOSIT, parameters.toString(), DepositAddress.class);
        return response.address;
    }

    /**
     * Successful withdrawal means that the withdrawal was successfully added to queue for processing. It does not mean
     * the transaction has been broadcast to the blockchain yet
     * 
     * @param currency
     *            e.g. THB or BTC
     * @param amount
     *            Major units in decimal format
     * @param address
     *            Only used for crypto withdrawals
     * @param bankId
     *            Only used for THB withdrawals
     * @param twofa
     *            Optional 2 Factor Authentication
     * @return the id of the withdrawal
     */
    public int requestWithdrawal(String currency, BigDecimal amount, String address, String bankId, String twofa)
            throws IOException {
        StringBuilder parameters = createSecurityParams(twofa);
        parameters.append("&currency=").append(currency);
        parameters.append("&amount=").append(amount);
        if (address != null) {
            parameters.append("&address=").append(address);
        }
        if (bankId != null) {
            parameters.append("&bank_id=").append(bankId);
        }
        Withdrawal response = callMethod(WITHDRAWAL, parameters.toString(), Withdrawal.class);
        return response.withdrawal_id;
    }

    /**
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return A map of the available currencies and their balances etc.
     */
    public Withdrawal[] getWithdrawalHistory(String twofa) throws IOException {
        WithdrawalHistory history = callMethod(WITHDRAWAL_HISTORY, createSecurityParams(twofa).toString(),
                WithdrawalHistory.class);
        return history.withdrawals;
    }

    /**
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return payment group types
     */
    public BillPaymentGroup[] getBillPaymentGroupTypes(String twofa) throws IOException {
        BillPaymentGroups response = callMethod(BILLGROUP, createSecurityParams(twofa).toString(),
                BillPaymentGroups.class);
        return response.groups;
    }

    /**
     * @param groupId
     *            ID of payment group type
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return payment service providers, for instance phone carriers
     */
    public BillPaymentServiceProvider[] getBillPaymentServiceProviders(int groupId, String twofa) throws IOException {
        BillPaymentServiceProviders response = callMethod(BILLER, createSecurityParams(twofa) + "&group_id="
                + groupId, BillPaymentServiceProviders.class);
        return response.providers;
    }

    /**
     * Bill payment is similar to a withdrawal, but the money you withdraw is used for the payment.
     * 
     * @param biller
     *            Bill payment service privider id
     * @param amount
     *            Amount to pay
     * @param account
     *            Phone number or account number for payment
     * @param twofa
     *            Optional 2 Factor Authentication value
     * @return withdrawal-id
     */
    public int createBillPayment(int biller, BigDecimal amount, String account, String twofa)
            throws IOException {
        WithdrawalId response = callMethod(BILLPAY, createSecurityParams(twofa) + "&biller=" + biller + "&amount="
                + amount + "&account=" + account, WithdrawalId.class);
        return response.withdrawal_id;
    }

    @Override
    protected void setConnectionProperties(HttpURLConnection con) {
        // BX requires User-Agent
        con.setRequestProperty("User-Agent", "BX Java API");
    }

    /*
     * Turn various errors into exceptions.
     */
    @Override
    protected void checkErrorResponse(HttpURLConnection con, JsonObject response) throws HttpResponseException,
            IOException {
        int responseCode = con.getResponseCode();
        JsonElement errorElement = response.get("error");
        if (errorElement != null && !errorElement.isJsonNull()) {
            String error = errorElement.getAsString();
            if (error.length() > 0) {
                throw new BxApiException(responseCode, error);
            }
        }
        if (responseCode >= 300) {
            throw new HttpResponseException(responseCode, con.getResponseMessage());
        }
    }

    /**
     * Build common security parameters such as nonce and signature.
     */
    private StringBuilder createSecurityParams(String twofa) {
        if (apiKey == null || apiSecret == null) {
            throw new IllegalStateException("Missing api key/secret");
        }
        long nonce = nextNonce();
        String signature = sha256(apiKey + nonce + apiSecret);
        StringBuilder parameters = new StringBuilder();
        parameters.append("key=").append(apiKey);
        parameters.append("&nonce=").append(nonce);
        parameters.append("&signature=").append(signature);
        if (twofa != null) {
            parameters.append("&twofa=").append(twofa);
        }
        return parameters;
    }

    private long nextNonce() {
        return ++nonceCounter;
    }

    private static String sha256(String data) {
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA-256");
            hasher.update(data.getBytes());
            return toHex(hasher.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 missing"); // Can't happen
        }
    }

    private static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; ++i) {
            sb.append(Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    /*
     * Helper classes for parsing some responses
     */

    private static class MarketData extends HashMap<Integer, Ticker> {
    }

    private static class Pairings extends HashMap<Integer, Pairing> {
    }

    private class Balances {
        public HashMap<String, Balance> balance; // Map with currency code as keys
    }

    private class HistoricalTradeData {
        public TradeData data;
    }

    private static class Orders {
        public Order[] orders;
    }

    private static class TransactionHistory {
        public Transaction[] transactions;
    }

    private static class DepositAddress {
        public String address;
    }

    private static class WithdrawalId {
        public int withdrawal_id;
    }

    public class WithdrawalHistory {
        public Withdrawal[] withdrawals;
    }

    private static class BillPaymentGroups {
        public BillPaymentGroup[] groups;
    }

    private static class BillPaymentServiceProviders {
        public BillPaymentServiceProvider[] providers;
    }
}