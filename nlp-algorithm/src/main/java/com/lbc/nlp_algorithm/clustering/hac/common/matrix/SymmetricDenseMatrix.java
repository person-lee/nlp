package com.lbc.nlp_algorithm.clustering.hac.common.matrix;

public class SymmetricDenseMatrix extends DenseMatrix implements Matrix {

	public SymmetricDenseMatrix(int numberOfRows, int numberOfCols) {
		super(numberOfRows, numberOfCols);
	}
	
	@Override
	public void set(int row, int col, double value) {
		int small = row < col ? row : col;
		int big = row < col ? col : row;
		super.set(small, big, value);
	}
	
	@Override
	public Double get(int row, int col) {
		int small = row < col ? row : col;
		int big = row < col ? col : row;
		return super.get(small, big);
	}
}
