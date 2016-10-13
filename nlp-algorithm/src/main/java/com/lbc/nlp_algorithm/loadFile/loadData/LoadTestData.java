package com.lbc.nlp_algorithm.loadFile.loadData;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.lbc.nlp_algorithm.classification.liblinear.FeatureNode;
import com.lbc.nlp_modules.common.tuple.Pair;

public class LoadTestData {
	public Pair<Integer, Map<Integer, List<FeatureNode[]>>> loadData(String dirKey, String filenameKey) throws IOException{
		return new Pair<Integer, Map<Integer,List<FeatureNode[]>>>(null, null);
	}

}
