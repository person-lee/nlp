package com.lbc.nlp_algorithm.clustering.hac.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lbc.nlp_algorithm.clustering.hac.common.Constants;

/**
 * 读取聚类结果，聚类结果的文件格式是固定的，所以暂时只需支持这一种读取方法
 * 类之间的分隔符:"-------------------------------"
 * 每行表示一类中的一个case: [cluster id],[sentence] ##seperator## [sentence tag]
 *
 */
public class ClusterResultReader {
	
	private String seperator = Constants.STRING_SEPERATOR;

	public Map<String, List<ClusterResult>> readCluster(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		Map<String, List<ClusterResult>> data = new HashMap<String, List<ClusterResult>>();
		String line = null;
		while (null != (line = br.readLine())) {
			line = line.trim();
			String[] items = line.split(seperator);
			if (items.length < 2)
				continue;
			if (null == data.get(items[0]))
				data.put(items[0], new ArrayList<ClusterResult>());
			ClusterResult clusterResult = new ClusterResult();
			clusterResult.id = items[0];
			clusterResult.data = items[1];
			clusterResult.tag = items[2];
			data.get(items[0]).add(clusterResult);
		}
		br.close();
		return data;
	}
}
