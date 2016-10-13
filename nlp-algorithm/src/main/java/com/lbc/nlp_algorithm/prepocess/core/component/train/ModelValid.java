package com.lbc.nlp_algorithm.prepocess.core.component.train;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.classification.liblinear.FeatureNode;
import com.lbc.nlp_algorithm.classification.liblinear.Linear;
import com.lbc.nlp_algorithm.classification.liblinear.Model;

public class ModelValid {
	private static final Logger log = LoggerFactory.getLogger(ModelValid.class.getName());
	
	public static <U> double computeAccuracy(Map<U, List<FeatureNode[]>> testData, Model model){
		if (MapUtils.isNotEmpty(testData)) {
			int correctNum = 0;
			int totalNum = 0;
			for (Entry<U, List<FeatureNode[]>> cate : testData.entrySet()) {
				List<FeatureNode[]> sentences = cate.getValue();
				U label = cate.getKey();
				
				totalNum += sentences.size();
				
				if (CollectionUtils.isNotEmpty(sentences)) {
					for (FeatureNode[] sentence : sentences) {
						// 预测输入问题
						double[] probabilities = new double[model.getLabels().length];
						Integer h = -1;
						if (model.isProbabilityModel()) {
							h = (int) Linear.predictProbability(model, sentence, probabilities);
						} else {
							h = (int) Linear.predictValues(model, sentence, probabilities);
						}
						
						if (label.equals(h)) {
							correctNum++;
						}
						
						Double predictValue = 0.0;
						if (h != -1) {
							predictValue = probabilities[h];
						}
						log.info("实际分类为：" + label + "预测分类为：" + h + "，预测概率：" + predictValue);
					}
				}
			}
			return correctNum * 1d / totalNum;
		} else {
			return 0d;
		}
	}

}
