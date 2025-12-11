package com.example.boogle.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.boogle.model.SearchItem;
import com.example.boogle.service.GoogleCseService;

@Controller // 給spring讀的註解，表示這邊負責處理HTTP請求
public class SearchController {

    private final GoogleCseService cseService;

    public SearchController(GoogleCseService cseService) {
        this.cseService = cseService;
    }

    @GetMapping("/") // 給spring讀的註解，發出HTTP GET時會被觸發
    public String home() {
        return "forward:/index.html";
    }

    // 若你想前端用 fetch，可提供 JSON API
    @GetMapping("/api/search")
    @ResponseBody
    public List<SearchItem> api(@RequestParam("q") String q,
            @RequestParam(value = "num", defaultValue = "5") int num) {
        return cseService.search(q, num);
    }
}
