package com.lbc.nlp_algorithm.clustering.hac.common.matrix;

/**
 * 2维数组
 * @author jiangwen
 *
 */
public interface Matrix {

	public void set(int row, int col, double value);
	
	public Double get(int row, int col);
	
	public int getNumberOfRows();
	
	public int getNumberOfCols();
}
