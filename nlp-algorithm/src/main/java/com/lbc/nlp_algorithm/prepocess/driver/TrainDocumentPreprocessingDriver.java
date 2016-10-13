package com.lbc.nlp_algorithm.prepocess.driver;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.ProcessorType;
import com.lbc.nlp_algorithm.prepocess.core.component.BasicInformationCollector;
import com.lbc.nlp_algorithm.prepocess.core.component.DocumentPreprocess;
import com.lbc.nlp_algorithm.prepocess.core.component.DocumentTFIDFComputation;
import com.lbc.nlp_algorithm.prepocess.core.component.DocumentWordsCollector;
import com.lbc.nlp_algorithm.prepocess.core.component.train.FeaturedTermVectorSelector;
import com.lbc.nlp_algorithm.prepocess.core.component.train.TrainModelController;
import com.lbc.nlp_algorithm.prepocess.core.component.train.OutputtingQuantizedTrainData;
import com.lbc.nlp_algorithm.prepocess.core.utils.PreprocessingUtils;

/**
 * The driver for starting components to process TRAIN data set.
 * It includes the following 5 components:
 * <ol>
 * <li>{@link BasicInformationCollector}</li>
 * <li>{@link DocumentWordsCollector}</li>
 * <li>{@link FeaturedTermVectorSelector}</li>
 * <li>{@link DocumentTFIDFComputation}</li>
 * <li>{@link OutputtingQuantizedTrainData}</li>
 * </ol>
 * Executing above components in order can output the normalized
 * data for feeding libSVM classifier developed by <code>Lin Chih-Jen</code>
 * (<a href="www.csie.ntu.edu.tw/~cjlin/libsvm/‎">www.csie.ntu.edu.tw/~cjlin/libsvm/‎</a>)</br>
 * It can produce 2 files represented by the specified properties:
 * <ol>
 * <li>a term vector file property: <code>processor.dataset.chi.term.vector.file</code></li>
 * <li>a label vector file property: <code>processor.dataset.label.vector.file</code></li>
 * </ol>
 * which are used by {@link TestDocumentPreprocessingDriver} to produce TEST vector data.
 * 
 * @author Shirdrn
 */
public class TrainDocumentPreprocessingDriver extends AbstractDocumentProcessorDriver {

	private static final String CONFIG = "config-train.properties";
	
	@Override
	public void preprocess() {
		// 加载配置文件
		Context context = super.newContext(ProcessorType.TRAIN, CONFIG);
		context.setVectorMetadata(PreprocessingUtils.newVectorMetadata());
		
		// build component chain for train data
		Class<?>[] classes = new Class[] {
				BasicInformationCollector.class,
				DocumentPreprocess.class,
				DocumentWordsCollector.class,
				FeaturedTermVectorSelector.class,
				DocumentTFIDFComputation.class,
				OutputtingQuantizedTrainData.class,
				TrainModelController.class
		};
		
		// 初始化所有的处理类
		run(PreprocessingUtils.newChainedComponents(context, classes));
	}
	
	public static void main(String[] args) {
		AbstractDocumentProcessorDriver.start(TrainDocumentPreprocessingDriver.class);	
	}

}
