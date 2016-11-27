package com.lbc.nlp_algorithm.clustering.hac.core.densitypeaks;

public class CutoffKernel implements DensityKernel {

	@Override
	public double computeDcDistance(double dissimilarity, double dc) {
		if (dissimilarity < dc)
			return 1.0;
		return 0.0;
	}

}
