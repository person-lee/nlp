package com.lbc.nlp_algorithm.clustering.hac.core.hac;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.core.ClusterMethod;
import com.lbc.nlp_algorithm.clustering.hac.core.kmeans.KMeans;
import com.lbc.nlp_algorithm.clustering.hac.core.kmeans.SplitKMeans;
import com.lbc.nlp_algorithm.clustering.hac.data.Clusters;
import com.lbc.nlp_algorithm.clustering.hac.data.ExperimentNode;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;
import com.lbc.nlp_algorithm.clustering.hac.properties.PropertiesUtil;
import com.lbc.nlp_modules.common.thread.ThreadPoolUtils;

/**
 * 由于HACFaster方法占用内存过多，所以尝试结合一个快速的预先聚类方法，将原始数据分为多个精度较粗的
 * cluster,依次对这几个cluster进行HAC,再对结果进行合并
 */
public class PartitionedHAC implements ClusterMethod {
	private final Logger LOG = LoggerFactory.getLogger(PartitionedHAC.class);

	private Experiments experiments;
	private ClusteringBuilder clusteringBuilder;
	private DissimilarityMeasure measure;
	private AgglomerationMethod agglomerationMethod;
	private Clusters finalClusters;

	// 聚类截止水平
	private double stopThreshold = PropertiesUtil.getClusterProperties().getPartitionedHACStopThreshold();
	// 最终聚类的语料下限
	private int sizeThreshold = PropertiesUtil.getClusterProperties().getPartitionedHACSizeThreshold();
	// HACFaster所能支持的最大语料上限(64位8G内存)
	private int sizePerCluster = PropertiesUtil.getClusterProperties().getPartitionedHACSizePerCluster();
	// 是否对预分类后仍然超大的子类进行拆分
	private static final boolean useSplit = PropertiesUtil.getClusterProperties().getPartitionedHACUseSplit();
	// 是否对层次聚类启用多线程处理
	private static final boolean useParallel = PropertiesUtil.getClusterProperties().getPartitionedHACUseParallel();
	// 是否启用预处理
	private static final boolean usePreTreatment = PropertiesUtil.getClusterProperties().getPartitionedHACUsePreTreatment();

	public PartitionedHAC(final Experiments experiments, final ClusteringBuilder clusteringBuilder,
			final DissimilarityMeasure dissimilarityMeasure,
			final AgglomerationMethod agglomerationMethod,
			final double stopThreshold, final int sizeThreshold, final int sizePerCluster) {
		this.experiments = experiments;
		this.clusteringBuilder = clusteringBuilder;
		this.measure = dissimilarityMeasure;
		this.agglomerationMethod = agglomerationMethod;
		if (stopThreshold > 0)
			this.stopThreshold = stopThreshold;
		if (sizeThreshold > 0)
			this.sizeThreshold = sizeThreshold;
		if (sizePerCluster > 0)
			this.sizePerCluster = sizePerCluster;
	}

	@Override
	public void cluster() throws IOException {
		LOG.info("PartitionedHAC cluster begin .......... experiments size : " + experiments.numberOfExperiments());
		long beginTime = System.currentTimeMillis() / 1000;

		//预聚类：处理相同语料
		Experiments preExperiments = preTreatmentCluster(experiments);

		//粗分类
		List<Experiments> sliceExperiments = partitionRoughCluster(preExperiments);
		Collections.sort(sliceExperiments, new ExperimentsComparator());	//对sliceExperiments进行排序以便并行处理

		//层次聚类
		Experiments highLevelExperiments = runHACParallel(sliceExperiments, measure);

		//对highLevelExperiments进行二次聚类
		clusteringBuilder.init(highLevelExperiments.numberOfExperiments(), getExperimentCount(highLevelExperiments));
		ClusterMethod hac = new HierarchicalAgglomerativeClusterFaster(
				highLevelExperiments, clusteringBuilder, measure, agglomerationMethod,
				stopThreshold, sizeThreshold);
		hac.cluster();
		Clusters clusters = hac.getClusters();

		//将highlevel的节点还原成原始节点
		finalClusters = restoreClusters(clusters);
		long endTime = System.currentTimeMillis() / 1000;
		LOG.info("PartitionHAC cluster end ..... Time eclapse : " + (endTime - beginTime));
	}

