package com.lbc.nlp_algorithm.clustering.hac.distributed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.InnerProductMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.core.ClusterMethod;
import com.lbc.nlp_algorithm.clustering.hac.core.kmeans.KMeans;
import com.lbc.nlp_algorithm.clustering.hac.core.kmeans.SplitKMeans;
import com.lbc.nlp_algorithm.clustering.hac.data.Clusters;
import com.lbc.nlp_algorithm.clustering.hac.data.ExperimentNode;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;
import com.lbc.nlp_algorithm.clustering.hac.properties.PropertiesUtil;

public class MapExperiment {
	private final Logger LOGGER = LoggerFactory.getLogger(MapExperiment.class);

	private Experiments experiments;
	private DissimilarityMeasure measure;
	private String seperator = Constants.STRING_SEPERATOR;
	private List<Experiments> result;

	// HACFaster所能支持的最大语料上限(64位8G内存)
	private static final int sizePerCluster = PropertiesUtil.getClusterProperties().getPartitionedHACSizePerCluster();
	// 是否启用预处理
	private static final boolean usePreTreatment = PropertiesUtil.getClusterProperties().getPartitionedHACUsePreTreatment();
	// 合并语料数量上限
	private static final int sizeMergeThreshold = PropertiesUtil.getClusterProperties().getMapExperimentSizeMergeThreshold();
	// 会被忽略的语料结束词
	private static final String trimStr = "、.。,，!！?？嗯啊哦呢吗呀";

	public MapExperiment(final Experiments experiments) {
		this.experiments = experiments;
		this.measure = new InnerProductMeasure();
	}

