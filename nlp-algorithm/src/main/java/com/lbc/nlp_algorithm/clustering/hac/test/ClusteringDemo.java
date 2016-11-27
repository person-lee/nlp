package com.lbc.nlp_algorithm.clustering.hac.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lbc.nlp_algorithm.clustering.hac.common.measure.DissimilarityMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.measure.InnerProductMeasure;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.DenseVector;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.SplitedText2VectorTfidf;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vectorization;
import com.lbc.nlp_algorithm.clustering.hac.core.ClusterMethod;
import com.lbc.nlp_algorithm.clustering.hac.core.densitypeaks.CutoffKernel;
import com.lbc.nlp_algorithm.clustering.hac.core.densitypeaks.DensityKernel;
import com.lbc.nlp_algorithm.clustering.hac.core.densitypeaks.DensityPeaks;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.AgglomerationMethod;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.ClusteringBuilder;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.DendrogramBuilder;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.HierarchicalAgglomerativeClusterFaster;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.PartitionedHAC;
import com.lbc.nlp_algorithm.clustering.hac.core.hac.WardLinkage;
import com.lbc.nlp_algorithm.clustering.hac.core.kmeans.KMeans;
import com.lbc.nlp_algorithm.clustering.hac.data.CombineExperimentsImporter;
import com.lbc.nlp_algorithm.clustering.hac.data.CommonExperimentsImporter;
import com.lbc.nlp_algorithm.clustering.hac.data.DirectImporter;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;
import com.lbc.nlp_algorithm.clustering.hac.data.ExperimentsCombiner;
import com.lbc.nlp_algorithm.clustering.hac.data.ExperimentsImporter;
import com.lbc.nlp_algorithm.clustering.hac.data.RawReader;
import com.lbc.nlp_algorithm.clustering.hac.data.SimplestRawReader;
import com.lbc.nlp_algorithm.clustering.hac.data.SplitWordRawReader;
import com.lbc.nlp_algorithm.clustering.hac.data.VectorConcatCombiner;
import com.lbc.nlp_algorithm.clustering.hac.eval.ClusterEvaluator;
import com.lbc.nlp_algorithm.clustering.hac.eval.PurityEvaluator;

public class ClusteringDemo {

	public void densityPeaks() throws IOException {
		//String corpus = "D:\\data\\cluster\\desityPicksMethod\\raw\\para_vectors.bin.phone";
		String corpus = "D:\\data\\cluster\\merge\\paragraphvectors.bin";
		DissimilarityMeasure measure = new InnerProductMeasure();
		Vector vectorTemplate = new DenseVector(0);
		//RawReader rawReader = new SimplestRawReader(new File(corpus));
		RawReader rawReader = new SplitWordRawReader(new File(corpus), vectorTemplate);
		Vectorization vectorization = new SplitedText2VectorTfidf(vectorTemplate);
		ExperimentsImporter importer = new CommonExperimentsImporter(rawReader, vectorization);
		Experiments experiments = importer.read();
		DensityKernel kernel = new CutoffKernel();
		double densityThreshold = 10;
		double deltaThreshold = 0.42;
		double percent = -0.1;
		DensityPeaks densityPeaks = new DensityPeaks(experiments, measure, kernel, densityThreshold,
				deltaThreshold, percent);
		densityPeaks.cluster();
		densityPeaks.dumpFile(new File("D:\\data\\cluster\\merge\\cluster.out"), false);
	}
	
	//concat tf-idf向量和paragraph2vec的向量
	public void densityPeaks2() throws IOException {
		//String corpus = "D:\\data\\cluster\\desityPicksMethod\\raw\\minklov.sentence.bin.phone";
		String corpus = "D:\\data\\cluster\\merge\\paragraphvectors.bin";
		DissimilarityMeasure measure = new InnerProductMeasure();
		Vector vectorTemplate = new DenseVector(0);
		//tf-idf向量
		//RawReader splitwordRawReader = new SplitWordRawReader(new File(corpus));
		RawReader rawReader = new SimplestRawReader(new File(corpus), vectorTemplate);
		Vectorization vectorization = new SplitedText2VectorTfidf(vectorTemplate);
		ExperimentsImporter commonExperimentsImporter = new CommonExperimentsImporter(rawReader, vectorization);
		//paragraph2vec向量
		RawReader simplestRawReader = new SimplestRawReader(new File(corpus), vectorTemplate);
		DirectImporter directImporter = new DirectImporter(simplestRawReader);
		//组合Importer
		List<ExperimentsImporter> importers = new ArrayList<ExperimentsImporter>();
		importers.add(commonExperimentsImporter);
		importers.add(directImporter);
		ExperimentsCombiner combiner = new VectorConcatCombiner(vectorTemplate);
		ExperimentsImporter combinerImporter = new CombineExperimentsImporter(importers, combiner);
		Experiments experiments = combinerImporter.read();

		DensityKernel kernel = new CutoffKernel();
		double densityThreshold = 10;
		double deltaThreshold = 0.42;
		double percent = -0.1;
		DensityPeaks densityPeaks = new DensityPeaks(experiments, measure, kernel, densityThreshold,
				deltaThreshold, percent);
		densityPeaks.cluster();
		densityPeaks.dumpFile(new File("D:\\data\\cluster\\merge\\cluster.out"), false);
	}

