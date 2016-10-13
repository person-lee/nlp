package com.lbc.nlp_algorithm.common.parseFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.lbc.nlp_algorithm.cutword.ansj.AnjsCutword;
import com.lbc.nlp_domain.Word;
import com.lbc.nlp_modules.common.thread.ThreadPoolUtils;

/**
 * Created by cdlibaocang on 2016/7/10.
 */
public class ParseText implements Runnable{
    private static final Logger log = LoggerFactory.getLogger(ParseText.class);

    private static int trainWordsCount = 0;
    private File readFile = null;
    private static AtomicInteger integer = new AtomicInteger(0);

    public static ParseText getInstance(File file){
        return new ParseText(file);
    }

    private ParseText(File file){
    	readFile = file;
    }
    
    @Override
    public void run(){
        try{
            readVocab();
        } catch (Exception e) {
            log.error("run error:" + e.getMessage());
        }
    }

    private void readVocab() throws Exception{
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(readFile), "GB2312"))) {
            String temp = null;
            StringBuffer stringBuffer = new StringBuffer();
            while ((temp = br.readLine()) != null) {
            	temp = regex(temp, true);
            	if(StringUtils.isBlank(temp)){
            		continue;
            	}
            	
                List<Word> segWords = AnjsCutword.getInstance().doCutword(temp);
                if(CollectionUtils.isNotEmpty(segWords)){
                	trainWordsCount += segWords.size();
                    for (Word word : segWords) {
                        stringBuffer.append(word.getTerm());
                        stringBuffer.append(" ");
                    }
                }
            }
            WriteFile.write(stringBuffer.toString());
        }
    }
    
    final static class WriteFile{
    	private static FileWriter writerFile = null;
    	private static String fileName = "F://word2vec/SogouCS/vocab.txt";
    	private static ReentrantLock lock = new ReentrantLock();
    	
    	public static void init(){
    		try {
            	writerFile = new FileWriter(fileName, true);
    		} catch (Exception e) {
    			log.error(e.getMessage());
    			return;
    		}
    	}
    	
    	public static void shutdown(){
    		try {
    			writerFile.close();
    		} catch (Exception e) {
    			log.error(e.getMessage());
    			return;
    		}
    	}
    	
		public static void write(String text) {
			while(true){
				if(lock.tryLock()){
					try {
						if (writerFile != null) {
							writerFile.write(text);
							log.error(integer.decrementAndGet() + " "	+ " is finished!");
						} else {
							log.error("writerFile is null");
						}
						break;
					} catch (Exception e) {
						log.error(e.getMessage());
					} finally {
						lock.unlock();
					}
				}
			}
		}
    }
    
    private String regex(String text, boolean flag){
    	if(flag){
    		String re = "^<content>.*?</content>$|^<contenttitle>.*?</contenttitle>$";
    		String targetRe = "[^(\\u4E00-\\u9FA5|A-Za-z|0-9)]";
        	Pattern pattern = Pattern.compile(re);
        	Matcher m = pattern.matcher(text);
        	if(m.find()){
        		text = text.replaceAll("(<content>|</content>|<contenttitle>|</contenttitle>)", "");
        		if(StringUtils.isNoneBlank(text)){
        			Pattern targetPattern = Pattern.compile(targetRe);
            		String ret = targetPattern.matcher(text).replaceAll("");
            		return StringUtils.trimToNull(ret);
        		}
        	}
    	}else{
    		if(StringUtils.isNotBlank(text)){
        		return StringUtils.trimToNull(text);
        	}
    	}
    	
    	return null;
    }

    public static int getTrainWordsCount() {
        return trainWordsCount;
    }

    public static void setTrainWordsCount(int trainWordsCount) {
        ParseText.trainWordsCount = trainWordsCount;
    }

    public static void main(String[] args){
    	long begin = System.currentTimeMillis();
    	
        ExecutorService pool = ThreadPoolUtils.getExecutor();       
        LinkedList<Future> futures = Lists.newLinkedList();
        ParseText.WriteFile.init();
		try {
			String path = "F://corpus/SogouCS/";
	        File file = new File(path);
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File eachFile : files) {
					int value = integer.incrementAndGet();
					System.out.println("now is " + value + ":" + eachFile.getName());
					
					futures.add(pool.submit(ParseText.getInstance(eachFile)));
				}
			} else {
				ParseText.getInstance(file);
			}
			
			for (Future future : futures){
				future.get();
			}
		} catch (Exception e) {
			log.error("cutword error" + e.getMessage());
		}
			
		ParseText.WriteFile.shutdown();		
		System.out.println(ParseText.trainWordsCount + ": 消耗时间:" + (System.currentTimeMillis() - begin));
    }
}
