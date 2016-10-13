package com.lbc.nlp_algorithm.classification.domain;

import java.util.List;
import java.util.Map;

import com.lbc.nlp_algorithm.classification.liblinear.FeatureNode;

/**
 * 用于训练的模型向量化表示
 */

public class ModelVector {

	/**
	 * 特征向量的维度
	 */
	private int featureNum;
	/**
	 * 样本数
	 */
	private int sampleNum;
	/**
	 * 向量化的数据
	 */
	private Map<Integer, List<FeatureNode[]>> vectors;
	
	public static class Builder {
		private int featureNum;
		private int sampleNum;
		private Map<Integer, List<FeatureNode[]>> vectors;
		
		public Builder featureNum(int featureNum) {
			this.featureNum = featureNum;
			return this;
		}
		
		public Builder sampleNum(int sampleNum) {
			this.sampleNum = sampleNum;
			return this;
		}
		
		public Builder vectors(Map<Integer, List<FeatureNode[]>> vectors) {
			this.vectors = vectors;
			return this;
		}
		
		public ModelVector build() {
			return new ModelVector(this);
		}
	}
	
	public ModelVector(Builder builder) {
		this.featureNum = builder.featureNum;
		this.sampleNum = builder.sampleNum;
		this.vectors = builder.vectors;
	}
	
	public int getFeatureNum() {
		return featureNum;
	}
	public void setFeatureNum(int featureNum) {
		this.featureNum = featureNum;
	}
	public int getSampleNum() {
		return sampleNum;
	}
	public void setSampleNum(int sampleNum) {
		this.sampleNum = sampleNum;
	}
	public Map<Integer, List<FeatureNode[]>> getVectors() {
		return vectors;
	}
	public void setVectors(Map<Integer, List<FeatureNode[]>> vectors) {
		this.vectors = vectors;
	}
	
}
