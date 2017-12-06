BX Java API
===========

This Java library implements all the methods and types in the [API of bx.in.th](https://bx.in.th/info/api/) except the Options API. [BX](https://bx.in.th) is a Thai cryptocurrency exchange.

If you find a bug or know some way to improve the library please report it to me as [an issue](https://github.com/nadam/bx-java-api/issues) or [in private](https://telegram.me/nadam).

Features
--------
- Full implementation of the Public API, Private API and Private Bill Payment API
- Javadoc
- Errors automatically turned into exceptions

Example usage
-------------

Public access:

```java
BxApi bx = new BxApi();
Ticker[] tickers = bx.getMarketData();
for (Ticker ticker : tickers) {
    // ...
}
```

Using the private API
---------------------

To access the private API you need an account at BX and you need to generate an API key at in your [account area](https://bx.in.th/account/). Make sure you create a key that only has access to the functionality you need and enable 2FA if appropriate.

```java
BxApi bx = new BxApi(YOUR_API_KEY, YOUR_API_SECRET);
Balances balances = bx.getBalances(null);
Balance btcBalance = balances.balance.get("BTC");
```

Exception handling
------------------

All errors will result in exceptions being thrown by the methods. You can simply catch IOException or add additional handling for BxApiException and HttpResponseException.

```java
try {
    Pairing[] pairings = bx.getCurrencyPairings();
    for (Pairing pairing : pairings) {
        // Do something with each pairing
    }
} catch (BxApiException e) {
    // Error from BX. Check e.getMessage().
} catch (HttpResponseException e) {
    // Unexpected response code from BX. Check e.getResponseCode() and e.getMessage().
} catch (IOException e) {
    // Communication error, for instance connection error or timeout
}
```

Including in your project
-------------------------
#### Dependencies
The project depends on [GSON](https://github.com/google/gson)  2.8.2.

#### Binary
No binaries published yet.

#### Building from source code
A pom.xml for Maven is included in the project. For other options just make sure you include GSON 2.8.2 or later.

License
----------------
[Apache License 2.0](LICENSE)