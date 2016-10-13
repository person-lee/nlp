package com.lbc.nlp_algorithm.prepocess.core.component.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.core.component.AbstractOutputtingQuantizedData;
import com.lbc.nlp_algorithm.prepocess.core.utils.FileUtils;

public class OutputtingQuantizedTestData extends AbstractOutputtingQuantizedData {

	private static final Log LOG = LogFactory.getLog(OutputtingQuantizedTestData.class);
	
	public OutputtingQuantizedTestData(final Context context) {
		super(context);
	}

	@Override
	public void fire() {
		super.fire();
	}

	@Override
	protected void quantizeTermVectors() {
		// load label vector
		LOG.info("Load label vector...");
		// <label, labelId> pairs
		Map<String, Integer> globalLabelToIdMap = Maps.newHashMap();
		// <labelId, label> pairs
		Map<Integer, String> globalIdToLabelMap = Maps.newHashMap();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(context.getFDMetadata()
							.getLabelVectorFile()), context.getCharset()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					String[] aLabel = line.split("\\s+");
					if (aLabel.length == 2) {
						int labelId = Integer.parseInt(aLabel[0]);
						String label = aLabel[1];
						globalIdToLabelMap.put(labelId, label);
						globalLabelToIdMap.put(label, labelId);
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeQuietly(reader);
		}
		LOG.info("Loaded: globalIdToLabelMap=" + globalIdToLabelMap);
		context.getVectorMetadata().putIdToLabelPairs(globalIdToLabelMap);
		context.getVectorMetadata().putLabelToIdPairs(globalLabelToIdMap);
	}
	
}
