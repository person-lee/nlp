package com.lbc.nlp_algorithm.common;

public class ArrayUtils {
	
	public static void printArray(int[][] array, int len1, int len2){
		for(int row = 0; row < len1; row++) {
			for(int col = 0; col < len2; col++) {
				System.out.print(array[row][col] + "\t");
			}
			System.out.println("\n");
		}
	}

}
