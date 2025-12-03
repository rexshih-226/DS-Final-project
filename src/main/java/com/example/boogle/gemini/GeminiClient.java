package com.example.boogle.gemini;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 負責跟 Google Gemini API 溝通的底層 client。
 * 其他 service（翻譯、摘要）只需要給 modelId + prompt，就能拿到文字結果。
 */
@Service
public class GeminiClient {

    // 在 application.properties 設定的 API key
    @Value("${gemini.apiKey}")
    private String apiKey;

    // 例如：https://generativelanguage.googleapis.com/v1beta/models
    @Value("${gemini.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 呼叫 Gemini 的 generateContent，回傳第一個候選的純文字內容。
     *
     * @param modelId 要使用的模型名稱，例如 "gemini-2.5-flash"
     * @param prompt  傳給 Gemini 的文字提示
     * @return Gemini 回傳的文字（已 trim），如果失敗則回傳 null
     */
    public String generateText(String modelId, String prompt) {
        try {
            // 組 URL：{baseUrl}/{modelId}:generateContent?key={API_KEY}
            String url = String.format("%s/%s:generateContent?key=%s", baseUrl, modelId,
                    apiKey);

            // 建立 request body，對應官方文件的格式：
            // {
            // "contents": [
            // {
            // "parts": [ { "text": "..." } ]
            // }
            // ]
            // }
            Map<String, Object> body = new HashMap<>();

            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);

            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(part);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", parts);
            // role 可以不設定，預設即可

            List<Map<String, Object>> contents = new ArrayList<>();
            contents.add(content);

            body.put("contents", contents);

            // 設定 HTTP 標頭
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // 呼叫 Gemini API
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                // 非 2xx 直接視為失敗
                return null;
            }

            // 解析回應的 JSON 結構：
            // candidates[0].content.parts[].text
            return extractTextFromResponse(response.getBody());

        } catch (Exception e) {
            // 真實專案會用 logger，這裡先簡單印出錯誤
            System.err.println("Gemini API error: " + e.getMessage());
            return null;
        }
    }

    /**
     * 從 Gemini 的回應 Map 裡，把主要文字內容抽出來。
     * 目前策略：取第一個 candidate，將其所有 parts.text 串起來。
     */
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> respBody) {
        Object candidatesObj = respBody.get("candidates");
        if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) {
            return null;
        }

        Object firstCandidate = candidates.get(0);
        if (!(firstCandidate instanceof Map<?, ?> candidateMap)) {
            return null;
        }

        Object contentObj = candidateMap.get("content");
        if (!(contentObj instanceof Map<?, ?> contentMapRaw)) {
            return null;
        }

        Map<String, Object> contentMap = (Map<String, Object>) contentMapRaw;
        Object partsObj = contentMap.get("parts");
        if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Object p : parts) {
            if (p instanceof Map<?, ?> partMapRaw) {
                Map<String, Object> partMap = (Map<String, Object>) partMapRaw;
                Object text = partMap.get("text");
                if (text != null) {
                    sb.append(text.toString());
                }
            }
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }
}
