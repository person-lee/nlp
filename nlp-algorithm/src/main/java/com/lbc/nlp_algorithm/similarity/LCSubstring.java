package com.lbc.nlp_algorithm.similarity;

import com.lbc.nlp_algorithm.common.ArrayUtils;

/**
 * 最长公共子串
 * @author cdlibaocang
 *
 */
public class LCSubstring extends AbstractSimilarity<String>{
	private static LCSubstring lCSubstring = null;
	
	private LCSubstring() {
	}
	
	public static LCSubstring getInstance(){
		if(lCSubstring == null){
			lCSubstring = new LCSubstring();
		}
		return lCSubstring;
	}
	
	@Override
	public double sim(String subStr, String obStr){
		int textLen1 = subStr.length();
		int textLen2 = obStr.length();
		int maxLen = textLen1 > textLen2 ? textLen1 : textLen2;
		return (double) simStr(subStr, obStr, textLen1, textLen2) / maxLen;
	}
	
	private int simStr(String subStr, String obStr, int len1, int len2){
		int[][] countArr = new int[len1][len2];
		
		int maxLen = 0;
		
		for (int row = 0; row < len1; row++){
			for (int col = 0; col < len2; col++){
				if(subStr.charAt(row) == obStr.charAt(col)){
					if(row == 0 || col == 0){
						countArr[row][col] = 1;
					}else{
						countArr[row][col] = countArr[row - 1][col - 1] + 1;
					}
				}else{
					countArr[row][col] = 0;
				}
				
				if (maxLen < countArr[row][col]){
					maxLen = countArr[row][col];
				}
			}
		}
		ArrayUtils.printArray(countArr, len1, len2);
		return maxLen;
	}
	
	public static void main(String[] args) {
		String str1 = new String("abbbaac");
		String str2 = new String("cbbbadcef");
		LCSubstring lCSubstring = LCSubstring.getInstance();
		System.out.println(lCSubstring.sim(str1, str2));
	}

}
