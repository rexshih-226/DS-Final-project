package com.example.boogle.gemini;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 負責「把英文搜尋句子 → 壓縮成適合 Google CSE 的關鍵字」。
 * 例如： "What is Java?" → "Java"
 */
@Service
public class GeminiSummarizationService {

    private final GeminiClient geminiClient;

    // 摘要用的模型 ID，可以和翻譯用同一個或不同
    @Value("${gemini.summarize-model}")
    private String summarizeModel;

    public GeminiSummarizationService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    /**
     * 將英文搜尋句子壓縮成非常短的關鍵字 / 詞組。
     * 如果 API 失敗，會 fallback 回原始英文句子。
     */
    public String summarizeForSearch(String englishQuery) {
        // Prompt 設計：要求模型只回核心關鍵字
        String prompt = """
                You are a search keyword extractor.
                Task: Given the following English search query, extract the core topic as a
                very short phrase
                that is suitable as a search keyword.

                Rules:
                1) Output only the final keyword or short phrase.
                2) Do not output any explanation, punctuation, or extra words.
                3) Keep it under 5 words.
                4) Use English only.
                5) This is for web search engines, which is focus on java programming language.
                6) Make sure the keyword is as concise as possible.

                Search query:
                """ + englishQuery;

        String result = geminiClient.generateText(summarizeModel, prompt);

        if (result == null || result.isBlank()) {
            return englishQuery;
        }
        return result.trim();
    }
}
