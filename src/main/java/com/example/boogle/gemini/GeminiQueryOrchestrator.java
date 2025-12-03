package com.example.boogle.gemini;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 負責把「翻譯」與「摘要」兩個 service 串起來，
 * 並提供一個對外方法，回傳最後要給 Google CSE 的搜尋關鍵字。
 *
 * 同時會用 logger 把中間結果（原始輸入、翻譯、摘要）印到 console，
 * 在 VS Code 的 terminal 就能直接看到。
 */
@Service
public class GeminiQueryOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(GeminiQueryOrchestrator.class);

    private final GeminiTranslationService translationService;
    private final GeminiSummarizationService summarizationService;

    public GeminiQueryOrchestrator(GeminiTranslationService translationService,
            GeminiSummarizationService summarizationService) {
        this.translationService = translationService;
        this.summarizationService = summarizationService;
    }

    /**
     * 將使用者原始輸入依序丟給：
     * 1. GeminiTranslationService → 變成英文
     * 2. GeminiSummarizationService → 壓縮成關鍵字
     *
     * 並在 console 印出每一步的結果，最後回傳「給 Google CSE 用的關鍵字」。
     */
    public String buildFinalSearchQuery(String userInput) {
        // 第一步：翻譯成英文
        String englishQuery = translationService.translateToEnglish(userInput);

        // 第二步：壓縮成關鍵字
        String finalQuery = summarizationService.summarizeForSearch(englishQuery);

        // 在 console（VS Code terminal）輸出完整 trace，方便 debug / demo
        log.info("Gemini pipeline - original input: {}", userInput);
        log.info("Gemini pipeline - translated to English: {}", englishQuery);
        log.info("Gemini pipeline - summarized for Google CSE: {}", finalQuery);

        return finalQuery;
    }
}
