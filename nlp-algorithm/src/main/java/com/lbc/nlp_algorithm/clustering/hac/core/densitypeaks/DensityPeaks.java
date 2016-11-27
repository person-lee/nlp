package com.lbc.nlp_algorithm.clustering.hac.core.densitypeaks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.matrix.Matrix;
import com.lbc.nlp_algorithm.clustering.hac.common.matrix.SymmetricDenseMatrix;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.core.ClusterMethod;
import com.lbc.nlp_algorithm.clustering.hac.data.Clusters;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;

/**
 *
 */
public class DensityPeaks implements ClusterMethod {
	private final Logger LOG = LoggerFactory.getLogger(DensityPeaks.class);

	//距离衡量方法
	private DissimilarityMeasure measure;
	//dc截断距离,用于计算点的密度(如果用于文本聚类,InnerProductMeasure，此值建议在0.3-0.5之间)
	private double dc = 0.35;
	private double ignore = dc * 2; // dc * 2
	//计算密度的kernel 
	private DensityKernel kernel;
	//聚类结果
	private DPClusterNode[] clusterResult;
	//实验数据
	private Experiments experiments;
	//密度数据
	private DPClusterNode[] pDensity;
	//是否自己查找截断距离(按照经验值，选择的dc能保证所有点的平均neighbor的数量在10-100之间)
	//这个值其实极度依赖于待聚类数据分布，dc基本上要接近于每个cluster的数据量的下界最好.dc太小，则
	//选出的cluster中心点过多，dc太大，则选出的cluster中心点太少.所以上面提到的10-100其实
	//是跟我们具体的文本聚类场景相关的，很多文本类别的数据量下界基本在10左右.当然如果你的待聚类数据
	//量翻倍了，那这个neighbor值可能也要随着增长
	private double percent = -1.0; //如果percent>0，表示需要自动查找截断距离
	private double densityThreshold = 10;
	private double deltaThreshold = 0.42;
	//对每个点归类的时候，是更倾向于spherical的形状，还是倾向于用density的任意形状.
	//基于spherical的方法，会限制一个阈值，阈值越小，准确率也会越高
	//基于density的方法，也最好有一个阈值，在这个阈值之内的点才会合并，否则，如果中心点选得不好，这种
	//方法会引入很多的噪声数据，为后面查找噪声点增加难度
	private boolean preferSpherical = true;
	//是否要输出delta-density坐标图
	//private boolean isCoordinate = false;
	
	//使用percent来自动查找dc，当然，两个threshold还是需要人工来确定的
	public DensityPeaks(Experiments experiments, DissimilarityMeasure measure, DensityKernel kernel,
			double densityThreshold, double deltaThreshold, double percent) {
		if (densityThreshold > 0 && deltaThreshold > 0) {
			this.densityThreshold = densityThreshold;
			this.deltaThreshold = deltaThreshold;
		}
		if (percent > 0)
			this.percent = percent;
		this.experiments = experiments;
		this.measure = measure;
		this.kernel = kernel;
	}

