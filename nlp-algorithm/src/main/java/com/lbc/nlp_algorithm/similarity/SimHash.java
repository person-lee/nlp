package com.lbc.nlp_algorithm.similarity;

import java.util.List;

import com.lbc.nlp_algorithm.common.algorithms.HammingDistance;
import com.lbc.nlp_algorithm.common.algorithms.MurmurHash;
import com.lbc.nlp_algorithm.cutword.Cutword;
import com.lbc.nlp_algorithm.cutword.ansj.AnjsCutword;
import com.lbc.nlp_domain.Word;

/**
 * simhash用于比较大文本，比如500字以上效果都还蛮好，距离小于3的基本都是相似，误判率也比较低。
 * @author cdlibaocang
 *
 */
public class SimHash extends AbstractSimilarity<String>{
	private int bitLen = 64;
	private static SimHash simHash = null;
	
	private SimHash(){
	}
	
	public static SimHash getInstance() {
		if(simHash == null){
			simHash = new SimHash();
		}
		return simHash;
	}
	
	@Override
	public double sim(String subStr, String obStr){
		Cutword cutword = AnjsCutword.getInstance();
		List<Word> segWordSub = cutword.doCutword(subStr);
		List<Word> segWordOb = cutword.doCutword(obStr);

		int dist = HammingDistance.hammingDistance(simhash(segWordSub), simhash(segWordOb));
		int textLen1 = subStr.length();
		int textLen2 = obStr.length();
		int maxLen = textLen1 > textLen2 ? textLen1 : textLen2;
		return 1 - (double) dist / maxLen;
	}
	
	private long simhash(List<Word> tokens) {
		int[] bits = new int[bitLen];		
		for (Word t : tokens) {
			long v = MurmurHash.hash64(t.getTerm());
			for (int i = bitLen; i >= 1; --i) {
				if (((v >> (bitLen - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
		}
		long hash = 0x0000000000000000;
		long one = 0x0000000000000001;
		for (int i = bitLen; i >= 1; --i) {
			if (bits[i - 1] > 1) {
				hash |= one;
			}
			one = one << 1;
		}
		return hash;
	}
	
	public static void main(String[] args) {  
		SimHash simHash = SimHash.getInstance();
		String str1 = "为了降低空间和时间计算复杂性，可以对shingle集合进行抽样，比如Min-Wise，Modm，Mins方法；";
		String str2 = "为了降低空间和时间计算复杂性，可以对文件特征进行降维处理，比如simhash和bloom filter；";
		System.out.println(simHash.sim(str1, str2));
    }

}
