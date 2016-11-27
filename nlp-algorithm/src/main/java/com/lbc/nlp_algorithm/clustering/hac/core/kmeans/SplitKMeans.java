package com.lbc.nlp_algorithm.clustering.hac.core.kmeans;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.core.ClusterMethod;
import com.lbc.nlp_algorithm.clustering.hac.data.Clusters;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;

/**
 * This is based on mallet's KMeans code
 * This modify version will have similar cluster in sizey
 */
public class SplitKMeans implements ClusterMethod {
	private final Logger LOG = LoggerFactory.getLogger(SplitKMeans.class);

	private Experiments experiments;
	private DissimilarityMeasure measure;
	private int numClusters;
	private Clusters finalClusters;

	class SimNode {
		private int index;
		private float dist;

		public SimNode(int index, float dist){
			this.index = index;
			this.dist = dist;
		}
	}

	//如果是RBTree,现在只支持Inorder的升序排列
    Comparator<SimNode> nodeComparator = new Comparator<SimNode>() {
		@Override
		public int compare(SimNode o1, SimNode o2) {
			int c = new Float(o1.dist).compareTo(o2.dist);
			if (0 == c) {
				return (o1.index - o2.index);
			}
			return c;
		}
	};

	//emptyAction暂时未处理
	public SplitKMeans(Experiments experiments, int numClusters, DissimilarityMeasure measure) {
		this.experiments = experiments;
		this.numClusters = numClusters;
		this.measure = measure;
	}

    @Override
	public void cluster() {
    	LOG.info("Split K-means cluster begin ............................");
    	long beginTime = System.currentTimeMillis();
    	int nObservations = experiments.numberOfExperiments();
    	int[] clusterLabels = new int[nObservations];
    	Vector mean = getMean();

    	//先计算出所有点距离中心点的距离
    	TreeSet<SimNode> trees = new TreeSet<SimNode>(nodeComparator);
		for (int j = 0; j < nObservations; j++) {
			float dist = (float) measure.computeDissimilarity(experiments.get(j).getVector(), mean);
			trees.add(new SimNode(j, dist));
		}

    	//再根据距离将点划分到不同的分类
		int label = 0;
		int numPerCluster = nObservations / numClusters + 1;
		int count = 0;
		while (!trees.isEmpty()) {
			clusterLabels[trees.pollFirst().index] = label;
			if (++count == numPerCluster) {
				label++;
				count = 0;
			}
		}

    	finalClusters = getClustersByClusterLabels(clusterLabels);
    	long endTime = System.currentTimeMillis();
    	LOG.info("Split K-means cluster end ......, time eclapse: " + ((endTime - beginTime) / 1000));
    }

    private Clusters getClustersByClusterLabels(int[] clusterLabels) {
    	Clusters clusters = new Clusters(numClusters);
    	for (int i = 0; i < clusterLabels.length; i++) {
    		clusters.addExperimentNode(clusterLabels[i], experiments.get(i));
    	}
    	return clusters;
    }

    private Vector getMean() {
    	Vector v = experiments.get(0).getVector();
    	Vector mean = v.clone();
    	mean.resetValues();
    	int count = 0;
    	for (int i = 0; i < experiments.numberOfExperiments(); i++) {
    		Vector vector = experiments.get(i).getVector();
    		mean.elementPlus(vector);
    		count++;
    	}
    	mean.elementDivid(count);
    	return mean;
    }

	@Override
	public void dumpFile(File file, boolean isOnline) throws IOException {
		finalClusters.dumpFile(file, isOnline);
		LOG.info("Split K-means cluster dumpFile done .............");
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