	@Override
	public void cluster() throws IOException {
		LOG.info("DensityPeaks cluster begin ......");
		Matrix matrix = computeDissimilarityMatrix(experiments);

		//double[][] matrix = computeDissimilarityMatrix(experiments);
		//是否需要用步长迭代的方法来确定dc的值呢，dc的值太大或者太小都不好，但是这个dc是依赖你的距离度量
		//方法的，所以用步长的方法，也需要根据场景来确定一个大致的范围
		if (percent > 0.0) {
			this.dc = computeDc(matrix);
			LOG.info("auto matically find dc = " + dc);
		}
		//pDensity是按密度排序的
		pDensity = computeDensity(matrix);
		computeDelta(pDensity, matrix);
		int sum = 0;
		for (int i = 0; i < pDensity.length; i++) {
			sum += pDensity[i].density;
		}
		LOG.info("average density: " + (sum / (double)pDensity.length));
		Set<Integer> centers = findCenters(pDensity);
		//draw(pDensity);
		Set<String> nameSet = new HashSet<String>();
		for (Integer center : centers) {
			LOG.info(experiments.get(center).getDescription() + "##" + experiments.get(center).getTag());
			nameSet.add(experiments.get(center).getTag());
		}
		LOG.info("centers size: " + centers.size() + ", unduplicate:" + nameSet.size());
		
		//开始归类部分
		int id = 0;
		for (int i = 0; i < pDensity.length; i++) {
			if (centers.contains(pDensity[i].index)) {
				pDensity[i].clusterId = id++;
				pDensity[i].centerId = pDensity[i].index;
			}
		}

		double[] densityUpBound = new double[centers.size()];
		for (int i = 0; i < densityUpBound.length; i++)
			densityUpBound[i] = 0.0;
		
		//cluster的归类记录在pCluster里,按语料Index排序的数组
		DPClusterNode[] pCluster = pDensity.clone();
		Arrays.sort(pCluster, comparatorId);
		
		if (true == preferSpherical) {
			sphericalCluster(pDensity, pCluster, matrix, centers);
		} else {
			arbitraryCluster(pDensity, pCluster, matrix);
		}

		//先标出来一部分离群点
		/* 效果提升不明显，可以不用这一步
		int count = 0;
		for (int i = 0; i < pDensity.length; i++) {
			if (pDensity[i].density < 1.5 && pDensity[i].delta > deltaThreshold) {
				pDensity[i].clusterId = -1;
				pCluster[pDensity[i].index].clusterId = -1;
				count++;
			}
		}
		System.out.println("boarder 1: " + count);
		*/
		//计算每个cluster的平均密度上界，用于后续计算离群点
		//TODO 平均密度上界的值普遍偏大，用这个值会导致大量的数据被判定为离群点，想想怎么优化一下
		int count = 0;
		for (int i = 0; i < pCluster.length; i++) {
			for (int j = 0; j < pCluster.length; j++) {
				Double value = matrix.get(i, j);
				if (null == value)
					value = measure.getMaxDissimilarity();
				if (pCluster[i].clusterId != pCluster[j].clusterId && value < pCluster[j].delta
						&& value < dc) {
				//if (pCluster[i].clusterId != pCluster[j].clusterId
				//		&& matrix[i][j] < dc) {
					//如果这两个点不属于同一个cluster,并且它们之间的距离小于dc
					/*
					double density = pCluster[i].density;
					int iClusterId = pCluster[i].clusterId;
					if (-1 == iClusterId)
						continue;
					if (density > densityUpBound[iClusterId])
						densityUpBound[iClusterId] = density;
						*/

					int iClusterId = pCluster[i].clusterId;
					int jClusterId = pCluster[j].clusterId;
					if (-1 == iClusterId || -1 == jClusterId)
						continue;
					double avgDensity = 0.5 * (pCluster[i].density + pCluster[j].density);
					densityUpBound[iClusterId] = Math.max(avgDensity, densityUpBound[iClusterId]);
					densityUpBound[jClusterId] = Math.max(avgDensity, densityUpBound[jClusterId]);
					count++;
				}
			}
		}
		LOG.info("probably halo count : " + count);

		//标示halo节点
		//如果true==preferSpherical，则这里可以不用表示halo节点，只要deltaThreshold设置得合理
		/*
		for (int i = 0; i < pCluster.length; i++) {
			int clusterId = pCluster[i].clusterId;
			if (-1 == clusterId)
				continue;
			if (pCluster[i].density < densityUpBound[clusterId] * 0.05)
				pCluster[i].halo = true;
		}
		*/
		this.clusterResult = pCluster;
		LOG.info("DensityPeaks cluster done ......");
	}
	
	/**
	 * 画出delta-density的二维坐标图，通过人工参与的方式来选定较优的阈值
	 * @param pDensity
	 */
	/*
	private void draw(DPClusterNode[] pDensity) {
		TestGraphic graphic = new TestGraphic("");
		XYSeries xyseries = new XYSeries("data");
		for (int i = 0; i < pDensity.length; i++)
			xyseries.add(pDensity[i].density, pDensity[i].deltaValue);
		XYSeriesCollection xyseriescollection = new XYSeriesCollection();
		xyseriescollection.addSeries(xyseries);
		graphic.draw(xyseriescollection);
	}
	*/
	
	//找到聚类中心点,使用阈值进行聚类中心点选择。这样选出来的聚类中心点数量是不确定的
	//可以考虑换一种方法，固定聚类中心的个数，这样更可控一些
	private Set<Integer> findCenters(DPClusterNode[] pDensity) {
		Set<Integer> centers = new HashSet<Integer>();
		for (int i = 0; i < pDensity.length; i++) {
			if (pDensity[i].deltaValue > deltaThreshold && pDensity[i].density > densityThreshold)
				centers.add(pDensity[i].index);
		}
		return centers;
	}
	
