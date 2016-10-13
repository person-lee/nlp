package com.lbc.nlp_algorithm.prepocess.api.api;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DocumentAnalyzer {

	Map<String, Term> analyze(File file, List<Set<String>/*一个句子*/> sentences);
	
	Map<String, Term> analyze(File file);
	
	Map<String, Term> analyzer(String line, List<Set<String>/*一个句子*/> sentences);
}
