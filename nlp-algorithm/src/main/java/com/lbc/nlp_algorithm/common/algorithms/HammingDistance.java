package com.lbc.nlp_algorithm.common.algorithms;

public class HammingDistance {
	public static int hammingDistance(long hash1, long hash2) {
		long i = hash1 ^ hash2;
		i = i - ((i >>> 1) & 0x5555555555555555L);
		i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
		i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		i = i + (i >>> 32);
		return (int) i & 0x7f;
	}
	
	public static boolean isEqual(long lhs, long rhs, short n) {
		short cnt = 0;
	    lhs ^= rhs;
	    while(lhs > 0 && cnt <= n)
	    {
	        lhs &= lhs - 1;
	        cnt++;
	    }
	    if(cnt <= n)
	    {
	        return true;
	    }
	    return false;
	}

}
