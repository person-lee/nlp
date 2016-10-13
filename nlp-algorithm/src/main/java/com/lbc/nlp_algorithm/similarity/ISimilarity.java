package com.lbc.nlp_algorithm.similarity;

public interface ISimilarity<T> {
	
	public double sim(T sub, T ob);
	
	public double sim(T sub);

}
