package com.lbc.nlp_algorithm.prepocess.core.utils;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.ProcessorType;
import com.lbc.nlp_algorithm.prepocess.api.api.VectorMetadata;
import com.lbc.nlp_algorithm.prepocess.core.common.Component;
import com.lbc.nlp_algorithm.prepocess.core.common.ContextImpl;
import com.lbc.nlp_algorithm.prepocess.core.common.VectorMetadataImpl;

public class PreprocessingUtils {

	public static VectorMetadata newVectorMetadata() {
		return new VectorMetadataImpl();
	}
	
	public static Context newContext(ProcessorType type, String config) {
		return new ContextImpl(type, config);
	}
	
	public static Component[] newChainedComponents(final Context context, Class<?>[] classes) {
		final int nComponent = classes.length;
		Component[] components = new Component[nComponent];
		for(int i=0; i<classes.length; i++) {
			components[i] = ReflectionUtils.newInstance(classes[i], Component.class, new Object[] {context});
		}
		return components;
	}
}
