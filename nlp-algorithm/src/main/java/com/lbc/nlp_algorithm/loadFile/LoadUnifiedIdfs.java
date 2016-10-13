package com.lbc.nlp_algorithm.loadFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadUnifiedIdfs {
	private final static Logger LOG = LoggerFactory.getLogger(
			LoadUnifiedIdfs.class.getName());
	
    private static final String IdfResource = "tfidf/unified_idfs.txt";
    private static Map<String, Float> idfs;
    public synchronized static Map<String, Float> getIdfs() {
        if(idfs==null){
            idfs = new HashMap<String, Float>();
            InputStreamReader streamReader = new InputStreamReader(LoadUnifiedIdfs.class.getClassLoader().getResourceAsStream(IdfResource));
            try {
            	List<String> strList = IOUtils.readLines(streamReader);
            	for(String line : strList){
                    String[] arr = line.toLowerCase().split("\t");
                    if(arr.length==2){
                        if(!idfs.containsKey(arr[0])){
                            idfs.put(arr[0], Float.valueOf(arr[1]));
                        }
                    } else {
                    	LOG.warn("Invalid idf line: " + line);
                    }
                }
            	streamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("load unified_idfs fail. ", e);
            }
        }
        return idfs;
    }

    public static void setIdfs(Map<String, Float> newIdfs){
        idfs = newIdfs;
    }

}
