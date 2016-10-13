package com.lbc.nlp_algorithm.similarity.simStrategy;

import org.apache.commons.lang3.StringUtils;

import com.lbc.nlp_algorithm.loadFile.LoadTongYiCi;
import com.lbc.nlp_domain.SpeechDefine;
import com.lbc.nlp_domain.Word;

public class AddressSim extends CalSimilar{
	/**
     * 定义两个不在同义词词典中的地址词的相似度
     */
    private final Float AddressSimilarThreshold = 0.3f;
    
    private static AddressSim addressSim = null;
	
	private AddressSim(){
	}
	
	public static AddressSim getInstance(){
		if(addressSim == null){
			addressSim = new AddressSim();
		}
		return addressSim;
	}
	
	public Float handler(Word word1, Word word2){
		if(StringUtils.equals(word1.getSpeech(), word2.getSpeech())
                && SpeechDefine.isCustomerDefinedSpeech(word1.getSpeech()) 
                && SpeechDefine.AddressSpeech.equals(word1.getSpeech())){
            if(word1.getNormedTerm().equals(word2.getNormedTerm())){
                return 1f;
            } else {
                return similar(word1.getTerm(), word2.getTerm(), AddressSimilarThreshold);
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
