package com.lbc.nlp_algorithm.similarity.simStrategy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.lbc.nlp_algorithm.similarity.category.Brand;
import com.lbc.nlp_algorithm.similarity.category.BrandItem;
import com.lbc.nlp_domain.SpeechDefine;
import com.lbc.nlp_domain.Word;

public class BrandSim extends CalSimilar{
	/**
     * 定义相同三级品类下, 不同品牌词的相似度
     */
    private final Float BrandThirdCateSimilarThreshold = 0.5f;
    /**
     * 定义品牌商品出现最多的二级品类相同时, 不同品牌词的相似度
     */
    private final Float BrandSecondCateSimilarThreshold = 0.3f;
    
    private static BrandSim brandSim = null;
	
	private BrandSim(){
	}
	
	public static BrandSim getInstance(){
		if(brandSim == null){
			brandSim = new BrandSim();
		}
		return brandSim;
	}
	
	public Float handler(Word word1, Word word2){
		Float similar = null;
		if(StringUtils.equals(word1.getSpeech(), word2.getSpeech())
                && SpeechDefine.isCustomerDefinedSpeech(word1.getSpeech()) 
                && SpeechDefine.BrandSpeech.equals(word1.getSpeech())){
            if(word1.getNormedTerm().equals(word2.getNormedTerm())){
                return 1f;
            } else {
            	similar =  brandSimilar(word1.getTerm(), word2.getTerm());
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
	
	private Float brandSimilar(String str1, String str2){
		Set<BrandItem> brandItemsa = Brand.getInstance().getBrandnameItems(str1);
        Set<BrandItem> brandItemsb = Brand.getInstance().getBrandnameItems(str2);
        int maxsecondcatea = -1, maxsecondcateb = -1;
        int maxwarenuma = -1, maxwarenumb = -1;
        if(CollectionUtils.isNotEmpty(brandItemsa) && CollectionUtils.isNotEmpty(brandItemsb)){
            Set<Integer> brandCatesa = new HashSet<>();
            for(BrandItem brandItem : brandItemsa){
                Collection<Integer> thirdcates = brandItem.getThirdCates();
                if(thirdcates != null){
                    brandCatesa.addAll(thirdcates);
                }
                int warenum = brandItem.getBrandItemCate().getWarenum();
                if(warenum > maxwarenuma){
                    maxwarenuma = warenum;
                    maxsecondcatea = brandItem.getBrandItemCate().getMaxsecondcate();
                }
            }
            for(BrandItem brandItem : brandItemsb){
                Collection<Integer> thirdcates = brandItem.getThirdCates();
                for(Integer thirdcate : thirdcates){
                    if(brandCatesa.contains(thirdcate)){
                        return BrandThirdCateSimilarThreshold;
                    }
                }
                int warenum = brandItem.getBrandItemCate().getWarenum();
                if(warenum>maxwarenumb){
                    maxwarenumb = warenum;
                    maxsecondcateb = brandItem.getBrandItemCate().getMaxsecondcate();
                }
            }
            if(maxsecondcatea != -1 && maxsecondcatea == maxsecondcateb){
                return BrandSecondCateSimilarThreshold;
            }
        }
        return null;
	}
}
