package com.lbc.nlp_algorithm.classification.domain;

import java.util.List;
import java.util.Map;

/**
 * 用于训练的模型向量化表示
 * @param <T>
 */

public class ModelVector<T> {

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
	private Map<Integer, List<T[]>> vectors;
	
	public static class Builder<T> {
		private int featureNum;
		private int sampleNum;
		private Map<Integer, List<T[]>> vectors;
		
		public Builder<T> featureNum(int featureNum) {
			this.featureNum = featureNum;
			return this;
		}
		
		public Builder<T> sampleNum(int sampleNum) {
			this.sampleNum = sampleNum;
			return this;
		}
		
		public Builder<T> vectors(Map<Integer, List<T[]>> vectors) {
			this.vectors = vectors;
			return this;
		}
		
		public ModelVector<T> build() {
			return new ModelVector<T>(this);
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
	public Map<Integer, List<T[]>> getVectors() {
		return vectors;
	}
	public void setVectors(Map<Integer, List<T[]>> vectors) {
		this.vectors = vectors;
	}
	
}
