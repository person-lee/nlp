package com.lbc.nlp_algorithm.clustering.hac.distributed;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.InnerProductMeasure;
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

public class ReduceExperiment {
	private final Logger LOGGER = LoggerFactory.getLogger(ReduceExperiment.class);

	private Experiments originalExperiments;
	private Experiments experiments;
	private ClusteringBuilder clusteringBuilder;
	private DissimilarityMeasure measure;
	private AgglomerationMethod agglomerationMethod;
	private String seperator = Constants.STRING_SEPERATOR;
	private Clusters result;

	// 聚类截止水平
	private double stopThreshold = PropertiesUtil.getClusterProperties().getPartitionedHACStopThreshold();
	// 最终聚类的语料下限
	private int sizeThreshold = PropertiesUtil.getClusterProperties().getPartitionedHACSizeThreshold();

	public ReduceExperiment(final Experiments originalExperiments, final Experiments experiments) {
		this.originalExperiments = originalExperiments;
		this.experiments = experiments;
		this.clusteringBuilder = new DendrogramBuilder(experiments.numberOfExperiments());
		this.measure = new InnerProductMeasure();
		this.agglomerationMethod = new WardLinkage();
	}

	public void execute() {
		try {
			LOGGER.info("ReduceExperiment begin .....");
			long beginTime = System.currentTimeMillis();

			result = clusterExperiments(experiments);

			long endTime = System.currentTimeMillis();
			LOGGER.info("ReduceExperiment end ..... Time eclapse : " + (endTime - beginTime) + "ms");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public Clusters getResult() {
		return result;
	}

	public void dumpFile(File file, boolean isOnline) throws IOException {
		result.dumpFile(file, isOnline);
	}

	private Clusters clusterExperiments(Experiments inExperiments) throws IOException {
		LOGGER.info("slice size : " + inExperiments.numberOfExperiments());
		clusteringBuilder.init(inExperiments.numberOfExperiments(), getExperimentCount(inExperiments));

		ClusterMethod hac = new HierarchicalAgglomerativeClusterFaster(
				inExperiments, clusteringBuilder, measure, agglomerationMethod,
				stopThreshold, sizeThreshold);
		hac.cluster();

		return restoreClusters(hac.getClusters());
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
	 * 从聚类结果中还原原始语料
	 * @param hlClusters	聚类结果
	 * @return	聚类后的原始语料
	 */
	private Clusters restoreClusters(Clusters hlClusters) {
		int numberOfClusters = hlClusters.getNumberOfClusters();
		Clusters clusters = new Clusters(numberOfClusters);
		for (int i = 0; i < numberOfClusters; i++) {
			int clusterSize = hlClusters.getClusterSize(i);
			for (int j = 0; j < clusterSize; j++) {
				ExperimentNode hlNode = hlClusters.getExperimentNodeByCluster(i, j);
				String[] ids = hlNode.getDescription().split(seperator);

				for (String id : ids) {
					ExperimentNode node = originalExperiments.get(Integer.valueOf(id));
					clusters.addExperimentNode(i, node);
				}
			}
		}

		return clusters;
	}
}
