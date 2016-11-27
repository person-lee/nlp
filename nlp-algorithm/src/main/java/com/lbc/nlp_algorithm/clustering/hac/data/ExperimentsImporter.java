package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.IOException;

public interface ExperimentsImporter {

	public Experiments read() throws IOException;
}
