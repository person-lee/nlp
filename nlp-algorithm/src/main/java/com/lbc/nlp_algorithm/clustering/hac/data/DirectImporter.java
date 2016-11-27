package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;

/**
 * 直接读取数据，数据都不需要转换
 * @author jiangwen
 *
 */
public class DirectImporter implements ExperimentsImporter {
	private final Logger LOG = LoggerFactory.getLogger(DirectImporter.class);

	private RawReader rawReader;
	
	public DirectImporter(RawReader rawReader) {
		this.rawReader = rawReader;
	}

	@Override
	public Experiments read() throws IOException {
		Experiments experiments = new Experiments();
		rawReader.read();
		int nObservations = rawReader.numberOfObservations();
		for (int i = 0; i < nObservations; i++) {
			Vector vector = rawReader.get(i).getVector();
			if (null == vector) {
				LOG.error("vector is null, check your data!");
				return null;
			}
			ExperimentNode node = new ExperimentNode(vector, rawReader.get(i).getSentence(),
					rawReader.get(i).getTag(), i);
			experiments.add(node);
		}
		
		return experiments;
	}
}
