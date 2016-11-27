package com.lbc.nlp_algorithm.clustering.hac.common.measure;

/**
 * 衡量两个对象之间的距离(dissimilary)，值越大，表示距离越远，相似度越低
 *
 */
public interface DissimilarityMeasure {

	public double computeDissimilarity(Object obj1, Object obj2);
	
	//两个对象之间可能的最大距离
	public double getMaxDissimilarity();
}
