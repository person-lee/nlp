package com.lbc.nlp_algorithm.prepocess.core.component.train;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lbc.nlp_algorithm.classification.liblinear.FeatureNode;
import com.lbc.nlp_algorithm.classification.liblinear.Linear;
import com.lbc.nlp_algorithm.classification.liblinear.Model;
import com.lbc.nlp_algorithm.prepocess.measure.utils.Roc;
import com.lbc.nlp_domain.PredictResult;

public class ModelValid {
	private static final Log log = LogFactory.getLog(ModelValid.class);
	
	public static <U> double computeAccuracy(Map<U, List<FeatureNode[]>> testData, Model model, List<PredictResult> predictResults){
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
						
						PredictResult predictResult = new PredictResult.Builder().index(h)
								.label((Integer)label).predictProb(predictValue).probility(probabilities).build();
						predictResults.add(predictResult);
						if (!label.equals(h)) {
							log.info("实际分类为：" + label + "预测分类为：" + h + "，预测概率：" + predictValue);
						}
					}
				}
			}
			return correctNum * 1d / totalNum;
		} else {
			return 0d;
		}
	}
	
	/**
	 * 计算模型的性能。并把错误的预测和整体的性能结果写入文件。
	 * @param predictResults
	 * @param testData
	 * @param cluster
	 * @param false_predict
	 * @param performance_info
	 * @throws IOException
	 */
	public static void calculatePerformance(List<PredictResult> predictResults, List<String> cates) throws Exception {
		int totalNum=predictResults.size();
		int right_num=0, business_right=0, other_right=0;
		int otherNum = 0;
		for(PredictResult predictResult : predictResults){
			Roc roc = new Roc();
			boolean positive = predictResult.getIndex() == predictResult.getLabel();
			roc.addResult(predictResult.getPredictProb(), positive);
			if (positive) {
				if ("other".equals(cates.get(predictResult.getIndex()))) {
					other_right += 1;
				} else {
					business_right += 1;
				}
				right_num += 1;
			}
			
			if ("other".equals(cates.get(predictResult.getIndex()))) {
				otherNum ++;
			}
		}
		double accurate = 1. * right_num/totalNum;
		double businessAccurate = 1. * business_right/(totalNum-otherNum);
		double otherAccurate = 1. * other_right/otherNum;
		log.info("整体准确率为：" + accurate + "," + businessAccurate + "，other准确率为：" + otherAccurate);
	}
}
