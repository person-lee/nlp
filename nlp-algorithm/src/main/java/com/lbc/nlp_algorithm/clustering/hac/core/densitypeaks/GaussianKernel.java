package com.lbc.nlp_algorithm.clustering.hac.core.densitypeaks;

/**
 * 高斯核
 *
 */
public class GaussianKernel implements DensityKernel {

	@Override
	public double computeDcDistance(double dissimilarity, double dc) {
		return Math.pow(Math.E, 0 - Math.pow(dissimilarity / dc, 2));
	}

}
