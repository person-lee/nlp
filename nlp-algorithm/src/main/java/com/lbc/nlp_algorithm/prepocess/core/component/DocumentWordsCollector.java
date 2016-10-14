package com.lbc.nlp_algorithm.prepocess.core.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.DocumentAnalyzer;
import com.lbc.nlp_algorithm.prepocess.api.api.Term;
import com.lbc.nlp_algorithm.prepocess.api.api.TermFilter;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_algorithm.prepocess.core.common.AbstractComponent;
import com.lbc.nlp_algorithm.prepocess.core.utils.ReflectionUtils;
import com.lbc.nlp_modules.common.thread.ThreadPoolUtils;

public class DocumentWordsCollector extends AbstractComponent {
	
	private static final Log LOG = LogFactory.getLog(DocumentWordsCollector.class);
	private final Set<TermFilter> filters = new HashSet<TermFilter>();
	private ExecutorService executorService;
	private CountDownLatch latch;
	
	public DocumentWordsCollector(final Context context) {
		super(context);
		// load term filter classes
		String filterClassNames = context.getConfiguration().get(ConfigKeys.DOCUMENT_FILTER_CLASSES);
		if(StringUtils.isNotBlank(filterClassNames)) {
			LOG.info("Load filter classes: classNames=" + filterClassNames);
			String[] aClazz = filterClassNames.split("\\s*,\\s*");
			for(String clazz : aClazz) {
				TermFilter filter = ReflectionUtils.getInstance(
						clazz, TermFilter.class,  new Object[] { context });
				if(filter == null) {
					throw new RuntimeException("Fail to reflect: class=" + clazz);
				}
				filters.add(filter);
				LOG.info("Added filter instance: filter=" + filter);
			}
		}
	}
	
	@Override
	public void fire() {
		int labelCnt = context.getFDMetadata().getPreprocessDir().list().length;
		LOG.info("Start to collect: labelCnt=" + labelCnt);
		latch = new CountDownLatch(labelCnt);
		executorService = ThreadPoolUtils.getExecutor();
		try {
			for(String label : context.getFDMetadata().getPreprocessDir().list()) {
				LOG.info("Collect words for: label=" + label);
				executorService.execute(new EachLabelWordAnalysisWorker(label));
			}
		} catch(Exception e){
			LOG.error(e.getMessage());
		} finally {
			try {
				latch.await();
			} catch (InterruptedException e) { 
				LOG.error("InterruptedException" + e.getMessage());
			} catch(Exception e) {
				LOG.error("execute error: " + e.getMessage());
			} 
			LOG.info("Shutdown executor service: " + executorService);
		}
		
		// output statistics
		stat();
	}
	
	protected void filterTerms(Map<String, Term> terms) {
		for(TermFilter filter : filters) {
			filter.filter(terms);
		}
	}

	private void stat() {
		LOG.info("STAT: totalDocCount=" + context.getVectorMetadata().totalDocCount());
		LOG.info("STAT: labelCount=" + context.getVectorMetadata().labelCount());
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context.getVectorMetadata().termTableIterator();
		while(iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> entry = iter.next();
			Iterator<Entry<String, Map<String, Term>>> docIter = entry.getValue().entrySet().iterator();
			int termCount = 0;
			while(docIter.hasNext()) {
				termCount += docIter.next().getValue().size();
			}
			LOG.info("STAT: label=" + entry.getKey() + ", docCount=" + entry.getValue().size() + ", termCount=" + termCount);
		}
	}
	
	private final class EachLabelWordAnalysisWorker extends Thread {
		
		private final String label;
		private final DocumentAnalyzer analyzer;
		
		public EachLabelWordAnalysisWorker(String label) {
			this.label = label;
			String analyzerClass = context.getConfiguration().get(ConfigKeys.DOCUMENT_ANALYZER_CLASS);
			LOG.info("Analyzer class name: class=" + analyzerClass);
			analyzer = ReflectionUtils.getInstance(
					analyzerClass, DocumentAnalyzer.class, new Object[] { context.getConfiguration() });
		}
		
		// 计算文档中的词频
		@Override
		public void run() {
			try {
				File labelDir = new File(context.getFDMetadata().getPreprocessDir(), label);
				File[] files = labelDir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.getAbsolutePath().endsWith(context.getFDMetadata().getFileExtensionName());
					}
				});
				LOG.info("Prepare to analyze: label=" + label + ", totalFiles=" + files.length);
				int n = 0;
				for(File file : files) {
					analyze(label, file);
					++n;
				}
				LOG.info("Finish to analyze: label=" + label + ", fileCount=" + n);
			} catch(Exception e){
				LOG.error(e.getMessage());
			} finally {
				latch.countDown();
			}
		}
		
		// 统计每个文档包含的词和词频
		protected void analyze(String label, File file) {
			String doc = file.getAbsolutePath();
			LOG.debug("Process document: label=" + label + ", file=" + doc);
			analyze(file, label);
		}
		
		private void analyze(File file, String label) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
				String line = null;
				while((line = br.readLine()) != null) {
					String[] arr = line.split("\t");
					String quest = arr[1];
					List<Set<String>> sentences = Lists.newArrayList();
					Map<String, Term> terms = analyzer.analyzer(quest, sentences);
					// filter terms
					filterTerms(terms);
					// construct memory structure
					context.getVectorMetadata().addTerms(label, quest, terms);
					// add inverted table as needed
					context.getVectorMetadata().addTermsToInvertedTable(label, quest, terms);
					// add sentence
					context.getVectorMetadata().addSentence(label, quest, sentences);
				}
			} catch (Exception e) {
				throw new RuntimeException("", e);
			} finally {
				try {
					if(br != null) {
						br.close();
					}
				} catch (IOException e) {
					LOG.warn(e);
				}
			}
		}
	}
}
