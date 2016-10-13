package com.lbc.nlp_algorithm.similarity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import com.lbc.nlp_algorithm.cutword.ansj.AnjsCutword;
import com.lbc.nlp_algorithm.loadFile.LoadStopWord;
import com.lbc.nlp_domain.Word;

public class BloomFilterSim extends AbstractSimilarity<Word>{
	private static BloomFilter<Word> bloomFilter;
	private List<Word> stored = Lists.newArrayList();
	
	private static BloomFilterSim bloomFilterSim = null;
	
	private BloomFilterSim(){
		init();
	}
	
	private static BloomFilterSim getInstance(){
		if(null == bloomFilterSim){
			bloomFilterSim = new BloomFilterSim();
		}
		return bloomFilterSim;
	}
	
	//初始化加载本地数据
	private void init(){
		readFile();
		setUpBloomFilter(stored.size());
	}
	
	private void readFile(){
		List<String> lines = new ArrayList<String>();
		try {
			InputStreamReader streamReader = 
					new InputStreamReader(LoadStopWord.class.getClassLoader().getResourceAsStream("unused/cate/manual.txt"));
			lines = IOUtils.readLines(streamReader);
		} catch (IOException e) {
			System.out.println("文件读取异常" + e.getMessage());
		}
		for(String line : lines){
			List<Word> segWord = AnjsCutword.getInstance().doCutword(line);
			stored.addAll(segWord);
		}
	}
	
	@Override
	public double sim(Word word){
		boolean mightContain = bloomFilter.mightContain(word);
		if(mightContain){
			return 1d;
		}else{
			return 0d;
		}
	}
	
	private class WordFunnel implements Funnel<Word> {
        @Override
        public void funnel(Word word, PrimitiveSink into) {
            into.putString(word.getTerm(), Charsets.UTF_8);
        }
    }
	
	private void setUpBloomFilter(int numInsertions) {
        bloomFilter = BloomFilter.create(new WordFunnel(), numInsertions);
        addStoredWordsToBloomFilter();
    }
	
	private void addStoredWordsToBloomFilter() {
        for (Word word : stored) {
            bloomFilter.put(word);
        }
    }
	
	public static void main(String[] args){
		List<Word> segWord = AnjsCutword.getInstance().doCutword("翡翠");
		for (Word word : segWord){
			System.out.println(BloomFilterSim.getInstance().sim(word));
		}
	}

}
