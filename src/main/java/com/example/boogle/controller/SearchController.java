package com.example.boogle.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.boogle.model.SearchItem;
import com.example.boogle.service.GoogleCseService;
import com.example.boogle.service.TranslationService;//new

@Controller // 給spring讀的註解，表示這邊負責處理HTTP請求
public class SearchController {

    private final GoogleCseService cseService;
    private final TranslationService translationService;//new

    public SearchController(GoogleCseService cseService, TranslationService translationService) {//change
        this.cseService = cseService;
        this.translationService = translationService;//new
    }

    @GetMapping("/") // 給spring讀的註解，發出HTTP GET時會被觸發
    public String home() {
        return "forward:/index.html";
    }

    // 若你想前端用 fetch，可提供 JSON API
    @GetMapping("/api/search")
    @ResponseBody
    public List<SearchItem> api(@RequestParam("q") String q,
            @RequestParam(value = "num", defaultValue = "20") int num) {
        
        //new 翻譯成英文
        String translatedQuery = translationService.translateToEnglish(q);
        System.out.println(translatedQuery);

        //new 傳給GoogleCseService
        return cseService.search(translatedQuery + " java coding", num);//change
    }
}
