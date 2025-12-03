package com.example.boogle.gemini;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 負責「把任意語言的使用者輸入 → 翻成英文搜尋句子」。
 */
@Service
public class GeminiTranslationService {

    private final GeminiClient geminiClient;

    // 翻譯用的模型 ID，例如 gemini-2.5-flash
    @Value("${gemini.translate-model}")
    private String translateModel;

    public GeminiTranslationService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    /**
     * 將使用者輸入翻成簡短英文，適合拿去做搜尋。
     * 如果 Gemini 呼叫失敗，會 fallback 回原始輸入。
     */
    public String translateToEnglish(String userQuery) {
        // Prompt 設計：告訴模型只做翻譯，不要解釋
        String prompt = """
                You are a translator for web search queries.
                Task: Detect the language of the following user query and translate it into
                natural English suitable for a search engine.
                Rules:
                1) Reply in English only.
                2) Do not add explanations or quotes.
                3) Keep the translation concise (no more than 15 words).

                User query:
                """ + userQuery;

        String result = geminiClient.generateText(translateModel, prompt);

        // 若 API 回傳空白或失敗，就直接用原始使用者輸入
        if (result == null || result.isBlank()) {
            return userQuery;
        }
        return result.trim();
    }
}
