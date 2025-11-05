package com.example.boogle.model;

public class SearchItem {
    private String title;
    private String link;

    public SearchItem() {
    }

    public SearchItem(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }
}
