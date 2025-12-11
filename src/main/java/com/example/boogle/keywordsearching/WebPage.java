package com.example.boogle.keywordsearching;

import java.io.IOException;
import java.util.ArrayList;

import com.example.boogle.model.Keyword;

public class WebPage {
	public String url;
	public String name;
	public WordCounter counter;
	public double score;

	
	public WebPage(String url,String name){
		this.url = url;
		this.name = name;
		this.counter = new WordCounter(url);	
	}
	
	public void setScore(ArrayList<Keyword> keywords) throws IOException{
		// YOUR TURN
		// 1. calculate the score of this webPage
		this.score = 0.0;
		for(int i=0;i<keywords.size();i++) {
			try{
				int number = counter.countKeyword(keywords.get(i).name);
				this.score += number * keywords.get(i).weight;
			}catch(IOException e){}
		}

	}
	
}