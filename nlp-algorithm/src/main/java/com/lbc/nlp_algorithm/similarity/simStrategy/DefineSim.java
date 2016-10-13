package com.lbc.nlp_algorithm.similarity.simStrategy;

import com.lbc.nlp_algorithm.loadFile.LoadTongYiCi;
import com.lbc.nlp_domain.SpeechDefine;
import com.lbc.nlp_domain.Word;

public class DefineSim extends CalSimilar{
	private static DefineSim defineSim = null;
	
	private DefineSim(){
	}
	
	public static DefineSim getInstance(){
		if(defineSim == null){
			defineSim = new DefineSim();
		}
		return defineSim;
	}
	
	public Float handler(Word word1, Word word2){
		Float similar = null;
		if ((word1.getSpeech() != null && word2.getSpeech() != null 
				&& word1.getSpeech().charAt(0) == word2.getSpeech().charAt(0))
                || (SpeechDefine.isCustomerDefinedSpeech(word1.getSpeech()) 
                		|| SpeechDefine.isCustomerDefinedSpeech(word2.getSpeech()))) {
            // 词性相同或者是自定义词，做同义词比较
            similar = LoadTongYiCi.getInstance().getWordsSimilarity(word1.getTerm(), word2.getTerm());
        }
		if(similar != null){
			return similar;
		}else{
	    	if (this.getCalSimilar() != null){
	    		return this.getCalSimilar().handler(word1, word2);
	    	}else{
	    		return null;
	    	}
	    }
	}

}
