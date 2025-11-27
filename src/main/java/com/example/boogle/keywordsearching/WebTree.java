package com.example.boogle.keywordsearching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebTree {
	public WebNode root;
	private static final double CHILD_WEIGHT = 0.6; //子頁面分數權重
	private static final int MAX_DEPTH = 2; //最大遞迴深度

	public WebTree(WebPage rootPage){
		this.root = new WebNode(rootPage);
	}

	public void setPostOrderScore(ArrayList<Keyword> keywords) throws IOException{
		setPostOrderScore(root, keywords);
	}

	private void setPostOrderScore(WebNode startNode, ArrayList<Keyword> keywords) throws IOException{
		if(startNode.children.size() > 0){
			for(WebNode child : startNode.children){
				setPostOrderScore(child, keywords);
			}
		}
		startNode.setNodeScore(keywords);
	}

	public void eularPrintTree(){
		eularPrintTree(root);
	}

	private void eularPrintTree(WebNode startNode){
		String space = repeat("\t", startNode.getDepth() - 1);
		if(startNode.children.size() == 0) {
			System.out.println(space + "(" + startNode.webPage.url + "," + startNode.nodeScore + ")");
		} else {
			System.out.println(space + "(" + startNode.webPage.url + "," + startNode.nodeScore);
			for(WebNode child : startNode.children){
				eularPrintTree(child);
			}
			System.out.println(space + ")");
		}
	}

	private String repeat(String str,int repeat){
		StringBuilder retVal  = new StringBuilder();
		for(int i=0;i<repeat;i++){
			retVal.append(str);
		}
		return retVal.toString();
	}

	//新增：自動建立子節點，遞迴探索 up to depth
	public void autoBuild(WebNode node, int currentDepth) throws IOException {
		if (currentDepth >= MAX_DEPTH) return; // 超過深度不再抓
		String html = node.webPage.counter.getContent();
		if (html == null || html.isEmpty()) return; // 抓不到就略過

		

		List<String> links = WordCounter.extractLinks(html);

		for (String link : links) {
			WebPage subPage = new WebPage(link, link);
			WebNode childNode = new WebNode(subPage);
			node.addChild(childNode);
			System.out.println("新增子節點：" + link);
			autoBuild(childNode, currentDepth + 1); //遞迴
		}
	}

	//新增：啟動自動建樹（從 root 開始）
	public void buildAutomatically() throws IOException {
		root.children.get(0).webPage.counter.getContent(); //預先抓取 Publications 內容
		root.children.get(0).children.get(0).webPage.counter.getContent(); //預先抓取 Springer 內容
		root.children.get(1).webPage.counter.getContent(); //預先抓取 Projects 內容
		root.children.get(2).webPage.counter.getContent(); //預先抓取 Members 內容
		root.children.get(3).webPage.counter.getContent(); //預先抓取 Course 內容
		for(WebNode child : root.children){
			autoBuild(child, 1);
		}

		autoBuild(root, 0);

	}
}

