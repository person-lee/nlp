package com.lbc.nlp_algorithm.clustering.hac.distributed;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.InnerProductMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.core.ClusterMethod;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.AgglomerationMethod;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.ClusteringBuilder;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.DendrogramBuilder;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.HierarchicalAgglomerativeClusterFaster;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.WardLinkage;
import com.lbc.nlp_algorithm.clustering.hac.data.Clusters;
import com.lbc.nlp_algorithm.clustering.hac.data.ExperimentNode;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;
import com.lbc.nlp_algorithm.clustering.hac.properties.PropertiesUtil;


public class ClusterExperiment {
	private final Logger LOGGER = LoggerFactory.getLogger(ClusterExperiment.class);

	private Experiments experiments;
	private ClusteringBuilder clusteringBuilder;
	private DissimilarityMeasure measure;
	private AgglomerationMethod agglomerationMethod;
	private ClusterMethod clusterMethod;
	private String seperator = Constants.STRING_SEPERATOR;
	private Experiments result;

	// 聚类截止水平
	private double stopThreshold = PropertiesUtil.getClusterProperties().getPartitionedHACStopThreshold();
	// 最终聚类的语料下限
	private int sizeThreshold = PropertiesUtil.getClusterProperties().getPartitionedHACSizeThreshold();

	public ClusterExperiment(final Experiments experiments) {
		this.experiments = experiments;
		this.clusteringBuilder = new DendrogramBuilder(experiments.numberOfExperiments());
		this.measure = new InnerProductMeasure();
		this.agglomerationMethod = new WardLinkage();
	}

	public void execute() {
		try {
			LOGGER.info("ClusterExperiment begin .....");
			long beginTime = System.currentTimeMillis();

			result = clusterExperiments(experiments);

			long endTime = System.currentTimeMillis();
			LOGGER.info("ClusterExperiment end ..... Time eclapse : " + (endTime - beginTime) + "ms");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public Experiments getResult() {
		return result;
	}

	public void dumpFile(File file,  boolean isOnline) throws IOException {
		clusterMethod.dumpFile(file, isOnline);
	}

	private Experiments clusterExperiments(Experiments inExperiments) throws IOException {
		LOGGER.info("slice size : " + inExperiments.numberOfExperiments());
		clusteringBuilder.init(inExperiments.numberOfExperiments(), getExperimentCount(inExperiments));

		if (inExperiments.getSplited()) {
			//由于是拆分后的片段，减少sizeThreshold以获得相似的最终结果
			clusterMethod = new HierarchicalAgglomerativeClusterFaster(inExperiments,
					clusteringBuilder, measure, agglomerationMethod, stopThreshold,
					sizeThreshold - 2);
		} else {
			clusterMethod = new HierarchicalAgglomerativeClusterFaster(inExperiments,
					clusteringBuilder, measure, agglomerationMethod, stopThreshold,
					sizeThreshold);
		}
		clusterMethod.cluster();

		return createNewExperiments(clusterMethod.getClusters());
	}

    /**
     * 获取处理后语料中的语料数量
     * @param experiments	处理后的语料
     * @return				每个语料中的语料数量
     */
    private int[] getExperimentCount(Experiments experiments) {
    	int cnt = experiments.numberOfExperiments();
    	int[] result = new int[cnt];

    	for (int i=0; i<cnt; i++) {
    		result[i] = experiments.get(i).getDescription().split(seperator).length;

    		LOGGER.debug("Experiments index " + i + ", cnt = " + result[i] + ", desc = " + experiments.get(i).getDescription());
    	}

    	return result;
    }

    /**
     * 将聚类结果转化为新的语料
     * @param clusters	聚类结果
     * @return	新的语料
     */
	private Experiments createNewExperiments(Clusters clusters) {
		Experiments newExperiments = new Experiments();
		int numberOfClusters = clusters.getNumberOfClusters();
		for (int i = 0; i < numberOfClusters; i++) {
			ExperimentNode experimentNode = getCenter(clusters, i);
			newExperiments.add(experimentNode);
		}
		return newExperiments;
	}

	/**
	 * 在聚类结果中获取中心
	 * @param clusters	聚类结果
	 * @param clusterId	结果编号
	 * @return	中心
	 */
	private ExperimentNode getCenter(Clusters clusters, int clusterId) {
		int clusterSize = clusters.getClusterSize(clusterId);
		Vector vector = clusters.getExperimentNodeByCluster(clusterId, 0).getVector();
		Vector center = vector.clone();
		//description里面包含这个center所代表的所有点
		StringBuilder description = new StringBuilder();
		for (int i = 0; i < clusterSize; i++) {
			ExperimentNode experimentNode = clusters.getExperimentNodeByCluster(clusterId, i);
			vector = experimentNode.getVector();
			center.elementPlus(vector);
			//description.append(experimentNode.getId() + seperator);
			description.append(experimentNode.getDescription() + seperator);
		}
		center.elementDivid(clusterSize);

		ExperimentNode experimentNode = new ExperimentNode(center, description.toString(), "", 0);
		return experimentNode;
	}
}