	private Clusters restoreClusters(Clusters hlClusters) {
		int numberOfClusters = hlClusters.getNumberOfClusters();
		Clusters clusters = new Clusters(numberOfClusters);
		for (int i = 0; i < numberOfClusters; i++) {
			int clusterSize = hlClusters.getClusterSize(i);
			for (int j = 0; j < clusterSize; j++) {
				ExperimentNode hlNode = hlClusters.getExperimentNodeByCluster(i, j);
				String[] ids = hlNode.getDescription().split(Constants.STRING_SEPERATOR);

				for (String id : ids) {
					ExperimentNode node = experiments.get(Integer.valueOf(id));
					clusters.addExperimentNode(i, node);
				}
			}
		}

		return clusters;
	}

	private Experiments createNewExperiments(Clusters clusters) {
		Experiments newExperiments = new Experiments();
		int numberOfClusters = clusters.getNumberOfClusters();
		for (int i = 0; i < numberOfClusters; i++) {
			ExperimentNode experimentNode = getCenter(clusters, i);
			newExperiments.add(experimentNode);
		}
		return newExperiments;
	}

	//向量均值求得的中心点
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
			description.append(experimentNode.getDescription() + Constants.STRING_SEPERATOR);
		}
		center.elementDivid(clusterSize);

		ExperimentNode experimentNode = new ExperimentNode(center, description.toString(), "", 0);
		return experimentNode;
	}
	private ExperimentNode getCenterPre(List<ExperimentNode> nodes) {
		int clusterSize = nodes.size();
		Vector vector = nodes.get(0).getVector();
		Vector center = vector.clone();
		//description里面包含这个center所代表的所有点
		StringBuilder description = new StringBuilder();
		for (ExperimentNode node : nodes) {
			vector = node.getVector();
			center.elementPlus(vector);
			description.append(node.getId() + Constants.STRING_SEPERATOR);
		}
		center.elementDivid(clusterSize);

		ExperimentNode experimentNode = new ExperimentNode(center,
				description.substring(0, description.length() - Constants.STRING_SEPERATOR.length()), "", 0);
		return experimentNode;
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
		LOG.info("slice " + index + " split, total size : " + clusters.getClusterSize(index));
		int sizeOfClusters = clusters.getClusterSize(index);
		Experiments tmpSlice = new Experiments();
		for (int j = 0; j < sizeOfClusters; j++) {
			tmpSlice.add(clusters.getExperimentNodeByCluster(index, j));
		}

		ClusterMethod splitCluster = new SplitKMeans(tmpSlice, pieces, measure);
		splitCluster.cluster();
		Clusters splitClusters = splitCluster.getClusters();
		List<Experiments> splitSlices = Lists.newArrayList();
		int numberOfClusters = splitClusters.getNumberOfClusters();

		for (int i = 0; i < numberOfClusters; i++) {
			Experiments slice = new Experiments();
			slice.setSplited(true);
			sizeOfClusters = splitClusters.getClusterSize(i);
			for (int j = 0; j < sizeOfClusters; j++) {
				slice.add(splitClusters.getExperimentNodeByCluster(i, j));
			}
			LOG.info("slice " + index + ", part " + i + ", size : " + slice.numberOfExperiments());
			splitSlices.add(slice);
		}

		return splitSlices;
	}

	//预分类
	private Experiments preTreatmentCluster(Experiments inExperiments) {
		if (!usePreTreatment) {
			Experiments result = new Experiments();
			for( int i=0; i<inExperiments.numberOfExperiments();i++){
				Vector v = inExperiments.get(i).getVector();
				String d = String.valueOf(i);
				result.add(new ExperimentNode(v, d, "", 0));
			}
			return result;
		}

		LOG.info("preTreatmentCluster begin ..........");
		long beginTime = System.currentTimeMillis() / 1000;

		HashMap<String, ArrayList<ExperimentNode>> descriptions = Maps.newHashMap();
		for (int i = 0; i < inExperiments.numberOfExperiments(); i++) {
			ExperimentNode node = inExperiments.get(i);
//			if (StringUtils.isBlank(node.getTag())) {
//				//过滤掉没有TAG的语料，这些语料是因为原始问题中有换行，导致读取时被拆分成了多个语料
//				LOG.debug("preTreatmentCluster filtered " + node.getDescription());
//				continue;
//			}

			String desc = trimEnd(node.getDescription());
			if (descriptions.containsKey(desc)) {
				descriptions.get(desc).add(node);
				LOG.debug("preTreatmentCluster merged " + node.getTag() + "(" + node.getDescription() + ") into " + descriptions.get(desc).get(0).getId());
			} else {
				ArrayList<ExperimentNode> newDescription = Lists.newArrayList();
				newDescription.add(node);
				descriptions.put(desc, newDescription);
				LOG.debug("preTreatmentCluster added " + node.getTag() + "(" + node.getDescription() + ") with id " + node.getId());
			}
		}

		Iterator<Entry<String, ArrayList<ExperimentNode>>> iter = descriptions.entrySet().iterator();
		Experiments result = new Experiments();
		while (iter.hasNext()) {
			Map.Entry<String, ArrayList<ExperimentNode>> entry = iter.next();
			ArrayList<ExperimentNode> array = entry.getValue();
			result.add(getCenterPre(array));
		}

		long endTime = System.currentTimeMillis() / 1000;
		LOG.info("preTreatmentCluster end ..... experiments size : " + result.numberOfExperiments() + " Time eclapse : " + (endTime - beginTime));
		return result;
	}

	//粗分类
	private List<Experiments> partitionRoughCluster(Experiments inExperiments) throws IOException {
		int nObservations = inExperiments.numberOfExperiments();
		int numClusters = getPartationNum(nObservations, sizePerCluster);

		//若语料比拆分数据下限少，无需经过KMeans处理，直接返回
		if (numClusters == 1) {
			LOG.info("Too few experiments to part, immediatily return.");
			List<Experiments> slices = Lists.newArrayList();
			slices.add(inExperiments);
			return slices;
		}

		ClusterMethod localCluster = new KMeans(inExperiments, numClusters, measure);
		localCluster.cluster();
		Clusters clusters = localCluster.getClusters();
		List<Experiments> slices = Lists.newArrayList();
		int numberOfClusters = clusters.getNumberOfClusters();
		for (int i = 0; i < numberOfClusters; i++) {
			int sizeOfClusters = clusters.getClusterSize(i);

			//对于sizeOfClusters超过指定数据，再次将其拆分以降低内存需求
			if (useSplit && sizeOfClusters > sizePerCluster * 1.5) {
				//使用顺序拆分。因Cluster已被预分类，此处使用顺序拆分可以获得相近的中心点。也可考虑其他平均拆分的方法
				//注意：若拆分过细可能造成聚类后数据减少
				List<Experiments> sliceParts = partitionSplit1(clusters, i, getPartationNum(sizeOfClusters, (int) (sizePerCluster * 1.5)));
				slices.addAll(sliceParts);
			} else {
				Experiments slice = new Experiments();
				for (int j = 0; j < sizeOfClusters; j++) {
					slice.add(clusters.getExperimentNodeByCluster(i, j));
				}
				LOG.info("slice " + i + ", size : " + slice.numberOfExperiments());
				slices.add(slice);
			}
		}
		return slices;
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
	 * 对Experiments类进行比较排序
	 */
	class ExperimentsComparator implements Comparator<Experiments> {
		@Override
		public int compare(Experiments o1, Experiments o2) {
			return o2.numberOfExperiments() - o1.numberOfExperiments();
		}
	}

    //多线程计算每个节点最近的中心点
    private Experiments runHACParallel(List<Experiments> sliceExperiments, DissimilarityMeasure measure) {
    	long begTime = System.currentTimeMillis() / 1000;
    	Experiments highLevelExperiments = new Experiments();
    	LOG.info("computeMeanParallel begin .................");

    	//语料数量较多的任务单线程处理
    	int si;
    	for (si = 0; si < sliceExperiments.size(); si++) {
    		if (useParallel && sliceExperiments.get(si).numberOfExperiments() < sizePerCluster * 0.8) {
    			break;
    		}
			if (sliceExperiments.get(si).numberOfExperiments() < sizeThreshold) {
				LOG.info("partition cluster immediate finished ....... slice size : " + sliceExperiments.get(si).numberOfExperiments()
						+ ", part : " + si + " is to small。");
				continue;
			}
			Experiments newExperiments = RunHACParallelSingle(sliceExperiments.get(si), measure, si);
			highLevelExperiments.addAll(newExperiments);
    	}

    	//语料数量较少的任务多线程处理
    	if (useParallel && si < sliceExperiments.size()) {
			ExecutorService pool = ThreadPoolUtils.getExecutor();
			List<Future<Experiments>> futures = Lists.newLinkedList();
			for (; si < sliceExperiments.size(); si++) {
				Future<Experiments> future = pool.submit(new RunHACParallel(sliceExperiments.get(si), measure, si));
				futures.add(future);
			}

			for (int i = futures.size() - 1; i >= 0; i--) {
				try {
					if (futures.get(i).isDone()) {
						Future<Experiments> future = futures.get(i);
						highLevelExperiments.addAll(future.get());
						futures.remove(future);
					}
				} catch (InterruptedException e) {
					LOG.error("partition cluster thread error : ", e);
				} catch (ExecutionException e) {
					LOG.error("partition cluster thread error : ", e);
				}
 			}
    	}

    	long endTime = System.currentTimeMillis() / 1000;
    	LOG.info("computeMeanParallel done ................. time : " + (endTime - begTime));
    	return highLevelExperiments;
    }

    /**
     * 用于非并行计算每个节点最近的中心点的方法
     */
    private Experiments RunHACParallelSingle(Experiments slice, DissimilarityMeasure measure, int part) {
			LOG.info("partition cluster begin ....... slice size : " + slice.numberOfExperiments() + ", part : " + (part++));
			clusteringBuilder.init(slice.numberOfExperiments(), getExperimentCount(slice));

			ClusterMethod clusterMethod;
			if (slice.getSplited()) {
				//由于是拆分后的片段，减少sizeThreshold以获得相似的最终结果
				clusterMethod = new HierarchicalAgglomerativeClusterFaster(slice,
						clusteringBuilder, measure, agglomerationMethod, stopThreshold,
						(sizeThreshold < 3) ? 1 : (sizeThreshold - 2));
			} else {
				clusterMethod = new HierarchicalAgglomerativeClusterFaster(slice,
						clusteringBuilder, measure, agglomerationMethod, stopThreshold,
						sizeThreshold);
			}

			try {
				clusterMethod.cluster();
				Clusters clusters = clusterMethod.getClusters();

				//for debug
				return createNewExperiments(clusters);
			} catch (IOException e) {
				LOG.error("partition cluster error ....... part : " + (part++), e);
			}

			return null;
    }

    /**
     * 用于并行计算每个节点最近的中心点的执行类
     */
    class RunHACParallel implements Callable<Experiments> {
    	private Experiments slice;
    	private DissimilarityMeasure measure;
    	private int part;
    	private ClusteringBuilder privateClusteringBuilder;

    	public RunHACParallel(Experiments slice, DissimilarityMeasure measure, int part) {
    		this.slice = slice;
    		this.part = part;
    		this.measure = measure;
    		this.privateClusteringBuilder = clusteringBuilder.clone(slice.numberOfExperiments());
    	}

		@Override
		public Experiments call() {
			LOG.info("partition cluster begin ....... slice size : " + slice.numberOfExperiments() + ", part : " + (part++));

			ClusterMethod clusterMethod;
			privateClusteringBuilder.init(slice.numberOfExperiments(), getExperimentCount(slice));
			if (slice.getSplited()) {
				clusterMethod = new HierarchicalAgglomerativeClusterFaster(slice,
						privateClusteringBuilder, measure, agglomerationMethod, stopThreshold,
						sizeThreshold - 2);
			} else {
				clusterMethod = new HierarchicalAgglomerativeClusterFaster(slice,
						privateClusteringBuilder, measure, agglomerationMethod, stopThreshold,
						sizeThreshold);
			}

			try {
				clusterMethod.cluster();
				Clusters clusters = clusterMethod.getClusters();

				//for debug
				return createNewExperiments(clusters);
			} catch (IOException e) {
				LOG.error("partition cluster error ....... part : " + (part++), e);
			}
			return null;
		}
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
    		result[i] = experiments.get(i).getDescription().split(Constants.STRING_SEPERATOR).length;

    		LOG.debug("Experiments index " + i + ", cnt = " + result[i] + ", desc = " + experiments.get(i).getDescription());
    	}

    	return result;
    }

    /**
     * 去除语料末尾的符号以更好地匹配
     * @param str	原始语料
     * @return		处理后的语料
     */
    private String trimEnd(String str) {
    	if (StringUtils.isNotEmpty(str) && str.length() > 2) {
    		if (Constants.trimStr.indexOf(str.charAt(str.length() - 2)) > 0) {
    			LOG.debug("Trim [" + str + "] to [" + str.substring(0, str.length() - 2) + " ]");
    			return trimEnd(str.substring(0, str.length() - 2) + " ");
    		}
    	}

    	return str;
    }

	@Override
	public void dumpFile(File file, boolean isOnline) throws IOException {
		finalClusters.dumpFile(file, isOnline);
	}

	@Override
	public void eval(File file) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public Map<String, Object> getExtralResutl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Clusters getClusters() {
		return finalClusters;
	}

}
