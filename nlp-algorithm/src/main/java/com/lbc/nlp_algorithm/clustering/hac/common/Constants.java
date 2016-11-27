package com.lbc.nlp_algorithm.clustering.hac.common;

/**
 * 所有模块通用的常量，常量最好也规范化，这样看起来更统一，应用上也更方便
 *
 */
public class Constants {
	
	//保证该类不能被实例化，这个类只当一个结构体在使用
	private Constants() {}
	
	//字符串之间的分隔符
	public final static String STRING_SEPERATOR = "##seperator##";
	
	//线上格式
	public final static String ONLINE_STRING_SEPERATOR = "\t";
	
	//cluster之间的分隔符
	public final static String CLUSTER_SEPERATOR = "------------------------------------";
	
	public static final String trimStr = "、.。,，!！?？嗯啊哦呢吗呀";
}
