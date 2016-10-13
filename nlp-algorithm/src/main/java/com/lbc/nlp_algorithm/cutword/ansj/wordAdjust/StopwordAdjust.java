package com.lbc.nlp_algorithm.cutword.ansj.wordAdjust;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.lbc.nlp_algorithm.loadFile.LoadStopWord;
import com.lbc.nlp_domain.Word;

public class StopwordAdjust implements WordAdjust{
	private StopwordAdjust(){
	}
	
	public static StopwordAdjust getInstance(){
		return Nested.singleton;
	}
	
	private static class Nested {
		private static StopwordAdjust singleton = new StopwordAdjust();
	}
	
	@Override
	public void adjust(List<Word> words){
		Set<String> stopWords = LoadStopWord.getInstance().getStopword();
		List<Word> rmWords = new ArrayList<Word>();
		for(Word word : words){
			if(stopWords.contains(word.getTerm())){
				rmWords.add(word);
			}
		}
		words.removeAll(rmWords);
	}

}
