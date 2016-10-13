package com.lbc.nlp_algorithm.prepocess.api.api;

import java.util.Set;

import com.lbc.nlp_algorithm.prepocess.measure.utils.SortUtils.Result;

/**
 * It's used to choose term vector from the train data set 
 * according to the specified selection policy.
 * 
 * @author yanjun
 */
public interface FeatureTermSelector {

	Set<TermFeatureable> select(Context context);
	
	void load(Context context);
	
	Result getSortedResult(String label);
}
