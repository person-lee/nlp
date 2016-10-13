package com.lbc.nlp_modules.common.tree.trie;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class TrieMap<V> {
    private String key;
    private V value;
    private TrieMap<V> parent;
    private Map<String, TrieMap<V>> children;

    public TrieMap(){
        children = new HashMap<String, TrieMap<V>>();
    }

    public static String[] charArrayKeys(String key){
        String[] keys = new String[key.length()];
        int i = 0;
        for(char c : key.toCharArray()){
            keys[i++] = String.valueOf(c);
        }
        return keys;
    }

    public static String[] makeWordKey(String word){
        List<String> keys = new ArrayList<String>(word.length());
        for(int i=0; i<word.length(); ){
            int j=i;
            for(; j<word.length(); j++){
                if(!CharUtils.isAscii(word.charAt(j))){
                    break;
                }
            }
            if(i==j){
                keys.add(String.valueOf(word.charAt(i)));
                i++;
            } else {
                keys.add(word.substring(i, j).toLowerCase());
                i=j;
            }
        }
        return keys.toArray(new String[0]);
    }

    public void putwithWordKey(String key, V value){
        put(makeWordKey(key), value);
    }

    public void put(String[] keys, V value){
        if(keys.length==0){
            return;
        }
        TrieMap<V> position = this;
        for(int i=0; i<keys.length; i++) {
            TrieMap<V> child = position.children.get(keys[i]);
            if(child==null){
                child = new TrieMap<V>();
                child.key = keys[i];
                child.parent = position;
                position.children.put(keys[i], child);
            }
            position = child;
        }
        position.value = value;
    }

    public V get(String[] keys){
        TrieMap<V> position = this;
        for(int i=0; i<keys.length; i++) {
            TrieMap<V> child = position.children.get(keys[i]);
            if(child==null){
                return null;
            }
            position = child;
        }
        return position.value;
    }

    public TrieMap<V> getMostMatchNode(String[] keys){
        return getMostMatchNode(keys, 0);
    }

    public TrieMap<V> getMostMatchNode(String[] keys, int key_offset){
        TrieMap<V> position = this;
        for(int i=key_offset; i<keys.length; i++) {
            TrieMap<V> child = position.children.get(keys[i]);
            if(child==null){
                break;
            }
            position = child;
        }
        return position;
    }

    public V getValue(){
        return value;
    }

    public String[] getKeys(){
        List<String> keys = new ArrayList<String>();
        TrieMap<V> position = this;
        while(position.parent!=null){
            keys.add(position.key);
            position = position.parent;
        }
        Collections.reverse(keys);
        return keys.toArray(new String[0]);
    }

    public static void main(String[] args){
        TrieMap<Integer> trieMap = new TrieMap<Integer>();
        trieMap.put(new String[]{"双", "卡", "双"}, 5);
        trieMap.put(new String[]{"双", "卡", "双", "待"}, 6);
        System.out.println(trieMap.get(new String[]{"双", "卡", "双", "待"}));
        System.out.println(trieMap.get(new String[]{"双", "卡", "双"}));
        TrieMap<Integer> trieMap1 = trieMap.getMostMatchNode(new String[]{"单", "卡", "一", "侍"});
        System.out.println(StringUtils.join(trieMap1.getKeys()));
    }
}
