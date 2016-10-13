package com.lbc.nlp_modules.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

public class Constants {
	public static String NUMBER_TYPE = "NUMBER_TYPE"; //连续的数字串替换成这种类别
    public static String LETTER_TYPE = "LETTER_TYPE"; //连续的字母串替换成这种类别
    public static String NUMBER_LETTER_TYPE = "NUMBER_LETTER_TYPE"; //连续的数字或字母替换成这种类别
    public static String PRODUCT_TYPE = "PRODUCT_TYPE"; //商品类别、名称或属性替换成这种类别
    public static String ADDRESS_TYPE = "ADDRESS_TYPE"; //地名替换成这种类别
    public static String LINK_TYPE = "LINK_TYPE"; //链接替换成这种类别

    public static Set<String> tokenTypes = new HashSet<String>();
    static {
        tokenTypes.add(NUMBER_TYPE);
        tokenTypes.add(LETTER_TYPE);
        tokenTypes.add(NUMBER_LETTER_TYPE);
        tokenTypes.add(PRODUCT_TYPE);
        tokenTypes.add(ADDRESS_TYPE);
        tokenTypes.add(LINK_TYPE);
    }
    
    public static Map<String, String> GenericType = Maps.newHashMap();
    static {
    	GenericType.put("wareid", "WAREID");
    	GenericType.put("number", "NUMBER");
    	GenericType.put("engnumtype", "ENGLISH_NUM_TYPE");
    	GenericType.put("engtype", "ALL_ENGLISH");
    	GenericType.put("address", "ADDRESS");
    	GenericType.put("date", "DATE");
    	GenericType.put("dateday", "DATE_DAY");
    	GenericType.put("measure", "MEASURE");
    	GenericType.put("money", "MONEY");
    	GenericType.put("attachment", "ATTACHMENT");
    	GenericType.put("url", "URL");
    	GenericType.put("decimal", "DECIMAL");
    }
    
    public static final class FEATURE_TYPE {
    	public static String TFIDF = "tfidf";
    	public static String BINARY = "binary";
    }
    
}
