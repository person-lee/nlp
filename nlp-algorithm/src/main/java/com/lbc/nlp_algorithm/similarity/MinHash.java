package com.lbc.nlp_algorithm.similarity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.lbc.nlp_algorithm.common.algorithms.MurmurHash;
import com.lbc.nlp_algorithm.similarity.hash.Hash;

/**
 * MinHash单Hash实现函数
 * 尚未使用,主要问题：
 * 1、结果有随机性
 * 2、实质为比较两个字符串中具有最小Hash值的一个词，因此聚类效果较差
 */
public class MinHash {
	int numHashes;
	List<Hash> listHashes;

	public MinHash(int numHashes) {
		this.numHashes = numHashes;
		this.listHashes = new ArrayList<Hash>();

		for (int i=0; i<numHashes; i++) {
			this.listHashes.add(new MurmurHash());
		}
	}

	private BigInteger minHash(String source, Hash function) {
		int result = Integer.MAX_VALUE;
		String[] strs = source.split(" ");

		for (String str : strs) {
			int hash = function.hash(str);
			if (hash < result) {
				result = hash;
			}
		}

		return BigInteger.valueOf(result);
	}

	public List<BigInteger> hash(String source) {
		List<BigInteger> result = new ArrayList<BigInteger>();
		for (int i=0; i<numHashes; i++) {
			result.add(minHash(source, listHashes.get(i)));
		}
		return result;
	}

	public float getDistance(String str1, String str2) {
		List<BigInteger> hash1 = hash(str1);
		List<BigInteger> hash2 = hash(str2);

		int match = 0;
		for (int i=0; i<numHashes; i++) {
			if (hash1.get(i).equals(hash2.get(i))) {
				match++;
			}
		}

		return match / (float) numHashes;
	}

	public static void main(String[] args){
		String s1 = "非常 抱歉 ， 因为 队列 只能 不同 ， 您 这个 问题 我 目前 暂时 无法 为 您 解答 ， 不过 您 放心 ， 我 马上 为 您 转接 到 我们 对应 的 队列 组 ， 他们 会 为 您 处理 ， 现在 帮 您 转接 可以 吗 ？ ";
		String s2 = "# e - 可怜 # e - 可怜 非常 抱歉 ， 因为 队列 只能 不同 ， 您 这个 问题 我 目前 暂时 无法 为 您 解答 ， 不过 您 放心 ， 我 马上 为 您 转接 到 我们 对应 的 队列 组 ， 他们 会 为 您 处理 ， 现在 帮 您 转接 可以 吗 ？ ";
		String s3 = "您好 ， 因为 这边 是 理财 专线 ， 队列 只能 不同 ， 您 的 这个 问题 目前 暂时 无法 为 您 解答 呢 ， 不过 您 放心 ， 我 马上 为 您 转接 到 我们 对应 的 队列 组 ， 他们 会 专业 的 为 您 处理 ， 现在 帮 您 转接 您 看 可以 吗 ？ ";
		String s4 = "请问 还有 其他 问题 需要 帮 您 解决 的 吗 ？";

		MinHash instance = new MinHash(10);
		System.out.println(instance.getDistance(s1, s2));
		System.out.println(instance.getDistance(s1, s3));
		System.out.println(instance.getDistance(s1, s4));

	}
}