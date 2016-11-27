package com.lbc.nlp_algorithm.clustering.hac.common.vectorization;

import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 *
 */
public class DenseVector implements Vector {
	private final Logger LOG = LoggerFactory.getLogger(DenseVector.class);
	//向量长度
	private int length;
	//必须要按照index排序
	private HashMap<Integer, Double> values;

	public DenseVector(int length) {
		this.length = length;
		this.values = Maps.newHashMap();
	}

	public DenseVector(int length, HashMap<Integer, Double> values) {
		this.length = length;
		this.values = values;
	}

	@Override
	public void set(int index, double value) {
		values.put(index, value);
	}

	@Override
	public void norm() {
		double sum = 0.0;
		for (Entry<Integer, Double> entry : values.entrySet())
			sum += (entry.getValue() * entry.getValue());
		sum = Math.sqrt(sum);
		for (Entry<Integer, Double> entry : values.entrySet())
			values.put(entry.getKey(), entry.getValue() / sum);
	}

	@Override
	public int getLength() {
		return length;
	}

	//如果有一个向量是空向量，那么它和任何向量的距离都是最大值
	@Override
	public double cosine(Vector v) {
		double mole = 0.0, deno1 = 0.0, deno2 = 0.0;
		@SuppressWarnings("unchecked")
		HashMap<Integer, Double> newValues = (HashMap<Integer, Double>) v.getVales();
		for (Entry<Integer, Double> entry : values.entrySet()) {
			Double number = newValues.get(entry.getKey());
			if (null != number)
				mole += (number * entry.getValue());
			deno1 += (entry.getValue() * entry.getValue());
		}
		for (Entry<Integer, Double> entry : newValues.entrySet()) {
			deno2 += (entry.getValue() * entry.getValue());
		}
		double score = mole / (Math.sqrt(deno1) * Math.sqrt(deno2));
		if (mole == 0.0)
			score = -1;
		return 1 - score;
	}

	@Override
	public Object getVales() {
		return values;
	}

	//必须要进行对象的深拷贝
	@Override
	public Vector clone() {
		HashMap<Integer, Double> newValues = Maps.newHashMap();
		for (Entry<Integer, Double> entry : values.entrySet())
			newValues.put(entry.getKey(), entry.getValue());
		DenseVector newVector = new DenseVector(length, newValues);
		return newVector;
	}

	//DenseVector只存储非0数据
	@Override
	public void reset(double[] v) {
		this.length = v.length;
		values = Maps.newHashMap();
		for (int i = 0; i < v.length; i++) {
			Double d = v[i];
			if (0 != d.compareTo(0.0))
				values.put(i, v[i]);
		}
	}

	@Override
	public void elementPlus(final Vector v) {
		if (v.getLength() != getLength()) {
			LOG.error("Vector size are not the same ......");
			return ;
		}
		@SuppressWarnings("unchecked")
		HashMap<Integer, Double> newValues = (HashMap<Integer, Double>) v.getVales();
		for (Entry<Integer, Double> entry : newValues.entrySet()) {
			Double number = values.get(entry.getKey());
			if (null == number) {
				values.put(entry.getKey(), entry.getValue());
			} else {
				values.put(entry.getKey(), number + entry.getValue());
			}
		}
	}

	@Override
	public void elementDivid(double d) {
		for (Integer key : values.keySet()) {
			values.put(key, values.get(key) / d);
		}
	}

	@Override
	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public void resetValues() {
		this.values = Maps.newHashMap();
	}

	//index要顺移,将v向量append在后面
	@Override
	public void concat(Vector v) {
		@SuppressWarnings("unchecked")
		HashMap<Integer, Double> newValues = (HashMap<Integer, Double>) v.getVales();
		for (Entry<Integer, Double> entry : newValues.entrySet()) {
			values.put(getLength() + entry.getKey(), entry.getValue());
		}
		this.length += v.getLength();
	}

	@Override
	public Double get(int index) {
		return values.get(index);
	}
}
