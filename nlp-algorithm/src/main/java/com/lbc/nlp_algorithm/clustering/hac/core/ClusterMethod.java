package com.lbc.nlp_algorithm.clustering.hac.core;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.lbc.nlp_algorithm.clustering.hac.data.Clusters;

/**
 * 聚类算法接口，各种不用的聚类方法需要实现这些接口
 *
 */
public interface ClusterMethod {
	/**
	 * 聚类操作
	 * @throws IOException 
	 */
	public void cluster() throws IOException;
	
	/**
	 * 输出聚类结果
	 * @throws IOException 
	 */
	public void dumpFile(File file, boolean isOnline) throws IOException;
	
	/**
	 * 评估聚类结果，线下进行，线上数据没有标记，只能人工评估了
	 * @param file
	 */
	public void eval(File file) throws IOException;
	
	/**
	 * 获取聚类结果
	 */
	public Clusters getClusters(); 
	
	/**
	 * 获取一些额外的聚类结果，这些结果不是必须的，作为第三方应用时的补充。
	 * 比如density peaks的density-delta点图，这个如果集成到xnlp中，显得太复杂了，xnlp要力求精简,
	 * 只提供最基础的核心功能
	 */
	public Map<String, Object> getExtralResutl();
}

