package com.lbc.nlp_algorithm.prepocess.core.component.train.schedule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.classification.domain.ModelVector;
import com.lbc.nlp_algorithm.classification.domain.PredictResult;
import com.lbc.nlp_algorithm.classification.liblinear.FeatureNode;
import com.lbc.nlp_algorithm.classification.liblinear.Linear;
import com.lbc.nlp_algorithm.classification.liblinear.Model;
import com.lbc.nlp_algorithm.classification.liblinear.Parameter;
import com.lbc.nlp_algorithm.classification.liblinear.Problem;
import com.lbc.nlp_algorithm.classification.liblinear.SolverType;
import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_algorithm.prepocess.core.common.AbstractComponent;

public class TrainModelImpl extends AbstractComponent implements TrainModel {
	private static final Logger log = LoggerFactory.getLogger(TrainModel.class.getName());
	
	private SolverType solverType = SolverType.L2R_LR_DUAL;
	private Double bestc = 15d;
	private Double esp = 0.01d;
	private Model model = null;
	private final int fold = 5;		//交差验证划分份数
	
	public TrainModelImpl(Context context) {
		super(context);
	}
	
	@Override
	public void fire() {};
	
	@Override
	public boolean trainModel(ModelVector trainVector, String modelName) throws Exception {
		Problem problem = generateProblem(trainVector);
        
        if(bestc==null){
        	bestc = chooseBestC(problem);
        }
        Parameter parameter = new Parameter(solverType, bestc, esp);

        //----根据数据占比设定权重，处理数据不平衡的问题------
        Map<Integer, List<FeatureNode[]>> vectors = trainVector.getVectors();
		int classNum = vectors.size();
		int[] weightLabel = new int[classNum];
		double[] weight = new double[classNum];
		
        int sample_num = trainVector.getSampleNum();	// 训练数据量
        int index = 0;
        for (Entry<Integer, List<FeatureNode[]>> eachCate : vectors.entrySet()) {
        	weight[index] = 1.0 - (double)(eachCate.getValue().size())/(double)sample_num;
            weightLabel[index] = eachCate.getKey();
            index++;
        }
        
        parameter.setWeights(weight, weightLabel);
        
        //-----------------------------------------
        long start_time = System.currentTimeMillis();
        model = Linear.train(problem, parameter);
        log.info(" used time: "	+ (System.currentTimeMillis()-start_time)/1000 + "s");
        
        saveModel(modelName);
        return true;
	}
	
	@Override
	public boolean loadModel(File modelPath) throws IOException {
		return true;
	}
	
	@Override
	public List<PredictResult> testModel(ModelVector testVector) throws Exception{
		Problem problem = generateProblem(testVector);
		List<PredictResult> predictResults = new ArrayList<PredictResult>(problem.l);
		if(model.isProbabilityModel()){
			double[] probabilities = new double[model.getLabels().length];
			for(int i=0; i<problem.l; i++){
				Integer h = (int) Linear.predictProbability(model, problem.x[i], probabilities);
				predictResults.add(new PredictResult.Builder().label(h).probility(probabilities[h]).build());
			}
		} else {
			for(int i=0; i<problem.l; i++){
				Integer h = (int) Linear.predict(model, problem.x[i]);
				predictResults.add(new PredictResult.Builder().label(h).probility(null).build());
			}
		}
		return predictResults;
	}
	
	/**
	 * 保存模型文件
	 */
	@Override
	public boolean saveModel(String modelName) throws IOException {
		File file = new File(context.getConfiguration().get(ConfigKeys.DATASET_TRAIN_SVM_VECTOR_OUTPUT_DIR), modelName);
		Linear.saveModel(file, model);
		return true;
	}
	
	/**
	 * 交叉验证挑选参数c
	 * @param problem
	 * @param fold
	 * @return
	 */
	private double chooseBestC(Problem problem) throws Exception{
        double maxAcc = 0.0;
        double bestC = 1.0;
        int decrease_n = 0;
        for (double c = 1; decrease_n<=2; c*=2) {
            Parameter parameter = new Parameter(solverType, c, esp);

            double[] hx = new double[problem.l];
            Linear.crossValidation(problem,parameter,fold,hx);

            //统计准确率
            int right = 0;
            for (int i = 0; i < problem.l; i++) {
                if (problem.y[i] == hx[i])
                    right++;
            }
            double acc = (double)right/problem.l;
            if (maxAcc < acc) {
                maxAcc = acc;
                bestC = c;
                decrease_n = 0;
            } else {
            	decrease_n ++;
            }
            log.info(" 交叉验证，当前准确率:" + acc + " 参数为:" + c + "。最优准确率：" + maxAcc + "，最优参数为：" + bestC);
        }

        log.info(" 交叉验证完成。准确率:" + maxAcc + "，最优参数为：" + bestC);
        return bestC;
    }
	
	/**
	 * 把向量表示转为liblinear支持的problem表示。
	 * @param vector
	 * @return
	 */
	private Problem generateProblem(ModelVector vector){
		Map<Integer, List<FeatureNode[]>> vectors = vector.getVectors();
		Problem problem = new Problem();
        problem.n = vector.getFeatureNum();
        int sample_num = vector.getSampleNum();	// 训练数据样本
        problem.l = sample_num;
        problem.y = new double[sample_num];
        problem.x = new FeatureNode[sample_num][];
        
        int j = 0;
        for (Entry<Integer, List<FeatureNode[]>> eachLabelVector : vectors.entrySet()) {
        	int label = eachLabelVector.getKey();
        	for(FeatureNode[] sample : eachLabelVector.getValue()){
                problem.y[j] = label;
                problem.x[j] = sample;
                j++;
        	}
        }
        
        
        return problem;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
