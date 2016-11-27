package com.lbc.nlp_algorithm.clustering.hac.common.filter;

//数据过滤器，目前用在读入数据的时候进行数据过滤,目前只支持字符串
public interface Filter {

	//是否是需要的数据
	public boolean isUseful(String data);
	
	public String filter(String data);
}
