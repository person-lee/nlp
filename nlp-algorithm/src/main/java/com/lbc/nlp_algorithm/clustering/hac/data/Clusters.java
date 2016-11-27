package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.google.common.collect.Lists;
import com.lbc.nlp_algorithm.clustering.hac.common.Constants;

/**
 * 存储所有聚类结果,考虑到现在的输入数据都是用Experiments,在这里可以只记录数据在Experiments中的index,
 * 而不用记录具体数据
 *
 */
public class Clusters {
	List<List<ExperimentNode>> clusterList = Lists.newArrayList();
	private String clusterSeperator = Constants.CLUSTER_SEPERATOR;
	private String stringSeperator = Constants.STRING_SEPERATOR;

	/**
	 * 初始化clusters, 必须制定总共有多少个cluster
	 * @param numberOfClusters
	 */
	public Clusters(int numberOfClusters) {
		for (int i = 0; i < numberOfClusters; i++)
			clusterList.add(Lists.<ExperimentNode>newArrayList());
	}

	public int getNumberOfClusters() {
		return clusterList.size();
	}

	/**
	 * 指定的cluster里有多少个元素
	 * @param clusterId
	 * @return
	 */
	public int getClusterSize(int clusterId) {
		return clusterList.get(clusterId).size();
	}

	/**
	 * 获取某个cluster中的某个元素
	 * @param clusterId
	 * @param index
	 * @return
	 */
	public ExperimentNode getExperimentNodeByCluster(int clusterId, int index) {
		return clusterList.get(clusterId).get(index);
	}

	/**
	 * 添加某个元素到指定的cluster中
	 * @param clusterId
	 * @param node
	 */
	public void addExperimentNode(int clusterId, ExperimentNode node) {
		clusterList.get(clusterId).add(node);
	}

	//线上和线下的输出格式是不一样的
	public void dumpFile(File file, boolean isOnline) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));   //指定输出文件为UTF-8编码
		int numberOfClusters = clusterList.size();
		for (int i = 0; i < numberOfClusters; i++) {
			bw.write(clusterSeperator + "\n");
			int clusterSize = clusterList.get(i).size();
			for (int j = 0; j < clusterSize; j++) {
				ExperimentNode node = clusterList.get(i).get(j);
				if (false == isOnline) {
					bw.write(i + stringSeperator + node.getDescription() + stringSeperator
							+ node.getTag() + "\n");
				} else {
					bw.write(node.getTag() + Constants.ONLINE_STRING_SEPERATOR + node.getDescription() + "\r\n");
				}
			}
		}
		bw.close();
	}
}
