package com.lbc.nlp_algorithm.loadFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_modules.common.proterties.Configuration;

public class LoadStopWord {
	private final static Logger LOG = LoggerFactory
			.getLogger(LoadStopWord.class.getName());

	private String charSet = "UTF-8";
	private static final Set<String> STOP_WORDS = new HashSet<String>();
	private static final AtomicBoolean isLoaded = new AtomicBoolean(false);

	public static LoadStopWord getInstance() {
		return Nested.singleton;
	}
	
	public Set<String> getStopword() {
		return STOP_WORDS;
	}
	
	private static class Nested{
		private static LoadStopWord singleton = new LoadStopWord();
	}
	
	private LoadStopWord(){
		Configuration configuration = new Configuration();
		loadFolder(configuration);
	}

	public void loadFolder(Configuration configuration) {
		// set charset
		String charSet = configuration.get(
				ConfigKeys.DATASET_FILE_CHARSET);
		if (charSet != null) {
			this.charSet = charSet;
		}
		// try to load stop words
		if (isLoaded.compareAndSet(false, true)) {
			// stop words
			String stopWordsDir = configuration.get(
					ConfigKeys.DOCUMENT_ANALYZER_STOPWORDS_PATH);
			stopWordsDir = getClass().getClassLoader()
					.getResource(stopWordsDir).getPath();
			if (stopWordsDir != null) {
				File dir = new File(stopWordsDir);
				File[] files = dir.listFiles(new FileFilter() {

					@Override
					public boolean accept(File file) {
						if (file.isFile()) {
							return true;
						}
						return false;
					}

				});
				for (File file : files) {
					try {
						load(file);
					} catch (Exception e) {
						LOG.warn("Fail to load stop words: file=" + file, e);
					}
				}
			}
		}
	}

	private void load(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), charSet));
			String word = null;
			while ((word = reader.readLine()) != null) {
				word = word.trim();
				if (!word.isEmpty()) {
					if (!STOP_WORDS.contains(word)) {
						STOP_WORDS.add(word);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
