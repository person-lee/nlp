package com.lbc.nlp_algorithm.prepocess.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Charsets;

public class FileUtils {

	private static final Log LOG = LogFactory.getLog(FileUtils.class);
	
	public static void closeQuietly(Closeable... streams) {
		for(Closeable stream : streams) {
			try {
				stream.close();
			} catch (Exception e) {
				LOG.warn("Fail to close: ", e);
			}
		}
	}
	
	public static void close(Closeable... streams) throws IOException {
		for(Closeable stream : streams) {
			stream.close();
		}
	}
	
	public static <T, K> void save2File(File parentFileName, String fileName, String charset, Map<T, K> lines) throws IOException {
    	try {
    		if (MapUtils.isNotEmpty(lines)) {
    			File file = null;
    			if (parentFileName != null) {
    				file = new File(parentFileName, fileName);
    			} else {
    				file = new File(fileName);
    			}
    			
    			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
    			for (Entry<T, K> line : lines.entrySet()) {
    				String wLine = line.getKey() + "\t" + line.getValue();
    				writer.write(wLine);
    				writer.newLine();
    			}
    			closeQuietly(writer);
    		} else {
    			LOG.error("写入文件为空");
    			throw new RuntimeException("写入文件为空");
    		}
		} catch (Exception e) {
			LOG.error("写入文件出错，" + e.getMessage());
			throw new IOException("写入文件出错，" + e.getMessage());
		}
    	LOG.info("文件写入成功。");
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
	
	public static <T> void saveList2File(File parentFileName, String fileName, String charset, List<T> lines) throws IOException {
    	try {
    		if (CollectionUtils.isNotEmpty(lines)) {
    			File file = null;
    			if (parentFileName != null) {
    				file = new File(parentFileName, fileName);
    			} else {
    				file = new File(fileName);
    			}
    			
    			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
    			for (T line : lines) {
    				writer.write(String.valueOf(line));
    				writer.newLine();
    			}
    			closeQuietly(writer);
    		} else {
    			LOG.error("写入文件为空");
    			throw new RuntimeException("写入文件为空");
    		}
		} catch (Exception e) {
			LOG.error("写入文件出错，" + e.getMessage());
			throw new IOException("写入文件出错，" + e.getMessage());
		}
    	LOG.info("文件写入成功。");
    }
}
