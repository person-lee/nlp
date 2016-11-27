package com.lbc.nlp_algorithm.clustering.hac.common.measure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;

/**
 * 归一化的向量内积，就是两个向量的夹角的cosin值。
 * 向量必须用sparse的表示方法，即一个double[]数组，dense vector的方法暂时还未实现
 *
 */
public class InnerProductMeasure implements DissimilarityMeasure {
	private final Logger LOG = LoggerFactory.getLogger(InnerProductMeasure.class);

	@Override
	public double computeDissimilarity(Object obj1, Object obj2) {
		Vector v1 = (Vector) obj1;
		Vector v2 = (Vector) obj2;
		if (v1.getLength() != v2.getLength()) {
			LOG.error("vector size error ...... v1.lenght:" + v1.getLength() + ", v2.length:" + v2.getLength());
			return 0.0;
		}
		double score = v1.cosine(v2);
		return score;
	}

	@Override
	public double getMaxDissimilarity() {
		return 2.0;
	}
}
