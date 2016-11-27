package com.lbc.nlp_algorithm.similarity.hash;

import java.util.Random;

public class PolynomialHash implements Hash {
	Random random = new Random();
	int seedA = random.nextInt(10) + 1;
	int seedB = random.nextInt(10) + 1;
	int seedC = random.nextInt(10) + 1;

	@Override
	public int hash(String str) {
		byte[] bytes = str.getBytes();
		long hashValue = 31;
		for (long byteVal : bytes) {
			hashValue *= seedA * (byteVal >> 4);
			hashValue += seedB * byteVal + seedC;
		}
		return Math.abs((int) (hashValue % MAX_INT_SMALLER_TWIN_PRIME));
	}

	public static void main(String[] args) {
		Hash instance = new PolynomialHash();
		System.out.println(instance.hash("hello"));
		System.out.println(instance.hash("hallo"));
	}
}
