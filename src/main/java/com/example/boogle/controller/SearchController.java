package com.example.boogle.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

// 新增：匯入 Gemini 的協調器
import com.example.boogle.gemini.GeminiQueryOrchestrator;
import com.example.boogle.model.SearchItem;
import com.example.boogle.service.GoogleCseService;

@Controller // 給 Spring 掃描用的註解
public class SearchController {

    private final GoogleCseService cseService;

    // 新增：Gemini 管線協調器，負責「翻譯 + 摘要」兩件事
    private final GeminiQueryOrchestrator geminiQueryOrchestrator;

    // 透過建構子注入兩個 service
    public SearchController(GoogleCseService cseService,
            GeminiQueryOrchestrator geminiQueryOrchestrator) {
        this.cseService = cseService;
        this.geminiQueryOrchestrator = geminiQueryOrchestrator;
    }

    @GetMapping("/") // 進入點，轉到前端的 index.html
    public String home() {
        // forward 到 src/main/resources/static/index.html
        return "forward:/index.html";
    }

    // JSON API，給前端 app.js 用 fetch 呼叫
    @GetMapping("/api/search")
    @ResponseBody
    public List<SearchItem> api(@RequestParam("q") String q,
            @RequestParam(value = "num", defaultValue = "10") int num) {
        // 1. 先把使用者輸入丟給 Gemini 管線：
        // - Translation：翻譯成英文
        // - Summarization：壓縮成適合 Google CSE 的關鍵字
        // - 過程中會把每一步印在 console（VS Code terminal 看得到）
        String finalQuery = geminiQueryOrchestrator.buildFinalSearchQuery(q);

        // 2. 再用 Gemini 產生出來的「最後關鍵字」去 Google CSE 搜尋
        return cseService.search(finalQuery, num);
    }
}
