package liblinear;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1：加载训练文件。 2：包装为liblinear支持的向量格式。 3：调用liblinear函数train进行模型训练。
 * 
 * @author cdlibaocang
 *
 */
public class LrTrain {
	private static final Logger log = LoggerFactory.getLogger(LrTrain.class.getName());
	
	private static final String CONFIG = "config-train.properties";
	
	public static void main(String[] args) {
		try {
//			// 加载数据
//			LoadData ld = new LoadTrainData(CONFIG);
//			Pair<Integer, Map<Integer, List<FeatureNode[]>>> vectorPair = ld.loadData(ConfigKeys.DATASET_TRAIN_SVM_VECTOR_OUTPUT_DIR, ConfigKeys.DATASET_TRAIN_SVM_VECTOR_FILE);
//			Map<Integer, List<FeatureNode[]>> vectors = vectorPair.getValue1();
//			int featureNum = vectorPair.getValue0();
//			
//			// 统计句子总数
//			int sampleNum = 0;
//			for (Entry<Integer, List<FeatureNode[]>> vector : vectors
//					.entrySet()) {
//				sampleNum += vector.getValue().size();
//			}
//
//			// 转化数据格式为liblinear支持的格式
//			ModelVector modelVector = new ModelVector.Builder().featureNum(featureNum)
//					.sampleNum(sampleNum).vectors(vectors).build();
//
//			// 调用liblinear进行训练
//			Context context = PreprocessingUtils.newContext(ProcessorType.TRAIN, CONFIG);
//			TrainModel trainModel = new TrainModelImpl(context);
//			boolean ret = trainModel.trainModel(modelVector, "SouGou");

			// 加载测试语料
//			Map<Integer, List<FeatureNode[]>> testVector = ld.loadData(ConfigKeys.DATASET_TRAIN_SVM_VECTOR_OUTPUT_DIR, ConfigKeys.DATASET_TRAIN_SVM_VECTOR_FILE);
//
//			// 统计句子总数
//			sampleNum = 0;
//			for (Entry<Integer, List<FeatureNode[]>> vector : vectors.entrySet()) {
//				sampleNum += vector.getValue().size();
//			}
//
//			// 转化数据格式为liblinear支持的格式
//			ModelVector testModelVector = new ModelVector.Builder().featureNum(sampleNum)
//					.sampleNum(sampleNum).vectors(vectors).build();
//
//			// 对训练的模型进行测试
//			List<PredictResult> predictResults = trainModel.testModel(testModelVector);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
