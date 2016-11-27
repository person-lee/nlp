package com.lbc.nlp_algorithm.clustering.hac.test;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.lbc.nlp_algorithm.clustering.hac.common.filter.FilterUtils;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.InnerProductMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.DenseVector;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.SplitedText2VectorTfidf;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vectorization;
import com.lbc.nlp_algorithm.clustering.hac.core.ClusterMethod;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.AgglomerationMethod;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.ClusteringBuilder;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.DendrogramBuilder;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.PartitionedHAC;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.WardLinkage;
import com.lbc.nlp_algorithm.clustering.hac.data.CommonExperimentsImporter;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;
import com.lbc.nlp_algorithm.clustering.hac.data.ExperimentsImporter;
import com.lbc.nlp_algorithm.clustering.hac.data.RawReader;
import com.lbc.nlp_algorithm.clustering.hac.data.SplitWordRawReader;
import com.lbc.nlp_algorithm.clustering.hac.eval.InnerEvaluator;

public class ClusteringTest {
	private static final double hacStopThreshold = 0.6;	// 相似度阈值，这个阈值适用于tf-idf的句子向量,其他类型的向量需要酌情调整
	private static final int hacSizeThreshold = 5; 		// 每个cluster的size下限
	private static final int hacSizePerCluster = 5000;	// partitionHAC使用，按此大小对原始数据进行切分, HAC耗内存较严重，采用切分聚类再合并的方式进行

	public static void main(String[] args) throws Exception {
		String filePath = "F://corpus/SogouCS/news.sohunews.010801.txt";
		if(args.length > 0 && StringUtils.isNotEmpty(args[0])) {
			filePath = args[0];
		}

		File tmpFile = new File(filePath);
		DissimilarityMeasure measure = new InnerProductMeasure();
		Vector vectorTemplate = new DenseVector(0);
		RawReader rawReader = new SplitWordRawReader(tmpFile, vectorTemplate, FilterUtils.getFilterList());
		Vectorization vectorization = new SplitedText2VectorTfidf(vectorTemplate, FilterUtils.getStopWords());
		ExperimentsImporter importer = new CommonExperimentsImporter(rawReader, vectorization);
		Experiments experiments = importer.read();
		ClusteringBuilder clusteringBuilder = new DendrogramBuilder(experiments.numberOfExperiments());
		AgglomerationMethod agglomerationMethod = new WardLinkage();
		
		ClusterMethod partitionedHAC = new PartitionedHAC(experiments, clusteringBuilder, measure, agglomerationMethod,
				hacStopThreshold, hacSizeThreshold, hacSizePerCluster);
		partitionedHAC.cluster();

		System.out.println("已分配内存= " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB");

		File outFile = new File(filePath.substring(0,filePath.length()-4) + ".cluster.txt");
		partitionedHAC.dumpFile(outFile, true);

		//进行聚类效果评估
		InnerEvaluator evaluator = new InnerEvaluator();
		evaluator.eval(tmpFile, outFile);
	}

}
