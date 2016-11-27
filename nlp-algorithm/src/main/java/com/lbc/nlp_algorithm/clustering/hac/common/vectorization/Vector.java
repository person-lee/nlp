package com.lbc.nlp_algorithm.clustering.hac.common.vectorization;

public interface Vector {

	//如存在，则覆盖原有值
	public void set(int index, double value);

	public Double get(int index);

	public void norm();

	public int getLength();

	public void setLength(int length);

	public Object getVales();

	public Vector clone();

	public void reset(double[] values);

	public void resetValues();

	//向量夹角
	public double cosine(Vector v);

	//向量连接
	public void concat(Vector v);

	//向量加和
	public void elementPlus(Vector v);

	//向量除法
	public void elementDivid(double d);
}
