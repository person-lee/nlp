package com.lbc.nlp_algorithm.clustering.hac.common.filter;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class FilterUtils {
	private static final String stopWords = ". 。 , ， ! ！ ? ？ / \" # 嗯 啊 nbsp";
	
	public static List<Filter> getFilterList() {
		List<Filter> filterList = Lists.newArrayList();
		List<String> expressions = Lists.newArrayList();
		expressions.add("http://item\\.jd\\.com/.*\\.html.*");
		filterList.add(new RegExpFilter(expressions));
		filterList.add(new ReplaceFilter("\"", ""));
		return filterList;
	}

	public static Set<String> getStopWords() {
		Set<String> set = Sets.newHashSet();
		String[] words = stopWords.split("\\s+");
		for (String word : words)
			set.add(word);
		return set;
	}

}
