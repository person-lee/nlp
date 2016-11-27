package com.lbc.nlp_algorithm.clustering.hac.data;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.clustering.hac.common.vectorization.Vector;

public class VectorConcatCombiner implements ExperimentsCombiner {
	private final Logger LOG = LoggerFactory.getLogger(SplitWordRawReader.class);
	private Vector vectorClone;
	
	public VectorConcatCombiner(Vector vectorClone) {
		this.vectorClone = vectorClone;
	}

	@Override
	public Experiments combine(List<Experiments> experimentsList) {
		if (null == experimentsList || experimentsList.size() < 2) {
			LOG.error("combine input data error ......");
			return null;
		}
			
		Experiments combinedExperiments = new Experiments();
		int nObservations = experimentsList.get(0).numberOfExperiments();
		for (int i = 0; i < nObservations; i++) {
			Vector mergedVector = vectorClone.clone();
			mergedVector.resetValues();
			for (Experiments experiments : experimentsList) {
				mergedVector.concat(experiments.get(i).getVector());
			}

			//所有ExperimentNode的description/tag/id都是一样的,否则这种合并方式就丢失信息了
			ExperimentNode node = experimentsList.get(0).get(i);
			combinedExperiments.add(new ExperimentNode(mergedVector, node.getDescription(), 
					node.getTag(), node.getId()));
		}
		return combinedExperiments;
	}

}
