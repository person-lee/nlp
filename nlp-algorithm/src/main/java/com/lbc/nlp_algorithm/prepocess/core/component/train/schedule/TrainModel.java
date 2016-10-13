package com.lbc.nlp_algorithm.prepocess.core.component.train.schedule;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.lbc.nlp_algorithm.classification.domain.ModelVector;
import com.lbc.nlp_algorithm.classification.domain.PredictResult;

public interface TrainModel {
	/**
	 * 训练模型
	 * @param trainVector
	 * @return
	 * @throws Exception
	 */
	boolean trainModel(ModelVector trainVector, String modelPath) throws Exception;
	/**
	 * 加载模型文件
	 * @param modelPath
	 * @return
	 * @throws IOException
	 */
	boolean loadModel(File modelPath) throws IOException;
	/**
	 * 测试模型效果
	 * @param testVector
	 * @return
	 * @throws Exception
	 */
	List<PredictResult> testModel(ModelVector testVector) throws Exception;
	/**
	 * 保存模型文件
	 * @param model
	 * @return
	 * @throws IOException
	 */
	boolean saveModel(String modelName) throws IOException;

}
