package com.example.boogle.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.boogle.service.GoogleCseService;
import com.example.boogle.model.SearchItem;

@Controller // 給spring讀的註解
public class SearchController {

    private final GoogleCseService cseService;

    public SearchController(GoogleCseService cseService) {
        this.cseService = cseService;
    }

    @GetMapping("/") // 一樣是給spring讀的註解，看網址最後是甚麼
    public String home() {
        return "forward:/index.html";
    } // 不要加副檔名

    // @GetMapping("/search")
    // public String search(@RequestParam("q") String q, Model model) {
    //     List<Map<String, String>> items = cseService.search(q, 10);
    //     model.addAttribute("query", q);
    //     model.addAttribute("items", items);
    //     return "index";
    // }

    // 若你想前端用 fetch，可提供 JSON API
    @GetMapping("/api/search")
    @ResponseBody
    public List<SearchItem> api(@RequestParam("q") String q,
            @RequestParam(value = "num", defaultValue = "10") int num) {
        return cseService.search(q, num);
    }
}
