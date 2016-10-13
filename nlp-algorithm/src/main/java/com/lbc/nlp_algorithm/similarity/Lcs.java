package com.lbc.nlp_algorithm.similarity;

import com.lbc.nlp_algorithm.common.ArrayUtils;


/**
 * 最大公共序列算法（LCS）
 * @author cdlibaocang
 *
 */
public class Lcs extends AbstractSimilarity<String>{
	private static Lcs lcs = null;
	
	private Lcs() {
	}
	
	public static Lcs getInstance(){
		if(lcs == null){
			lcs = new Lcs();
		}
		return lcs;
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
				if(row == 0 || col == 0){
					//如果相等则加1
					if(subStr.charAt(row) == obStr.charAt(col)){
						countArr[row][col] = 1;
					}else{
						if(row == 0 && col == 0){
							countArr[row][col] = 0;
						}else{
							if(row == 0){
								countArr[row][col] = countArr[row][col - 1];
							}else{
								countArr[row][col] = countArr[row - 1][col];
							}
						}
					}
				}else{
					//如果相等则加1
					if(subStr.charAt(row) == obStr.charAt(col)){
						countArr[row][col] = countArr[row - 1][col - 1] + 1;
					}else{
						countArr[row][col] = Math.max(countArr[row][col - 1], countArr[row - 1][col]);
					}
					
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
		Lcs lcs = Lcs.getInstance();
		System.out.println(lcs.sim(str1, str2));
	}
}