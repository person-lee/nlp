package com.lbc.nlp_algorithm.similarity.simStrategy;

import org.apache.commons.lang3.StringUtils;

import com.lbc.nlp_domain.SpeechDefine;
import com.lbc.nlp_domain.Word;

public class PartitionSim extends CalSimilar{
	private static PartitionSim partitionSim = null;
	
	private PartitionSim(){
	}
	
	public static PartitionSim getInstance(){
		if(partitionSim == null){
			partitionSim = new PartitionSim();
		}
		return partitionSim;
	}
	
	public Float handler(Word word1, Word word2){
		if (StringUtils.equals(word1.getSpeech(), word2.getSpeech())
                && SpeechDefine.isCustomerDefinedSpeech(word1.getSpeech())
			&& SpeechDefine.PartitionSpeech.equals(word1.getSpeech())) {
	        // partition,则比较归一化之后的词
	        if (word1.getNormedTerm().equals(word2.getNormedTerm())) {
	            return 1f;
	        } else {
	            return 0f;
	        }
	    }else{
	    	if (this.getCalSimilar() != null){
	    		return this.getCalSimilar().handler(word1, word2);
	    	}else{
	    		return null;
	    	}
	    }
	}
}
