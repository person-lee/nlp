package com.lbc.nlp_algorithm.clustering.hac.data;

import java.util.ArrayList;

import com.google.common.collect.Lists;

public class Experiments {
	private ArrayList<ExperimentNode> objs = Lists.newArrayList();
	private boolean splited = false;				//是否为切分后的片

	public boolean getSplited() {
		return splited;
	}

	public void setSplited(boolean splited) {
		this.splited = splited;
	}

	public ExperimentNode get(int index) {
		return objs.get(index);
	}

	public ExperimentNode getByTag(String tag) {
		for (int i = 0; i < objs.size(); i++) {
			if (objs.get(i).getTag().equals(tag)) {
				return objs.get(i);
			}
		}

		return null;
	}

	public void add(ExperimentNode node) {
		objs.add(node);
	}

	public void addAll(Experiments experiments) {
		int nObservation = experiments.numberOfExperiments();
		for (int i = 0; i < nObservation; i++)
			objs.add(experiments.get(i));
	}

	public int numberOfExperiments() {
		return objs.size();
	}

	//测试时使用
	public void cleanMemory() {
		int nObservation = objs.size();
		for (int i = 0; i < nObservation; i++)
			objs.get(i).cleanMemory();
	}

}
