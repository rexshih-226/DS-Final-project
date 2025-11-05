package com.example.boogle.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleCseService {

    @Value("${google.cse.enabled:false}")
    private boolean enabled;

    @Value("${google.cse.apiKey:}")
    private String apiKey;

    @Value("${google.cse.cx:}")
    private String cx;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, String>> search(String query, int num) {
        List<Map<String, String>> results = new ArrayList<>();
        if (!enabled)
            return results;

        try {
            String q = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
            String url = "https://www.googleapis.com/customsearch/v1?key=" + apiKey
                    + "&cx=" + cx + "&num=" + num + "&q=" + q;

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = resp.getBody();
            if (body == null)
                return results;

            Object itemsObj = body.get("items");
            if (itemsObj instanceof List) {
                List<?> items = (List<?>) itemsObj;
                for (Object itemObj : items) {
                    if (itemObj instanceof Map) {
                        Map<?, ?> item = (Map<?, ?>) itemObj;
                        Object titleObj = item.get("title");
                        Object linkObj = item.get("link");
                        String title = titleObj == null ? "" : titleObj.toString();
                        String link = linkObj == null ? "" : linkObj.toString();
                        if (!title.isEmpty() && !link.isEmpty()) {
                            Map<String, String> m = new HashMap<>();
                            m.put("title", title);
                            m.put("link", link);
                            results.add(m);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("CSE error: " + e.getMessage());
        }
        return results;
    }
}
