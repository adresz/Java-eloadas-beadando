package com.example.javaeloadasbeadando;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountSummary;

public class OandaService {

    private final Context ctx;

    public OandaService(String token) {
        this.ctx = new Context("https://api-fxpractice.oanda.com", token);
    }

    public AccountSummary getAccountSummary(String accountId) {
        try {
            return ctx.account.summary(new AccountID(accountId)).getAccount();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
