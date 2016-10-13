package com.lbc.nlp_algorithm.similarity;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.lbc.nlp_algorithm.cutword.ansj.AnjsCutword;
import com.lbc.nlp_domain.Word;

public class Shingle extends AbstractSimilarity<String>{
	private Shingle(){
	}
	
	private static Shingle getInstance(){
		return Nested.singleton;
	}
	
	private static class Nested{
		private static Shingle singleton = new Shingle();
	}
	
	@Override
	public double sim(String subStr, String obStr){
		return jaccard_similarity_coeff(shingles(subStr), shingles(obStr));
	}
	
	private Set<String> shingles(File file) throws Exception{
		if(file != null){
			HashSet<String> shingles = new HashSet<String>();
			List<String> lines = FileUtils.readLines(file, Charsets.UTF_8);
			for(String line : lines){
				List<Word> segWord = AnjsCutword.getInstance().doCutword(line);
				for(Word word : segWord){
					shingles.add(word.getTerm());
				}
			}
			return shingles;
		}else{
			return Collections.EMPTY_SET;
		}
	}
	
	private Set<String> shingles(String str) {
		if(StringUtils.isNoneBlank(str)){
			HashSet<String> shingles = new HashSet<String>();
			List<Word> segWord = AnjsCutword.getInstance().doCutword(str);
			for(Word word : segWord){
				shingles.add(word.getTerm());
			}
			
			return shingles;
		}else{
			return Collections.EMPTY_SET;
		}
	}

	private float jaccard_similarity_coeff(Set<String> shinglesA,
			Set<String> shinglesB) {
		float neumerator = Sets.intersection(shinglesA, shinglesB).size();
		float denominator = Sets.union(shinglesA, shinglesB).size();
		return neumerator / denominator;
	}

	public static void main(String[] args) {
		String str1 = new String("今天星期五");
		String str2 = new String("今天星期六");
		System.out.println(Shingle.getInstance().sim(str1, str2));
	}

}
