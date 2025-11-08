package com.example.javaeloadasbeadando;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class Controllers {

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
}
