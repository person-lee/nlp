package com.lbc.nlp_algorithm.similarity.simStrategy;

import org.apache.commons.lang3.StringUtils;

import com.lbc.nlp_algorithm.loadFile.LoadTongYiCi;
import com.lbc.nlp_domain.SpeechDefine;
import com.lbc.nlp_domain.Word;

public class ColourSim extends CalSimilar{
	/**
     * 定义两个不在同义词词典中的颜色词的相似度
     */
    private final Float ColourSimilarThreshold = 0.5f;
    
    private static ColourSim colourSim = null;
	
	private ColourSim(){
	}
	
	public static ColourSim getInstance(){
		if(colourSim == null){
			colourSim = new ColourSim();
		}
		return colourSim;
	}
	
	public Float handler(Word word1, Word word2){
		if(StringUtils.equals(word1.getSpeech(), word2.getSpeech())
                && SpeechDefine.isCustomerDefinedSpeech(word1.getSpeech()) 
                && SpeechDefine.ColourSpeech.equals(word1.getSpeech())){
			String wordnormed1 = word1.getNormedTerm();
			String wordnormed2 = word2.getNormedTerm();
			if(StringUtils.equals(wordnormed1, wordnormed2)){
				return 1f;
			}else if(wordnormed1.contains(wordnormed2) || wordnormed2.contains(wordnormed1)){
				return 0.8f;
			}else {
                return similar(word1.getTerm(), word2.getTerm(), ColourSimilarThreshold);
            }
        }else{
	    	if (this.getCalSimilar() != null){
	    		return this.getCalSimilar().handler(word1, word2);
	    	}else{
	    		return null;
	    	}
	    }
	}
	
	private Float similar(String str1, String str2, Float defaultSim){
        Float similar = LoadTongYiCi.getInstance().getWordsSimilarity(str1, str2);
        if(similar==null){
            similar = defaultSim;
        }
        return similar;
    }

}
