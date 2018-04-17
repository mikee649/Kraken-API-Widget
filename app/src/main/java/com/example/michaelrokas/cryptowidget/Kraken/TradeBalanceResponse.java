package com.example.michaelrokas.cryptowidget.Kraken;

/**
 * Created by michaelrokas on 2018-04-17.
 */

public class TradeBalanceResponse {
    private Result result;
    private String[] error;

    public Result getResult(){return result;}
    public String[] getError(){return error;}

    public class Result {
        private String eb;      // equivalent balance (combined balance of all currencies)
        private String tb;      // trade balance (combined balance of all equity currencies)
        private String m;       // margin amount of open positions
        private String n;       // unrealized net profit/loss of open positions
        private String c;       // cost basis of open positions
        private String v;       // current floating valuation of open positions
        private String e;       // equity = trade balance + unrealized net profit/loss
        private String mf;      // free margin = equity - initial margin (maximum margin available to open new positions)
        private String ml;      // margin
        private String level;    // (equity / initial margin) * 100

        public String getEquivalentBalance(){return eb;}
    }

}
