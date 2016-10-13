package com.lbc.nlp_algorithm.similarity;

public abstract class AbstractSimilarity<T> implements ISimilarity<T> {
	public AbstractSimilarity(){
		super();
	}
	
	public double sim(T sub, T ob){
		return 0d;
	}
	
	public double sim(T sub){
		return 0d;
	}

}
