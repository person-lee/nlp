package com.lbc.nlp_algorithm.similarity.simStrategy;

import com.lbc.nlp_domain.Word;

public class DefaultSim extends CalSimilar{
	
	private static DefaultSim defaultSim = null;
	
	private DefaultSim(){
	}
	
	public static DefaultSim getInstance(){
		if(defaultSim == null){
			defaultSim = new DefaultSim();
		}
		return defaultSim;
	}
	
	public Float handler(Word word1, Word word2){
		int len1 = word1.getTerm().length();
		int len2 = word2.getTerm().length();
		String term1 = word1.getTerm();
		String term2 = word2.getTerm();
		
		if (term1.contains(term2) || term2.contains(term1)) {
            return 1f * Math.min(len1, len2) / Math.max(len1, len2);
        } else if("m".equals(word1.getSpeech()) && "m".equals(word2.getSpeech()) && len1 == len2){
            // 处理“6天”与“7天”这类case
            int i=0;
            while(i < len1 && term1.charAt(len1 - i - 1) == term2.charAt(len1 - i - 1)){
                i++;
            }
            return 1f * i / len1;
        }else{
        	return null;
        }
	}

}
