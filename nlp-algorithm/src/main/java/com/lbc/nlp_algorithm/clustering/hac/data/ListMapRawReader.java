package com.lbc.nlp_algorithm.clustering.hac.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.filter.Filter;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;

/**
 * 使用List<Map>>初始化数据，Map应包含以下字段：
 * sentence		要聚类的语料(String,必选)
 * tag			语料标识(String,可选)
 * vector		语料向量(String,可选)
 */
public class ListMapRawReader implements RawReader {
	private final Logger LOG = LoggerFactory.getLogger(ListMapRawReader.class);

	public final static String SENTENCE_KEY = "sentence";
	public final static String TAG_KEY = "tag";
	public final static String VECTOR_KEY = "vector";

	private List<Map<String, String>> input;
	private List<RawNode> nodes;
	private Vector vectorClone;
	private List<Filter> filterList;

	public ListMapRawReader(List<Map<String, String>> input, Vector vectorClone) {
		this.input = input;
		this.vectorClone = vectorClone;
		nodes = new ArrayList<RawNode>();
	}

	public ListMapRawReader(List<Map<String, String>> input, Vector vectorClone, List<Filter> filterList) {
		this.input = input;
		this.vectorClone = vectorClone;
		nodes = new ArrayList<RawNode>();
		this.filterList = filterList;
	}

	@Override
	public void read() throws IOException {
		for (Map<String, String> map : input) {
			if (map == null || map.size() == 0 || !map.containsKey(SENTENCE_KEY)) {
				LOG.error("map data format error ......");
				System.exit(1);
			}
			RawNode rawNode = new RawNode();

			String sentence = map.get(SENTENCE_KEY);
			if (null != filterList) {
				boolean skip = false;
				for (Filter filter : filterList) {
					if (! filter.isUseful(sentence)) {
						LOG.info("filtered data=" + sentence.trim());
						skip = true;
						break;
					}
					sentence = filter.filter(sentence);
				}
				if (true == skip)
					continue;
			}
			List<Term> words = ToAnalysis.parse(sentence.trim());
			String splited = "";
			for (Term term : words) {
				splited += (term.getName() + " ");
			}
			splited = splited.replaceAll("\\s+", " ");
			rawNode.setSentence(splited);

			if (map.containsKey(TAG_KEY)) {
				rawNode.setTag(map.get(TAG_KEY));
			}

			if (map.containsKey(VECTOR_KEY)) {
				String[] numbers = map.get(VECTOR_KEY).trim().split("\\s+");
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

	@Override
	public int numberOfObservations() {
		return nodes.size();
	}

	@Override
	public RawNode get(int index) {
		return nodes.get(index);
	}

}
