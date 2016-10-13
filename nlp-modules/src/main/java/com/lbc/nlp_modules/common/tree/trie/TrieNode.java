package com.lbc.nlp_modules.common.tree.trie;

import java.util.HashMap;
import java.util.Map;


public class TrieNode {
    private String content;  //内容
    private boolean isEnd;    //标志位，isEnd=1表示为串的结尾符，isEnd=0表示不是串的结尾符
    private Map<String, TrieNode> children; //子结点

    public TrieNode(){
    	this(null, false, new HashMap<String, TrieNode>());
    }
    public TrieNode(String content, boolean isEnd, Map<String, TrieNode> children) {
        this.content = content;
        this.isEnd = isEnd;
        this.children = children;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public Map<String, TrieNode> getChildren() {
        return children;
    }

    public void setChildren(Map<String, TrieNode> children) {
        this.children = children;
    }
}