	//index这个点的delta。deltai是这样一个点,它的密度比i大，但是距离i最近
	private void computeDelta(DPClusterNode[] pDensity, Matrix dissimilarityMatrix) {
		double maxMin = 0.0;
		for (int i = 1; i < pDensity.length; i++) {
			double min = Integer.MAX_VALUE;
			int minIndex = i;
			for (int j = 0; j < i; j++) {
				Double dissimilarity = dissimilarityMatrix.get(pDensity[i].index, pDensity[j].index);
				if (null == dissimilarity)
					dissimilarity = measure.getMaxDissimilarity();
				if (dissimilarity < min) {
					min = dissimilarity;
					minIndex = pDensity[j].index;
				}
			}
			pDensity[i].delta = minIndex;
			pDensity[i].deltaValue = min;
			//从最小值里面找到一个最大值
			if (min > maxMin)
				maxMin = min;
		}

		//TODO pDensity[0]要单独处理,距离设置为所有点的deltaValue最大的值
		pDensity[0].delta = 0;
		pDensity[0].deltaValue = maxMin;
	}
	/*
	private void computeDelta(DPClusterNode[] pDensity, double[][] dissimilarityMatrix) {
		double maxMin = 0.0;
		for (int i = 1; i < pDensity.length; i++) {
			double min = Integer.MAX_VALUE;
			int minIndex = i;
			for (int j = 0; j < i; j++) {
				double dissimilarity = dissimilarityMatrix[pDensity[i].index][pDensity[j].index];
				if (dissimilarity < min) {
					min = dissimilarity;
					minIndex = pDensity[j].index;
				}
			}
			pDensity[i].delta = minIndex;
			pDensity[i].deltaValue = min;
			//从最小值里面找到一个最大值
			if (min > maxMin)
				maxMin = min;
		}

		//TODO pDensity[0]要单独处理,距离设置为所有点的deltaValue最大的值
		pDensity[0].delta = 0;
		pDensity[0].deltaValue = maxMin;
	}
	*/
	
	/**
	 * 找到距离最近，且距离小于deltaThreshold的center，并归到这个center所属的类别
	 * @param pDensity
	 * @param pCluster
	 * @param matrix
	 * @param centers
	 */
	private void sphericalCluster(DPClusterNode[] pDensity, DPClusterNode[] pCluster, 
			Matrix matrix, Set<Integer> centers) {
		for (int i = 0; i < pDensity.length; i++) {
			if (-1 != pDensity[i].clusterId) //已经归类，跳过
				continue;
			double min = Double.MAX_VALUE; 
			for (Integer center : centers) {
				//center的密度必须比点i的密度大，并且两者的距离小于deltaThreshold
				if (pCluster[center].density < pDensity[i].density)
					continue;
				Double dissimilarity = matrix.get(center, pDensity[i].index);
				if (null == dissimilarity)
					dissimilarity = measure.getMaxDissimilarity();
				if (dissimilarity > deltaThreshold)
					continue;
				int index = pDensity[i].index;
				if (dissimilarity < min) {
					min = dissimilarity;
					pDensity[i].clusterId = pCluster[index].clusterId = pCluster[center].clusterId;
					pDensity[i].centerId = pCluster[index].centerId = pCluster[center].centerId;
				}
			}
		}
	}
	/*
	private void sphericalCluster(DPClusterNode[] pDensity, DPClusterNode[] pCluster, 
			double[][] matrix, Set<Integer> centers) {
		for (int i = 0; i < pDensity.length; i++) {
			if (-1 != pDensity[i].clusterId) //已经归类，跳过
				continue;
			double min = Double.MAX_VALUE; 
			for (Integer center : centers) {
				//center的密度必须比点i的密度大，并且两者的距离小于deltaThreshold
				if (pCluster[center].density < pDensity[i].density)
					continue;
				double dissimilarity = matrix[center][pDensity[i].index];
				if (dissimilarity > deltaThreshold)
					continue;
				int index = pDensity[i].index;
				if (dissimilarity < min) {
					min = dissimilarity;
					pDensity[i].clusterId = pCluster[index].clusterId = pCluster[center].clusterId;
					pDensity[i].centerId = pCluster[index].centerId = pCluster[center].centerId;
				}
			}
		}
	}
	*/
	
