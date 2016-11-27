package com.lbc.nlp_algorithm.clustering.hac.common.vectorization;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lbc.nlp_algorithm.clustering.hac.properties.PropertiesUtil;
import com.lbc.nlp_modules.common.StringUtil;

public class SplitedText2VectorTfidf implements Vectorization {
	private Vector vectorClone;
	private Set<String> stopSet = Sets.newHashSet();
	private boolean combineNumbers = PropertiesUtil.getVectorizationProperties().getCombineNumbers();

	public SplitedText2VectorTfidf(Vector vectorClone) {
		this.vectorClone = vectorClone;
	}
	public SplitedText2VectorTfidf(Vector vectorClone, Set<String> stopSet) {
		this.vectorClone = vectorClone;
	}

	@Override
	public Vector[] convert2Vector(Object[] objList) {
		int nObservations = objList.length;
		Map<String, Integer> df = Maps.newHashMap();
		Map<String, Integer> feature = Maps.newHashMap();
		int featureId = 0;
		for (int i = 0; i < nObservations; i++) {
			String sentence = String.valueOf(objList[i]);
			String[] words = sentence.split("\\s+");
			Set<String> set = Sets.newHashSet();
			for (String word : words) {
				if (stopSet.contains(word))
					continue;
				if (combineNumbers && StringUtil.isNumber(word))	//数组处理为0认为均相同
					word = "0";
				set.add(word);
			}
			for (String word : set) {
				if (null == df.get(word))
					df.put(word, 0);
				df.put(word, df.get(word) + 1);
				if (null == feature.get(word))
					feature.put(word, featureId++);
			}
		}

		Vector vectors[] = new Vector[nObservations];
		vectors = convert2Vector(objList, feature, df);
		return vectors;
	}

	private Vector[] convert2Vector(Object[] objList, Map<String, Integer> feature,
			Map<String, Integer> df) {
		int nObservations = objList.length;
		Vector vectors[] = new Vector[nObservations];
		//TODO 这里暂时没有考虑tf，只用了idf
		for (int i = 0; i < nObservations; i++) {
			String[] words = (String.valueOf(objList[i])).split("\\s+");
			Set<String> used = Sets.newHashSet();
			Vector v = vectorClone.clone();
			for (String word : words) {
				if (stopSet.contains(word))
					continue;
				if (combineNumbers && StringUtil.isNumber(word))	//数组处理为0认为均相同
					word = "0";
				if (used.contains(word))
					continue;
				used.add(word);
				v.set(feature.get(word), Math.log(nObservations / (double) df.get(word)));
			}
			v.setLength(feature.size()); //denseVector的长度无法自己生成，必须手动设置, sparseVector的长度可自己生成
			v.norm();
			vectors[i] = v;
		}
		return vectors;
	}

}
