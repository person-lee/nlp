package com.lbc.nlp_algorithm.prepocess.core.common;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.FDMetadata;
import com.lbc.nlp_algorithm.prepocess.api.api.ProcessorType;
import com.lbc.nlp_algorithm.prepocess.api.api.VectorMetadata;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_modules.common.proterties.ConfigReadable;
import com.lbc.nlp_modules.common.proterties.Configuration;

public class ContextImpl implements Context {

	private final ConfigReadable configuration;
	private VectorMetadata vectorMetadata;
	private final FDMetadata fDMetadata;
	private final ProcessorType processorType;
	
	public ContextImpl(ProcessorType processorType, String config) {
		this.processorType = processorType;
		this.configuration = new Configuration("config.properties");
		((Configuration) this.configuration).addResource(config);
		this.fDMetadata = new FDMetadataImpl(processorType, configuration);
	}
	
	public ContextImpl() {
		this.processorType = ProcessorType.TRAIN;
		this.configuration = new Configuration();
		this.fDMetadata = new FDMetadataImpl(processorType, configuration);
	}
	
	@Override
	public FDMetadata getFDMetadata() {
		return fDMetadata;
	}

	@Override
	public ConfigReadable getConfiguration() {
		return configuration;
	}
	
	@Override
	public VectorMetadata getVectorMetadata() {
		return vectorMetadata;
	}
	
	@Override
	public void setVectorMetadata(VectorMetadata vectorMetadata) {
		this.vectorMetadata = vectorMetadata;
	}

	@Override
	public ProcessorType getProcessorType() {
		return processorType;
	}

	@Override
	public String getCharset() {
		return configuration.get(ConfigKeys.DATASET_FILE_CHARSET, "UTF-8");
	}

}
