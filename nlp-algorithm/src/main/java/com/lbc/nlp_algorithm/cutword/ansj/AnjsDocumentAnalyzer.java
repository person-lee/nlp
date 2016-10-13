package com.lbc.nlp_algorithm.cutword.ansj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import com.lbc.nlp_algorithm.cutword.Cutword;
import com.lbc.nlp_algorithm.prepocess.api.api.DocumentAnalyzer;
import com.lbc.nlp_algorithm.prepocess.api.api.Term;
import com.lbc.nlp_algorithm.prepocess.core.common.AbstractDocumentAnalyzer;
import com.lbc.nlp_algorithm.prepocess.core.common.TermImpl;
import com.lbc.nlp_domain.Word;
import com.lbc.nlp_modules.common.proterties.ConfigReadable;

public class AnjsDocumentAnalyzer extends AbstractDocumentAnalyzer implements DocumentAnalyzer {

	private static final Log LOG = LogFactory.getLog(AnjsDocumentAnalyzer.class);
	private final Cutword analyzer;
	
	public AnjsDocumentAnalyzer(ConfigReadable configuration) {
		super(configuration);
		analyzer = AnjsCutword.getInstance();
	}

	@Override
	public Map<String, Term> analyze(File file, List<Set<String>/*一个句子*/> sentences) {
		String doc = file.getAbsolutePath();
		LOG.debug("Process document: file=" + doc);
		Map<String, Term> terms = Maps.newHashMap();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet));
			String line = null;
			while((line = br.readLine()) != null) {
				String[] arr = line.split("\t");
				String quest = arr[1];
				LOG.debug("Process line: " + quest);
				List<Word> words = analyzer.doCutword(quest);
				Set<String> sentence = word2str(words);
				sentences.add(sentence);
				if (CollectionUtils.isNotEmpty(words)) {
					for (Word word : words) {
						String wordTerm = null;
						if(word != null) {
							wordTerm = word.getTerm();
						}
						if(StringUtils.isNoneBlank(wordTerm)) {
							Term term = terms.get(wordTerm);
							if(term == null) {
								term = new TermImpl(wordTerm);
								terms.put(wordTerm, term);
							}
							term.incrFreq();
						} else {
							LOG.debug("Filter out stop word: file=" + file + ", word=" + word);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("", e);
		} finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch (IOException e) {
				LOG.warn(e);
			}
			LOG.debug("Done: file=" + file + ", termCount=" + terms.size());
		}
		return terms;
	}
	
	@Override
	public Map<String, Term> analyze(File file) {
		String doc = file.getAbsolutePath();
		LOG.debug("Process document: file=" + doc);
		Map<String, Term> terms = Maps.newHashMap();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet));
			String line = null;
			while((line = br.readLine()) != null) {
				LOG.debug("Process line: " + line);
				List<Word> words = analyzer.doCutword(line);
				if (CollectionUtils.isNotEmpty(words)) {
					for (Word word : words) {
						String wordTerm = null;
						if(word != null) {
							wordTerm = word.getTerm();
						}
						if(StringUtils.isNoneBlank(wordTerm)) {
							Term term = terms.get(wordTerm);
							if(term == null) {
								term = new TermImpl(wordTerm);
								terms.put(wordTerm, term);
							}
							term.incrFreq();
						} else {
							LOG.debug("Filter out stop word: file=" + file + ", word=" + word);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("", e);
		} finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch (IOException e) {
				LOG.warn(e);
			}
			LOG.debug("Done: file=" + file + ", termCount=" + terms.size());
		}
		return terms;
	}
	
	@Override
	public Map<String, Term> analyzer(String line, List<Set<String>/*一个句子*/> sentences) {
		LOG.debug("Process line: " + line);
		Map<String, Term> terms = Maps.newHashMap();
		List<Word> words = analyzer.doCutword(line);
		Set<String> sentence = word2str(words);
		sentences.add(sentence);
		if (CollectionUtils.isNotEmpty(words)) {
			for (Word word : words) {
				if(word != null) {
					Term term = terms.get(word.getTerm());
					if(term == null) {
						term = new TermImpl(word.getTerm());
						terms.put(word.getTerm(), term);
					}
					term.incrFreq();
				} else {
					LOG.debug("Filter out stop word: file=" + line + ", word=" + word.getTerm());
				}
			}
		}
		
		return terms;
	}

}

