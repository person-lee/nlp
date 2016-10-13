package com.lbc.nlp_modules.common.tree.trie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class TrieUtil {

    /**
     * 在trie树中，寻找给定content的子串，如果没有就返回false，有就返回true
     * @param root //root结点是没有值的
     */
    public static String findSubString(TrieNode root, String content) {
        if (StringUtils.isEmpty(content) || root == null)
            return null;

        String[] contents = splitWord(content);
        for (int i = 0; i < contents.length; i++) {
            TrieNode trieNode = root.getChildren().get(contents[i]);
            if(trieNode!=null){
                boolean re = find(trieNode, contents, i);
                if (re) {
                    return findMaxLenPrefix(root, content.substring(i));
                }
            }
        }
        return null;
    }
    
    /**
     * 查找content的任意一个前缀是否存在于trie树中，如果存在，返回最长的一个匹配前缀
     * 
     * @param root
     * @param content
     */
    public static String findMaxLenPrefix(TrieNode root, String content) {
    	if (null == root || StringUtils.isEmpty(content)){
    		return null;
    	}
    	
    	String[] words = splitWord(content);
    	int max = findmlp(root.getChildren(), words, 0, -1);
    	if (-1 == max)
    		return null;
    	StringBuffer ret = new StringBuffer();
    	for(int i = 0; i <= max; i++){
    		ret.append(words[i]);
    	}
    	return ret.toString();
    }
    
    /**
     * 判断words起始的词是否存在于trie树中，如果存在，则返回匹配词的长度。会尝试做最大匹配。
     * 如果不存在，返回-1
     * @param root
     */
    public static int findStartWordLen(TrieNode root, String[] words, int startIndex){
    	if (null == root || words.length == 0 || words.length <= startIndex)
    		return -1;
    	int len = -1;
    	for(int i = startIndex; i < words.length; i++){
    		Map<String, TrieNode> children = root.getChildren();
    		if(children!=null && children.containsKey(words[i])){
    			root = children.get(words[i]);
    			if(root.isEnd())
    				len = i - startIndex + 1;
    		}
    		else{
    			break;
    		}
    	}
    	return len;
    }
    
    /**
     * 判断words起始的词是否存在于trie树中，如果存在，则返回所有匹配上的词。
     * 如果不存在，返回空。
     * @see #findStartWordLen(TrieNode, String[], int)
     * @param root
     * @param words
     * @param startIndex
     */
    public static List<String> findAllStartWords(TrieNode root, String[] words, int startIndex){
    	List<String> matchedWords = new ArrayList<String>();
    	if (null == root || words.length == 0 || words.length <= startIndex)
    		return matchedWords;
    	for(int i = startIndex; i < words.length; i++){
    		Map<String, TrieNode> children = root.getChildren();
    		if(children != null && children.containsKey(words[i])){
    			root = children.get(words[i]);
    			if(root.isEnd()){
    				StringBuilder matchedStr = new StringBuilder();
    				for(int j = startIndex; j <= i; j++){
    					matchedStr.append(words[j]);
    				}
    				matchedWords.add(matchedStr.toString());
    			}
    		}
    		else{
    			break;
    		}
    	}
    	return matchedWords;
    }

    private static int findmlp(Map<String, TrieNode> nodeMap, String[] content, int index, int max) {
    	if (index >= content.length || null == nodeMap)
    		return max;
    	
    	String element = content[index];
    	TrieNode node = nodeMap.get(element);
    	if (null == node || !StringUtils.equalsIgnoreCase(node.getContent(), element)){
    		return max;
    	}
    	
    	if (node.isEnd())
    		max = index;
    	
    	Map<String, TrieNode> childMap = node.getChildren();
    	return findmlp(childMap, content, index + 1, max);
    }

    private static boolean find(TrieNode node, String[] content, int index) {
        if (index >= content.length)
            return false;
        if (node.getContent().equals(content[index])) {
            if (node.isEnd())
                return true;
            if (node.getChildren() == null)
                return false;
            for (Map.Entry<String, TrieNode> child : node.getChildren().entrySet()) {
                boolean re = find(child.getValue(), content, index+1);
                if (re)
                    return true;
            }
        }

        return false;
    }

    /**
     * 将输入的字符按中英文进行拆分，拆成多个词。
     */
    public static String[] splitWord(String str){
    	List<String> words = Lists.newArrayListWithCapacity(str.length());
        String tmpword = "";
        for(int i = 0; i < str.length(); i++){
        	char c = str.charAt(i);
        	if( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_'){
        		tmpword += c;
        	}
        	else{
        		if(tmpword.length() > 0){
        			words.add(tmpword);
        			tmpword = "";
        		}
        		words.add(String.valueOf(c));
        	}
        }
        if(tmpword.length() > 0){
        	words.add(tmpword);
        }
        String[] ret = new String[words.size()];
        words.toArray(ret);
        return ret;
    }
    
    public static void addWord (TrieNode root, String word) {
        if (StringUtils.isBlank(word)) {
        	return;
        }
        addWord(root, splitWord(StringUtils.trimToEmpty(word)), 0);
    }
    
    private static void addWord(TrieNode node, String[] words, int index) {
        if (index == words.length) {
            node.setEnd(true);
            return;
        }
        Map<String, TrieNode> children = node.getChildren();
        if (MapUtils.isEmpty(children)) {
            children = new HashMap<>();
            node.setChildren(children);
        }
        String key = words[index];
        TrieNode child = children.get(key);
        if (child == null) {
            child = new TrieNode(key, false, null);
            children.put(key, child);
        }
        addWord(child, words, index + 1);
    }

    public static void main(String[] args) {
        TrieNode root = new TrieNode(null, false, null);
        addWord(root, "中国ssb人民");
        addWord(root, "人民");
        addWord(root, "中");
        addWord(root, "sb");
        addWord(root, "Ucc");
        addWord(root, "人民sb");
        System.out.println(findSubString(root, "sb"));
        String prefix = findMaxLenPrefix(root, "我Ucc浏览器");
        System.out.println(prefix);
        
        for(String matched : findAllStartWords(root, new String[]{"人","民","sb"}, 0)){
        	System.out.println(matched);
        }
        
        System.out.println(findStartWordLen(root, new String[]{"人","民","sb"}, 0));
    }
}
