package com.lbc.nlp_algorithm.clustering.hac.core.densitypeaks;

public class DPClusterNode {

	//记录每个点的密度
	public double density; //密度
	int index; //experiments中的索引，对应到experiments中的某个点
	int delta; //index这个点的delta。deltai是这样一个点,它的密度比i大，但是距离i最近
	public double deltaValue; //对应delta的距离值
	int clusterId; //所属的clusterId
	int centerId; //所属的cluster的centerId
	boolean halo; //
	public DPClusterNode(double density, int index) {
		this.density = density;
		this.index = index;
		this.clusterId = -1;
		this.centerId = -1;
		this.halo = false;
		this.deltaValue = 0.0;
	}
}
