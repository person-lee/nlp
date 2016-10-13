package com.lbc.nlp_algorithm.prepocess.api.api;

import java.io.File;

public interface FDMetadata {
	// 输入文件根目录
	File getInputRootDir();
	// 输出文件根目录
	File getOutputDir();
	// 输出向量文件
	String getOutputVectorFile();
	// 文件后缀
	String getFileExtensionName();
	// 标签文件
	File getLabelVectorFile();
	// 特征文件
	File getFeatureTermVectorFile();
	// 输出词表
	String getOutputDictName();
	// 输出处理后的文件根目录
	File getPreprocessDir();
	// 重复句子保存文件
	String getDupFile();

}
