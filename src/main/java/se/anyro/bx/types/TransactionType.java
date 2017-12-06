package se.anyro.bx.types;

public enum TransactionType {
    TRADE, FEE, DEPOSIT, WITHDRAWAL;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}