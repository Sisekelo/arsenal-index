package com.ekhaya.arsenalindex.model;

public class StockOrder {
    private final String symbol;
    private final double notional;
    private final String side;
    private final String type;
    private final String timeInForce;

    public StockOrder(String symbol, double notional) {
        this.symbol = symbol;
        this.notional = notional;
        this.side = "buy";
        this.type = "market";
        this.timeInForce = "day";
    }

    public String toJson() {
        return String.format("""
                {
                    "symbol": "%s",
                    "notional": %.2f,
                    "side": "%s",
                    "type": "%s",
                    "time_in_force": "%s"
                }
                """, symbol, notional, side, type, timeInForce);
    }
}