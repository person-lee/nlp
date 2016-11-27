package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.filter.Filter;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;

/**
 * 对输入的原始数据的第一列进行分词(第一列默认都是用户句子)
 * 在第三列加入一个分类字段，因此Vector后移一列。 分类字段会并入tag以进行区分
 */
public class SplitWordWithTagRawReader implements RawReader {
	private final Logger LOG = LoggerFactory.getLogger(SplitWordWithTagRawReader.class);
	private File file;
	private List<RawNode> nodes;
	private String seperator = Constants.STRING_SEPERATOR;
	private Vector vectorClone;
	private List<Filter> filterList;

	public SplitWordWithTagRawReader(File file, Vector vectorClone) {
		this.file = file;
		this.vectorClone = vectorClone;
		nodes = new ArrayList<RawNode>();
	}

	public SplitWordWithTagRawReader(File file, Vector vectorClone, List<Filter> filterList) {
		this.file = file;
		this.vectorClone = vectorClone;
		nodes = new ArrayList<RawNode>();
		this.filterList = filterList;
	}

	@Override
	public void read() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		String line = null;
		while (null != (line = br.readLine())) {
			String[] items = line.trim().split(seperator);
			if (0 == items.length) {
				LOG.error("raw data file format error ......");
				System.exit(1);
			}
			RawNode rawNode = new RawNode();
			//和SimplestRawReader唯一不同的在这里，对原始句子进行ansj分词
			if (items.length >= 1) {
				if (null != filterList) {
					boolean skip = false;
					for (Filter filter : filterList) {
						if (! filter.isUseful(items[0])) {
							LOG.info("filtered data=" + line.trim());
							skip = true;
							break;
						}
						items[0] = filter.filter(items[0]);
					}
					if (true == skip)
						continue;
				}
				List<Term> words = ToAnalysis.parse(items[0].trim());
				String splited = "";
				for (Term term : words)
					splited += (term.getName() + " ");
				splited = splited.replaceAll("\\s+", " ");
				rawNode.setSentence(splited);
			}
			if (items.length >= 2)
				rawNode.setTag(items[1].trim());
			if (items.length >= 3 && StringUtils.isNoneBlank(items[2]))
				rawNode.setTag(items[1].trim() + seperator + items[2].trim());
			if (items.length >= 4) {
				String[] numbers = items[3].trim().split("\\s+");
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
