package com.lbc.nlp_algorithm.prepocess.core.component.test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lbc.nlp_algorithm.classification.liblinear.FeatureNode;
import com.lbc.nlp_algorithm.classification.liblinear.Linear;
import com.lbc.nlp_algorithm.classification.liblinear.Model;
import com.lbc.nlp_algorithm.loadFile.loadData.LoadTrainData;
import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_algorithm.prepocess.core.common.AbstractComponent;
import com.lbc.nlp_modules.common.tuple.Pair;

public class ModelTest extends AbstractComponent {
	private static final Log LOG = LogFactory.getLog(ModelTest.class);
	
	public ModelTest(final Context context) {
		super(context);
	}
	
	@Override
	public void fire() {
		try {
			// 加载测试数据
			Pair<Integer, Map<Integer, List<FeatureNode[]>>> vectorPair = LoadTrainData
					.loadData(context.getConfiguration().get(ConfigKeys.DATASET_TEST_SVM_VECTOR_OUTPUT_DIR), 
							context.getConfiguration().get(ConfigKeys.DATASET_TEST_SVM_VECTOR_FILE));
			Map<Integer, List<FeatureNode[]>> vectors = vectorPair.getValue1();
			
			// 加载模型文件
			Model model = Linear.loadModel(LoadTrainData.loadFile(context.getConfiguration().get(ConfigKeys.DATASET_TEST_SVM_VECTOR_OUTPUT_DIR), 
					context.getConfiguration().get(ConfigKeys.DATASET_TRAIN_MODEL_FILE)));

			for (Entry<Integer, List<FeatureNode[]>> eachCate : vectors.entrySet()) {
				LOG.info("predict label " + eachCate.getKey());
				List<FeatureNode[]> sentences = eachCate.getValue();
				if (CollectionUtils.isNotEmpty(sentences)) {
					for (FeatureNode[] sentence : sentences) {
//						// 加载分类标签
//						BufferedReader br = LoadTrainData.loadFile(context.getConfiguration().get(ConfigKeys.DATASET_TRAIN_SVM_VECTOR_OUTPUT_DIR), 
//								context.getConfiguration().get(ConfigKeys.DATASET_LABEL_VECTOR_FILE));
//						String line = null;
//						while((line = br.readLine()) != null) {
//							line = StringUtils.trimToEmpty(line);
//							if(StringUtils.isNotBlank(line)) {
//							}
//						}
						
						// 预测输入问题
						double[] probabilities = new double[model.getLabels().length];
						Integer h = -1;
						if (model.isProbabilityModel()) {
							h = (int) Linear.predictProbability(model, sentence, probabilities);
						} else {
							h = (int) Linear.predictValues(model, sentence, probabilities);
						}
						
						Double predictValue = 0.0;
						System.out.println(model.getLabels().length);
						if (h != -1) {
							predictValue = probabilities[h];
						}

						LOG.info("预测分类为：" + h + "，预测概率：" + predictValue);
					}
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
}
