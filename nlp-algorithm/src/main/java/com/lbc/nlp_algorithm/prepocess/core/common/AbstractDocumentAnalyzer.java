package com.lbc.nlp_algorithm.prepocess.core.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;
import com.lbc.nlp_domain.Word;
import com.lbc.nlp_modules.common.proterties.ConfigReadable;

public class AbstractDocumentAnalyzer {

	private static final Log LOG = LogFactory.getLog(AbstractDocumentAnalyzer.class);
	protected String charSet = "UTF-8";
	protected final ConfigReadable configuration;
	private final Set<String> stopwords = new HashSet<String>();
	
	public AbstractDocumentAnalyzer(ConfigReadable configuration) {
		this.configuration = configuration;
		// set charset
		String charSet = configuration.get("processor.common.charset");
		if(charSet != null) {
			this.charSet = charSet;
		}
		// stop words
		String stopWordsDir = configuration.get("processor.analyzer.stopwords.path");
		if(stopWordsDir != null) {
			File dir = new File(stopWordsDir);
			File[] files = dir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					if(file.isFile()) {
						return true;
					}
					return false;
				}
				
			});
			for(File file : files) {
				try {
					load(file);
				} catch (Exception e) {
					LOG.warn("Fail to load stop words: file=" + file, e);
				}
			}
		}
	}
	
	public boolean isStopword(String word) {
		if(word != null) {
			word = word.trim();
			if(word.isEmpty()) {
				return true;
			} else {
				return stopwords.contains(word);
			}
		}
		return true;
	}

	private void load(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
			String word = null;
			while((word = reader.readLine()) != null) {
				word = word.trim();
				if(!word.isEmpty()) {
					if(!stopwords.contains(word)) {
						stopwords.add(word);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}  catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	protected Set<String> word2str(List<Word> words) {
		Set<String> wordSet = Sets.newHashSet();
		if (CollectionUtils.isNotEmpty(words)) {
			for (Word word : words) {
				wordSet.add(word.getTerm());
			}
		}
		return wordSet;
	}
}
