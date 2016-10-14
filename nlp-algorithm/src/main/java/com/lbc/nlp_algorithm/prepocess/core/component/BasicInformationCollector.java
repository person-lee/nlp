package com.lbc.nlp_algorithm.prepocess.core.component;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Charsets;
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
			
			// 统计文件行数
			int docCount = 0;
			if (ArrayUtils.isNotEmpty(files)) {
				for (File file : files) {
					try {
						List<String> lines = FileUtils.readLines(file, Charsets.UTF_8);
						docCount += lines.size();
					} catch (Exception e) {
						LOG.error(e.getMessage());
					}
				}
			}
			
			context.getVectorMetadata().putLabelledTotalDocCount(label, docCount);
			LOG.info("Put document count: label= " + label + ", docCount=" + docCount);
			totalDocCount += docCount;
		}
		LOG.info("Total documents: totalCount= " + totalDocCount);
		context.getVectorMetadata().setTotalDocCount(totalDocCount);
	}

}
