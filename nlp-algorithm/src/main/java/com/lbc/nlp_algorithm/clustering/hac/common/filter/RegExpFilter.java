package com.lbc.nlp_algorithm.clustering.hac.common.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public class RegExpFilter implements Filter {
	private List<Pattern> patterns = Lists.newArrayList();
	
	public RegExpFilter(List<String> expressions) {
		for (String exp : expressions)
			patterns.add(Pattern.compile(exp));
	}

	@Override
	public boolean isUseful(String data) {
		for (Pattern pattern : patterns) {
			if (pattern.matcher(data).matches())
				return false;
		}
		return true;
	}

	public static void main(String[] args) {
		List<String> expressions = new ArrayList<String>();
		expressions.add("http\\://item\\.jd\\.com/.*\\.html.*");
		RegExpFilter regExpFilter = new RegExpFilter(expressions);
		System.out.println(regExpFilter.isUseful("423432	http://item.jd.com/1012307881.html"));
	}

	@Override
	public String filter(String data) {
		return data;
	}
}
