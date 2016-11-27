package com.lbc.nlp_algorithm.clustering.hac.core.kmeans;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.core.ClusterMethod;
import com.lbc.nlp_algorithm.clustering.hac.data.Clusters;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;
import com.lbc.nlp_algorithm.clustering.hac.properties.PropertiesUtil;
import com.lbc.nlp_modules.common.thread.ThreadPoolUtils;

/**
 * This is based on mallet's KMeans code
 * v2：引入并行计算，运行时间缩短为原来的1/2。增大线程可能对CPU造成巨大压力
 */
public class KMeans implements ClusterMethod {
	private final Logger LOG = LoggerFactory.getLogger(KMeans.class);
	
	private static CountDownLatch latch;

	// Stop after movement of means is less than this
	private static final double MEANS_TOLERANCE = PropertiesUtil.getClusterProperties().getKmeansTolerance();

	// Maximum number of iterations
	private static final int MAX_ITER = PropertiesUtil.getClusterProperties().getKmeansMaxIter();

	// Minimum fraction of points that move
	private static final double POINTS_TOLERANCE = PropertiesUtil.getClusterProperties().getKmeansPointsTolerance();

	// Maximum size of parallel computation
	private static final int PARALLEL_SIZE = PropertiesUtil.getClusterProperties().getKmeansParallelSize();

	private Experiments experiments;
	private DissimilarityMeasure measure;
	private int numClusters;
	private Clusters finalClusters;
	//此变量提出并修改为原子类型以适应多线程改造
	private AtomicInteger deltaPoints;

	//emptyAction暂时未处理
	public KMeans(Experiments experiments, int numClusters, DissimilarityMeasure measure) {
		this.experiments = experiments;
		this.numClusters = numClusters;
		this.measure = measure;
	}

    @Override
	public void cluster() {
    	LOG.info("K-means cluster begin ............................");
    	long beginTime = System.currentTimeMillis();
    	List<Vector> clusterMeans = initializeMeansSample();
    	int nObservations = experiments.numberOfExperiments();
    	double deltaMeans = Double.MAX_VALUE;
    	deltaPoints = new AtomicInteger(nObservations);
    	int[] clusterLabels = new int[nObservations];
    	for (int iteration = 0; iteration < MAX_ITER; iteration++) {
    		//满足任意一个迭代终止条件
    		if (deltaMeans < MEANS_TOLERANCE || deltaPoints.get() < nObservations * POINTS_TOLERANCE) {
    			break;
    		}
    		deltaPoints.set(0);

    		//将每个点归类到离它最近的中心点
    		computeMeanParallel(clusterMeans, clusterLabels, measure);

    		deltaMeans = 0.0;
    		for (int i = 0; i < clusterMeans.size(); i++) {
    			Vector newMean = getMean(i, clusterLabels);
    			deltaMeans += measure.computeDissimilarity(clusterMeans.get(i),
    					newMean);
    			clusterMeans.set(i, newMean);
    		}

    		LOG.info("Iter " + iteration + " deltaMeans = " + deltaMeans);
    	}
    	finalClusters = getClustersByClusterLabels(clusterLabels);
    	long endTime = System.currentTimeMillis();
    	LOG.info("K-means cluster end ......, time eclapse: " + ((endTime - beginTime) / 1000));
    }

    private Vector getMean(int clusterId, int[] clusterLabels) {
    	int nObservations = clusterLabels.length;
    	Vector v = experiments.get(0).getVector();
    	Vector mean = v.clone();
    	mean.resetValues();
    	int count = 0;
    	for (int i = 0; i < nObservations; i++) {
    		if (clusterLabels[i] != clusterId) {
    			continue;
    		}
    		Vector vector = experiments.get(i).getVector();
    		mean.elementPlus(vector);
    		count++;
    	}
    	mean.elementDivid(count);
    	return mean;
    }

