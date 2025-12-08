package com.example.boogle.model;

import java.util.Comparator;

public class SearchItem implements Comparator<SearchItem> {
    private String title;
    private String link;
    private String snippet;
    private double score;

    public SearchItem() {
    }

    public SearchItem(String title, String link, String snippet) {
        this.title = title;
        this.link = link;
        this.snippet = snippet;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    @Override
    public int compare(SearchItem o1, SearchItem o2) {
        if(o1.getScore() == o2.getScore()) return 0;

        if(o1.getScore() > o2.getScore()) return 1;

        if(o1.getScore() < o2.getScore()) return -1;

        return 0;
    }

    // @Override
    // public int compareTo(SearchItem other) {
    //     return Double.compare(other.getScore(), this.getScore());
    // }

    
}