	public void densityPeaks3() throws IOException {
		//String corpus = "D:\\data\\cluster\\desityPicksMethod\\raw\\para_vectors.bin.phone";
		String corpus = "D:\\data\\cluster\\merge\\paragraphvectors.bin";
		DissimilarityMeasure measure = new InnerProductMeasure();
		Vector vectorTemplate = new DenseVector(0);
		//RawReader rawReader = new SimplestRawReader(new File(corpus));
		RawReader rawReader = new SimplestRawReader(new File(corpus), vectorTemplate);
		ExperimentsImporter importer = new DirectImporter(rawReader);
		Experiments experiments = importer.read();
		DensityKernel kernel = new CutoffKernel();
		double densityThreshold = 5;
		double deltaThreshold = 0.1;
		double percent = -0.1;
		DensityPeaks densityPeaks = new DensityPeaks(experiments, measure, kernel, densityThreshold,
				deltaThreshold, percent);
		densityPeaks.cluster();
		densityPeaks.dumpFile(new File("D:\\data\\cluster\\merge\\cluster.out"), false);
	}
	
	public void eval() throws IOException {
		ClusterEvaluator evaluator = new PurityEvaluator(); 
		//evaluator.eval(new File("D:\\data\\cluster\\kmeans\\cluster.out"));
		//evaluator.eval(new File("D:\\data\\cluster\\hac\\clusters.out"));
		evaluator.eval(new File("D:\\data\\cluster\\merge\\cluster.out"));
	}
	
	public void hac() throws IOException {
		//String corpus = "D:\\data\\cluster\\para_vectors.bin.phone.txt";
		String corpus = "D:\\data\\cluster\\desityPicksMethod\\raw\\para_vectors.bin.phone";
		DissimilarityMeasure measure = new InnerProductMeasure();
		Vector vectorTemplate = new DenseVector(0);
		RawReader rawReader = new SimplestRawReader(new File(corpus), vectorTemplate);
		Vectorization vectorization = new SplitedText2VectorTfidf(vectorTemplate);
		ExperimentsImporter importer = new CommonExperimentsImporter(rawReader, vectorization);
		Experiments experiments = importer.read();
		AgglomerationMethod agglomerationMethod = new WardLinkage();
		ClusteringBuilder dendrogramBuilder = new DendrogramBuilder(experiments.numberOfExperiments());

		HierarchicalAgglomerativeClusterFaster cluster = new HierarchicalAgglomerativeClusterFaster(experiments,
				dendrogramBuilder, measure, agglomerationMethod, 0.6, 5);
		cluster.cluster();
		cluster.dumpFile(new File("D:\\data\\cluster\\desityPicksMethod\\mine\\cluster.out"), false);
	}

	public void hac2() throws IOException {
		String corpus = "D:\\data\\cluster\\desityPicksMethod\\raw\\para_vectors.bin.phone";
		DissimilarityMeasure measure = new InnerProductMeasure();
		Vector vectorTemplate = new DenseVector(0);
		RawReader rawReader = new SimplestRawReader(new File(corpus), vectorTemplate);
		ExperimentsImporter importer = new DirectImporter(rawReader);
		Experiments experiments = importer.read();
		AgglomerationMethod agglomerationMethod = new WardLinkage();
		ClusteringBuilder dendrogramBuilder = new DendrogramBuilder(experiments.numberOfExperiments());

		HierarchicalAgglomerativeClusterFaster cluster = new HierarchicalAgglomerativeClusterFaster(experiments,
				dendrogramBuilder, measure, agglomerationMethod, 0.7, 5);
		cluster.cluster();
		cluster.dumpFile(new File("D:\\data\\cluster\\desityPicksMethod\\mine\\cluster.out"), false);
	}

	public void hac3() throws IOException {
		String corpus = "D:\\data\\cluster\\desityPicksMethod\\raw\\para_vectors.bin.phone";
		DissimilarityMeasure measure = new InnerProductMeasure();
		Vector vectorTemplate = new DenseVector(0);
		//tf-idf向量
		RawReader splitwordRawReader = new SplitWordRawReader(new File(corpus), vectorTemplate);
		Vectorization vectorization = new SplitedText2VectorTfidf(vectorTemplate);
		ExperimentsImporter commonExperimentsImporter = new CommonExperimentsImporter(splitwordRawReader, vectorization);
		//paragraph2vec向量
		RawReader simplestRawReader = new SimplestRawReader(new File(corpus), vectorTemplate);
		DirectImporter directImporter = new DirectImporter(simplestRawReader);
		//组合Importer
		List<ExperimentsImporter> importers = new ArrayList<ExperimentsImporter>();
		importers.add(commonExperimentsImporter);
		importers.add(directImporter);
		ExperimentsCombiner combiner = new VectorConcatCombiner(vectorTemplate);
		ExperimentsImporter combinerImporter = new CombineExperimentsImporter(importers, combiner);
		Experiments experiments = combinerImporter.read();

		AgglomerationMethod agglomerationMethod = new WardLinkage();
		ClusteringBuilder dendrogramBuilder = new DendrogramBuilder(experiments.numberOfExperiments());

		HierarchicalAgglomerativeClusterFaster cluster = new HierarchicalAgglomerativeClusterFaster(experiments,
				dendrogramBuilder, measure, agglomerationMethod, 0.7, 5);
		cluster.cluster();
		cluster.dumpFile(new File("D:\\data\\cluster\\desityPicksMethod\\mine\\cluster.out"), false);
	}
	
