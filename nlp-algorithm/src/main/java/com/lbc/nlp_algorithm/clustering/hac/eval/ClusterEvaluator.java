package com.lbc.nlp_algorithm.clustering.hac.eval;

import java.io.File;
import java.io.IOException;

/**
 * 聚类结果的评估接口，这个只针对聚类
 *
 */
public interface ClusterEvaluator {

	/**
	 * 评估聚类效果
	 * @param file
	 * @throws IOException
	 */
	public void eval(File file) throws IOException;
}
