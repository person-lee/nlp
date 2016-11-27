package com.lbc.nlp_algorithm.clustering.hac.common.vectorization;

/**
 * 对输入对象进行向量化，这里的向量是sparse vector,用二维数组存储的.每行表示一个向量
 *
 */
public interface Vectorization {

	//批量进行向量化，比如tf-idf这种方法，是需要批量数据的，而不是一条一条的处理
	//向量和原始数据的对应关系，交给使用者自己去处理吧，封装太多也不灵活
	public Vector[] convert2Vector(Object[] objects);
}
