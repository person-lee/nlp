package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.IOException;

public interface RawReader {

	/**
	 * 将原始数据读入内存
	 * @throws IOException
	 */
	public void read() throws IOException;
	
	/**
	 * 语料数量
	 * @return
	 */
	public int numberOfObservations();
	
	/**
	 * 返回第index个语料
	 * @param index
	 * @return
	 */
	public RawNode get(int index);
}
