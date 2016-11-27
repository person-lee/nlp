package com.lbc.nlp_algorithm.clustering.hac.core.hac;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.core.ClusterMethod;
import com.lbc.nlp_algorithm.clustering.hac.data.Clusters;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;
import com.lbc.nlp_algorithm.clustering.hac.properties.PropertiesUtil;
import com.lbc.nlp_modules.common.thread.ThreadPoolUtils;

/**
 * HierarchicalAgglomerativeCluster方法复杂度是O(n^3)。
 *
 * 这个类使用优先级队列提升算法性能，时间复杂度是O(n^2 * logn)；空间复杂度仍然是O(n^2),需要优化
 * @param <Node>
 *
 * v2. 利用simUpper进行优化，运行时间缩短为原来的1/6
 * v3. trees仅保存单向距离，本方法内存消耗减少为原来的2/3，同时总运行时间缩短为原来的4/5。其他修改:
 *     .此版本的聚类结果有所变化，原因在于merge时老版本会刷新ks到kb的距离，而本版本不刷新
 *     .强制使用多线程
 */
public class HierarchicalAgglomerativeClusterFaster implements ClusterMethod {
	private final Logger LOG = LoggerFactory.getLogger(HierarchicalAgglomerativeClusterFaster.class);

	private Experiments experiments;
    private DissimilarityMeasure dissimilarityMeasure;
    private AgglomerationMethod agglomerationMethod;
    private ClusteringBuilder clusteringBuilder;
    private Clusters finalClusters;

    //存储Trees的时候，大于simUpper的值会被直接过滤掉
    private double simUpper = PropertiesUtil.getClusterProperties().getHAC2SimUpper();
    //停止距离
    private double stopThreshold = PropertiesUtil.getClusterProperties().getHAC2StopThreshold();
    //每个分类的语料数量下限
    private int sizeThreshold = PropertiesUtil.getClusterProperties().getHAC2SizeThreshold();

    /**
     * 由于返回数值必然有序，因此不再进行比较
     */
    class Pair {
        private int cluster1;
        private int cluster2;
        private double dissimilarity;
        public final void set(final int cluster1, final int cluster2, double dissimilarity) {
            this.cluster1 = cluster1;
            this.cluster2 = cluster2;
            this.dissimilarity = dissimilarity;
        }
        public final int getLarger() {
        	return cluster1;
        }
        public final int getSmaller() {
        	return cluster2;
        }
        public final double getDissimilarity() {
        	return dissimilarity;
        }
    }

    class SimNode {
    	public float dissimilarity;
    	public int index;
    	public SimNode(float dissimilarity, int index) {
    		this.dissimilarity = dissimilarity;
    		this.index = index;
    	}
    }

	public HierarchicalAgglomerativeClusterFaster(final Experiments experiments,
			final ClusteringBuilder clusteringBuilder,
			final DissimilarityMeasure dissimilarityMeasure,
			final AgglomerationMethod agglomerationMethod,
			final double stopThreshold, final int sizeThreshold) {
		this.experiments = experiments;
		this.clusteringBuilder = clusteringBuilder;
		this.dissimilarityMeasure = dissimilarityMeasure;
		this.agglomerationMethod = agglomerationMethod;
		//只有参数为非负的时候才有效，否则使用默认值
		if (stopThreshold > 0) {
			this.stopThreshold = stopThreshold;
		}
		if (sizeThreshold > 0) {
			this.sizeThreshold = sizeThreshold;
		}
	}

	//如果是RBTree,现在只支持Inorder的升序排列
    Comparator<SimNode> nodeComparator = new Comparator<SimNode>() {
		@Override
		public int compare(SimNode o1, SimNode o2) {
			int c = new Float(o1.dissimilarity).compareTo(o2.dissimilarity);
			if (0 == c) {
				return (o1.index - o2.index);
			}
			return c;
		}
	};

	/**
	 * v3：由于ks和kb严格有序，移除部分需要排序的操作
	 */
	@Override
    public void cluster() throws IOException {
    	LOG.info("HACFaster cluster begin ..................");
    	//计算所有节点相似度的优先级队列
    	float[][] matrix = null;
    	TreeSet<SimNode>[] trees = null;

		matrix = computeDissimilarityMatrixParallel();
		trees = computeDissimilarityTreeParallel(matrix);

    	final int nObservations = experiments.numberOfExperiments();
    	int[] clusterCardinalities = new int[nObservations];
    	for (int i = 0; i < nObservations; i++) {
    		LOG.debug("clusters " + i + " desc " + experiments.get(i).getDescription());
    		clusterCardinalities[i] = experiments.get(i).getDescription().split(Constants.STRING_SEPERATOR).length;
    		if (experiments.get(i).getDescription().endsWith(Constants.STRING_SEPERATOR)) {
    			clusterCardinalities[i]--;
    		}
    	}
    	//开始聚类
    	long beginTime = System.currentTimeMillis() / 1000;
        float maxDissimilarity = (float) dissimilarityMeasure.getMaxDissimilarity();
    	//这个循环不好并行，有前后依赖关系
    	for (int a = 0; a < nObservations; a++) {
    		final Pair pair = findMostSimilarClusters(trees);
    		//开始merge,ks作为新的cluster，kb被合并进ks中
    		int ks = pair.getSmaller();
    		int kb = pair.getLarger();
    		if (-1 == ks || -1 == kb) {
    			break;
    		}
    		float d = matrix[kb][ks];
    		if (d > stopThreshold) {
    			break;
    		}

            LOG.debug("Agglomeration #" + a + ": merging clusters " + ks +
                    " (cardinality " + (clusterCardinalities[ks]) + ") and " + kb +
                    " (cardinality " + (clusterCardinalities[kb]) + ") with dissimilarity " + d);

   			merge(trees, matrix, clusterCardinalities, maxDissimilarity, nObservations, ks, kb);

    		trees[kb] = null;
    		clusterCardinalities[ks] += clusterCardinalities[kb];
    		clusteringBuilder.merge(ks, kb, d);
    	}
    	finalClusters = getClustersByClusteringBuilding();
    	long endTime = System.currentTimeMillis() / 1000;
    	LOG.info("HACFaster cluster done ......, time eclapse: " + (endTime - beginTime));
    }