	/**
	 * 基于密度的任意形状的聚类，这个最好也限制一个距离(这里用的截断距离)，小于此距离的才进行归类
	 * 对每个点进行归类，归类的操作顺序是按照密度从大到小的顺序来的。这样进行归类，主要是靠密度的方法，
	 * 如果想要准确率更高，可以尝试设定阈值，按圆的形状来聚类
	 * @param pDensity 按density排序的节点数组
	 * @param pCluster 按index排序的节点数组
	 */
	private void arbitraryCluster(DPClusterNode[] pDensity, DPClusterNode[] pCluster,
			Matrix matrix) {
		for (int i = 0; i < pDensity.length; i++) {
			DPClusterNode nodeDensity = pDensity[i];
			int index = nodeDensity.index;
			int delta = nodeDensity.delta;
			if (matrix.get(index, delta) > dc)
				continue;
			if (-1 == pDensity[i].clusterId) {
				DPClusterNode nodeIndex = pCluster[delta];
				pDensity[i].clusterId = pCluster[index].clusterId = nodeIndex.clusterId;
				pDensity[i].centerId = pCluster[index].centerId = nodeIndex.centerId;
			}
		}
	}
	/*
	private void arbitraryCluster(DPClusterNode[] pDensity, DPClusterNode[] pCluster,
			double[][] matrix) {
		for (int i = 0; i < pDensity.length; i++) {
			DPClusterNode nodeDensity = pDensity[i];
			int index = nodeDensity.index;
			int delta = nodeDensity.delta;
			if (matrix[index][delta] > dc)
				continue;
			if (-1 == pDensity[i].clusterId) {
				DPClusterNode nodeIndex = pCluster[delta];
				pDensity[i].clusterId = pCluster[index].clusterId = nodeIndex.clusterId;
				pDensity[i].centerId = pCluster[index].centerId = nodeIndex.centerId;
			}
		}
	}
	*/
	
