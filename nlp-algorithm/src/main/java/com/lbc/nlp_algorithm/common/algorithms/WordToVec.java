package com.lbc.nlp_algorithm.common.algorithms;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.lbc.nlp_algorithm.loadFile.LoadUnifiedIdfs;
import com.lbc.nlp_domain.SpeechDefine;
import com.lbc.nlp_domain.Word;
import com.lbc.nlp_domain.WordWeight;

public class WordToVec {
	/**
     * 没有idf词典时,默认idf值
     */
    private static final Float DefaultIdf = 1f;
    /**
     * 从idf词典中未找到该词时, 新词的idf
     */
    private static final Float UnseenIdf = 8f;
	
	private static float getWordIdf(Word word){
        Map<String, Float> idfs = LoadUnifiedIdfs.getIdfs();
        if(idfs==null){
            return DefaultIdf;
        }
        String term = word.getTerm();
        if(SpeechDefine.PartitionSpeech.equals(word.getSpeech())){
            term = word.getNormedTerm();
        }
        Float idf = idfs.get(term);
        if(idf==null){
            return UnseenIdf;
        }
        return idf;
    }
	
	public static WordWeight[] wordsToVector(List<Word> words, Map<String, Integer> wordids){
		Map<String, Integer> tfs = new HashMap<String, Integer>(words.size());
		calIdf(words, wordids, tfs);
		return normWord(words, wordids, tfs);
    }
	
	private static void calIdf(List<Word> words, Map<String, Integer> locOfWords/*词的坐标*/, Map<String, Integer> tfs) {
        int id=locOfWords.size();
        for(Word word : words){
            String term = word.getTerm();
            if(StringUtils.isBlank(term)){
                continue;
            }
            if(!locOfWords.containsKey(term)){
            	locOfWords.put(term, id++);
            }
            Integer freq = tfs.get(term);
            if(freq==null){
                freq=0;
            }
            tfs.put(term, freq+1);
        }
	}
	
	private static WordWeight[] normWord(List<Word> words, Map<String, Integer> locOfWords, Map<String, Integer> tfs) {
		WordWeight[] vector = new WordWeight[tfs.size()];
        Set<String> duplicate = new HashSet<String>(words.size());
        double sum = 0;
        int i=0;
        for(Word word : words){
            String term = word.getTerm();
            if(StringUtils.isBlank(term) || duplicate.contains(term)){
                continue;
            }
            duplicate.add(term);
            float value = getWordIdf(word) * tfs.get(term);
            sum += value * value;
            vector[i++] = new WordWeight.Builder().word(word).weight(value).index(locOfWords.get(term)).build();
        }
        sum = Math.sqrt(sum);
        for(WordWeight item : vector){
            item.setWeight((float) (item.getWeight() / sum));
        }
        Arrays.sort(vector, new Comparator<WordWeight>() {
            @Override
            public int compare(WordWeight o1, WordWeight o2) {
                return o1.getIndex().compareTo(o2.getIndex());
            }
        });
        return vector;
	}
}
