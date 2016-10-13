package com.lbc.nlp_algorithm.similarity.simStrategy;

import com.lbc.nlp_domain.Word;

public class WordSimilarity {
    private static CalSimilar partitionSim = PartitionSim.getInstance();
    private static CalSimilar brandSim = BrandSim.getInstance();
    private static CalSimilar productSim = ProductSim.getInstance();
    private static CalSimilar addressSim = AddressSim.getInstance();
    private static CalSimilar colourSim = ColourSim.getInstance();
    private static CalSimilar defineSim = DefineSim.getInstance();
    private static CalSimilar defaultSim = DefaultSim.getInstance();

    public static Float wordSimilar(Word word1, Word word2){   	
        calLogic();        
        Float similar = partitionSim.handler(word1, word2);
        if(similar != null && similar < 0.0001){
            similar = null;
        }
        return similar;
    }
    
    private static void calLogic(){ 	
    	partitionSim.setCalSimilar(brandSim);
    	brandSim.setCalSimilar(productSim);
    	productSim.setCalSimilar(addressSim);
    	addressSim.setCalSimilar(colourSim);
    	colourSim.setCalSimilar(defineSim);
    	defineSim.setCalSimilar(defaultSim);
    }
}
