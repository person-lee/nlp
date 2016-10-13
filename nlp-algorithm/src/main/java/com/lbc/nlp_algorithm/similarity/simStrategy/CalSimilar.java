package com.lbc.nlp_algorithm.similarity.simStrategy;

import com.lbc.nlp_domain.Word;

public abstract class CalSimilar {
	private CalSimilar calSimilar = null;
	
	public abstract Float handler(Word word1, Word word2);

	public CalSimilar getCalSimilar() {
		return calSimilar;
	}

	public void setCalSimilar(CalSimilar calSimilar) {
		this.calSimilar = calSimilar;
	}

}
