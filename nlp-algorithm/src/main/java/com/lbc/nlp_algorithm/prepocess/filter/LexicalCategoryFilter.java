package com.lbc.nlp_algorithm.prepocess.filter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.Term;
import com.lbc.nlp_algorithm.prepocess.api.api.TermFilter;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;

public class LexicalCategoryFilter implements TermFilter {

	private final Set<String> keptLexicalCategories = new HashSet<String>();
	
	public LexicalCategoryFilter(Context context) {
		// read configured lexical categories
		String lexicalCategories = 
				context.getConfiguration().get(ConfigKeys.DOCUMENT_FILTER_KEPT_LEXICAL_CATEGORIES, "n");
		for(String category : lexicalCategories.split("\\s*,\\s*")) {
			keptLexicalCategories.add(category);
		}
	}
	
	@Override
	public void filter(Map<String, Term> terms) {
		Iterator<Entry<String, Term>> iter = terms.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Term> entry = iter.next();
			if(!keptLexicalCategories.contains(entry.getValue().getLexicalCategory())) {
				iter.remove();
			}
		}
	}

}
