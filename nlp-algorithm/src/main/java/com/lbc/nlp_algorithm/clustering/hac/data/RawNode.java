package com.lbc.nlp_algorithm.clustering.hac.data;

import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;

/**
 * rawNode的数据格式是固定的，缺失的为null
 *
 */
public class RawNode {
	//句子(已切词)
	private String sentence = null;
	//句子对应的标记，线下测试的时候使用
	private String tag = "";
	//句子对应的向量
	private Vector vector = null;
	
	public String getSentence() {
		return sentence;
	}
	public void setSentence(String sentence) {
		this.sentence = sentence;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Vector getVector() {
		return vector;
	}
	public void setVector(Vector vector) {
		this.vector = vector;
	}
}
