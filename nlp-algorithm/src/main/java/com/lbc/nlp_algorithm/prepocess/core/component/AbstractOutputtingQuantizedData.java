package com.lbc.nlp_algorithm.prepocess.core.component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.ProcessorType;
import com.lbc.nlp_algorithm.prepocess.api.api.Term;
import com.lbc.nlp_algorithm.prepocess.api.api.TermFeatureable;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_algorithm.prepocess.core.common.AbstractComponent;
import com.lbc.nlp_algorithm.prepocess.core.utils.FileUtils;
import com.lbc.nlp_modules.common.Constants;

public abstract class AbstractOutputtingQuantizedData extends AbstractComponent {

	private static final Log LOG = LogFactory.getLog(AbstractOutputtingQuantizedData.class);
	private BufferedWriter writer;
	private Map<String, TermFeatureable> featuredTermsMap = Maps.newHashMap();
	
	public AbstractOutputtingQuantizedData(final Context context) {
		super(context);
	}
	
	@Override
	public void fire() {
		for(TermFeatureable term : context.getVectorMetadata().featuredTerms()) {
			featuredTermsMap.put(term.getWord(), term);
		}
		
		// create term vectors for outputting/inputting
		quantizeTermVectors();
		
		// output train/test vectors
		try {
			File file = new File(context.getFDMetadata().getOutputDir(), context.getFDMetadata().getOutputVectorFile());
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), context.getCharset()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context.getVectorMetadata().termTableIterator();
		
		// 词表，用来临时存储所有词的特征
		Map<Integer, Double> wordDict = Maps.newHashMap();
		Map<String, Double> word2Tfidf = Maps.newHashMap();
		computeWordDict(iter, wordDict, word2Tfidf);
		
		// 将句子的特征写入文件
		dumpSentence(writer, word2Tfidf);
		
		FileUtils.closeQuietly(writer);
		
		ProcessorType processType = context.getProcessorType();
		if (ProcessorType.TRAIN == processType) {
			try {
				FileUtils.save2File(context.getFDMetadata().getOutputDir(), context.getFDMetadata().getOutputDictName(), context.getCharset(), wordDict);
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		}
		
		LOG.info("Finished: outputVectorFile=" + context.getFDMetadata().getOutputVectorFile());
	}
	
	/**
	 * 计算词表
	 * @param iter
	 * @param wordDict
	 * @param word2Tfidf
	 */
	private void computeWordDict(Iterator<Entry<String, Map<String, Map<String, Term>>>> iter, 
			Map<Integer, Double> wordDict, Map<String, Double> word2Tfidf) {
		while(iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> labelledDocsEntry = iter.next();
			String label = labelledDocsEntry.getKey();
			Integer labelId = getLabelId(label);
			if(labelId != null) {
				Map<String, Map<String, Term>>  docs = labelledDocsEntry.getValue();
				Iterator<Entry<String, Map<String, Term>>> docsIter = docs.entrySet().iterator();
				while(docsIter.hasNext()) {
					Entry<String, Map<String, Term>> docsEntry = docsIter.next();
					Map<String, Term> terms = docsEntry.getValue();
					for(Entry<String, Term> termEntry : terms.entrySet()) {
						String word = termEntry.getKey();
						Integer wordId = getWordId(word);
						if(wordId != null) {
							Term term = termEntry.getValue();
							
							// wordId->tfidf
							if (!wordDict.containsKey(wordId)) {
								wordDict.put(wordId, term.getTfidf());
							}
							
							// word->tfidf
							if (!word2Tfidf.containsKey(word)) {
								word2Tfidf.put(word, term.getTfidf());
							}
						}
					}
				}
			} else {
				LOG.warn("Label ID can not be found: label=" + label + ", labelId=null");
			}
		}
	}
	
	/**
	 * 将每一行的句子转换为特征保存
	 * @param writer
	 * @param word2Tfidf
	 */
	private void dumpSentence(BufferedWriter writer, Map<String, Double> word2Tfidf) {
		Iterator<Entry<String, Map<String, List<Set<String>>>>> iter = context.getVectorMetadata().sentenceTableIterator();
		String featureType = context.getConfiguration().get(ConfigKeys.FEATURE_TYPE);
		// 词表，用来临时存储所有词的特征
		while(iter.hasNext()) {
			Entry<String, Map<String, List<Set<String>>>> labelledDocsEntry = iter.next();
			String label = labelledDocsEntry.getKey();
			Integer labelId = getLabelId(label);
			if(labelId != null) {
				Map<String, List<Set<String>>>  docs = labelledDocsEntry.getValue();
				Iterator<Entry<String, List<Set<String>>>> docsIter = docs.entrySet().iterator();
				while(docsIter.hasNext()) {
					Entry<String, List<Set<String>>> docsEntry = docsIter.next();
					List<Set<String>> terms = docsEntry.getValue();
					for(Set<String> termEntry : terms) {
						StringBuffer line = new StringBuffer();
						line.append(labelId).append(" ");
						for (String word : termEntry/*sentence*/) {
							Integer wordId = getWordId(word);
							if(wordId != null) {
								if (Constants.FEATURE_TYPE.BINARY.equals(featureType)) {
									line.append(wordId).append(":").append(1.0/Math.sqrt(termEntry.size())).append(" ");
								} else {
									Double tfidf = word2Tfidf.get(word);
									line.append(wordId).append(":").append(tfidf).append(" ");
								}
							}
						}
						try {
							String element = line.toString().trim();
							LOG.debug("Write line: " + element);
							writer.write(element);
							writer.newLine();
						} catch (IOException e) {
							LOG.error(e.getMessage());
						}
					}
				}
			} else {
				LOG.warn("Label ID can not be found: label=" + label + ", labelId=null");
			}
		}
	}
	
	private Integer getWordId(String word) {
		TermFeatureable term = featuredTermsMap.get(word);
		return term == null ? null : term.getId();
	}

	private Integer getLabelId(String label) {
		return context.getVectorMetadata().getlabelId(label);
	}

	protected abstract void quantizeTermVectors();

}
