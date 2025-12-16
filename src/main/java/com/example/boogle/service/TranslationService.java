package com.example.boogle.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

@Service
public class TranslationService {

    @Value("${google.translation.apiKey}")
    private String apiKey;

    public String translateToEnglish(String text) {
        if (text == null || text.isEmpty())
            return text;

        try {
            String url = "https://translation.googleapis.com/language/translate/v2"
                    + "?key=" + apiKey
                    + "&q={q}"
                    + "&target=en";

            RestTemplate restTemplate = new RestTemplate();
            Map response = restTemplate.getForObject(url, Map.class, text);

            if (response == null)
                return text;

            Map data = (Map) response.get("data");
            List translations = (List) data.get("translations");
            if (translations == null || translations.isEmpty())
                return text;

            Map first = (Map) translations.get(0);
            String translatedText = (String) first.get("translatedText");

            // ★ 關鍵修正
            translatedText = HtmlUtils.htmlUnescape(translatedText);

            return translatedText;

        } catch (Exception e) {
            e.printStackTrace();
            return text;
        }
    }
}
