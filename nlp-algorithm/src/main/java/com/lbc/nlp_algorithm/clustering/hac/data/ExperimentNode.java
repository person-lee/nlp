package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.Serializable;

import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;

public class ExperimentNode implements Serializable {

	private static final long serialVersionUID = 6887567192230963189L;

	private Vector vector;
	private String description;
	private String tag;
	//每个ExperimentNode有一个唯一的id号
	private int id;

	public ExperimentNode(Vector vector, String description, String tag, int id) {
		this.vector = vector;
		this.description = description;
		this.tag = tag;
		this.id = id;
	}

	public Vector getVector() {
		return vector;
	}
	public String getDescription() {
		return description;
	}
	public String getTag() {
		return tag;
	}
	public int getId() {
		return id;
	}

	//测试时使用
	public void cleanMemory() {
		this.vector = null;
	}
}
