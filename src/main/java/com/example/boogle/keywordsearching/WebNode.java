package com.example.boogle.keywordsearching;

import java.io.IOException;
import java.util.ArrayList;

public class WebNode {
	public WebNode parent;
	public ArrayList<WebNode> children;
	public WebPage webPage;
	public double nodeScore;
	private static final double CHILD_WEIGHT = 0.6; //子節點分數權重

	public WebNode(WebPage webPage){
		this.webPage = webPage;
		this.children = new ArrayList<>();
	}

	public void setNodeScore(ArrayList<Keyword> keywords) throws IOException{
		webPage.setScore(keywords);
		this.nodeScore = webPage.score;

		if(children.size() == 0) return;

		for(WebNode child : children){
			this.nodeScore += child.nodeScore * CHILD_WEIGHT; //子節點分數乘上 0.6
		}
	}

	public void addChild(WebNode child){
		this.children.add(child);
		child.parent = this;
	}

	public boolean isTheLastChild(){
		if(this.parent == null) return true;
		ArrayList<WebNode> siblings = this.parent.children;
		return this.equals(siblings.get(siblings.size() - 1));
	}

	public int getDepth(){
		int depth = 1;
		WebNode curr = this;
		while(curr.parent != null){
			depth++;
			curr = curr.parent;
		}
		return depth;
	}
}