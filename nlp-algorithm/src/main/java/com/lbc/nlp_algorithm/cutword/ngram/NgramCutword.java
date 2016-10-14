package com.lbc.nlp_algorithm.cutword.ngram;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.lbc.nlp_algorithm.cutword.Cutword;
import com.lbc.nlp_modules.common.Constants;
import com.lbc.nlp_modules.common.StringUtil;
import com.lbc.nlp_modules.common.jianfan.JianFan;

public class NgramCutword implements Cutword{
	private int minLen = 1;
	private int maxLen = 2;
	
	public NgramCutword(int minLen, int maxLen) {
		this.minLen = minLen;
		this.maxLen = maxLen;
	}
	
	public NgramCutword() {
		
	}
	
	public static NgramCutword getInstance() {
		return Nested.singleton;
	}
	
	private static class Nested {
		public static NgramCutword singleton = new NgramCutword();
	}
	
	@Override
	public List<String> doCutword(String text) {
		if (StringUtils.isBlank(text)) {
			return null;
		}
            
		text = JianFan.f2j(text);
		text = StringUtil.convertToLower(text);

        List<String> words = Lists.newArrayList();
        words.addAll(tokens(text, minLen, maxLen));

        return words;
	}
	
	/**
	 * 对句子进行切分
	 * @param line
	 * @param minLen
	 * @param maxLen
	 * @return
	 */
	private List<String> tokens(String line, int minLen, int maxLen) {
        List<String> tokens = Lists.newArrayList();
        List<String> singleChars = Lists.newArrayList();
        List<String> tmpTokens = hyperLinkMark(line);
        StringBuilder builder = new StringBuilder();
        for (String token : tmpTokens) {
            if (Constants.tokenTypes.contains(token)) {
                singleChars.add(token);
                continue;
            }

            for (char ch : token.toCharArray()) {
                if (StringUtil.isChineseCharacter(ch)) {
                    if (builder.length() > 0) {
                        singleChars.addAll(typeMark(builder.toString()));
                        builder = new StringBuilder();
                    }
                    singleChars.add(String.valueOf(ch));
                } else if (StringUtil.isEnglishOrNumberCharacter(ch) || "_".charAt(0) == ch) {
                    builder.append(ch);
                } else {
                    if (builder.length() > 0) {
                        singleChars.addAll(typeMark(builder.toString()));
                        builder = new StringBuilder();
                    }
                    tokens.addAll(generateTerms(singleChars, minLen, maxLen));
                    singleChars.clear();
                }
            }
            if (builder.length() > 0) {
                singleChars.addAll(typeMark(builder.toString()));
                builder = new StringBuilder();
            }
        }
        tokens.addAll(generateTerms(singleChars, minLen, maxLen));

        return tokens;
    }
	
	/**
     * 把line中的超链接替换成NUMBER_LETTER_TYPE
     * @param line  输入文本
     * @return 处理后的文本
     */
    private List<String> hyperLinkMark(String line) {
        if (StringUtils.isBlank(line))
            return Collections.EMPTY_LIST;

        List<String> tokens = Lists.newArrayList();
        Pattern pattern = Pattern.compile("http[s]?://([\\w-]+\\.)+[\\w-]+(/[\\w/?%&-=.]*)?[#[\\w-]+]?");
        Matcher matcher = pattern.matcher(line);
        int start = 0;
        while(matcher.find()) {
            String link = matcher.group();
            int index = line.indexOf(link);
            if (index - start > 0) {
                tokens.add(line.substring(start, index));
            }
            tokens.add(Constants.LINK_TYPE);
            start = index + link.length();
        }
        if (line.length() - start > 0) {
            tokens.add(line.substring(start));
        }

        return tokens;
    }
    
    /**
     * ngram对句子进行切分
     * @param singleChars
     * @param minLen 切分的最小长度
     * @param maxLen 切分的最大长度
     * @return
     */
    private List<String> generateTerms(List<String> singleChars, int minLen, int maxLen) {
        List<String> terms = Lists.newArrayList();
        for (int i = 0; i < singleChars.size(); i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = i; j < i + minLen && j < singleChars.size(); j++) {
                builder.append(singleChars.get(j));
            }
            terms.add(builder.toString());
            for (int j = minLen; j < maxLen && i + j < singleChars.size(); j++) {
                builder.append(singleChars.get(i + j));
                terms.add(builder.toString());
            }
        }

        return terms;
    }
    
    /**
     * 对英文和数字进行标记
     * @param line
     * @return
     */
    private List<String> typeMark(String line) {
        List<String> tokens = Lists.newArrayList();
        if (StringUtils.isBlank(line)) {
        	return tokens;
        }
        
        if (Constants.GenericType.containsKey(line)) {
        	tokens.add(Constants.GenericType.get(line));
        } else if (line.matches("[0-9]+")) {
        	tokens.add(Constants.NUMBER_TYPE);
        } else if (line.matches("[a-zA-Z]+")) {
        	tokens.add(Constants.LETTER_TYPE);
        } else {
        	tokens.add(Constants.NUMBER_LETTER_TYPE);
        }
        
        return tokens;
    }
    
    public static void main(String[] args) {
    	NgramCutword tokenizer = new NgramCutword(1, 2);
        List<String> tokens = tokenizer.doCutword("cate62 BRAND 、可以看网业上的在线视频吗?(falsh视频)");
        Set<String> noDupTokens = new HashSet<String>();
        noDupTokens.addAll(tokens);
        for (String token : noDupTokens) {
            System.out.println(token);
        }
    }

}
