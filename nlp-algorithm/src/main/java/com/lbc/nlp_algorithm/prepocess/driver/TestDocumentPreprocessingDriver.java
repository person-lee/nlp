package com.lbc.nlp_algorithm.prepocess.driver;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.ProcessorType;
import com.lbc.nlp_algorithm.prepocess.core.component.BasicInformationCollector;
import com.lbc.nlp_algorithm.prepocess.core.component.DocumentTFIDFComputation;
import com.lbc.nlp_algorithm.prepocess.core.component.DocumentWordsCollector;
import com.lbc.nlp_algorithm.prepocess.core.component.test.LoadFeatureTermVector;
import com.lbc.nlp_algorithm.prepocess.core.component.test.ModelTest;
import com.lbc.nlp_algorithm.prepocess.core.component.test.OutputtingQuantizedTestData;
import com.lbc.nlp_algorithm.prepocess.core.utils.PreprocessingUtils;

/**
 * The driver for starting components to process TEST data set.
 * It includes the following 5 components:
 * <ol>
 * <li>{@link BasicInformationCollector}</li>
 * <li>{@link DocumentWordsCollector}</li>
 * <li>{@link LoadFeatureTermVector}</li>
 * <li>{@link DocumentTFIDFComputation}</li>
 * <li>{@link OutputtingQuantizedTestData}</li>
 * </ol>
 * Executing above components in order can output the normalized
 * data for feeding libSVM classifier developed by <code>Lin Chih-Jen</code>
 * (<a href="www.csie.ntu.edu.tw/~cjlin/libsvm/‎">www.csie.ntu.edu.tw/~cjlin/libsvm/‎</a>)
 * 
 * @author Shirdrn
 */
public class TestDocumentPreprocessingDriver extends AbstractDocumentProcessorDriver {

	private static final String CONFIG = "config-test.properties";
	
	@Override
	public void preprocess() {
		// initial file and compacity
		Context context = super.newContext(ProcessorType.TEST, CONFIG);
		context.setVectorMetadata(PreprocessingUtils.newVectorMetadata());
		
		// build component chain for test data
		Class<?>[] classes = new Class[] {
				BasicInformationCollector.class/*统计文档数*/,
				DocumentWordsCollector.class/*加载数据*/,
				LoadFeatureTermVector.class/*加载特征*/,
				DocumentTFIDFComputation.class/*计算tfidf*/,
				OutputtingQuantizedTestData.class/*将向量保存至文件*/,
				ModelTest.class/*采用测试数据对模型进行验证*/
		};
		
		run(PreprocessingUtils.newChainedComponents(context, classes));
	}
	
	public static void main(String[] args) {
		AbstractDocumentProcessorDriver.start(
				TestDocumentPreprocessingDriver.class);		
	}

}
