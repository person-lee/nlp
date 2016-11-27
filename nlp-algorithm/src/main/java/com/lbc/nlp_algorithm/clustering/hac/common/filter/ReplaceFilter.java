package com.lbc.nlp_algorithm.clustering.hac.common.filter;

public class ReplaceFilter implements Filter {
	private String expression;
	private String replaced;
	
	public ReplaceFilter(String expression, String replaced) {
		this.expression = expression;
		this.replaced = replaced;
	}

	@Override
	public boolean isUseful(String data) {
		return true;
	}

	@Override
	public String filter(String data) {
		return data.replaceAll(expression, replaced);
	}

}
