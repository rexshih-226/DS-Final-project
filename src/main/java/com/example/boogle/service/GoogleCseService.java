package com.example.boogle.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.boogle.model.SearchItem;

@Service
public class GoogleCseService {

    @Value("${google.cse.enabled:false}")
    private boolean enabled;

    @Value("${google.cse.apiKey:}")
    private String apiKey;

    @Value("${google.cse.cx:}")
    private String cx;

    private final RestTemplate restTemplate = new RestTemplate();
    private final TranslationService translationService;//new

    //new
    public GoogleCseService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public List<SearchItem> search(String query, int num) {
        List<SearchItem> results = new ArrayList<>();
        if (!enabled)
            return results;

        try {
            //new 翻譯成英文
            String translatedQuery = translationService.translateToEnglish(query);
            String encodedQ = URLEncoder.encode(translatedQuery, StandardCharsets.UTF_8);//change

            int remaining = num; // 例如 num=20
            int start = 1; // Google 的第一頁從 1 開始
            int pageSize = 10; // Google API 的硬限制

            Sorttoten sorttoten = new Sorttoten(query);

            while (remaining > 0) {
                int fetchCount = Math.min(pageSize, remaining);

                String url = "https://www.googleapis.com/customsearch/v1"
                        + "?key=" + apiKey
                        + "&cx=" + cx
                        + "&num=" + fetchCount
                        + "&start=" + start
                        + "&q=" + encodedQ;

                ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
                Map<String, Object> body = resp.getBody();
                if (body == null)
                    break;

                Object itemsObj = body.get("items");
                if (itemsObj instanceof List) {
                    for (Object itemObj : (List<?>) itemsObj) {
                        if (itemObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> item = (Map<String, Object>) itemObj;

                            String title = item.get("title") != null ? item.get("title").toString() : "";
                            String link = item.get("link") != null ? item.get("link").toString() : "";
                            String snippet = item.get("snippet") != null ? item.get("snippet").toString() : "";

                            if (!title.isEmpty() && !link.isEmpty() && !snippet.isEmpty()) {
                                SearchItem sitem = new SearchItem(title, link, snippet);
                                // sitem.setScore(keywordService.calculateScore(link));
                                sitem.setScore(sorttoten.calculateScore(title,snippet));
                                results.add(sitem);
                            }
                        }
                    }
                }

                remaining -= fetchCount;
                start += fetchCount; // 下一頁（例如從 1 → 11）
            }

        } catch (Exception e) {
            System.err.println("CSE error: " + e.getMessage());
        }

        // 依照分數排序（由高到低）
        results.sort(Comparator.comparing(SearchItem::getScore).reversed());

        KeywordSearchingService keywordService = new KeywordSearchingService(query);
        for (int i = 0; i <= 10; i++) {
            results.get(i).setScore(keywordService.calculateScore(results.get(i).getLink()));
        }

        results.sort(Comparator.comparing(SearchItem::getScore).reversed());

        // 設定 rank 並印在 console
        int rank = 1;
        for (SearchItem item : results) {
            System.out.printf("[Result] rank=%d, score=%.4f, title=%s, link=%s%n",
                    rank, item.getScore(), item.getTitle(), item.getLink());
            rank++;
        }

        return results;

    }

}
