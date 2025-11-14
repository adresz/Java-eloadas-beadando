package com.example.javaeloadasbeadando;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Controller
public class Controllers {

    private final OandaService oandaService;

    public Controllers() {
        this.oandaService = new OandaService("345e2224e2678e6dbed8c2cf789c031d-d6859a32b66dc765643e874ec5f929e9");
    }

    @GetMapping("/")
    public String MainPage() {
        return "index";
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

    @Controller
    public class FnyitController {

        @GetMapping("/fnyit")
        public String fnyit(Model model) {
            model.addAttribute("param", new MessageFnyit());
            model.addAttribute("result", null);
            return "fnyit";
        }

        @PostMapping("/fnyit")
        public String fnyitSubmit(@ModelAttribute MessageFnyit messageFnyit, Model model) {
            String result;
            try {
                Context ctx = new Context(Config.URL, Config.TOKEN);
                InstrumentName instrument = new InstrumentName(messageFnyit.getInstrument());

                MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
                marketOrderRequest.setInstrument(instrument);
                marketOrderRequest.setUnits(messageFnyit.getUnits());

                OrderCreateRequest request = new OrderCreateRequest(Config.ACCOUNTID);
                request.setOrder(marketOrderRequest);

                OrderCreateResponse response = ctx.order.create(request);

                result = "Sikeres pozíciónyitás!<br>Instrumentum: "
                        + messageFnyit.getInstrument()
                        + "<br>Mennyiség: " + messageFnyit.getUnits()
                        + "<br>Trade ID: " + response.getOrderFillTransaction().getId();
            } catch (Exception e) {
                result = "Hiba történt: " + e.getMessage();
            }

            model.addAttribute("param", messageFnyit);
            model.addAttribute("result", result);
            return "fnyit";
        }
    }
    @GetMapping("/fzar")
    public String closePositionForm(Model model) {
        try {
            Context ctx = new Context(Config.URL, Config.TOKEN);

            // Nyitott trade-ek lekérése
            var tradesResponse = ctx.trade.listOpen(Config.ACCOUNTID);
            var trades = tradesResponse.getTrades();

            model.addAttribute("trades", trades);
            model.addAttribute("param", new MessageFzar());

        } catch (Exception e) {
            model.addAttribute("error", "Hiba a pozíciók lekérésekor: " + e.getMessage());
        }

        return "fzar";
    }

    @PostMapping("/fzar")
    public String closePositionSubmit(@ModelAttribute("param") MessageFzar form, Model model) {

        try {
            Context ctx = new Context(Config.URL, Config.TOKEN);

            String tradeId = String.valueOf(form.getTradeId());

            ctx.trade.close(new com.oanda.v20.trade.TradeCloseRequest(
                    Config.ACCOUNTID,
                    new com.oanda.v20.trade.TradeSpecifier(tradeId)
            ));

            model.addAttribute("msg", "✔ Pozíció sikeresen lezárva! ID = " + tradeId);

        } catch (Exception e) {
            model.addAttribute("msg", "❌ Zárási hiba: ");
        }

        // Újratöltjük a nyitott tradeket, hogy friss legyen
        try {
            Context ctx = new Context(Config.URL, Config.TOKEN);
            var trades = ctx.trade.listOpen(Config.ACCOUNTID).getTrades();
            model.addAttribute("trades", trades);
        } catch (Exception ignored) {}

        return "fzar";
    }
    @Controller
    public class ForexPozController {

        @GetMapping("/fpoz")
        public String listPositions(Model model) {
            try {
                Context ctx = new Context(Config.URL, Config.TOKEN);

                var positions = ctx.position.listOpen(Config.ACCOUNTID).getPositions();

                model.addAttribute("positions", positions);

            } catch (Exception e) {
                model.addAttribute("error", "Hiba a pozíciók lekérésekor: " + e.getMessage());
            }

            return "fpoz";
        }
    }

    @Controller
    public class ForexHistArController {

        @GetMapping("/forex-histar")
        public String histForm(Model model,
                               @RequestParam(defaultValue = "EUR_USD") String instrument,
                               @RequestParam(defaultValue = "D") String granularity) {

            model.addAttribute("selectedInstrument", instrument);
            model.addAttribute("selectedGranularity", granularity);

            // instrument lista
            model.addAttribute("instruments", List.of(
                    "EUR_USD", "GBP_USD", "USD_JPY", "AUD_USD", "USD_CAD", "USD_CHF"
            ));

            // granularity lista
            Map<String, String> gran = new LinkedHashMap<>();
            gran.put("S5", "5 mp");
            gran.put("S10", "10 mp");
            gran.put("M1", "1 perc");
            gran.put("M5", "5 perc");
            gran.put("M15", "15 perc");
            gran.put("H1", "1 óra");
            gran.put("D", "Nap");
            gran.put("W", "Hét");

            model.addAttribute("gran", gran);

            // historikus adatok lekérése
            try {
                Context ctx = new Context(Config.URL, Config.TOKEN);

                com.oanda.v20.instrument.InstrumentCandlesRequest req =
                        new com.oanda.v20.instrument.InstrumentCandlesRequest(
                                new InstrumentName(instrument)
                        );

                req.setCount(10L);
                req.setGranularity(CandlestickGranularity.valueOf(granularity));

                var candles = ctx.instrument.candles(req).getCandles();

                model.addAttribute("candles", candles);
            } catch (Exception e) {
                model.addAttribute("error", "Hiba a historikus adatok lekérésekor: " + e.getMessage());
            }


            return "forex-histar";
        }
    }
}
