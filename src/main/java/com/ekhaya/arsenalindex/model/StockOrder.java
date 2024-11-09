package com.ekhaya.arsenalindex.model;

public class StockOrder {
    private final String symbol;
    private final int quantity;
    private final String side;
    private final String type;
    private final String timeInForce;

    public StockOrder(String symbol) {
        this.symbol = symbol;
        this.quantity = 1;
        this.side = "buy";
        this.type = "market";
        this.timeInForce = "day";
    }

    public String toJson() {
        return String.format("""
                {
                    "symbol": "%s",
                    "qty": %d,
                    "side": "%s",
                    "type": "%s",
                    "time_in_force": "%s"
                }
                """, symbol, quantity, side, type, timeInForce);
    }
} 