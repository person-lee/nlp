package com.lbc.nlp_algorithm.cutword.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lbc.nlp_algorithm.prepocess.api.api.DocumentAnalyzer;
import com.lbc.nlp_algorithm.prepocess.api.api.Term;
import com.lbc.nlp_algorithm.prepocess.core.common.AbstractDocumentAnalyzer;
import com.lbc.nlp_algorithm.prepocess.core.common.TermImpl;
import com.lbc.nlp_modules.common.proterties.ConfigReadable;

public class LuceneDocumentAnalyzer extends AbstractDocumentAnalyzer implements DocumentAnalyzer {

	private static final Log LOG = LogFactory.getLog(LuceneDocumentAnalyzer.class);
	private final Analyzer analyzer;
	
	public LuceneDocumentAnalyzer(ConfigReadable configuration) {
		super(configuration);
		analyzer = new SmartChineseAnalyzer(false);
	}

	/**
	 * 对文件进行切分
	 */
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
				TokenStream ts = analyzer.tokenStream("", new StringReader(line));
				ts.reset();
				ts.addAttribute(CharTermAttribute.class); 
				while (ts.incrementToken()) {  
					CharTermAttributeImpl attr = (CharTermAttributeImpl) ts.getAttribute(CharTermAttribute.class);  
					String word = attr.toString().trim();
					if(!word.isEmpty() && !super.isStopword(word)) {
						Term term = terms.get(word);
						if(term == null) {
							term = new TermImpl(word);
							terms.put(word, term);
						}
						term.incrFreq();
					} else {
						LOG.debug("Filter out stop word: file=" + file + ", word=" + word);
					}
					ts.end();
				}
				ts.close();
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
	
	/**
	 * 对文件按行进行切分
	 */
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
				Set<String> sentence = Sets.newHashSet();
				String[] arr = line.split("\t");
				String quest = arr[1];
				LOG.debug("Process line: " + quest);
				TokenStream ts = analyzer.tokenStream("", new StringReader(quest));
				ts.reset();
				ts.addAttribute(CharTermAttribute.class); 
				while (ts.incrementToken()) {  
					CharTermAttributeImpl attr = (CharTermAttributeImpl) ts.getAttribute(CharTermAttribute.class);  
					String word = attr.toString().trim();
					if(!word.isEmpty() && !super.isStopword(word)) {
						sentence.add(word);
						Term term = terms.get(word);
						if(term == null) {
							term = new TermImpl(word);
							terms.put(word, term);
						}
						term.incrFreq();
					} else {
						LOG.debug("Filter out stop word: file=" + file + ", word=" + word);
					}
					ts.end();
				}
				ts.close();
				sentences.add(sentence);
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

	/**
	 * 对句子进行切分
	 */
	@Override
	public Map<String, Term> analyzer(String line, List<Set<String>/*一个句子*/> sentences) {
		try {
			LOG.debug("Process line: " + line);
			Set<String> sentence = Sets.newHashSet();
			Map<String, Term> terms = Maps.newHashMap();
			TokenStream ts = analyzer.tokenStream("", new StringReader(line));
			ts.reset();
			ts.addAttribute(CharTermAttribute.class); 
			while (ts.incrementToken()) {  
				CharTermAttributeImpl attr = (CharTermAttributeImpl) ts.getAttribute(CharTermAttribute.class);  
				String word = attr.toString().trim();
				if(!word.isEmpty() && !super.isStopword(word)) {
					sentence.add(word);
					Term term = terms.get(word);
					if(term == null) {
						term = new TermImpl(word);
						terms.put(word, term);
					}
					term.incrFreq();
				} else {
					LOG.debug("Filter out stop word: file=" + line + ", word=" + word);
				}
				ts.end();
			}
			ts.close();
			sentences.add(sentence);
			
			return terms;
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return MapUtils.EMPTY_MAP;
		}
	}
}
