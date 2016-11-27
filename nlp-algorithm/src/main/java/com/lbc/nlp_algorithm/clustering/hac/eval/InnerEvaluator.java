package com.lbc.nlp_algorithm.clustering.hac.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.filter.FilterUtils;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.InnerProductMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.DenseVector;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.SplitedText2VectorTfidf;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vectorization;
import com.lbc.nlp_algorithm.clustering.hac.data.CommonExperimentsImporter;
import com.lbc.nlp_algorithm.clustering.hac.data.ExperimentNode;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;
import com.lbc.nlp_algorithm.clustering.hac.data.ExperimentsImporter;
import com.lbc.nlp_algorithm.clustering.hac.data.RawReader;
import com.lbc.nlp_algorithm.clustering.hac.data.SplitWordRawReader;

/**
 * 使用内部信息评价聚类结果
 */
public class InnerEvaluator {
	private final Logger LOG = LoggerFactory.getLogger(InnerEvaluator.class);

	private static DissimilarityMeasure measure = new InnerProductMeasure();

	class ClusterInfo {
		int count;				//语料数量
		ExperimentNode center;	//中心点

		double maxInnerDist;	//内部元素到中心的最大距离
		double minInnerDist;	//内部元素到中心的最小距离
		double avgInnerDist;	//内部元素到中心的平均距离
		double innerVariance;	//内部距离方差

		double maxOuterDist;	//中心到其他类中心的最大距离
		double minOuterDist;	//中心到其他类中心的最小距离
		double avgOuterDist;	//中心到其他类中心的平均距离
		double outerVariance;	//外部距离方差
	}

	public void eval(File sourceFile, File resultFile) throws IOException {
		Experiments source = readSource(sourceFile);
		int sourceCount = source.numberOfExperiments();

		List<Experiments> result = readResult(source, resultFile);
		int resultClusterCount = result.size();

		int resultCount = 0;
		int resultMinCount = Integer.MAX_VALUE;
		int resultMaxCount = Integer.MIN_VALUE;
		double centerMinDistance = Double.MAX_VALUE;
		double centerMaxDistance = Double.MIN_VALUE;
		double innerMinDistance = Double.MAX_VALUE;
		double innerMaxDistance = Double.MIN_VALUE;
		List<ClusterInfo> clusterInfos = Lists.newArrayList();
		for (Experiments item : result) {
			ClusterInfo clusterInfo = getClusterInfo(item);

			resultCount += clusterInfo.count;
			resultMinCount = (resultMinCount > clusterInfo.count) ? clusterInfo.count : resultMinCount;
			resultMaxCount = (resultMaxCount < clusterInfo.count) ? clusterInfo.count : resultMaxCount;

			for (int j = 0; j < clusterInfos.size(); j++) {
				double dist = measure.computeDissimilarity(clusterInfo.center.getVector(), clusterInfos.get(j).center.getVector());
				centerMinDistance = (centerMinDistance > dist) ? dist : centerMinDistance;
				centerMaxDistance = (centerMaxDistance < dist) ? dist : centerMaxDistance;
			}

			innerMinDistance = (innerMinDistance > clusterInfo.avgInnerDist) ? clusterInfo.avgInnerDist : innerMinDistance;
			innerMaxDistance = (innerMaxDistance < clusterInfo.avgInnerDist) ? clusterInfo.avgInnerDist : innerMaxDistance;

			clusterInfos.add(clusterInfo);
		}

		LOG.info("聚类结果分析数据：");
		LOG.info("原始语料数量：" + sourceCount);
		LOG.info("结果语料数量：" + resultCount);
		LOG.info("结果分类数量：" + resultClusterCount);
		LOG.info("其中：最小单类语料数量：" + resultMinCount);
		LOG.info("其中：最大单类语料数量：" + resultMaxCount);
		LOG.info("其中：最小类中心距离：" + centerMinDistance);
		LOG.info("其中：最大类中心距离： " + centerMaxDistance);
		LOG.info("其中：最小类内平均距离：" + innerMinDistance);
		LOG.info("其中：最大类内平均距离： " + innerMaxDistance);

		for (int i = 0; i < clusterInfos.size(); i++) {
			clusterInfos.get(i).minOuterDist = Double.MAX_VALUE;
			clusterInfos.get(i).maxInnerDist = Double.MIN_VALUE;
			for (int j = 0; j < clusterInfos.size(); j++) {
				if (i == j) {
					continue;
				}
				double dist = measure.computeDissimilarity(clusterInfos.get(i).center.getVector(), clusterInfos.get(j).center.getVector());
				clusterInfos.get(i).minOuterDist = (clusterInfos.get(i).minOuterDist > dist) ? dist : clusterInfos.get(i).minOuterDist;
				clusterInfos.get(i).maxInnerDist = (clusterInfos.get(i).maxInnerDist < dist) ? dist : clusterInfos.get(i).maxInnerDist;
			}

			LOG.debug("类" + (i+1) + "，语料个数" + clusterInfos.get(i).count + "，内部最小距离" + clusterInfos.get(i).minInnerDist
					+ "，内部平均距离" + clusterInfos.get(i).avgInnerDist + "，内部最大距离" + clusterInfos.get(i).maxInnerDist
					+ "，外部最小距离" + clusterInfos.get(i).minOuterDist + "，外部最大距离" + clusterInfos.get(i).maxInnerDist);
		}
	}

