package com.lbc.nlp_algorithm.prepocess.api.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface VectorMetadata {
	
	void addLabel(String label);
	
	List<String> labels();
	
	int totalDocCount();
	
	void setTotalDocCount(int totalDocCount);
	
	void putLabelledTotalDocCount(String label, int labelledDocCount);
	
	int getLabelTotalDocCount(String label);
	
	
	//////// inverted table ////////
	
	void addTermToInvertedTable(String label, String doc, Term term);
	
	void addTermsToInvertedTable(String label, String doc, Map<String, Term> terms);
	
	int docCount(Term term);
	
	Iterator<Entry<String, Map<String, Set<String>>>> invertedTableIterator();
	Map<String, Map<String, Set<String>>> getInvertedTable();
	
	//////// term table ////////
	
	int termCount(String label, String doc);
	
	void addTerms(String label, String doc, Map<String, Term> terms);
	
	int labelCount();
	
	Iterator<Entry<String, Map<String, Map<String, Term>>>> termTableIterator();
	
	Map<String, Map<String, Map<String, Term>>> getTermTable();
	
	//////// label vector map ////////
	
	// label->id
	
	Iterator<Entry<String, Integer>> labelVectorMapIterator();
	
	Integer getlabelId(String label);
	
	void putLabelToIdPairs(Map<String, Integer> globalLabelToIdMap);
	
	// id->label
	
	void putIdToLabelPairs(Map<Integer, String> globalIdToLabelMap);
	
	String getLabelById(Integer labelId);
	
	//////// featured terms vector ////////
	
	void setFeaturedTerms(Set<TermFeatureable> terms);
	Set<TermFeatureable> featuredTerms();
	
	///////// sentence table //////////////
	void addSentence(String label, String doc, List<Set<String>> sentences);
	
	Iterator<Entry<String, Map<String, List<Set<String>>>>> sentenceTableIterator();
}
