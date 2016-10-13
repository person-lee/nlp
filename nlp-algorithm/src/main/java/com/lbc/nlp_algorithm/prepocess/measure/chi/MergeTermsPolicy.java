package com.lbc.nlp_algorithm.prepocess.measure.chi;

import java.util.Map;
import java.util.Set;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.TermFeatureable;

public interface MergeTermsPolicy {

	Set<TermFeatureable> merge(Context context, Map<String, Map<String, TermFeatureable>> terms);
	
}
