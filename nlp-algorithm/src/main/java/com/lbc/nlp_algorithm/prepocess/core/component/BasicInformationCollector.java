package com.lbc.nlp_algorithm.prepocess.core.component;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.core.common.AbstractComponent;

public class BasicInformationCollector extends AbstractComponent {
	
	private static final Log LOG = LogFactory.getLog(BasicInformationCollector.class);
	
	public BasicInformationCollector(final Context context) {
		super(context);
	}

	// 统计每个分类的文档频率
	@Override
	public void fire() {
		// get total document count for computing TF-IDF
		int totalDocCount = 0;
		for(String label : context.getFDMetadata().getInputRootDir().list()) {
			context.getVectorMetadata().addLabel(label);
			LOG.info("Add label: label=" + label);
			
			// 获取该标签下的所有文件
			File labelDir = new File(context.getFDMetadata().getInputRootDir(), label);
			File[] files = labelDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getAbsolutePath().endsWith(context.getFDMetadata().getFileExtensionName());
				}
			});
			context.getVectorMetadata().putLabelledTotalDocCount(label, files.length);
			LOG.info("Put document count: label= " + label + ", docCount=" + files.length);
			totalDocCount += files.length;
		}
		LOG.info("Total documents: totalCount= " + totalDocCount);
		context.getVectorMetadata().setTotalDocCount(totalDocCount);
	}

}