    //合并相似节点
    private void merge(TreeSet<SimNode>[] trees, float[][] matrix, int[] clusterCardinalities,
    		float maxDissimilarity, int nObservations, int ks, int kb) {
		// ks节点之前的节点树中包含ks和kb节点的距离，需同时处理
    	for (int i = 0; i < ks; i++) {
    		if (null == trees[i]) {
    			continue;
    		}

    		trees[i].remove(new SimNode(matrix[ks][i], ks));
    		trees[i].remove(new SimNode(matrix[kb][i], kb));

    		//添加新的ks节点，dissimilarity是重新计算过的
    		float dissimilarity = (float) agglomerationMethod.computeDissimilarity(matrix[kb][i], matrix[ks][i],
    				matrix[kb][ks], clusterCardinalities[kb], clusterCardinalities[ks], clusterCardinalities[i]);
    		if (dissimilarity > simUpper)  {
    			dissimilarity = maxDissimilarity;
    		} else {
    			trees[i].add(new SimNode(dissimilarity, ks));
    		}

    		matrix[ks][i] = dissimilarity;
    	}
		// ks到kb之间的节点树中不包含ks节点的距离，仅需处理kb节点树
    	for (int i = ks + 1; i < kb; i++) {
    		if (null == trees[i]) {
    			continue;
    		}

    		trees[ks].remove(new SimNode(matrix[i][ks], i));
    		trees[i].remove(new SimNode(matrix[kb][i], kb));

    		//添加新的ks节点，dissimilarity是重新计算过的
    		float dissimilarity = (float) agglomerationMethod.computeDissimilarity(matrix[kb][i], matrix[i][ks],
    				matrix[kb][ks], clusterCardinalities[kb], clusterCardinalities[ks], clusterCardinalities[i]);
    		if (dissimilarity > simUpper) {
    			dissimilarity = maxDissimilarity;
    		} else {
    			trees[ks].add(new SimNode(dissimilarity, i));
    		}

    		matrix[i][ks] = dissimilarity;
    	}
		// kb节点之后的节点树中不包含ks和kb节点的距离
    	for (int i = kb + 1; i < nObservations; i++) {
    		if (null == trees[i]){
    			continue;
    		}

    		trees[ks].remove(new SimNode(matrix[i][ks], i));

    		//添加新的ks节点，dissimilarity是重新计算过的
    		float dissimilarity = (float) agglomerationMethod.computeDissimilarity(matrix[i][kb], matrix[i][ks],
    				matrix[kb][ks], clusterCardinalities[kb], clusterCardinalities[ks], clusterCardinalities[i]);
    		if (dissimilarity > simUpper) {
    			dissimilarity = maxDissimilarity;
    		} else {
    			trees[ks].add(new SimNode(dissimilarity, i));
    		}

    		matrix[i][ks] = dissimilarity;
    	}
    	// 从ks节点中移除kb节点
    	trees[ks].remove(new SimNode(matrix[kb][ks], kb));
    }

    //子线程任务
    class DissimilarityTask implements Runnable {
    	private int row;
    	private float[][] matrix;
    	private Experiments experiments;
    	private DissimilarityMeasure measure;
    	private final CountDownLatch latch;
    	
    	public DissimilarityTask(CountDownLatch latch, int row, float[][] matrix, Experiments experiments,
    			DissimilarityMeasure measure) {
    		this.row = row;
    		this.matrix = matrix;
    		this.experiments = experiments;
    		this.measure = measure;
    		this.latch = latch;
		}

		@Override
		public void run() {
			try {
				matrix[row] = new float[row + 1];
				for (int j = 0; j < row; j++) {
					float dissimilarity = (float) measure.computeDissimilarity(experiments.get(row).getVector(),
							experiments.get(j).getVector());
					if (dissimilarity > simUpper) {
						dissimilarity = (float) measure.getMaxDissimilarity();
					}
	    			matrix[row][j] = dissimilarity;
				}
			} catch (Exception e) {
				LOG.error(e.getMessage());
			} finally {
				latch.countDown();
			}
		}
    }