	//读取原始语料
	private Experiments readSource(File file) {
		try {
			Vector vectorTemplate = new DenseVector(0);
			RawReader rawReader = new SplitWordRawReader(file, vectorTemplate, FilterUtils.getFilterList());
			Vectorization vectorization = new SplitedText2VectorTfidf(vectorTemplate, FilterUtils.getStopWords());
			ExperimentsImporter importer = new CommonExperimentsImporter(rawReader, vectorization);
			Experiments experiments = importer.read();
			return experiments;
		} catch (IOException e) {
			LOG.error(e.getMessage());
			return null;
		}
	}

	//读取结果语料
	private List<Experiments> readResult(Experiments source, File file) {
		List<Experiments> result = Lists.newArrayList();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			Experiments lastExperiments = new Experiments();

			while (null != (line = br.readLine())) {
				line = line.trim();
				if (line.equals(Constants.CLUSTER_SEPERATOR)) {
					if (lastExperiments.numberOfExperiments() > 0) {
						result.add(lastExperiments);
					}
					lastExperiments = new Experiments();
				} else {
					String[] items = line.split("\t");
					if (items.length >= 2) {
						lastExperiments.add(source.getByTag(items[0]));
					}
				}
			}
			if (lastExperiments.numberOfExperiments() > 0) {
				result.add(lastExperiments);
			}

			br.close();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return result;
	}

	//获取聚类中心
	private ExperimentNode getCenter(Experiments nodes) {
		int clusterSize = nodes.numberOfExperiments();
		Vector vector = nodes.get(0).getVector();
		Vector center = vector.clone();

		//description里面包含这个center所代表的所有点
		StringBuilder description = new StringBuilder();
		for (int i = 0; i < nodes.numberOfExperiments(); i++) {
			ExperimentNode node = nodes.get(i);
			vector = node.getVector();
			center.elementPlus(vector);
			description.append(node.getId() + Constants.STRING_SEPERATOR);
		}
		center.elementDivid(clusterSize);

		ExperimentNode experimentNode = new ExperimentNode(center,
				description.substring(0, description.length() - Constants.STRING_SEPERATOR.length()), "", 0);
		return experimentNode;
	}

	//获取类的相关数据
	private ClusterInfo getClusterInfo(Experiments nodes) {
		ClusterInfo result = new ClusterInfo();

		result.count = nodes.numberOfExperiments();
		result.center = getCenter(nodes);

		double distSum = 0.0;
		result.minInnerDist = Double.MAX_VALUE;
		result.maxInnerDist = Double.MIN_VALUE;
		for (int i = 0; i < result.count; i++) {
			double dist = measure.computeDissimilarity(result.center.getVector(), nodes.get(i).getVector());
			distSum += dist;

			result.minInnerDist = (result.minInnerDist > dist) ? dist : result.minInnerDist;
			result.maxInnerDist = (result.maxInnerDist < dist) ? dist : result.maxInnerDist;
		}
		result.avgInnerDist = distSum / result.count;

		double varianceSum = 0.0;
		for (int i = 0; i < result.count; i++) {
			double dist = measure.computeDissimilarity(result.center.getVector(), nodes.get(i).getVector());
			varianceSum +=  Math.pow(dist - result.avgInnerDist, 2);
		}
		result.innerVariance = varianceSum / result.count;

		return result;
	}

}
