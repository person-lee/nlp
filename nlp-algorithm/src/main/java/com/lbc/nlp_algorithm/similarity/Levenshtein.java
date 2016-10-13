package com.lbc.nlp_algorithm.similarity;

import com.lbc.nlp_algorithm.common.ArrayUtils;

/**
 * 最小编辑距离算法
 * @author cdlibaocang
 *
 */
public class Levenshtein extends AbstractSimilarity<String>{

	private Levenshtein(){
		
	}
	
	public static Levenshtein getInstance(){
		return Nested.singleton;
	}

	private static class Nested{
		public static Levenshtein singleton = new Levenshtein();
	}
	
	@Override
	public double sim(String subStr, String obStr){
		int textLen1 = subStr.length();
		int textLen2 = obStr.length();
		int maxLen = textLen1 > textLen2 ? textLen1 : textLen2;
		return 1 - (double) simStr(subStr, obStr, textLen1, textLen2) / textLen2;
	}
	
	private int simStr(String subStr, String obStr, int len1, int len2){
		len1 = len1 + 1; len2 = len2 + 1;//加上边框
		int[][] move = new int[len1][len2];
		
		int cost = 0;
		for (int row = 0; row < len1; row++) {
			for (int col = 0; col < len2; col++) {
				if (row == 0 || col == 0) {
					move[row][col] = row > 0 ? row : col;
				} else {
					if (subStr.charAt(row - 1) == obStr.charAt(col - 1)) {
						cost = 0;
					} else {
						cost = 1;
					}
					move[row][col] = min(move[row - 1][col] + 1, move[row][col - 1] + 1, move[row - 1][col - 1] + cost);
				}
			}
		}
		
		ArrayUtils.printArray(move, len1, len2);
		
		return move[len1 - 1][len2 - 1];
	}
	
	private int min(int first, int second, int third) {
		return Math.min(first, Math.min(second, third));
	}
	
	public static void main(String[] args) {  
		Levenshtein ls = Levenshtein.getInstance();
		String str1 = "今天星期四";
		String str2 = "今天是星期五哦";
		System.out.println(ls.sim(str1, str2));  
    }

}
