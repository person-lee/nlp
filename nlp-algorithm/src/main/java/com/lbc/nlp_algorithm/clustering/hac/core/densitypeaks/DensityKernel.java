package com.lbc.nlp_algorithm.clustering.hac.core.densitypeaks;

/**
 * Density kernel, compute density measure between i and j.
 * dc is cutoff distance.
 *
 */
public interface DensityKernel {

	public double computeDcDistance(double dissimilarity, double dc);
}
