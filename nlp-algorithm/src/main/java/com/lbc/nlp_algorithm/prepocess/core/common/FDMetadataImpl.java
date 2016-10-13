package com.lbc.nlp_algorithm.prepocess.core.common;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lbc.nlp_algorithm.prepocess.api.api.FDMetadata;
import com.lbc.nlp_algorithm.prepocess.api.api.ProcessorType;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_algorithm.prepocess.core.utils.CheckUtils;
import com.lbc.nlp_modules.common.proterties.ConfigReadable;

public class FDMetadataImpl implements FDMetadata {

	private static final Log LOG = LogFactory.getLog(FDMetadata.class);
	private final File inputRootDir;
	private final File preprocessDir;
	private final File outputDir;
	private final String outputVectorFile;
	private final String fileExtensionName;
	private final File labelVectorFile;
	private final File featureTermVectorFile;
	private final String outputDictName;
	private final String dupFile;
	
	/**
	 * 初始化文件
	 * @param processorType
	 * @param configuration
	 */
	public FDMetadataImpl(ProcessorType processorType, ConfigReadable configuration) {
		// initialize
		fileExtensionName = configuration.get(ConfigKeys.DATASET_TRAIN_FILE_EXTENSION, "");
		LOG.info("Train dataset file extension: name=" + fileExtensionName);
		String termsFile = configuration.get(ConfigKeys.DATASET_FEATURE_TERM_VECTOR_FILE);
		CheckUtils.checkNotNull(termsFile);
		featureTermVectorFile = new File(termsFile);
		
		switch (processorType) {
		case TRAIN:
			String trainInputRootDir = configuration.get(ConfigKeys.DATASET_TRAIN_INPUT_ROOT_FILE);
			String trainPreprocessDir = configuration.get(ConfigKeys.DATASET_TRAIN_PREPROCESS_ROOT_FILE);
			String train = configuration.get(ConfigKeys.DATASET_TRAIN_SVM_VECTOR_FILE);
			String trainOutputDir = configuration.get(ConfigKeys.DATASET_TRAIN_SVM_VECTOR_OUTPUT_DIR);
			outputDictName = configuration.get(ConfigKeys.DATASET_TRAIN_DICT_VECTOR_FILE);
			inputRootDir = new File(trainInputRootDir);
			preprocessDir = new File(trainPreprocessDir);
			
			outputVectorFile = train;
			outputDir = new File(trainOutputDir);
			dupFile = configuration.get(ConfigKeys.DATASET_TRAIN_DUPFILE);
			
			// check existence: 
			// parent directory of term file MUST exist
			CheckUtils.checkFile(featureTermVectorFile.getParentFile(), false);
			// term file MUST NOT exist
			CheckUtils.checkFile(featureTermVectorFile, true);	
			
			break;
		case TEST:
			String testInputRootDir = configuration.get(ConfigKeys.DATASET_TEST_INPUT_ROOT_FILE);
			String testPreprocessDir = configuration.get(ConfigKeys.DATASET_TEST_PREPROCESS_ROOT_FILE);
			String test = configuration.get(ConfigKeys.DATASET_TEST_SVM_VECTOR_FILE);
			String testOutputDir = configuration.get(ConfigKeys.DATASET_TEST_SVM_VECTOR_OUTPUT_DIR);
			outputDictName = configuration.get(ConfigKeys.DATASET_TEST_DICT_VECTOR_FILE);
			inputRootDir = new File(testInputRootDir);
			preprocessDir = new File(testPreprocessDir);
			outputVectorFile = test;
			outputDir = new File(testOutputDir);
			dupFile = configuration.get(ConfigKeys.DATASET_TEST_DUPFILE);
			
			CheckUtils.checkFile(featureTermVectorFile, false);
			
			break;
		default:
			throw new RuntimeException("Undefined processor type!");
		}
		
		if(!preprocessDir.exists()){
			preprocessDir.mkdirs();
		}
		
		String labels = configuration.get(ConfigKeys.DATASET_LABEL_VECTOR_FILE);
		labelVectorFile = new File(labels);
		
		LOG.info("Vector input root directory: outputDir=" + inputRootDir);
		LOG.info("Vector output directory: outputDir=" + outputDir);
		LOG.info("Vector output file: outputFile=" + outputVectorFile);
	}

	@Override
	public File getInputRootDir() {
		return inputRootDir;
	}
	
	@Override
	public File getOutputDir() {
		return outputDir;
	}

	@Override
	public String getOutputVectorFile() {
		return outputVectorFile;
	}

	@Override
	public String getFileExtensionName() {
		return fileExtensionName;
	}

	@Override
	public File getLabelVectorFile() {
		return labelVectorFile;
	}

	@Override
	public File getFeatureTermVectorFile() {
		return featureTermVectorFile;
	}

	@Override
	public String getOutputDictName() {
		return outputDictName;
	}
	
	@Override
	public File getPreprocessDir() {
		return preprocessDir;
	}
	
	@Override
	public String getDupFile() {
		return dupFile;
	}

}
