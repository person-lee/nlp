package com.lbc.nlp_algorithm.prepocess.api.api;

import java.util.Map;

/**
 * Omit terms which don't express the meaning of a
 * given document.
 * 
 * @author yanjun
 */
public interface TermFilter {

	void filter(Map<String, Term> terms);
}
