package com.lbc.nlp_algorithm.prepocess.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.Term;
import com.lbc.nlp_algorithm.prepocess.api.api.TermFilter;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_algorithm.prepocess.core.utils.ReflectionUtils;

public class AggregatedTermFilter implements TermFilter {

	private final List<TermFilter> filters = new ArrayList<TermFilter>(0);
	
	public AggregatedTermFilter(Context context) {
		String classes = context.getConfiguration().get(ConfigKeys.DOCUMENT_ANALYZER_CLASS);
		if(classes != null) {
			String[] aClass = classes.split("[,;\\s\\|:-]+");
			for(String className : aClass) {
				filters.add(ReflectionUtils.newInstance(className, TermFilter.class));
			}
		}
	}
	
	@Override
	public void filter(Map<String, Term> terms) {
		for(TermFilter filter : filters) {
			filter.filter(terms);
		}
	}

}
