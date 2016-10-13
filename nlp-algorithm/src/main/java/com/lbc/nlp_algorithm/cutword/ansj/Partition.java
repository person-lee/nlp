package com.lbc.nlp_algorithm.cutword.ansj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.lbc.nlp_domain.PartitionNormDefine;
import com.lbc.nlp_domain.SpeechDefine;
import com.lbc.nlp_domain.Word;

class Partition {
    static final String LINK_REGEX = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
    static final String ITEM_LINK_REGEX = "http:\\/\\/item.jd.com\\/\\d{6,11}.html([#?][\\w\\-\\@?^=%&amp;/~\\+#]*)?";
    /**
     * 时间正则表达式，如果2014-06-29 22:27:20
     */
    static final String TIME_REGEX = "20[0-9]{2}-[0-9]{2}-[0-9]{2}( [0-9]{2}:[0-9]{2}:[0-9]{2})?";
    /**
     * 邮箱地址
     */
    static final String MAIL_REGEX = "([a-zA-Z0-9_\\.\\-])+@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+";

    static List<Pair<Pattern, String>> PATTERN_NAME = new ArrayList<>();
    static {
        PATTERN_NAME.add(Pair.of(Pattern.compile(ITEM_LINK_REGEX), PartitionNormDefine.NormItem));
        PATTERN_NAME.add(Pair.of(Pattern.compile(LINK_REGEX), PartitionNormDefine.NormLink));
        PATTERN_NAME.add(Pair.of(Pattern.compile(TIME_REGEX), PartitionNormDefine.NormTime));
        PATTERN_NAME.add(Pair.of(Pattern.compile(MAIL_REGEX), PartitionNormDefine.NormMail));
    }

    /**
     * 对句子按照配置的规则进行partition拆分成多段.
     * @param sentence
     * @return
     */
    static List<Word> partitionSentence(String sentence){
        List<Word> partitions = new ArrayList<>();
        if(StringUtils.isEmpty(sentence)){
            return partitions;
        }

        Stack<Word> unpartitioned = new Stack<>();
        unpartitioned.add(new Word(sentence, null, 0, sentence.length()-1, sentence));
        while(!unpartitioned.empty()){
            Word part = unpartitioned.pop();
            boolean matched = false;
            for(Pair<Pattern, String> entry : PATTERN_NAME){
                Pattern pattern = entry.getKey();
                String s = part.getTerm();
                Matcher matcher = pattern.matcher(s);
                if(matcher.find()){
                    matched = true;
                    int start = matcher.start();
                    int end = matcher.end();
                    Word match_part = new Word(s.substring(start, end), 
                    		SpeechDefine.PartitionSpeech, part.getBegPos() + start, 
                    		part.getBegPos() + end - 1, entry.getValue());
                    partitions.add(match_part);

                    // 对partition左边和右边的部分继续做partition
                    if(start > 0){
                        List<Word> left = partitionSentence(s.substring(0, start));
                        for(Word word : left){
                            word.setBegPos(part.getBegPos() + word.getBegPos());
                            word.setEndPos(part.getBegPos() + word.getEndPos());
                            if(word.getSpeech() != null){
                                unpartitioned.add(word);
                            } else {
                                partitions.add(word);
                            }
                        }
                    }
                    if(end < s.length()){
                        List<Word> right = partitionSentence(s.substring(end));
                        for(Word word : right){
                            word.setBegPos(part.getBegPos() + word.getBegPos() + end);
                            word.setEndPos(part.getBegPos() + word.getEndPos() + end);
                            if(word.getSpeech() != null){
                                unpartitioned.add(word);
                            } else {
                                partitions.add(word);
                            }
                        }
                    }
                    break;
                }
            }
            if(!matched){
                partitions.add(part);
            }
        }

        // 按在句子中的顺序排序
        Collections.sort(partitions, new Comparator<Word>() {
            @Override
            public int compare(Word o1, Word o2) {
                return o1.getBegPos() - o2.getBegPos();
            }
        });
        return partitions;
    }

    public static void main(String[] args) throws Exception{
        List<Word> words = partitionSentence("http://item.jd.com/1270200.html http://item.jd.com/1270201.html#none http://item.jd.com/1270202.html#comment我https://www.baidu.com/s?wd=YY%20%20%E7%BE%8E%E8%82%A1&ie=utf-8&tn=SE_baiduhomet1_zehq5d4k&rsv_idx=2呵呵");
        for(Word word : words){
            System.out.println(word);
        }
        words = partitionSentence("2015-08-01 12:12:58 您的订单已经拣货完毕，待出库交付圆通快递lj_m@163.com cdliujia@jd.com");
        for(Word word : words){
            System.out.println(word);
        }
    }
}
