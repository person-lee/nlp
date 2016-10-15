package com.lbc.nlp_algorithm.prepocess.core.component.train;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lbc.nlp_algorithm.classification.domain.ModelVector;
import com.lbc.nlp_algorithm.classification.liblinear.FeatureNode;
import com.lbc.nlp_algorithm.loadFile.loadData.LoadTrainData;
import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_algorithm.prepocess.core.common.AbstractComponent;
import com.lbc.nlp_algorithm.prepocess.core.component.train.schedule.TrainModelImpl;
import com.lbc.nlp_domain.PredictResult;
import com.lbc.nlp_modules.common.tuple.Pair;

public class TrainModelController extends AbstractComponent {
	private static final Log LOG = LogFactory.getLog(TrainModelController.class);
	
	public TrainModelController(final Context context) {
		super(context);
	}
	
	@Override
	public void fire() {
		try {
			// 加载数据
			Pair<Integer, Map<Integer, List<FeatureNode[]>>> vectorPair = LoadTrainData
					.loadData(context.getConfiguration().get(ConfigKeys.DATASET_TRAIN_SVM_VECTOR_OUTPUT_DIR), 
							context.getConfiguration().get(ConfigKeys.DATASET_TRAIN_SVM_VECTOR_FILE));
			Map<Integer, List<FeatureNode[]>> vectors = vectorPair.getValue1();
			int featureNum = vectorPair.getValue0();
			
			// 对数据进行切分
			Map<Integer, List<FeatureNode[]>> trainVector = Maps.newHashMap();
			Map<Integer, List<FeatureNode[]>> testVector = Maps.newHashMap();
			splitData(context, vectors, trainVector, testVector);

			// 统计句子总数
			int sampleNum = 0;
			for (Entry<Integer, List<FeatureNode[]>> vector : trainVector.entrySet()) {
				sampleNum += vector.getValue().size();
			}

			// 转化数据格式为liblinear支持的格式
			ModelVector modelVector = new ModelVector.Builder()
					.featureNum(featureNum).sampleNum(sampleNum).vectors(trainVector)
					.build();

			// 调用liblinear进行训练
			TrainModelImpl trainModel = new TrainModelImpl(context);
			boolean ret = trainModel.trainModel(modelVector, 
					context.getConfiguration().get(ConfigKeys.DATASET_TRAIN_MODEL_FILE));
			
			// 模型准确率验证
			String[] labels = context.getFDMetadata().getPreprocessDir().list();
			List<PredictResult> predictResults = Lists.newArrayList();
			double accuracy = ModelValid.computeAccuracy(testVector, trainModel.getModel(), predictResults);
			ModelValid.calculatePerformance(predictResults, Arrays.asList(labels));
			LOG.info("该模型准确率为：" + accuracy);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
	/**
	 * 对数据按比例进行切分
	 * @param context
	 * @param vectors
	 * @param trainVector
	 * @param testVector
	 */
	private static void splitData(Context context, Map<Integer, List<FeatureNode[]>> vectors, 
			Map<Integer, List<FeatureNode[]>> trainVector, Map<Integer, List<FeatureNode[]>> testVector) {
		Float ratio = Float.valueOf(context.getConfiguration().get(ConfigKeys.DATASET_TRAIN_INPUT_RATIO));
		if (MapUtils.isNotEmpty(vectors)) {
			for (Entry<Integer, List<FeatureNode[]>> cate : vectors.entrySet()) {
				Integer label = cate.getKey();
				List<FeatureNode[]> sentences = cate.getValue();
				Collections.shuffle(sentences);
				int sep_index = (int)(sentences.size() * ratio);
				List<FeatureNode[]> trainSentences = sentences;
				List<FeatureNode[]> testSentences = sentences;
				if (sep_index < 10) {
					trainSentences.clear();testSentences.clear();
					trainSentences.addAll(sentences.subList(0, sep_index));
					testSentences.addAll(sentences.subList(sep_index, sentences.size()));
				} 
				trainVector.put(label, trainSentences);
				testVector.put(label, testSentences);
			}
		}
	}
}
