package com.lbc.nlp_algorithm.clustering.hac.eval;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 聚类purity(纯度)的评估方法
 * from http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
 *
 */
public class PurityEvaluator implements ClusterEvaluator {
	private final Logger LOG = LoggerFactory.getLogger(PurityEvaluator.class);
	
	private ClusterResultReader clusterResultReader = new ClusterResultReader();
	
	@Override
	public void eval(File file) throws IOException {
		Map<String, List<ClusterResult>> data = clusterResultReader.readCluster(file);
		Set<String> set = new HashSet<String>();
		int yes = 0, all = 0, num = 0;
		for (Entry<String, List<ClusterResult>> entry : data.entrySet()) {
			Map<String, Integer> counter = new HashMap<String, Integer>();
			if (entry.getValue().size() < 1)
				continue;
			num++;
			for (ClusterResult clusterResult : entry.getValue()) {
				if (null == counter.get(clusterResult.tag))
					counter.put(clusterResult.tag, 0);
				counter.put(clusterResult.tag, counter.get(clusterResult.tag) + 1);
			}
			Entry<String, Integer> max = null;
			for (Entry<String, Integer> cnEntry : counter.entrySet()) {
				if (null == max)
					max = cnEntry;
				if (cnEntry.getValue() > max.getValue())
					max = cnEntry;
			}
			double purity = max.getValue() / (double) entry.getValue().size();
			LOG.info("cluster id:" + entry.getKey() + ", name:" + max.getKey() + ", purity:" + purity);
			all += entry.getValue().size();
			yes += max.getValue();
			set.add(max.getKey());
		}
		double purity = yes / (double) all;
		LOG.info("all purity:" + purity + ", unduplicated cluster size:" + set.size() + ", cluster size:"
				+ num + ", recall:" + all);
		LOG.info("unduplicate cluster name:");
		String names = "";
		for (String string : set)
			names += (string + ", ");
		LOG.info(names);
	}
}