    /**
     * 初始化聚类中心
     * @return
     */
    private List<Vector> initializeMeansSample() {
    	List<Vector> clusterMeans = Lists.newArrayList();
    	Set<Integer> usedIndex = Sets.newHashSet();
    	int nObservations = experiments.numberOfExperiments();
    	for (int i = 0; i < numClusters; i++) {
    		double max = 0.0;
    		int selected = 0;
    		for (int j = 0; j < nObservations; j++) {
    			if (usedIndex.contains(j))
    				continue;
    			double min = Double.MAX_VALUE;
    			for (int k = 0; k < clusterMeans.size(); k++) {
    				double dist = measure.computeDissimilarity(experiments.get(j).getVector(),
    						clusterMeans.get(k));
    				min = Math.min(min, dist);
    			}
    			if (min > max) {
    				selected = j;
    				max = min;
    			}
    		}
    		usedIndex.add(selected);
    		clusterMeans.add(experiments.get(selected).getVector());
    	}
    	return clusterMeans;
    }

    private Clusters getClustersByClusterLabels(int[] clusterLabels) {
    	Clusters clusters = new Clusters(numClusters);
    	for (int i = 0; i < clusterLabels.length; i++) {
    		clusters.addExperimentNode(clusterLabels[i], experiments.get(i));
    	}
    	return clusters;
    }

    //多线程计算每个节点最近的中心点
    private void computeMeanParallel(List<Vector> clusterMeans, int[] clusterLabels, DissimilarityMeasure measure) {
    	long begTime = System.currentTimeMillis() / 1000;

    	latch = new CountDownLatch(PARALLEL_SIZE);
		ExecutorService pool = ThreadPoolUtils.getExecutor();
		
		try {
			for (int i = 0; i < PARALLEL_SIZE; i++) {
				pool.execute(new CalculateMeanTask(clusterMeans, clusterLabels, measure, i, latch));
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		} finally {
			try {
				latch.await();
			} catch (Exception e2) {
				LOG.error(e2.getMessage());
			}
		}

    	long endTime = System.currentTimeMillis() / 1000;
    	LOG.info("computeMeanParallel done ................. time : " + (endTime - begTime));
    }

    /**
     * 用于并行计算每个节点最近的中心点的执行类
     */
    class CalculateMeanTask implements Runnable {
    	private int nObservations = experiments.numberOfExperiments();
    	private int index;	//本线程所计算的节点基数，从0开始
    	private int[] clusterLabels;
    	private DissimilarityMeasure measure;
    	private CountDownLatch latch;
    	private List<Vector> clusterMeans;

    	public CalculateMeanTask(List<Vector> clusterMeans, int[] clusterLabels, DissimilarityMeasure measure, int index, CountDownLatch latch) {
    		this.clusterLabels = clusterLabels;
    		this.index = index;
    		this.measure = measure;
    		this.latch = latch;
    		this.clusterMeans = clusterMeans;
    	}

		@Override
		public void run() {
    		try {
    			for (int i = index; i < nObservations; i = i + PARALLEL_SIZE) {
        			double min = Double.MAX_VALUE;
        			int newClusterId = 0;
        			for (int j = 0; j < clusterMeans.size(); j++) {
        				double dist = measure.computeDissimilarity(experiments.get(i).getVector(),
        						clusterMeans.get(j));
        				if (dist < min) {
        					min = dist;
        					newClusterId = j;
        				}
        			}
        			if (clusterLabels[i] != newClusterId) {
        				clusterLabels[i] = newClusterId;
        				deltaPoints.incrementAndGet();
        			}
        		}
			} catch (Exception e) {
				LOG.error(e.getMessage());
			} finally {
				latch.countDown();
			}
		}
    }

	@Override
	public void dumpFile(File file, boolean isOnline) throws IOException {
		finalClusters.dumpFile(file, isOnline);
		LOG.info("K-means cluster dumpFile done .............");
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
