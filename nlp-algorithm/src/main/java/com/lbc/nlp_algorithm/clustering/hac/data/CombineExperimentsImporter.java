package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * 对各种ExperimentsImporter的数据进行合并,合并接口为ExperimentsCombiner
 * 各种ExperimentsImporter读取数据的顺序必须保持一致
 *
 */
public class CombineExperimentsImporter implements ExperimentsImporter {
	
	private List<ExperimentsImporter> importerList;
	private ExperimentsCombiner experimentsCombiner;
	
	public CombineExperimentsImporter(List<ExperimentsImporter> importerList, 
			ExperimentsCombiner experimentsCombiner) {
		this.importerList = importerList;
		this.experimentsCombiner = experimentsCombiner;
	}

	@Override
	public Experiments read() throws IOException {
		List<Experiments> experimentsList = Lists.newArrayList();
		for (ExperimentsImporter importer : importerList)
			experimentsList.add(importer.read());
		
		Experiments experiments = experimentsCombiner.combine(experimentsList);
		return experiments;
	}

}
