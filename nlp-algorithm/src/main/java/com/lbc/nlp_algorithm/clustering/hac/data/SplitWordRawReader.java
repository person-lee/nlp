package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.filter.Filter;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;

/**
 * 对输入的原始数据的第一列进行分词(第一列默认都是用户句子)
 *
 */
public class SplitWordRawReader implements RawReader {
	private final Logger LOG = LoggerFactory.getLogger(SplitWordRawReader.class);
	
	private File file;
	private List<RawNode> nodes = Lists.newArrayList();;
	private Vector vectorClone;
	private List<Filter> filterList;
	
	public SplitWordRawReader(File file, Vector vectorClone) {
		this.file = file;
		this.vectorClone = vectorClone;
	}

	public SplitWordRawReader(File file, Vector vectorClone, List<Filter> filterList) {
		this.file = file;
		this.vectorClone = vectorClone;
		this.filterList = filterList;
	}

	@Override
	public void read() throws IOException {
		Set<String> lines = Sets.newHashSet();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		String line = null;
		while (null != (line = br.readLine())) {
			if (StringUtils.isNotBlank(line)) {
				line = StringUtils.strip(line);
				if (!lines.contains(line)) {
					lines.add(line);
					String[] items = line.trim().split(Constants.STRING_SEPERATOR);
					if (ArrayUtils.isEmpty(items)) {
						LOG.error("raw data file format error ......");
						return;
					}
					RawNode rawNode = new RawNode();
					//和SimplestRawReader唯一不同的在这里，对原始句子进行ansj分词
					if (items.length >= 1) {
						if (CollectionUtils.isNotEmpty(filterList)) {
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
						StringBuilder splited = new StringBuilder();
						for (Term term : words)
							splited.append(term.getName() + " ");
						String sentence = splited.toString().replaceAll("\\s+", " ");
						rawNode.setSentence(sentence);
					}
					if (items.length >= 2)
						rawNode.setTag(items[1].trim());
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
			}
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