	private double computeDc(Matrix matrix) {
		int size = (int) (matrix.getNumberOfRows() * (matrix.getNumberOfCols() - 1) * percent);
		int numberOfRows = matrix.getNumberOfRows();
		PriorityQueue<Double> pq = new PriorityQueue<Double>(1, comparatorDouble);
		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < i; j++) {
				Double value = matrix.get(i, j);
				if (null == value)
					value = measure.getMaxDissimilarity();
				if (pq.size() < size)
					pq.add(value);
				else {
					double max = pq.peek();
					if (value < max) {
						pq.poll();
						pq.add(value);
					}
				}
			}
		}
		return pq.peek();
	}
	/*
	private double computeDc(double[][] matrix) {
		int size = (int) (matrix.length * (matrix.length - 1) * percent);
		PriorityQueue<Double> pq = new PriorityQueue<Double>(1, comparatorDouble);
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < i; j++) {
				double value = matrix[i][j];
				if (pq.size() < size)
					pq.add(matrix[i][j]);
				else {
					double max = pq.peek();
					if (value < max) {
						pq.poll();
						pq.add(value);
					}
				}
			}
		}
		return pq.peek();
	}
	*/
	
	//计算dissimilarity矩阵
	private Matrix computeDissimilarityMatrix(Experiments experiments) {
		int nObservations = experiments.numberOfExperiments();
		Matrix matrix = new SymmetricDenseMatrix(nObservations, nObservations);
		for (int i = 0; i < nObservations; i++) {
			System.out.println("computeMatrix " + i);
			for (int j = 0; j < i; j++) {
				Vector vi = experiments.get(i).getVector();
				Vector vj = experiments.get(j).getVector();
				double dissimilarity = measure.computeDissimilarity(vi, vj);
				if (dissimilarity > ignore)
					continue;
				matrix.set(i, j, dissimilarity);
			}
		}
		return matrix;
	}
	/*
	private double[][] computeDissimilarityMatrix(Experiments experiments) {
		int nObservations = experiments.numberOfExperiments();
		double[][] matrix = new double[nObservations][nObservations];
		for (int i = 0; i < nObservations; i++) {
			System.out.println(i);
			for (int j = 0; j <= i; j++) {
				Vector vi = experiments.get(i).getVector();
				Vector vj = experiments.get(j).getVector();
				matrix[i][j] = matrix[j][i] = measure.computeDissimilarity(vi, vj);
			}
		}
		LOG.info("computeDissimilarityMatrix done ......");
		return matrix;
	}
	*/
	
	//计算每个点的密度，并按密度从大到小排序
	private DPClusterNode[] computeDensity(Matrix dissimialrityMatrix) {
		int nObservations = dissimialrityMatrix.getNumberOfRows();
		DPClusterNode[] p = new DPClusterNode[nObservations];
		for (int i = 0; i < nObservations; i++) {
			double pi = 0.0;
			for (int j = 0; j < nObservations; j++) {
				if (i == j)
					continue;
				Double value = dissimialrityMatrix.get(i, j);
				if (null == value)
					value = measure.getMaxDissimilarity();
				double dcDistance = kernel.computeDcDistance(value, dc);
				pi += dcDistance;
			}
			p[i] = new DPClusterNode(pi, i);
		}
		Arrays.sort(p, comparatorDensity);
		return p;
	}
	/*
	private DPClusterNode[] computeDensity(double[][] dissimialrityMatrix) {
		int nObservations = dissimialrityMatrix.length;
		DPClusterNode[] p = new DPClusterNode[nObservations];
		for (int i = 0; i < nObservations; i++) {
			double pi = 0.0;
			for (int j = 0; j < nObservations; j++) {
				if (i == j)
					continue;
				double dcDistance = kernel.computeDcDistance(dissimialrityMatrix[i][j], dc);
				pi += dcDistance;
			}
			p[i] = new DPClusterNode(pi, i);
		}
		Arrays.sort(p, comparatorDensity);
		return p;
	}
	*/
	
	Comparator<Double> comparatorDouble = new Comparator<Double>() {
		@Override
		public int compare(Double o1, Double o2) {
			return o2.compareTo(o1);
		}
	};
	
	//按密度从大到小排序
	Comparator<DPClusterNode> comparatorDensity = new Comparator<DPClusterNode>() {
		@Override
		public int compare(DPClusterNode o1, DPClusterNode o2) {
			return new Double(o2.density).compareTo(o1.density);
		}
	};
	
	//按语料id从小到大排序
	Comparator<DPClusterNode> comparatorId = new Comparator<DPClusterNode>() {
		@Override
		public int compare(DPClusterNode o1, DPClusterNode o2) {
			return new Integer(o1.index).compareTo(o2.index);
		}
	};
	
	@Override
	public void dumpFile(File file, boolean isOnline) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < clusterResult.length; i++) {
			if (-1 == clusterResult[i].clusterId)
				continue;
			set.add(clusterResult[i].clusterId);
		}
		//用一个二维数组来存储聚类结果，每行是一个cluster
		@SuppressWarnings("unchecked")
		List<DPClusterNode>[] clusters = new List[set.size()];
		LOG.info("cluster size : " + clusters.length);
		for (int i = 0; i < clusters.length; i++)
			clusters[i] = new ArrayList<DPClusterNode>();
		for (int i = 0; i < clusterResult.length; i++) {
			int clusterId = clusterResult[i].clusterId;
			if (-1 == clusterId || true == clusterResult[i].halo)
				continue;
			clusters[clusterId].add(clusterResult[i]);
		}
		
		for (int i = 0; i < clusters.length; i++) {
			bw.write("-----------------------------------------------\n");
			List<DPClusterNode> nodes = clusters[i];
			for (int j = 0; j < nodes.size(); j++) {
				int index = nodes.get(j).index;
				if (false == isOnline) {
					bw.write(i + Constants.STRING_SEPERATOR + experiments.get(index).getDescription() 
							+ Constants.STRING_SEPERATOR + experiments.get(index).getTag() + "\n");
				} else {
					bw.write(experiments.get(index).getTag() + Constants.ONLINE_STRING_SEPERATOR +
							experiments.get(index).getDescription() + "\r\n");
				}
			}
		}
		bw.close();
		LOG.info("DensityPeaks dumpFile done ......");
	}

	@Override
	public Map<String, Object> getExtralResutl() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pDensity", pDensity);
		return map;
	}

	@Override
	public void eval(File file) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Clusters getClusters() {
		// TODO Auto-generated method stub
		return null;
	}

}
