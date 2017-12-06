package se.anyro.bx.types;

public enum OrderType {

    BUY, SELL;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
