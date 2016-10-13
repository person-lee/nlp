package com.lbc.nlp_algorithm.loadFile.loadData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lbc.nlp_algorithm.classification.liblinear.FeatureNode;
import com.lbc.nlp_algorithm.prepocess.core.utils.FileUtils;
import com.lbc.nlp_modules.common.tuple.Pair;

public class LoadTrainData{
	private static final Logger log = LoggerFactory.getLogger(LoadData.class.getName());
	
	public static Pair<Integer, Map<Integer, List<FeatureNode[]>>> loadData(String dir, String filename) throws IOException {
		BufferedReader reader = null;
		FileInputStream fis = null;
		Map<Integer/* label */, List<FeatureNode[]/* 对应一个句子的特征 */>> features = Maps.newHashMap();
		int featureNum = 0;
		try {
			fis = new FileInputStream(new File(dir, filename));
			reader = new BufferedReader(new InputStreamReader(fis, Charsets.UTF_8));
			String line = null;
			while((line = reader.readLine()) != null) {
				line = StringUtils.trimToEmpty(line);
				if(StringUtils.isNotBlank(line)) {
					String[] aWord = line.split("\\s");
					List<String> vectors = Arrays.asList(aWord);
					Integer label = Integer.valueOf(vectors.get(0));// 类别label
					List<String> vector = vectors.subList(1, vectors.size());
					FeatureNode[] featureNodes = new FeatureNode[vector.size()];
					int index = 0;
					for (String element : vector) {
						String[] arr = StringUtils.split(element, ":");
						int id = Integer.valueOf(arr[0]);
						if (id > featureNum) {
							featureNum = id;
						}
						featureNodes[index++] = new FeatureNode(id, Double.valueOf(arr[1]));
					}
					
					Arrays.sort(featureNodes, new Comparator<FeatureNode>() {
						@Override
						public int compare(FeatureNode o1, FeatureNode o2) {
							return o1.getIndex() - o2.getIndex();
						}
					});
					
					List<FeatureNode[]> featureNodeList = features.get(label);
					if (CollectionUtils.isEmpty(featureNodeList)) {
						featureNodeList = Lists.newArrayList();
					} 
					
					featureNodeList.add(featureNodes);
					features.put(label, featureNodeList);
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new FileExistsException("文件不存在");
		} finally {
			FileUtils.closeQuietly(fis, reader);
		}
		return new Pair<Integer, Map<Integer,List<FeatureNode[]>>>(featureNum, features);
	}
	
	public static BufferedReader loadFile(String dir, String filename) throws IOException {
		if (StringUtils.isNotBlank(dir) && StringUtils.isNotBlank(filename)) {
			FileInputStream fis = new FileInputStream(new File(dir, filename));
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis, Charsets.UTF_8));
			return reader;
		} else {
			return null;
		}
	}

}
