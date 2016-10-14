package com.lbc.nlp_algorithm.prepocess.measure.infoGain;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.FeatureTermSelector;
import com.lbc.nlp_algorithm.prepocess.api.api.Term;
import com.lbc.nlp_algorithm.prepocess.api.api.TermFeatureable;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_algorithm.prepocess.api.common.FeaturedTerm;
import com.lbc.nlp_algorithm.prepocess.core.utils.FileUtils;
import com.lbc.nlp_algorithm.prepocess.measure.utils.SortUtils;
import com.lbc.nlp_algorithm.prepocess.measure.utils.SortUtils.Result;
import com.lbc.nlp_modules.common.thread.ThreadPoolUtils;

public class InfoGainTermSelector implements FeatureTermSelector {
	private static final Log LOG = LogFactory.getLog(InfoGainTermSelector.class);
	private Context context;
	
	// Map<word, term>
	private final Map<String, TermFeatureable> TermsWithMeasures = Maps.newConcurrentMap();
	// Map<label, ChiCalculator>
	private final Map<String, InfoGainCalculator> calculators = Maps.newHashMap();
	
	private ExecutorService executorService;
	private CountDownLatch latch;
	private float keptTermsPercent;
	
	public InfoGainTermSelector() {
		super();
	}
	
	@Override
	public Set<TermFeatureable> select(Context context) {
		this.context = context;
		keptTermsPercent = context.getConfiguration().getFloat(ConfigKeys.FEATURE_EACH_LABEL_KEPT_TERM_PERCENT, 0.30F);
		
		int totalWords = context.getVectorMetadata().getInvertedTable().size();
		LOG.info("Initialize latch: totalWords=" + totalWords);
		latch = new CountDownLatch(totalWords);
		executorService = ThreadPoolUtils.getExecutor();
		double entropy = calHC(context);
		Result sortedResult = null;
		try {
			for(Entry<String, Map<String, Set<String>>> iter : context.getVectorMetadata().getInvertedTable().entrySet()) {
				String term = iter.getKey();
				calculators.put(term, new InfoGainCalculator(iter, TermsWithMeasures, entropy));
				executorService.execute(calculators.get(term));
			}
		} catch(Exception e){
			LOG.error(e.getMessage());
		} finally {
			try {
				latch.await();
			} catch (InterruptedException e) { 
				LOG.error("interruptedException: " + e.getMessage());
			} catch (Exception e) {
				LOG.error("executor Exception: " + e.getMessage());
			}
			LOG.info("Shutdown executor service: " + executorService);
		}
		
		Date start = new Date();
		int topN = (int) (TermsWithMeasures.size() * keptTermsPercent);
		LOG.info("Terms selection for sort: topN=" + topN);
		sortedResult = sort(TermsWithMeasures, topN);
		Date finish = new Date();
		LOG.info("infoGain terms sorted: " + ", timeTaken=" + (finish.getTime() - start.getTime()) + "(ms)");
		
		// wrap result
		Set<TermFeatureable> mergedTerms = Sets.newHashSet();
		LOG.info("Result: " + ", startIndex=" + sortedResult.getStartIndex() + ", endIndex=" + sortedResult.getEndIndex());
		for (int i = sortedResult.getStartIndex(); i <= sortedResult.getEndIndex(); i++) {
			Entry<String, TermFeatureable> termEntry = sortedResult.get(i);
			mergedTerms.add(termEntry.getValue());
		}
		return mergedTerms;
	}
	
	private Result sort(Map<String, TermFeatureable> terms, int topN) {
		SortUtils sorter = new SortUtils(terms, true, Math.min(topN, terms.size()));
		Result result = sorter.heapSort();
		return result;
	}
	
	private final class InfoGainCalculator extends Thread {
		
		private final Entry<String, Map<String, Set<String>>> word2doc;
		private final Map<String, TermFeatureable> terms;
		private Result sortedResult;
		private final double entropy;
		
		public InfoGainCalculator(Entry<String, Map<String, Set<String>>> word2doc, 
				Map<String, TermFeatureable> terms, double entropy) {
			this.word2doc = word2doc;
			this.terms = terms;
			this.entropy = entropy;
		}
		
		@Override
		public void run() {
			try {
				processSingleTermData();
			} catch(Exception e) {
				LOG.error(e.getMessage());
			} finally {
				latch.countDown();
				LOG.info("剩下" + latch.getCount() + "个任务需要执行。");
			}
		}
		
