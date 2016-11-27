package com.lbc.nlp_algorithm.clustering.hac.eval;

/**
 * 聚类结果
 *
 */
public class ClusterResult {

	//cluster id
	public String id;
	//聚类数据
	public String data;
	//data对应的tag，这个用于已知tag的时候，用来评估聚类效果
	String tag;
}