    //多线程版本
    private float[][] computeDissimilarityMatrixParallel(){
    	long begTime = System.currentTimeMillis() / 1000;
    	int nObservations = experiments.numberOfExperiments();
    	CountDownLatch latch = new CountDownLatch(nObservations);
		ExecutorService pool = ThreadPoolUtils.getExecutor();
    	float[][] matrix = new float[nObservations][];
    	try {
    		for (int i = 0; i < nObservations; i++) {
        		pool.execute(new DissimilarityTask(latch, i, matrix, experiments, dissimilarityMeasure));
        	}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		} finally {
			try {
				latch.await();
			} catch (Exception e2) {
				LOG.info(e2.getMessage());
			}
		}
    	
    	long endTime = System.currentTimeMillis() / 1000;
    	LOG.info("computeDissimilarityMatrixParallel done ....................... time : " + (endTime - begTime));
    	return matrix;
    }

    /**
     * v3:仅存储单向距离(小编号节点到大编号节点)，空间减少一半
     */
    class CreateTreeTask implements Runnable {
    	private TreeSet<SimNode>[] trees;
    	private float[][] matrix;
    	private int index;
    	private DissimilarityMeasure measure;
    	public CreateTreeTask(TreeSet<SimNode>[] trees, float[][] matrix, int index,
    			DissimilarityMeasure measure) {
    		this.trees = trees;
    		this.matrix = matrix;
    		this.index = index;
    		this.measure = measure;
    	}
		@Override
		public void run() {
			int count = 0;
			for (int j = index + 1; j < trees.length; j++) {
				float dissimilarity = matrix[j][index];
				if (dissimilarity == measure.getMaxDissimilarity()) {
					count++;
					continue;
				}
				SimNode node = new SimNode(dissimilarity, j);
				trees[index].add(node);
			}
			LOG.debug("filter node : " + count);
		}
    }

    //多线程版本
    private TreeSet<SimNode>[] computeDissimilarityTreeParallel(float[][] matrix) {
    	long begTime = System.currentTimeMillis() / 1000;
    	@SuppressWarnings("unchecked")
		TreeSet<SimNode>[] trees = new TreeSet[experiments.numberOfExperiments()];
    	for (int i = 0; i < trees.length; i++) {
    		trees[i] = new TreeSet<SimNode>(nodeComparator);
    	}

		try {
			ExecutorService pool = ThreadPoolUtils.getExecutor();
			List<Future> futures = Lists.newLinkedList();
			for (int i = 0; i < trees.length; i++) {
				futures.add(pool.submit(new CreateTreeTask(trees, matrix, i, dissimilarityMeasure)));
				
			}
	    	//1800s都没有计算完则直接返回
	    	for (Future future : futures) {
	    		future.get();
	    	}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
    	long endTime = System.currentTimeMillis() / 1000;
    	LOG.info("computeDissimlarityTreeParallel done ................. time : " + (endTime - begTime));
    	return trees;
    }

    /*
     * find the most similar nodes pair.
     * 时间复杂度O(n),n为节点数量
     */
    private Pair findMostSimilarClusters(final TreeSet<SimNode>[] trees) {
   		SimNode minNode = new SimNode((float) Double.MAX_VALUE, -1);
   		int k1 = -1;
    	for (int i = 0; i < trees.length; i++) {
    		//不能使用（之前已合并），则跳过
    		if (null == trees[i] || trees[i].size() <= 0) {
    			continue;
    		}
    		SimNode simNode = trees[i].first();
    		if (simNode.dissimilarity < minNode.dissimilarity) {
    			minNode = simNode;
    			k1 = i;
    		}
    	}
    	Pair pair = new Pair();
    	pair.cluster1 = minNode.index;
    	pair.cluster2 = k1;
    	pair.dissimilarity = minNode.dissimilarity;
    	return pair;
    }

    private Clusters getClustersByClusteringBuilding() {
    	//有的cluster是空的，所以需要过滤掉
    	String[] parts = clusteringBuilder.dumpClusters(sizeThreshold).split("\n");
    	int size = 0;
    	for (int i = 0; i < parts.length; i++) {
    		if (parts[i].trim().length() <= 0) {
    			continue;
    		}
    		size++;
    	}

    	Clusters clusters = new Clusters(size);
    	int index = 0;
    	for (int i = 0; i < parts.length; i++) {
    		//没有聚类结果的直接跳过
    		if (parts[i].trim().length() <= 0) {
    			continue;
    		}
    		String[] ids = parts[i].split("\\s+");
    		for (String id : ids) {
    			clusters.addExperimentNode(index, experiments.get(Integer.valueOf(id)));
    		}
    		index++;
    	}
    	return clusters;
    }

	@Override
	public void dumpFile(File file, boolean isOnline) throws IOException {
		finalClusters.dumpFile(file, isOnline);
		LOG.info("HACFaster dumpFile done .............");
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