		private void processSingleTermData() {
			String word = word2doc.getKey();
			Map<String, Set<String>> labelledDocs = word2doc.getValue();
			
			double tempAB = 0d;
			double tempCD = 0d;
			
			// all doc containing the word
			int totalDocContainingWord = 0;
			for (Entry<String, Set<String>> label : labelledDocs.entrySet()) {
				totalDocContainingWord += label.getValue().size();
			}
			double docProbilityContainingWord = (double)totalDocContainingWord / context.getVectorMetadata().totalDocCount();
			
			int totalDocsNotContainingWord = context.getVectorMetadata().totalDocCount() - totalDocContainingWord;
			for(String label : context.getVectorMetadata().labels()) {
				// A: doc count containing the word in this label
				int docCountContainingWordInLabel = 0;
				if(labelledDocs.get(label) != null) {
					docCountContainingWordInLabel = labelledDocs.get(label).size();
				}
				
				// B: doc count containing the word not in this label
				int docCountContainingWordNotInLabel = totalDocContainingWord - docCountContainingWordInLabel;
				
				// C: doc count not containing the word in this label
				int docCountNotContainingWordInLabel = 0;
				int totalDocsInLabel = computeDocCountInLabel(label);
				if (0 != totalDocsInLabel) {
					docCountNotContainingWordInLabel = totalDocsInLabel - docCountContainingWordInLabel;
				} 
				
				// D: doc count not containing the word not in this label
				int docCountNotContainingWordNotInLabel = totalDocsNotContainingWord - docCountNotContainingWordInLabel;
				
				// compute info gain value
				int A = docCountContainingWordInLabel;
				int B = docCountContainingWordNotInLabel;
				int C = docCountNotContainingWordInLabel;
				int D = docCountNotContainingWordNotInLabel;
				
				double tempA = (double)A / (A + B);
				double tempB = (double)B / (A + B);
				double tempC = (double)C / (C + D);
				double tempD = (double)D / (C + D);
				
				tempAB += getLog(tempA) + getLog(tempB);
				tempCD += getLog(tempC) + getLog(tempD);
			}
			double infoGain = entropy + docProbilityContainingWord * tempAB + (1 - docProbilityContainingWord) * tempCD;
			
			TermFeatureable term = new FeaturedTerm(word);
			term.setMeasureValue(infoGain);
			terms.put(word, term);
		}
		
		private double getLog(double value) {
			if (0 != value) {
				return value * Math.log(value);
			} else {
				return 0;
			}
		}
		
		private int computeDocCountInLabel(String label) {
			Map<String, Map<String, Map<String, Term>>> termTable = context.getVectorMetadata().getTermTable();
			if (MapUtils.isNotEmpty(termTable)) {
				Map<String, Map<String, Term>> docs = termTable.get(label);
				if (MapUtils.isNotEmpty(docs)) {
					return docs.size();
				}
			}
			return 0;
		}
		
	}
	
	@Override
	public Result getSortedResult(String label) {
		return calculators.get(label).sortedResult;
	}
	
	@Override
	public void load(Context context) {
		BufferedReader reader = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(context.getFDMetadata().getFeatureTermVectorFile());
			reader = new BufferedReader(new InputStreamReader(fis, context.getCharset()));
			String line = null;
			Set<TermFeatureable> terms = Sets.newHashSet();
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(!line.isEmpty()) {
					String[] aWord = line.split("\\s+");
					if(aWord.length == 2) {
						String word = aWord[0];
						int wordId = Integer.parseInt(aWord[1]);
						FeaturedTerm term = new FeaturedTerm(word);
						term.setId(wordId);
						terms.add(term);
						LOG.info("Load infoGain term: word=" + word + ", wordId=" + wordId);
					}
				}
			}
			context.getVectorMetadata().setFeaturedTerms(terms);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeQuietly(fis, reader);
		}		
	}
	
	/**
	 * 计算信息熵
	 * @param docCountContainingWordInLabel
	 * @param totalDocCount
	 * @return
	 */
	private double calHC(Context context) {
		double hc = 0d;
		int totalDocCount = context.getVectorMetadata().totalDocCount();
		for (String label : context.getVectorMetadata().labels()) {
			double p = (double) context.getVectorMetadata().getLabelTotalDocCount(label) / totalDocCount;
			hc += p * Math.log(p);
		}

		return -hc;
	}

}
