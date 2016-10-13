package com.lbc.nlp_algorithm.cutword.ansj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.cutword.Cutword;
import com.lbc.nlp_algorithm.cutword.ansj.wordAdjust.ColorAdjust;
import com.lbc.nlp_algorithm.cutword.ansj.wordAdjust.StopwordAdjust;
import com.lbc.nlp_algorithm.similarity.category.Brand;
import com.lbc.nlp_algorithm.similarity.category.ProductCate;
import com.lbc.nlp_domain.SpeechDefine;
import com.lbc.nlp_domain.Word;
import com.lbc.nlp_modules.common.tree.trie.TrieNode;
import com.lbc.nlp_modules.common.tree.trie.TrieUtil;

public class AnjsCutword implements Cutword {
	private final static Logger LOG = LoggerFactory.getLogger(
			AnjsCutword.class.getName());
	
	private final String DigwordResource = "mutable/words_digfrom_problem.txt";
    private final String AddressResource = "mutable/address.txt";
    private final String ColourWordResource = "stable/colour.txt";
    private final String QuestionWordResource = "stable/questword.txt";
    
    private static Boolean isLoading = false;

    private void init() throws IOException {
    	if(!isLoading){
    		addDigwords();
            addAddressWords();
            addColourWords();
            addQuestionWords();

            Set<String> productCateNames = ProductCate.getInstance().getValidCateNames();
            for(String word : productCateNames){
                UserDefineLibrary.insertWord(word.toLowerCase(), SpeechDefine.ProductCateSpeech, Integer.MAX_VALUE);
            }

            for(String word : Brand.getInstance().getFilteredBrandNameLowercases()){
                if(!productCateNames.contains(word)){
                    UserDefineLibrary.insertWord(word.toLowerCase(), SpeechDefine.BrandSpeech, Integer.MAX_VALUE);
                }
            }
            
            isLoading = true;
    	}
        
    }

    private void addAddressWords() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(AddressResource)));
        for(String word=br.readLine(); word!=null; word=br.readLine()){
            UserDefineLibrary.insertWord(word.toLowerCase(), SpeechDefine.AddressSpeech, Integer.MAX_VALUE);
        }
        br.close();
    }

    private void addQuestionWords() throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(QuestionWordResource)));
        for(String word=br.readLine(); word!=null; word=br.readLine()){
            UserDefineLibrary.insertWord(word.toLowerCase(), SpeechDefine.QuestionSpeech, Integer.MAX_VALUE);
        }
        br.close();
    }

    private void addColourWords() throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(ColourWordResource)));
        for(String word=br.readLine(); word!=null; word=br.readLine()){
            UserDefineLibrary.insertWord(word.toLowerCase(), SpeechDefine.ColourSpeech, Integer.MAX_VALUE);
        }
        br.close();
    }

    /**
     * 添加挖掘出的词汇
     * @throws IOException
     */
    private void addDigwords() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(DigwordResource)));
        List<String> words = new ArrayList<>();
        TrieNode termTrie = new TrieNode();
        for(String word = br.readLine(); word!=null; word=br.readLine()){
            if((StringUtils.isAlphanumeric(word) && StringUtils.isAsciiPrintable(word))
                    ||TrieUtil.findSubString(termTrie, word)==null) {
                words.add(word);
                TrieUtil.addWord(termTrie, word);
            }
        }
        br.close();
        for (String word : words) {
            List<Term> terms = ToAnalysis.parse(word);
            if(terms.size()==1 && terms.get(0).getName().equalsIgnoreCase(word)){
                continue;
            }
            // ansj会在切词前把词转为小写,自定义的词典也需要转成小写的词
            UserDefineLibrary.insertWord(word.toLowerCase(), SpeechDefine.DigWordSpeech, Integer.MAX_VALUE);
        }
    }
	
	public List<Word> doCutword(String text) {
		List<Word> wordsList = new ArrayList<>();
        List<Word> partitions = Partition.partitionSentence(text);
        for(Word partition : partitions){
            wordsList.addAll(cutPartition(partition));
        }
        
        adjustWord(wordsList);
        return wordsList;
	}
	
	public static AnjsCutword getInstance() {
		return Nested.singleton;
	}

    private static class Nested{
    	private static AnjsCutword singleton = new AnjsCutword();
    }
	
	private AnjsCutword() {
		try {
			init();
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}
	
	private List<Word> cutPartition(Word partiton) {
        List<Word> wordsList = new ArrayList<Word>();
        if(StringUtils.isNoneBlank(partiton.getSpeech())){
            // 已经是partition好的词,不需要再切分
            wordsList.add(partiton);
            return wordsList;
        }
        String sentence = partiton.getTerm();
        List<Term> terms = ToAnalysis.parse(sentence);
        new NatureRecognition(terms).recognition();

        for (Term term : terms) {
        	String termName = term.getName().toLowerCase();
            String nature=term.getNatureStr();
            String[] param = UserDefineLibrary.getParams(termName);
            if(ArrayUtils.isNotEmpty(param)){
                nature = param[0];
            }
            if(StringUtils.isBlank(termName)){
                // 处理ansj会把空格\t识别为nr的bug.
                nature=null;
            }
            if("null".equals(nature)){
                if(termName.length() == 1 && CharUtils.isAsciiPrintable(termName.charAt(0))){
                    nature = "w";     // ansj 标点符号识别的bug. https://github.com/NLPchina/ansj_seg/issues/204
                }else{
                	nature = null;
                }
            }
            Word word = new Word(termName, nature, partiton.getBegPos() + term.getOffe(), partiton.getBegPos() + term.toValue() - 1);
            wordsList.add(word);
        }

        return wordsList;
    }
	
	private void adjustWord(List<Word> words){
		ColorAdjust.getInstance().adjust(words);
		StopwordAdjust.getInstance().adjust(words);
	}
	
	public static void main(String[] args) {  
		Cutword cutword = AnjsCutword.getInstance();
		List<Word> segWords = cutword.doCutword("今天是星期五哦");
		for (Word word : segWords) {
			System.out.println(word.getTerm()); 
		}
    }

}
