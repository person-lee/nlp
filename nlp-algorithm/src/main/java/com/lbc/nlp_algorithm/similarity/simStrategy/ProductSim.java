package com.lbc.nlp_algorithm.similarity.simStrategy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.lbc.nlp_algorithm.similarity.category.ProductCate;
import com.lbc.nlp_domain.SpeechDefine;
import com.lbc.nlp_domain.Word;

public class ProductSim extends CalSimilar{
	/**
     * 定义相同三级品类,不同词的相似度(同义词), 比如: 连衣裙和裙子
     */
    private final Float ProductThirdCateSimilarThreshold = 0.9f;
    
    private static ProductSim productSim = null;
	
	private ProductSim(){
	}
	
	public static ProductSim getInstance(){
		if(productSim == null){
			productSim = new ProductSim();
		}
		return productSim;
	}
	
	public Float handler(Word word1, Word word2){
		Float similar = null;
		if(StringUtils.equals(word1.getSpeech(), word2.getSpeech())
                && SpeechDefine.isCustomerDefinedSpeech(word1.getSpeech()) 
                && SpeechDefine.ProductCateSpeech.equals(word1.getSpeech())){
            if(word1.getNormedTerm().equals(word2.getNormedTerm())){
                return 1f;
            } else {
            	similar = productCateSimilar(word1.getTerm(), word2.getTerm());
            }
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
	
	private Float productCateSimilar(String catea, String cateb){
        List<Integer> cateLista = ProductCate.getInstance().getThirdCateids(catea);
        List<Integer> cateListb = ProductCate.getInstance().getThirdCateids(cateb);
        if(cateLista!=null && cateListb!=null) {
            for (Integer cate : cateLista) {
                if (cateListb.contains(cate)) {
                    return ProductThirdCateSimilarThreshold;
                }
            }
        }
        return null;
    }

}