	private void kmeans() throws IOException {
		String corpus = "D:\\data\\cluster\\kmeans\\raw\\para_vectors.bin.phone";
		DissimilarityMeasure measure = new InnerProductMeasure();
		Vector vectorTemplate = new DenseVector(0);
		RawReader rawReader = new SimplestRawReader(new File(corpus), vectorTemplate);
		Vectorization vectorization = new SplitedText2VectorTfidf(vectorTemplate);
		ExperimentsImporter importer = new CommonExperimentsImporter(rawReader, vectorization);
		Experiments experiments = importer.read();
		ClusterMethod kmeans = new KMeans(experiments, 10, measure);
		kmeans.cluster();
		kmeans.dumpFile(new File("D:\\data\\cluster\\kmeans\\cluster.out"), false);
	}
	
	private void partitionHAC() throws IOException {
		String corpus = "D:\\data\\cluster\\merge\\paragraphvectors.bin";
		DissimilarityMeasure measure = new InnerProductMeasure();
		Vector vectorTemplate = new DenseVector(0);
		RawReader rawReader = new SimplestRawReader(new File(corpus), vectorTemplate);
		Vectorization vectorization = new SplitedText2VectorTfidf(vectorTemplate);
		ExperimentsImporter importer = new CommonExperimentsImporter(rawReader, vectorization);
		//ExperimentsImporter importer = new DirectImporter(rawReader);
		Experiments experiments = importer.read();
		ClusteringBuilder dendrogramBuilder = new DendrogramBuilder(experiments.numberOfExperiments());
		AgglomerationMethod agglomerationMethod = new WardLinkage();
		ClusterMethod phac = new PartitionedHAC(experiments, dendrogramBuilder, measure, 
				agglomerationMethod, 0.7, 4, 5000);
		phac.cluster();
		phac.dumpFile(new File("D:\\data\\cluster\\merge\\cluster.out"), false);
	}

	private void partitionHAC2() throws IOException {
		String corpus = "D:\\data\\cluster\\merge\\paragraphvectors.bin";
		DissimilarityMeasure measure = new InnerProductMeasure();
		Vector vectorTemplate = new DenseVector(0);
		//tf-idf向量
		RawReader splitwordRawReader = new SimplestRawReader(new File(corpus), vectorTemplate);
		Vectorization vectorization = new SplitedText2VectorTfidf(vectorTemplate);
		ExperimentsImporter commonExperimentsImporter = new CommonExperimentsImporter(splitwordRawReader, vectorization);
		//paragraph2vec向量
		RawReader simplestRawReader = new SimplestRawReader(new File(corpus), vectorTemplate);
		DirectImporter directImporter = new DirectImporter(simplestRawReader);
		//组合Importer
		List<ExperimentsImporter> importers = new ArrayList<ExperimentsImporter>();
		importers.add(commonExperimentsImporter);
		importers.add(directImporter);
		ExperimentsCombiner combiner = new VectorConcatCombiner(vectorTemplate);
		ExperimentsImporter combinerImporter = new CombineExperimentsImporter(importers, combiner);
		Experiments experiments = combinerImporter.read();

		AgglomerationMethod agglomerationMethod = new WardLinkage();
		ClusteringBuilder dendrogramBuilder = new DendrogramBuilder(experiments.numberOfExperiments());
		ClusterMethod phac = new PartitionedHAC(experiments, dendrogramBuilder, measure, 
				agglomerationMethod, 0.23, 4, 5000);
		phac.cluster();
		phac.dumpFile(new File("D:\\data\\cluster\\merge\\cluster.out"), false);
	}

	public static void main(String[] args) throws IOException {
		ClusteringDemo clusteringDemo = new ClusteringDemo();
		//clusteringDemo.densityPeaks();
		//clusteringDemo.densityPeaks2();
		//clusteringDemo.densityPeaks3();
		//clusteringDemo.eval();
		//clusteringDemo.hac();
		//clusteringDemo.hac2();
		//clusteringDemo.hac3();
		clusteringDemo.kmeans();
		//clusteringDemo.partitionHAC();
		//clusteringDemo.partitionHAC2();
		//clusteringDemo.eval();
	}
}
