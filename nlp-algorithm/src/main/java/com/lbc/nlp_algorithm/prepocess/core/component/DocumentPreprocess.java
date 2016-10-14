package com.lbc.nlp_algorithm.prepocess.core.component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.core.common.AbstractComponent;
import com.lbc.nlp_modules.common.tuple.Pair;

public class DocumentPreprocess extends AbstractComponent{
	private static final Log LOG = LogFactory.getLog(DocumentPreprocess.class);
	
	public DocumentPreprocess(final Context context) {
		super(context);
	}
	
	@Override
	public void fire() {
		String[] labels = context.getFDMetadata().getInputRootDir().list();
		if (ArrayUtils.isNotEmpty(labels)) {
			List<Pair<String, File[]>> allFiles = Lists.newArrayList();
			for (String label : labels) {
				File labelDir = new File(context.getFDMetadata().getInputRootDir(), label);
				File[] files = labelDir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.getAbsolutePath().endsWith(context.getFDMetadata().getFileExtensionName());
					}
				});
				allFiles.add(Pair.with(label, files));
			}
			
			try {
				File dupFile = new File(context.getFDMetadata().getPreprocessDir(), context.getFDMetadata().getDupFile());
				uniqFilesContent(allFiles, context.getFDMetadata().getPreprocessDir(), dupFile);
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		}
	}
	
	/**
	 * 获取文件的总行数
	 * @param files
	 * @return
	 */
	private int calLines(File[] files) {
		if (ArrayUtils.isNotEmpty(files)) {
			int length = 0;
			for (File file : files) {
				length += file.length();
			}
			return length;
		} else {
			return -1;
		}
	}
	
	/**
	 * 对文件集合去重。优先保留文件较小的数据内容。
	 * @param inputDir
	 * @param uniqedDir
	 * @param recordDuplicate
	 * @throws IOException 
	 */
	private void uniqFilesContent(List<Pair<String, File[]>> oriFiles, File uniqqedDir, File recordDuplicate) throws Exception{
		// 按语料大小从小到大排，这样在去重时，优先保留数据较少的语料
		Collections.sort(oriFiles, new Comparator<Pair<String, File[]>>() {
			@Override
			public int compare(Pair<String, File[]> o1, Pair<String, File[]> o2) {
				int object1 = calLines(o1.getValue1());
				int object2 = calLines(o2.getValue1());
				if (-1 == object1) {
					return -1;
				} else if (-1 == object2){
					return 1;
				} else {
					return (int) (object1 - object2);
				}
			}
		});
		
		Map<String, String> occured_data = Maps.newHashMap();
		BufferedWriter dup_bw = new BufferedWriter(new FileWriter(recordDuplicate));
		for(Pair<String, File[]> ori_file : oriFiles){
			String label = ori_file.getValue0();
			File[] files = ori_file.getValue1();
			File directory = new File(uniqqedDir, label);
			if(!directory.exists()){
				directory.mkdirs();
			} 
			
			if (ArrayUtils.isNotEmpty(files)) {
				for (File file : files) {
					List<String> out_lines = Lists.newArrayList();
					String file_name = file.getName();
					for(String line : FileUtils.readLines(file, "UTF-8")){
						if (StringUtils.isNotBlank(line)) {
							String[] quest = line.split("\t");
							String content = StringUtils.trimToEmpty(quest[1]);
							String occured_name = occured_data.get(content);
							if(StringUtils.isNoneBlank(occured_name)){
								LOG.warn(content + "\t\t" + occured_name + "####" + file_name);
								dup_bw.write(line + "\t" + occured_name + "\t" + file_name);
								dup_bw.newLine();
								continue;
							}
							occured_data.put(content, file_name);
							out_lines.add(line);
						}
					}
					if(out_lines.isEmpty()){
						LOG.error("Empty intent : " + file_name);
					}
					File dest_file = new File(directory, file_name);
					FileUtils.writeLines(dest_file, "UTF-8", out_lines);
				}
			}
		}
		dup_bw.close();
	}

}
