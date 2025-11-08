package com.example.javaeloadasbeadando;

import soapclient.MNBArfolyamServiceSoap;
import soapclient.MNBArfolyamServiceSoapImpl;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class BankService {

    public static List<ExchangeRateData> fetchExchangeRates(String currency, String startDate, String endDate) throws Exception {

        MNBArfolyamServiceSoapImpl impl = new MNBArfolyamServiceSoapImpl();
        MNBArfolyamServiceSoap service = impl.getCustomBindingMNBArfolyamServiceSoap();

        String xmlResponse = service.getExchangeRates(startDate, endDate, currency);

        List<ExchangeRateData> rates = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

        NodeList days = doc.getElementsByTagName("Day");

        for (int i = 0; i < days.getLength(); i++) {
            Element day = (Element) days.item(i);
            String date = day.getAttribute("date");

            NodeList rateNodes = day.getElementsByTagName("Rate");
            for (int j = 0; j < rateNodes.getLength(); j++) {
                Element rateElement = (Element) rateNodes.item(j);
                String rateValue = rateElement.getTextContent().trim();
                String curr = rateElement.getAttribute("curr");

                rates.add(new ExchangeRateData(date, curr, rateValue));
            }
        }

        return rates;
    }
}
