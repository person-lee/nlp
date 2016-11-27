package com.lbc.nlp_algorithm.clustering.hac.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtil {
	private final Logger LOG = LoggerFactory.getLogger(PropertiesUtil.class);

	//参数配置
	private static final String CLUSTER_PARTITIONEDHACSTOPTHRESHOLD = "xnlp.cluster.PartitionedHACStopThreshold";
	private static final String CLUSTER_PARTITIONEDHACSIZETHRESHOLD = "xnlp.cluster.PartitionedHACSizeThreshold";
	private static final String CLUSTER_PARTITIONEDHACUSESPLIT = "xnlp.cluster.PartitionedHACUseSplit";
	private static final String CLUSTER_PARTITIONEDHACUSEPARALLEL = "xnlp.cluster.PartitionedHACUseParallel";
	private static final String CLUSTER_PARTITIONEDHACUSEPRETREATMENT = "xnlp.cluster.PartitionedHACUsePreTreatment";
	private static final String CLUSTER_PARTITIONEDHACPARALLELSIZE = "xnlp.cluster.PartitionedHACParallelSize";
	private static final String CLUSTER_PARTITIONEDHACSIZEPERCLUSTER = "xnlp.cluster.PartitionedHACSizePerCluster";

	private static final String CLUSTER_KMEANSTOLERANCE = "xnlp.cluster.KmeansTolerance";
	private static final String CLUSTER_KMEANSMAXITER = "xnlp.cluster.KmeansMaxIter";
	private static final String CLUSTER_KMEANSPOINTSTOLERANCE = "xnlp.cluster.KmeansPointsTolerance";
	private static final String CLUSTER_KMEANSPARALLELSIZE = "xnlp.cluster.KmeansParallelSize";

	private static final String CLUSTER_HAC2STOPTHRESHOLD = "xnlp.cluster.HAC2StopThreshold";
	private static final String CLUSTER_HAC2SIZETHRESHOLD = "xnlp.cluster.HAC2SizeThreshold";
	private static final String CLUSTER_HAC2SIMUPPER = "xnlp.cluster.HAC2SimUpper";

	private static final String CLUSTER_MAPEXPERIMENTSIZEMERGETHRESHOLD = "xnlp.cluster.MapExperimentSizeMergeThreshold";

	private static final String VECTORIZATION_COMBINENUMBERS = "xnlp.vectorization.CombineNumbers";

	/**
	 * 聚类相关参数
	 */
	public class ClusterProperties {
		private ClusterProperties() {}

		//PartitionedHAC
		private double PartitionedHACStopThreshold = 0.6;
		private int PartitionedHACSizeThreshold = 5;
		private boolean PartitionedHACUseSplit = true;
		private boolean PartitionedHACUseParallel = true;
		private boolean PartitionedHACUsePreTreatment = true;
		private int PartitionedHACParallelSize = 3;
		private int PartitionedHACSizePerCluster = 7000;
		//KMeans
		private double KmeansTolerance = 1e-1;
		private int KmeansMaxIter = 10;
		private double KmeansPointsTolerance = .05;
		private int KmeansParallelSize = 3;
		//HierarchicalAgglomerativeClusterFaster2
		private double HAC2StopThreshold = 0.6;
	    private int HAC2SizeThreshold = 5;
	    private double HAC2SimUpper = 1.0;
	    //MapExperiment
	    private int MapExperimentSizeMergeThreshold = 1000;

		@Override
		public String toString(){
			return "#ClusterProperties#PartitionedHACStopThreshold=" + PartitionedHACStopThreshold + ",PartitionedHACSizeThreshold="
					+ PartitionedHACSizeThreshold + ",PartitionedHACUseSplit=" + PartitionedHACUseSplit
					+ ",PartitionedHACUseParallel=" + PartitionedHACUseParallel + ",PartitionedHACUsePreTreatment="
					+ PartitionedHACUsePreTreatment +",PartitionedHACParallelSize="
					+ PartitionedHACParallelSize + ",PartitionedHACSizePerCluster=" + PartitionedHACSizePerCluster
					+ ",KmeansTolerance=" + KmeansTolerance + ",KmeansMaxIter=" + KmeansMaxIter + ",KmeansPointsTolerance="
					+ KmeansPointsTolerance + ",KmeansParallelSize=" + KmeansParallelSize + ",HAC2StopThreshold="
					+ HAC2StopThreshold + ",HAC2SizeThreshold=" + HAC2SizeThreshold + ",HAC2SimUpper="
					+ HAC2SimUpper + ",MapExperimentSizeMergeThreshold=" + MapExperimentSizeMergeThreshold;
		}

		public double getPartitionedHACStopThreshold() {
			return PartitionedHACStopThreshold;
		}

		public int getPartitionedHACSizeThreshold() {
			return PartitionedHACSizeThreshold;
		}

		public boolean getPartitionedHACUseSplit() {
			return PartitionedHACUseSplit;
		}

		public boolean getPartitionedHACUseParallel() {
			return PartitionedHACUseParallel;
		}

		public boolean getPartitionedHACUsePreTreatment() {
			return PartitionedHACUsePreTreatment;
		}

		public int getPartitionedHACParallelSize() {
			return PartitionedHACParallelSize;
		}

		public int getPartitionedHACSizePerCluster() {
			return PartitionedHACSizePerCluster;
		}

		public double getKmeansTolerance() {
			return KmeansTolerance;
		}

		public int getKmeansMaxIter() {
			return KmeansMaxIter;
		}

		public double getKmeansPointsTolerance() {
			return KmeansPointsTolerance;
		}

		public int getKmeansParallelSize() {
			return KmeansParallelSize;
		}

		public double getHAC2StopThreshold() {
			return HAC2StopThreshold;
		}

		public int getHAC2SizeThreshold() {
			return HAC2SizeThreshold;
		}

		public double getHAC2SimUpper() {
			return HAC2SimUpper;
		}

		public int getMapExperimentSizeMergeThreshold() {
			return MapExperimentSizeMergeThreshold;
		}
	}
	private ClusterProperties ClusterPropertiesHolder;
	public static final ClusterProperties getClusterProperties() {
		return PropertiesHolder.INSTANCE.ClusterPropertiesHolder;
	}

	/**
	 * 向量化相关参数
	 */
	public class VectorizationProperties {
		private VectorizationProperties() {}

		private boolean CombineNumbers = false;

		@Override
		public String toString(){
			return "#ClusterProperties#CombineNumbers=" + CombineNumbers;
		}

		public boolean getCombineNumbers() {
			return CombineNumbers;
		}
	}
	private VectorizationProperties VectorizationPropertiesHolder;
	public static final VectorizationProperties getVectorizationProperties() {
		return PropertiesHolder.INSTANCE.VectorizationPropertiesHolder;
	}

	private static class PropertiesHolder {
		private static final PropertiesUtil INSTANCE = new PropertiesUtil();
	}

	public static final PropertiesUtil getProperties() {
		return PropertiesHolder.INSTANCE;
	}

	private PropertiesUtil() {
		LOG.info("开始加载xnlp参数。。。");
		ClusterPropertiesHolder = new ClusterProperties();
		VectorizationPropertiesHolder = new VectorizationProperties();

		//尝试从外部配置文件获取
        try {
        	String jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        	String srcPath = jarPath.substring(0, jarPath.indexOf("/lib"));
    		File f = new File(srcPath + "/xnlp.properties");
            Properties p = new Properties();
			p.load(new FileInputStream(f));
			LOG.info("xnlp从外部配置获取参数中。。。");
			readFromProperties(p);
			LOG.info("xnlp参数:");
			LOG.info(ClusterPropertiesHolder.toString());
			LOG.info(VectorizationPropertiesHolder.toString());

			return;
		} catch (Exception e) {
			LOG.info("从外部文件获取配置失败:" + e.getMessage());
		}

		//尝试从包内配置文件读取
        try {
	        InputStream inputStream = PropertiesUtil.class.getResourceAsStream("/xnlp.properties");
	        Properties p = new Properties();
			p.load(inputStream);
			LOG.info("xnlp从内部配置获取参数中。。。");
			readFromProperties(p);
			LOG.info("xnlp参数:");
			LOG.info(ClusterPropertiesHolder.toString());
			LOG.info(VectorizationPropertiesHolder.toString());

			return;
		} catch (Exception e) {
			LOG.info("从内部文件获取配置失败：" + e.getMessage());
		}

		//使用默认配置
        setDefault();
	}

	/**
	 * 从配置文件获取参数
	 * @param p	配置文件
	 */
	private void readFromProperties(Properties p) {
		//PartitionedHAC
		ClusterPropertiesHolder.PartitionedHACStopThreshold = getDouble(p, CLUSTER_PARTITIONEDHACSTOPTHRESHOLD, ClusterPropertiesHolder.PartitionedHACStopThreshold);
		ClusterPropertiesHolder.PartitionedHACSizeThreshold = getInt(p, CLUSTER_PARTITIONEDHACSIZETHRESHOLD, ClusterPropertiesHolder.PartitionedHACSizeThreshold);
		ClusterPropertiesHolder.PartitionedHACUseSplit = getBool(p, CLUSTER_PARTITIONEDHACUSESPLIT, ClusterPropertiesHolder.PartitionedHACUseSplit);
		ClusterPropertiesHolder.PartitionedHACUseParallel = getBool(p, CLUSTER_PARTITIONEDHACUSEPARALLEL, ClusterPropertiesHolder.PartitionedHACUseParallel);
		ClusterPropertiesHolder.PartitionedHACUsePreTreatment = getBool(p, CLUSTER_PARTITIONEDHACUSEPRETREATMENT, ClusterPropertiesHolder.PartitionedHACUsePreTreatment);
		ClusterPropertiesHolder.PartitionedHACParallelSize = getInt(p, CLUSTER_PARTITIONEDHACPARALLELSIZE, ClusterPropertiesHolder.PartitionedHACParallelSize);
		ClusterPropertiesHolder.PartitionedHACSizePerCluster = getInt(p, CLUSTER_PARTITIONEDHACSIZEPERCLUSTER, ClusterPropertiesHolder.PartitionedHACSizePerCluster);
		//Kmeans
		ClusterPropertiesHolder.KmeansTolerance = getDouble(p, CLUSTER_KMEANSTOLERANCE, ClusterPropertiesHolder.KmeansTolerance);
		ClusterPropertiesHolder.KmeansMaxIter = getInt(p, CLUSTER_KMEANSMAXITER, ClusterPropertiesHolder.KmeansMaxIter);
		ClusterPropertiesHolder.KmeansPointsTolerance = getDouble(p, CLUSTER_KMEANSPOINTSTOLERANCE, ClusterPropertiesHolder.KmeansPointsTolerance);
		ClusterPropertiesHolder.KmeansParallelSize = getInt(p, CLUSTER_KMEANSPARALLELSIZE, ClusterPropertiesHolder.KmeansParallelSize);
		//HierarchicalAgglomerativeClusterFaster2
		ClusterPropertiesHolder.HAC2StopThreshold = getDouble(p, CLUSTER_HAC2STOPTHRESHOLD, ClusterPropertiesHolder.HAC2StopThreshold);
		ClusterPropertiesHolder.HAC2SizeThreshold = getInt(p, CLUSTER_HAC2SIZETHRESHOLD, ClusterPropertiesHolder.HAC2SizeThreshold);
		ClusterPropertiesHolder.HAC2SimUpper = getDouble(p, CLUSTER_HAC2SIMUPPER, ClusterPropertiesHolder.HAC2SimUpper);
	    //MapExperiment
		ClusterPropertiesHolder.MapExperimentSizeMergeThreshold = getInt(p, CLUSTER_MAPEXPERIMENTSIZEMERGETHRESHOLD, ClusterPropertiesHolder.MapExperimentSizeMergeThreshold);
		//Vectorization
		VectorizationPropertiesHolder.CombineNumbers = getBool(p, VECTORIZATION_COMBINENUMBERS, VectorizationPropertiesHolder.CombineNumbers);
	}

	/**
	 * 设置为默认参数
	 */
	private void setDefault() {
		//由于默认配置已写在内部类的初始化中因此这里不用处理
		LOG.info("xnlp从默认配置获取参数中。。。");
		LOG.info("xnlp参数:");
		LOG.info(ClusterPropertiesHolder.toString());
		LOG.info(VectorizationPropertiesHolder.toString());
	}

	//从配置文件读取bool参数
	private boolean getBool(Properties p, String key, boolean def) {
		String value = p.getProperty(key);
		if (!StringUtils.isEmpty(value)) {
			if (value.equalsIgnoreCase("true")) {
				return true;
			} else if (value.equalsIgnoreCase("false")) {
				return false;
			} else {
				LOG.info("转换bool配置" + key + "出错，值为" + value);
			}
		}
		return def;
	}
	//从配置文件读取int参数
	private int getInt(Properties p, String key, int def) {
		String value = p.getProperty(key);
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			LOG.info("转换int配置" + key + "出错，值为" + value);
		}
		return def;
	}
	//从配置文件读取long参数
	private long getLong(Properties p, String key, long def) {
		String value = p.getProperty(key);
		try {
			return Long.parseLong(value);
		} catch (Exception e) {
			LOG.info("转换long配置" + key + "出错，值为" + value);
		}
		return def;
	}
	//从配置文件读取double参数
	private double getDouble(Properties p, String key, double def) {
		String value = p.getProperty(key);
		try {
			return Double.parseDouble(value);
		} catch (Exception e) {
			LOG.info("转换double配置" + key + "出错，值为" + value);
		}
		return def;
	}
	//从配置文件读取String参数
	private String getString(Properties p, String key, String def) {
		String value = p.getProperty(key);
		if (StringUtils.isEmpty(value)) {
			LOG.info("转换String配置" + key + "出错，值为" + value);
			return def;
		}
		return value;
	}

	//测试方法
	public static void main(String[] args) {
		PropertiesUtil instance = PropertiesUtil.getProperties();
		System.out.println(instance.getClusterProperties().toString());
	}
}
