package com.lbc.nlp_algorithm.clustering.hac.common.matrix;

import java.util.List;

import com.google.common.collect.Lists;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.DenseVector;

public abstract class DenseMatrix implements Matrix {

	protected int numberOfRows;
	protected int numberOfCols;
	
	private List<DenseVector> values = Lists.newArrayList();
	
	public DenseMatrix(int numberOfRows, int numberOfCols) {
		this.numberOfRows = numberOfRows;
		this.numberOfCols = numberOfCols;
		for (int i = 0; i < numberOfRows; i++)
			values.add(new DenseVector(numberOfCols));
	}

	@Override
	public void set(int row, int col, double value) {
		values.get(row).set(col, value);
	}

	@Override
	public int getNumberOfRows() {
		return numberOfRows;
	}

	@Override
	public int getNumberOfCols() {
		return numberOfCols;
	}

	@Override
	public Double get(int row, int col) {
		return values.get(row).get(col);
	}
}
