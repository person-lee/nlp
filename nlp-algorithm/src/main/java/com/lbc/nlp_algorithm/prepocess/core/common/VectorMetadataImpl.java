package com.lbc.nlp_algorithm.prepocess.core.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.MapUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lbc.nlp_algorithm.prepocess.api.api.Term;
import com.lbc.nlp_algorithm.prepocess.api.api.TermFeatureable;
import com.lbc.nlp_algorithm.prepocess.api.api.VectorMetadata;

/**
 * 保存中间数据的容器类
 * @author cdlibaocang
 *
 */
public class VectorMetadataImpl implements VectorMetadata {

	private int totalDocCount;
	private final List<String> labels = new ArrayList<String>();
	// Map<label, docCnt>
	private final Map<String, Integer> labelledTotalDocCountMap = Maps.newHashMap();
	//  Map<label, Map<doc ,Map<word, term>>>
	private final Map<String, Map<String, Map<String, Term>>> termTable = Maps.newHashMap();
	//  Map<word ,Map<label, Set<doc>>>
	private final Map<String, Map<String, Set<String>>> invertedTable = Maps.newHashMap();
	// Map<label, Map<doc, List<List<word>/*sentence*/>>>
	private final Map<String, Map<String, List<Set<String>>>> sentenceTable = Maps.newHashMap();
	
	// <labelId, label>
	private final Map<Integer, String> idToLabelMap = Maps.newHashMap();
	// <label, labelId>
	private final Map<String, Integer> labelToIdMap = Maps.newHashMap();
	
	private Set<TermFeatureable> featuredTerms = Sets.newHashSet();
	
	@Override
	public void addLabel(String label) {
		if(!labels.contains(label)) {
			labels.add(label);
		}
	}
	
	@Override
	public List<String> labels() {
		return Collections.unmodifiableList(labels);
	}
	
	@Override
	public int totalDocCount() {
		return totalDocCount;
	}
	
	@Override
	public void setTotalDocCount(int totalDocCount) {
		this.totalDocCount = totalDocCount;
	}
	
	@Override
	public void putLabelledTotalDocCount(String label, int labelledDocCount) {
		labelledTotalDocCountMap.put(label, labelledDocCount);
	}
	
	
	//////// inverted table ////////
	
	@Override
	public synchronized void addTermToInvertedTable(String label, String doc, Term term) {
		String word = term.getWord();
		Map<String, Set<String>> labelledDocs = invertedTable.get(word);
		if(labelledDocs == null) {
			labelledDocs = Maps.newHashMap();
			invertedTable.put(word, labelledDocs);
		}
		Set<String> docs = labelledDocs.get(label);
		if(docs == null) {
			docs = Sets.newHashSet();
			labelledDocs.put(label, docs);
		}
		docs.add(doc);
	}
	
	@Override
	public void addTermsToInvertedTable(String label, String doc, Map<String, Term> terms) {
		Iterator<Entry<String, Term>> iter = terms.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Term> entry = iter.next();
			addTermToInvertedTable(label, doc, entry.getValue());
		}
	}
	
	@Override
	public int docCount(Term term) {
		String word = term.getWord();
		int count = 0;
		Map<String, Set<String>> labelledDocs = invertedTable.get(word);
		Iterator<Entry<String, Set<String>>> iter = labelledDocs.entrySet().iterator();
		while(iter.hasNext()) {
			count += iter.next().getValue().size();
		}
		return count;
	}
	
	@Override
	public Iterator<Entry<String, Map<String, Set<String>>>> invertedTableIterator() {
		return invertedTable.entrySet().iterator();
	}
	
	@Override
	public Map<String, Map<String, Set<String>>> getInvertedTable() {
		return invertedTable;
	}
	
	//////// term table ////////
	
	@Override
	public int termCount(String label, String doc) {
		int size = 0;
		// avoid empty file
		if(termTable.get(label) != null && termTable.get(label).get(doc) != null) {
			size = termTable.get(label).get(doc).size();
		}
		return size;
	}
	
	@Override
	public synchronized void addTerms(String label, String doc, Map<String, Term> terms) {
		Map<String, Map<String, Term>> docs = termTable.get(label);
		if(MapUtils.isEmpty(docs)) {
			docs = Maps.newHashMap();
			termTable.put(label, docs);
		}
		docs.put(doc, terms);
	}
	
	@Override
	public int labelCount() {
		return termTable.keySet().size();
	}
	
	@Override
	public Iterator<Entry<String, Map<String, Map<String, Term>>>> termTableIterator() {
		return termTable.entrySet().iterator();
	}
	
	@Override
	public Map<String, Map<String, Map<String, Term>>> getTermTable() {
		return termTable;
	}
	
	//////// label vector map ////////
	
	// label->id
	@Override
	public Iterator<Entry<String, Integer>> labelVectorMapIterator() {
		return labelToIdMap.entrySet().iterator();
	}
	
	@Override
	public Integer getlabelId(String label) {
		return labelToIdMap.get(label);
	}
	
	@Override
	public void putLabelToIdPairs(Map<String, Integer> globalLabelToIdMap) {
		this.labelToIdMap.putAll(globalLabelToIdMap);
	}
	
	// id->label
	@Override
	public void putIdToLabelPairs(Map<Integer, String> globalIdToLabelMap) {
		this.idToLabelMap.putAll(globalIdToLabelMap);
	}
	
	@Override
	public String getLabelById(Integer labelId) {
		return idToLabelMap.get(labelId);
	}
	
	//// featured vectors ////////
	
	@Override
	public void setFeaturedTerms(Set<TermFeatureable> terms) {
		this.featuredTerms = terms;		
	}

	@Override
	public Set<TermFeatureable> featuredTerms() {
		return Collections.unmodifiableSet(featuredTerms);
	}
	
	///////// sentence table /////////////
	@Override
	public synchronized void addSentence(String label, String doc, List<Set<String>> sentences){
		Map<String, List<Set<String>>> docs = sentenceTable.get(label);
		if(MapUtils.isEmpty(docs)) {
			docs = Maps.newHashMap();
			sentenceTable.put(label, docs);
		}
		docs.put(doc, sentences);
	}
	
	@Override
	public Iterator<Entry<String, Map<String, List<Set<String>>>>> sentenceTableIterator(){
		return sentenceTable.entrySet().iterator();
	}
	
	@Override
	public int getLabelTotalDocCount(String label) {
		if (MapUtils.isNotEmpty(labelledTotalDocCountMap)) {
			return labelledTotalDocCountMap.get(label);
		} else {
			return 0;
		}
	}

}
