package com.example.javaeloadasbeadando;

import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.*;


@Controller
public class Controllers {

    private final OandaService oandaService;

    public Controllers() {
        this.oandaService = new OandaService("345e2224e2678e6dbed8c2cf789c031d-d6859a32b66dc765643e874ec5f929e9");
    }


    @GetMapping("/soap")
    public String soapForm(Model model) {
        model.addAttribute("param", new MessagePrice());
        return "form";
    }

    @PostMapping("/soap")
    public String soapSubmit(@ModelAttribute MessagePrice messagePrice, Model model) throws Exception {

        List<ExchangeRateData> rates = BankService.fetchExchangeRates(
                messagePrice.getCurrency(),
                messagePrice.getStartDate(),
                messagePrice.getEndDate()
        );

        List<String> dates = new ArrayList<>();
        List<String> values = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd", new Locale("hu"));

        for (ExchangeRateData r : rates) {
            LocalDate localDate = LocalDate.parse(r.getDate());
            dates.add(localDate.format(formatter));
            values.add(r.getRate().replace(",", "."));
        }

        model.addAttribute("rates", rates);
        model.addAttribute("currency", messagePrice.getCurrency());
        model.addAttribute("dates", dates);
        model.addAttribute("values", values);

        return "result";
    }

    @RestController
    public class AccountController {

        @GetMapping(value = "/forex-account", produces = "text/html; charset=UTF-8")
        public String getAccountSummary() {
            AccountSummary s;
            try {
                s = Config.getContext().account.summary(Config.ACCOUNTID).getAccount();
            } catch (Exception e) {
                return "<h2 style='text-align: center; color: red;'>❌ Hiba történt az adatok lekérésekor!</h2>";
            }


            return "<html><body style='display: flex; justify-content: center; align-items: center; flex-direction: column; font-family: Arial; margin-top: 30px;'>"
                    + "<h2 style='text-align: center;'>Számlainformációk</h2>"
                    + "<table border='1' style='border-collapse: collapse; width: 60%; text-align: left;'>"
                    + "<tr style='background-color: #f2f2f2;'><th style='padding: 8px;'>Adat</th><th style='padding: 8px;'>Érték</th></tr>"
                    + row("ID", s.getId())
                    + row("Alias", s.getAlias())
                    + row("Currency", s.getCurrency())
                    + row("Balance", s.getBalance())
                    + row("Created By User ID", s.getCreatedByUserID())
                    + row("Created Time", s.getCreatedTime())
                    + row("Guaranteed SL Order Mode", s.getGuaranteedStopLossOrderMode())
                    + row("Profit/Loss (PL)", s.getPl())
                    + row("Resettable PL", s.getResettablePL())
                    + row("Resettable PL Time", s.getResettablePLTime())
                    + row("Financing", s.getFinancing())
                    + row("Commission", s.getCommission())
                    + row("Guaranteed Execution Fees", s.getGuaranteedExecutionFees())
                    + row("Margin Rate", s.getMarginRate())
                    + row("Margin Call Enter Time", s.getMarginCallEnterTime())
                    + row("Margin Call Extension Count", s.getMarginCallExtensionCount())
                    + row("Last Margin Call Extension Time", s.getLastMarginCallExtensionTime())
                    + row("Open Trade Count", s.getOpenTradeCount())
                    + row("Open Position Count", s.getOpenPositionCount())
                    + row("Pending Order Count", s.getPendingOrderCount())
                    + row("Hedging Enabled", s.getHedgingEnabled())
                    + row("Last Order Fill Timestamp", s.getLastOrderFillTimestamp())
                    + row("Unrealized PL", s.getUnrealizedPL())
                    + row("Margin Used", s.getMarginUsed())
                    + row("Margin Available", s.getMarginAvailable())
                    + row("Position Value", s.getPositionValue())
                    + row("Margin Closeout Unrealized PL", s.getMarginCloseoutUnrealizedPL())
                    + row("Margin Closeout NAV", s.getMarginCloseoutNAV())
                    + row("Margin Closeout Margin Used", s.getMarginCloseoutMarginUsed())
                    + row("Margin Closeout Percent", s.getMarginCloseoutPercent())
                    + row("Margin Closeout Position Value", s.getMarginCloseoutPositionValue())
                    + row("Withdrawal Limit", s.getWithdrawalLimit())
                    + row("Margin Call Margin Used", s.getMarginCallMarginUsed())
                    + row("Margin Call Percent", s.getMarginCallPercent())
                    + row("Last Transaction ID", s.getLastTransactionID())
                    + row("NAV", s.getNAV())
                    + "</table></body></html>";
        }

        private String row(String label, Object value) {
            return "<tr><td style='padding: 8px;'><b>" + label + "</b></td><td style='padding: 8px;'>" + (value != null ? value : "") + "</td></tr>";
        }
    }

    @Controller
    public class PricePageController {

        @GetMapping("/forex-aktár")
        public String pricePage(@RequestParam(defaultValue = "EUR_USD") String instrument, Model model) {
            Map<String, Object> priceData = new LinkedHashMap<>();
            priceData.put("error", null);

            try {
                // Config használata
                PricingGetRequest request = new PricingGetRequest(Config.ACCOUNTID, Collections.singletonList(instrument));
                ClientPrice price = Config.getContext().pricing.get(request).getPrices().get(0);

                priceData.put("instrument", price.getInstrument());
                priceData.put("time", price.getTime());
                priceData.put("bid", price.getBids().get(0).getPrice());
                priceData.put("ask", price.getAsks().get(0).getPrice());

            } catch (Exception e) {
                priceData.put("error", "Nem sikerült lekérni az árat: " + e.getMessage());
            }

            model.addAttribute("priceData", priceData);
            model.addAttribute("selectedInstrument", instrument);
            return "price";
        }
    }
}




