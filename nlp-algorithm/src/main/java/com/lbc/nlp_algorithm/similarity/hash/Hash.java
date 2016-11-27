package com.lbc.nlp_algorithm.similarity.hash;


/**
 * 用于LSH的哈希函数接口
 */
public interface Hash {

	/**
	 * 整型范围内最大孪生素数（相差为2的两个数都是质数的情况）的较小值
	 */
	static final int MAX_INT_SMALLER_TWIN_PRIME = 2147482949;

	/**
	 * 计算哈希值
	 * @param str	计算对象
	 * @return		哈希值
	 */
	public int hash(String str);
}
