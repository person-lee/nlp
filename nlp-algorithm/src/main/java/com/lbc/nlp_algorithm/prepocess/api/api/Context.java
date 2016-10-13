package com.lbc.nlp_algorithm.prepocess.api.api;

import com.lbc.nlp_modules.common.proterties.ConfigReadable;

public interface Context {
	
	String getCharset();
	
	FDMetadata getFDMetadata();

	ConfigReadable getConfiguration();
	
	VectorMetadata getVectorMetadata();
	
	void setVectorMetadata(VectorMetadata vectorMetadata);

	ProcessorType getProcessorType();

}
