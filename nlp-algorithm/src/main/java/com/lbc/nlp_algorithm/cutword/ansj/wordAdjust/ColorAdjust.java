package com.lbc.nlp_algorithm.cutword.ansj.wordAdjust;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.lbc.nlp_domain.SpeechDefine;
import com.lbc.nlp_domain.Word;

public class ColorAdjust implements WordAdjust {

    private static final Set<Character> colours = new HashSet<>(Arrays.asList(new Character[]
                    {'白', '黑', '银', '灰', '赤', '橙', '黄', '绿', '青', '蓝', '紫', '金', '铜', '粉', '棕', '红', '褐', '兰', '墨'}));

    /**
     * 对颜⾊词进⾏行规范化。如把“多瑙蓝”规范化为“蓝色”。
     * 如果原词包含多种颜色,则规范化之后多种颜色使用空格分开,如“橙黄色”规范化为“橙色 黄色”
     * @param words
     * @return
     */
    @Override
    public void adjust(List<Word> words) {
        for (Word word : words) {
            if (SpeechDefine.ColourSpeech.equalsIgnoreCase(word.getSpeech())) {
                StringBuilder norm = new StringBuilder();
                for(char c : word.getTerm().toCharArray()){
                    if(colours.contains(c)){
                    	norm.append(c);
                    	norm.append("色 ");
                    }
                }
                if(norm.length() == 0){
                    norm.append(word.getTerm());
                }
                word.setNormedTerm(StringUtils.trimToEmpty(norm.toString()));
            }
        }
    }
    
    private ColorAdjust(){
    }
    
    public static ColorAdjust getInstance(){
    	return Nested.signleton;
    }
    
    public static class Nested {
    	public static ColorAdjust signleton = new ColorAdjust();
    }
}
