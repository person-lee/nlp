package com.lbc.nlp_algorithm.similarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.lbc.nlp_algorithm.clustering.hac.common.Constants;
import com.lbc.nlp_algorithm.clustering.hac.common.filter.Filter;
import com.lbc.nlp_algorithm.clustering.hac.common.filter.RegExpFilter;
import com.lbc.nlp_algorithm.clustering.hac.common.filter.ReplaceFilter;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.DenseVector;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.SplitedText2VectorTfidf;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;
import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vectorization;
import com.lbc.nlp_algorithm.clustering.hac.data.CommonExperimentsImporter;
import com.lbc.nlp_algorithm.clustering.hac.data.Experiments;
import com.lbc.nlp_algorithm.clustering.hac.data.ExperimentsImporter;
import com.lbc.nlp_algorithm.clustering.hac.data.RawReader;
import com.lbc.nlp_algorithm.clustering.hac.data.SplitWordForCosineLshRawReader;

/**
 * 使用余弦距离的局部敏感哈希函数
 * 尚未使用,主要问题：
 * 1、结果有随机性
 * 2、聚类效果不好
 */
public class CosineLsh {
	private static Random ra =new Random();

	private Vector sample_vector;	//模板向量
	private int hyperebene_size;	//超平面数量
	private List<Vector> hyperebene_list;	//超平面
	private List<Integer> hyperebene_cnt;	//超平面命中统计
	private int hashed_cnt;	//hash调用计数

	/**
	 * 使用随机变量初始化超平面
	 * @param hyperebene_size
	 * @param sample_vector
	 */
	public CosineLsh (int hyperebene_size, Vector sample_vector) {
		this.sample_vector = sample_vector;
		this.hyperebene_size = hyperebene_size;
		this.hyperebene_list = new ArrayList<Vector>();
		this.hyperebene_cnt = new ArrayList<Integer>();
		this.hashed_cnt = 0;

		for (int i=0; i<hyperebene_size; i++) {
			this.hyperebene_list.add(i, randHhyperebene());
			this.hyperebene_cnt.add(0);
		}
	}

	/**
	 * 生成一个随机超平面
	 * @return	超平面向量
	 */
	private Vector randHhyperebene() {
		Vector vector = sample_vector.clone();
		for (int i=0; i<vector.getLength(); i++) {
			vector.set(i, ra.nextGaussian());
		}

		return vector;
	}

	/**
	 * 获得向量的hash值
	 * @param vector	向量
	 * @return			hash值
	 */
	private String hash(Vector vector) {
		hashed_cnt++;
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<hyperebene_size; i++) {
			double distance = vector.cosine(hyperebene_list.get(i));
			int ret = (distance>1)?1:0;
			sb.append(ret);
			hyperebene_cnt.set(i, hyperebene_cnt.get(i)+ret);
		}

		return sb.toString();
	}

	/**
	 * 去除区分度不佳的超平面
	 * 基本没效果
	 */
	public void filterHash() {
		for (int i=hyperebene_size-1; i>=0; i--) {
			double ratio = hyperebene_cnt.get(i) / (double) hashed_cnt;
			if (ratio < 0.4 || ratio > 0.6) {
				hyperebene_list.remove(i);
				hyperebene_cnt.remove(i);
			}
		}
		hyperebene_size = hyperebene_list.size();
		for (int i=0; i<hyperebene_size; i++) {
			hyperebene_cnt.set(i, 0);
		}
	}

	public static void main(String[] args) {
		String filePath = "D:\\workspaces\\practiseData\\cluster\\test00.txt";

		List<Filter> filterList = new ArrayList<Filter>();
		List<String> expressions = new ArrayList<String>();
		expressions.add("http://item\\.jd\\.com/.*\\.html.*");
		filterList.add(new RegExpFilter(expressions));
		filterList.add(new ReplaceFilter("\"", ""));

		String stopWords = ". 。 , ， ! ！ ? ？ / \" # 嗯 啊 nbsp";
		Set<String> stopWordSet = new HashSet<String>();
		String[] words = stopWords.split("\\s+");
		for (String word : words) {
			stopWordSet.add(word);
		}

		try {
			File tmpFile = new File(filePath);
			BufferedReader reader = new BufferedReader(new FileReader(tmpFile));
			String tempString;
			Map<String,String> originList = new HashMap<String,String>();
			while ((tempString = reader.readLine()) != null) {
				String[] items = tempString.split(Constants.STRING_SEPERATOR);
				originList.put(items[1], items[1] + "####" + items[0]);
			}
			reader.close();

			Vector vectorTemplate = new DenseVector(0);
			RawReader rawReader = new SplitWordForCosineLshRawReader(tmpFile, vectorTemplate, filterList);
			Vectorization vectorization = new SplitedText2VectorTfidf(vectorTemplate, stopWordSet);
			ExperimentsImporter importer = new CommonExperimentsImporter(rawReader, vectorization);
			Experiments experiments = importer.read();

			List<Vector> vector_list = new ArrayList<Vector>();
			for (int i=0; i<experiments.numberOfExperiments(); i++) {
				vector_list.add(experiments.get(i).getVector());
			}

			CosineLsh instance = new CosineLsh(10, vector_list.get(0));
			Map<String, Integer> hash_cnt = new HashMap<String, Integer>();
			Map<String, List<String>> hash_detail = new HashMap<String, List<String>>();
			for (int i=0; i<vector_list.size(); i++) {
				String code = instance.hash(vector_list.get(i));
				if (hash_cnt.containsKey(code)) {
					hash_cnt.put(code, hash_cnt.get(code) + 1);
					List<String> temp = hash_detail.get(code);
					temp.add(originList.get(experiments.get(i).getTag()));
				} else {
					hash_cnt.put(code, 1);
					List<String> temp = new ArrayList<String>();
					temp.add(originList.get(experiments.get(i).getTag()));
					hash_detail.put(code, temp);
				}
			}

			int clustered_cnt = 0;
			for (Entry<String, Integer> entry : hash_cnt.entrySet()) {
				if (entry.getValue() > 1) {
					System.out.println();
					System.out.println(entry.getKey() + "::" + entry.getValue());
					for (String str : hash_detail.get(entry.getKey())) {
						System.out.println(str);
						clustered_cnt++;
					}
				}
			}
			System.out.println("source size : " + vector_list.size());
			System.out.println("hash size : " + hash_cnt.size());
			System.out.println("clustered size : " + clustered_cnt);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
