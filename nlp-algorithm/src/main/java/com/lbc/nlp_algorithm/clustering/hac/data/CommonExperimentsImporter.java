package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.IOException;

import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vectorization;

public class CommonExperimentsImporter implements ExperimentsImporter {

	private RawReader rawReader;
	private Vectorization vectorization;
	
	public CommonExperimentsImporter(RawReader rawReader, Vectorization vectorization) {
		this.rawReader = rawReader;
		this.vectorization = vectorization;
	}

	@Override
	public Experiments read() throws IOException {
		Experiments experiments = new Experiments();
		rawReader.read();
		
		int nObservations = rawReader.numberOfObservations();
		String[] objects = new String[nObservations];
		for (int i = 0; i < nObservations; i++) {
			objects[i] = rawReader.get(i).getSentence();
		}

		Vector vectors[] = vectorization.convert2Vector(objects);
		for (int i = 0; i < vectors.length; i++) {
			ExperimentNode node = new ExperimentNode(vectors[i], rawReader.get(i).getSentence(), 
					rawReader.get(i).getTag(), i);
			experiments.add(node);
		}
		
		return experiments;
	}
}