	public void execute() {
		try {
			LOGGER.info("MapExperiment begin .....");
			long beginTime = System.currentTimeMillis();

			//预聚类：处理相同语料
			Experiments preExperiments = preTreatmentCluster(experiments);

			//粗分类
			List<Experiments> listExperiments = partitionRoughCluster(preExperiments);

			//合并小分类
			Collections.sort(listExperiments, new ExperimentsComparator());
			result = mergeSmall(listExperiments);

			long endTime = System.currentTimeMillis();
			LOGGER.info("MapExperiment end ..... Time eclapse : " + (endTime - beginTime) + "ms");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Experiments> getResult() {
		return result;
	}

	/**
	 * 预分类
	 * @param inExperiments 原始语料
	 * @return	分类后的语料
	 */
	private Experiments preTreatmentCluster(Experiments inExperiments) {
		if (!usePreTreatment) {
			Experiments result = new Experiments();
			for( int i=0; i<inExperiments.numberOfExperiments();i++){
				Vector v = inExperiments.get(i).getVector();
				String d = String.valueOf(i);
				//String t = array.get(0).getTag();
				//int i = array.get(0).getId();
				result.add(new ExperimentNode(v, d, "", 0));
			}
			return result;
		}

		HashMap<String, ArrayList<ExperimentNode>> descriptions = new HashMap<String, ArrayList<ExperimentNode>>();
		for (int i = 0; i < inExperiments.numberOfExperiments(); i++) {
			ExperimentNode node = inExperiments.get(i);
			if (StringUtils.isBlank(node.getTag())) {
				//过滤掉没有TAG的语料，这些语料是因为原始问题中有换行，导致读取时被拆分成了多个语料
				LOGGER.debug("preTreatmentCluster filtered " + node.getDescription());
				continue;
			}

			String desc = trimEnd(node.getDescription());
			if (descriptions.containsKey(desc)) {
				descriptions.get(desc).add(node);
				LOGGER.debug("preTreatmentCluster merged " + node.getTag() + "(" + node.getDescription() + ") into " + descriptions.get(desc).get(0).getId());
			} else {
				ArrayList<ExperimentNode> newDescription = new ArrayList<ExperimentNode>();
				newDescription.add(node);
				descriptions.put(desc, newDescription);
				LOGGER.debug("preTreatmentCluster added " + node.getTag() + "(" + node.getDescription() + ") with id " + node.getId());
			}
		}

		Iterator<Entry<String, ArrayList<ExperimentNode>>> iter = descriptions.entrySet().iterator();
		Experiments result = new Experiments();
		while (iter.hasNext()) {
			Map.Entry<String, ArrayList<ExperimentNode>> entry = iter.next();
			ArrayList<ExperimentNode> array = entry.getValue();
			result.add(getCenterPre(array));
		}

		return result;
	}

	/**
	 * 粗分类
	 * @param inExperiments	待分类的语料
	 * @return	分类结果
	 * @throws IOException
	 */
	private List<Experiments> partitionRoughCluster(Experiments inExperiments) throws IOException {
		int nObservations = inExperiments.numberOfExperiments();
		int numClusters = getPartationNum(nObservations, sizePerCluster);

		//若语料比拆分数据下限少，无需经过KMeans处理，直接返回
		if (numClusters == 1) {
			LOGGER.info("Too few experiments to part, immediatily return.");
			List<Experiments> slices = new ArrayList<Experiments>();
			slices.add(inExperiments);
			return slices;
		}

		ClusterMethod localCluster = new KMeans(inExperiments, numClusters, measure);
		localCluster.cluster();
		Clusters clusters = localCluster.getClusters();
		List<Experiments> slices = new ArrayList<Experiments>();
		int numberOfClusters = clusters.getNumberOfClusters();
		for (int i = 0; i < numberOfClusters; i++) {
			int sizeOfClusters = clusters.getClusterSize(i);

			//对于sizeOfClusters超过指定数据，再次将其拆分以降低内存需求
			if (sizeOfClusters > sizePerCluster * 1.5) {
				//使用顺序拆分。因Cluster已被预分类，此处使用顺序拆分可以获得相近的中心点。也可考虑其他平均拆分的方法
				//注意：若拆分过细可能造成聚类后数据减少
				List<Experiments> sliceParts = partitionSplit1(clusters, i, getPartationNum(sizeOfClusters, (int) (sizePerCluster * 1.5)));
				slices.addAll(sliceParts);
			} else {
				Experiments slice = new Experiments();
				for (int j = 0; j < sizeOfClusters; j++) {
					slice.add(clusters.getExperimentNodeByCluster(i, j));
				}
				LOGGER.info("slice " + i + ", size : " + slice.numberOfExperiments());
				slices.add(slice);
			}
		}
		return slices;
	}

	/**
	 * 合并较小的语料
	 * @param inExperiments	原始语料
	 * @return				合并后的语料
	 */
	private List<Experiments> mergeSmall(List<Experiments> inExperiments) {
		if (CollectionUtils.isEmpty(inExperiments)) {
			return inExperiments;
		}

		List<Experiments> outExperiments = new ArrayList<Experiments>();
		Experiments lastExperiments = null;
		for (Experiments experiments : inExperiments) {
			if (experiments.numberOfExperiments() > sizeMergeThreshold) {
				if (null != lastExperiments) {
					outExperiments.add(lastExperiments);
				}
				outExperiments.add(experiments);
				continue;
			}

			if (null != lastExperiments) {
				lastExperiments.addAll(experiments);
				if (lastExperiments.numberOfExperiments() > sizeMergeThreshold) {
					outExperiments.add(lastExperiments);
					lastExperiments = null;
				}
			} else {
				lastExperiments = experiments;
			}
		}
		if (null != lastExperiments) {
			outExperiments.add(lastExperiments);
		}

		return outExperiments;
	}

	/**
	 * 获取拆分成的数量
	 * @param total	总数
	 * @param limit	每份最大数量
	 * @return		份数
	 */
	private int getPartationNum(int total, int limit) {
		if (total % limit == 0) {
			return total / limit;
		} else {
			return total / limit + 1;
		}
	}

	/**
	 * 切分指定的experiments
	 * @param clusters	集群
	 * @param index		在集群中的编号
	 * @param pieces	切分的份数
	 * @return			切分后的数组
	 * @throws IOException
	 */
	private List<Experiments> partitionSplit1(Clusters clusters, int index, int pieces) throws IOException {
		LOGGER.info("slice " + index + " split, total size : " + clusters.getClusterSize(index));
		int sizeOfClusters = clusters.getClusterSize(index);
		Experiments tmpSlice = new Experiments();
		for (int j = 0; j < sizeOfClusters; j++) {
			tmpSlice.add(clusters.getExperimentNodeByCluster(index, j));
		}

		ClusterMethod splitCluster = new SplitKMeans(tmpSlice, pieces, measure);
		splitCluster.cluster();
		Clusters splitClusters = splitCluster.getClusters();
		List<Experiments> splitSlices = new ArrayList<Experiments>();
		int numberOfClusters = splitClusters.getNumberOfClusters();

		for (int i = 0; i < numberOfClusters; i++) {
			Experiments slice = new Experiments();
			slice.setSplited(true);
			sizeOfClusters = splitClusters.getClusterSize(i);
			for (int j = 0; j < sizeOfClusters; j++) {
				slice.add(splitClusters.getExperimentNodeByCluster(i, j));
			}
			LOGGER.info("slice " + index + ", part " + i + ", size : " + slice.numberOfExperiments());
			splitSlices.add(slice);
		}

		return splitSlices;
	}

	/**
	 * 在粗分类中获取中心
	 * @param nodes	粗分类语料集
	 * @return	中心
	 */
	private ExperimentNode getCenterPre(List<ExperimentNode> nodes) {
		int clusterSize = nodes.size();
		Vector vector = nodes.get(0).getVector();
		Vector center = vector.clone();
		//description里面包含这个center所代表的所有点
		StringBuilder description = new StringBuilder();
		for (ExperimentNode node : nodes) {
			vector = node.getVector();
			center.elementPlus(vector);
			description.append(node.getId() + seperator);
		}
		center.elementDivid(clusterSize);

		ExperimentNode experimentNode = new ExperimentNode(center,
				description.substring(0, description.length() - seperator.length()), "", 0);
		return experimentNode;
	}

    /**
     * 去除语料末尾的符号以更好地匹配
     * @param str	原始语料
     * @return		处理后的语料
     */
    private String trimEnd(String str) {
    	if (StringUtils.isNotEmpty(str) && str.length() > 2) {
    		if (trimStr.indexOf(str.charAt(str.length() - 2)) > 0) {
    			LOGGER.debug("Trim [" + str + "] to [" + str.substring(0, str.length() - 2) + " ]");
    			return trimEnd(str.substring(0, str.length() - 2) + " ");
    		}
    	}

    	return str;
    }

	/**
	 * 对Experiments类进行比较排序
	 * @author cdlinjianghua
	 */
	class ExperimentsComparator implements Comparator<Experiments> {
		@Override
		public int compare(Experiments o1, Experiments o2) {
			return o1.numberOfExperiments() - o2.numberOfExperiments();
		}
	}
}
