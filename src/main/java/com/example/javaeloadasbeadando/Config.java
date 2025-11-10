package com.example.javaeloadasbeadando;

import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountID;

public class Config {

    private Config() {}

    public static final String URL = "https://api-fxpractice.oanda.com";
    public static final String TOKEN = "345e2224e2678e6dbed8c2cf789c031d-d6859a32b66dc765643e874ec5f929e9";
    public static final AccountID ACCOUNTID = new AccountID("101-004-37634228-001");

    // Központi Context példány
    private static Context ctx;

    public static Context getContext() {
        if (ctx == null) {
            ctx = new ContextBuilder(URL)
                    .setToken(TOKEN)
                    .setApplication("JavaEloadasBeadando")
                    .build();
        }
        return ctx;
    }
}
