package com.example.javaeloadasbeadando;

import com.oanda.v20.account.AccountSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        private final OandaService oandaService;

        public AccountController() {
            this.oandaService = new OandaService("345e2224e2678e6dbed8c2cf789c031d-d6859a32b66dc765643e874ec5f929e9");
        }

        @GetMapping("/forex-account")
        public AccountSummary getAccountSummary() {
            String accountId = "101-004-37634228-001";
            return oandaService.getAccountSummary(accountId);
        }
    }


}
