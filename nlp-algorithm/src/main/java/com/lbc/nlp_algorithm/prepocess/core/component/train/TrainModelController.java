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
import com.lbc.nlp_algorithm.classification.libsvm.Svm;
import com.lbc.nlp_algorithm.classification.libsvm.SvmModel;
import com.lbc.nlp_algorithm.classification.libsvm.SvmNode;
import com.lbc.nlp_algorithm.classification.libsvm.SvmParameter;
import com.lbc.nlp_algorithm.classification.libsvm.SvmProblem;
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
			
			// ***************************** SVM **************************************
			// cast FeatureNode to SvmProblem
			Map<Integer, List<SvmNode[]>> trainSvmVector = format(trainVector);
			Map<Integer, List<SvmNode[]>> testSvmVector = format(testVector);
			
			// generate svm problem
			SvmProblem problem = new SvmProblem();
	        problem.l = sampleNum;
	        problem.y = new double[sampleNum];
	        problem.x = new SvmNode[sampleNum][];
	        
	        int j = 0;
	        for (Entry<Integer, List<SvmNode[]>> eachLabelVector : trainSvmVector.entrySet()) {
	        	int label = eachLabelVector.getKey();
	        	for(SvmNode[] sample : eachLabelVector.getValue()){
	                problem.y[j] = label;
	                problem.x[j] = sample;
	                j++;
	        	}
	        }
	        
	        SvmParameter param = new SvmParameter();
	        param.kernel_type = 0; // linear
	        param.svm_type = 0; // c-svc
	        param.eps = 1e-5;
	        param.cache_size = 256;
	        param.C = 2;
	        
	        // 针对语料不平衡，设置不同的惩罚系数
	        param.nr_weight = trainSvmVector.keySet().size();
	        param.weight = new double[trainSvmVector.keySet().size()];
	        param.weight_label = new int[trainSvmVector.keySet().size()];
	        
	        int index = 0;
	        for (Entry<Integer, List<SvmNode[]>> eachCate : trainSvmVector.entrySet()) {
	        	param.weight[index] = 1.0 - (double)(eachCate.getValue().size())/(double)sampleNum;
	        	param.weight_label[index] = eachCate.getKey();
	            index++;
	        }
	        
	        do_cross_validation(problem, param, 10);
	        
			SvmModel svmModel = Svm.svm_train(problem, param);
			Svm.svm_save_model(context.getConfiguration().get(ConfigKeys.DATASET_TRAIN_MODEL_FILE), svmModel);
			
			int correctNum = 0;
			int totalNum = 0;
			for (Entry<Integer, List<SvmNode[]>> eachEntry : testSvmVector.entrySet()) {
				int label = eachEntry.getKey();
				for (SvmNode[] query : eachEntry.getValue()) {
					int hx = (int) Svm.svm_predict(svmModel, query);
					
					if (label == hx) {
						correctNum++;
					}
					
					if (label != hx) {
						LOG.info("实际分类为：" + label + "预测分类为：" + hx);
					}
					totalNum++;
				}
			}
			LOG.info("测试语料总数为：" + totalNum + "，测试语料正确个数为：" + correctNum + "，模型准确率为：" + 1.0 * correctNum / totalNum);

			// 转化数据格式为liblinear支持的格式
			ModelVector<FeatureNode> modelVector = new ModelVector.Builder<FeatureNode>()
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
	
	//交叉验证
    private void do_cross_validation(SvmProblem prob, SvmParameter param,int nr_fold)
    {
        int i;
        int total_correct = 0;
        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[prob.l];
        try {
			Svm.svm_cross_validation(prob,param,nr_fold,target);
		} catch (CloneNotSupportedException e) {
			LOG.error(e.getMessage());
		}
        if(param.svm_type == SvmParameter.EPSILON_SVR ||
           param.svm_type == SvmParameter.NU_SVR)
        {
            for(i=0;i<prob.l;i++)
            {
                double y = prob.y[i];
                double v = target[i];
                total_error += (v-y)*(v-y);
                sumv += v;
                sumy += y;
                sumvv += v*v;
                sumyy += y*y;
                sumvy += v*y;
            }
            LOG.info("Cross Validation Mean squared error = "+total_error/prob.l+"\n");
            LOG.info("Cross Validation Squared correlation coefficient = "+
                ((prob.l*sumvy-sumv*sumy)*(prob.l*sumvy-sumv*sumy))/
                ((prob.l*sumvv-sumv*sumv)*(prob.l*sumyy-sumy*sumy))+"\n"
                );
        }
        else
        {
            for(i=0;i<prob.l;i++)
                if(target[i] == prob.y[i])
                    ++total_correct;
            LOG.info("Cross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n");
        }
    }
	
	private static Map<Integer, List<SvmNode[]>> format(Map<Integer, List<FeatureNode[]>> vectors) {
		Map<Integer, List<SvmNode[]>> svmVector = Maps.newHashMap();
		for (Entry<Integer, List<FeatureNode[]>> vector : vectors.entrySet()) {
			List<FeatureNode[]> featureNodeList = vector.getValue();
			List<SvmNode[]> svmNodeList = Lists.newArrayList();
			for (FeatureNode[] featureNodes : featureNodeList) {
				SvmNode[] svmNodes = new SvmNode[featureNodes.length];
				int idx = 0;
				for (FeatureNode fn : featureNodes) {
					SvmNode svmNode = new SvmNode();
					svmNode.index = fn.index;
					svmNode.value = fn.value;
					svmNodes[idx++] = svmNode;
				}
				svmNodeList.add(svmNodes);
			}
			svmVector.put(vector.getKey(), svmNodeList);
		}
		return svmVector;
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
				List<FeatureNode[]> trainSentences = Lists.newArrayList(sentences);
				List<FeatureNode[]> testSentences = Lists.newArrayList(sentences);
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
