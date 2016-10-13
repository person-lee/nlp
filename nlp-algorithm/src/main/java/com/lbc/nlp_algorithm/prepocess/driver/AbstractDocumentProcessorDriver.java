package com.lbc.nlp_algorithm.prepocess.driver;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.ProcessorType;
import com.lbc.nlp_algorithm.prepocess.core.common.Component;
import com.lbc.nlp_algorithm.prepocess.core.utils.PreprocessingUtils;
import com.lbc.nlp_algorithm.prepocess.core.utils.ReflectionUtils;

public abstract class AbstractDocumentProcessorDriver {

	public abstract void preprocess();
	
	protected void run(Component[] chain) {
		for (int i = 0; i < chain.length - 1; i++) {
			Component current = chain[i];
			Component next = chain[i + 1];
			current.setNext(next);
		}
		
		for (Component componennt : chain) {
			componennt.fire();
		}
	}
	
	public Context newContext(ProcessorType type, String config) {
		return PreprocessingUtils.newContext(type, config);
	}
	
	public static void start(Class<? extends AbstractDocumentProcessorDriver> driverClass) {
		AbstractDocumentProcessorDriver driver = ReflectionUtils.newInstance(driverClass);
		driver.preprocess();
	}
}
