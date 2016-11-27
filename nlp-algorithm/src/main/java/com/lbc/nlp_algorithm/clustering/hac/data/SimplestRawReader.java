package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;

/**
 * 读入原始数据，格式必须是固定的.目前只支持读取到第二列.分隔符也是固定的
 * 第一列：句子。第二列：句子tag（测试用）。第三列：向量 ...
 *
 */
public class SimplestRawReader implements RawReader {
	private final Logger LOG = LoggerFactory.getLogger(SimplestRawReader.class);

	private File file;
	private List<RawNode> nodes;
	//分隔符这种东西直接固定了吧，把所有模块的都规范起来
	private String seperator = Constants.STRING_SEPERATOR;
	private Vector vectorClone;
	
	public SimplestRawReader(File file, Vector vectorClone) {
		this.file = file;
		nodes = new ArrayList<RawNode>();
		this.vectorClone = vectorClone;
	}

	@Override
	public void read() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		while (null != (line = br.readLine())) {
			String[] items = line.trim().split(seperator);
			if (0 == items.length) {
				LOG.error("[cluster] DirectVectorsReader file format error!");
				System.exit(1);
			}
			RawNode rawNode = new RawNode();
			if (items.length >= 1) {
				rawNode.setSentence(items[0].trim());
			}
			if (items.length >= 2) {
				rawNode.setTag(items[1].trim());
			}
			if (items.length >= 3) {
				String[] numbers = items[2].trim().split("\\s+");
				double[] values = new double[numbers.length];
				for (int i = 0; i < numbers.length; i++)
					values[i] = Double.valueOf(numbers[i]);
				Vector vector = vectorClone.clone();
				vector.reset(values);
				rawNode.setVector(vector);
			}
			nodes.add(rawNode);
		}
		br.close();
	}

	@Override
	public int numberOfObservations() {
		return nodes.size();
	}

	@Override
	public RawNode get(int index) {
		return nodes.get(index);
	}

}
