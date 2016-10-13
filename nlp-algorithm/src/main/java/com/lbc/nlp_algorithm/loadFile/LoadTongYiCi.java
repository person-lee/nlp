package com.lbc.nlp_algorithm.loadFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_domain.LevelCode;

/**
 * 加载同义词文件到内存。有的词有多层意思，为了提升计算相似度的效率，提前计算出含有多层意思的词与其它词的相似度。
 *
 */
public class LoadTongYiCi {
	private final static Logger LOG = LoggerFactory.getLogger(
			LoadTongYiCi.class.getName());
	
	private final String TongyiciResource = "cilin/tongyici.txt";
	private Map<String, Float> cilinComplexwordSimilarity = new HashMap<String, Float>();
	private LevelCode cilinCodes[];
	private Map<String, Integer> cilinUniqwordId = new HashMap<String, Integer>();
	private Set<String> cilinComplexwords = new HashSet<String>();
	private static LoadTongYiCi tongYiCi = null;
	
	private LoadTongYiCi() throws IOException{
		List<String> content = new ArrayList<String>();
		try {
			InputStreamReader streamReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(TongyiciResource));
			content = IOUtils.readLines(streamReader);
		} catch (Exception e) {
			LOG.error(e.toString());
		}
		
		if(CollectionUtils.isEmpty(content)){
			LOG.info("Cilin" + "为空");
			return;
		}
		
		initUniqwordId(content);
		handleCluster(content);
		
		LOG.info("Cilin complex map size: " + cilinComplexwordSimilarity.size() + " and Cilin uniq word num: " + cilinUniqwordId.size());
	}

	public static LoadTongYiCi getInstance() {
		if(tongYiCi==null){
			try {
				tongYiCi = new LoadTongYiCi();
			} catch (IOException e) {
				e.printStackTrace();
				LOG.error("init tongyici error. ", e);
			}
		}
		return tongYiCi;
	}
	
	private void handleCluster(List<String> rawContent) {
		String clusterstarts = null;
		List<String[]> cluster = new ArrayList<String[]>();
		for(String line : rawContent){
			if(clusterstarts==null){
				clusterstarts = line.substring(0, 4);
			}
			if(!line.startsWith(clusterstarts)){
				calculateComplexwordSimilarity(cluster);//计算同一行词的相似度
				clusterstarts = line.substring(0, 4);
				cluster.clear();
			}
			cluster.add(line.split(" "));
		}
		calculateComplexwordSimilarity(cluster);
	}

	private void initUniqwordId(List<String> rawContent){
		cilinCodes = new LevelCode[rawContent.size()];
		for(int i=0; i<cilinCodes.length; i++){
			String splits[] = rawContent.get(i).split(" ");
			cilinCodes[i] = new LevelCode(splits[0]);
			for(int j=1; j<splits.length; j++){
				String word = splits[j];
				if(cilinComplexwords.contains(word)){
					continue;
				}
				if(cilinUniqwordId.containsKey(word)){
					cilinUniqwordId.remove(word);
					cilinComplexwords.add(word);
				} else {
					cilinUniqwordId.put(word, i);
				}
			}
		}
	}
	
	private void calculateComplexwordSimilarity(List<String[]> content){
		for(int i=0; i<content.size(); i++){
			String spliti[] = content.get(i);
			
			LevelCode codei = new LevelCode(spliti[0]);
			float inneri = codei.getSimilar(codei);		//同一行内
			for(int j=1; j<spliti.length; j++){
				for(int k=j+1; k<spliti.length; k++){
					updateComplexSimilarity(spliti[j], spliti[k], inneri);
				}

				for(int m=i+1; m<content.size(); m++){
					String splitm[] = content.get(m);
					LevelCode codem = new LevelCode(splitm[0]);
					float vim = codei.getSimilar(codem);
					for(int n=1; n<splitm.length; n++){
						updateComplexSimilarity(spliti[j], splitm[n], vim);
					}
				}
			}
		}
	}
	
	private void updateComplexSimilarity(String worda, String wordb, float value){
		if(worda.equals(wordb)){
			return;
		}
		if(cilinUniqwordId.containsKey(worda) && cilinUniqwordId.containsKey(wordb)){
			return;
		}
		String key = getKey(worda, wordb);
		Float cval = cilinComplexwordSimilarity.get(key);
		if(cval==null || cval<value){
			cilinComplexwordSimilarity.put(key, value);
		}
	}
	
	private String getKey(String worda, String wordb){
		String key;
		if(worda.compareTo(wordb)>0){
			key = wordb + " " + worda;
		} else {
			key = worda + " " + wordb;
		}
		return key;
	}
	
	/**
	 * 计算两个词在同义词词典中的相似度。没有
	 * @param worda
	 * @param wordb
	 * @return
	 */
	public Float getWordsSimilarity(String worda, String wordb){
		if(worda.equals(wordb)){
			return 1f;
		}
		Integer ida = cilinUniqwordId.get(worda);
		Integer idb = cilinUniqwordId.get(wordb);
		if(ida!=null && idb!=null){
			return cilinCodes[ida].getSimilar(cilinCodes[idb]);
		} else {
			boolean complexa = cilinComplexwords.contains(worda);
			boolean complexb = cilinComplexwords.contains(wordb);
			if ((ida != null && complexb) ||
				(idb != null && complexa) ||
				(ida == null && idb == null && complexa && complexb)) {
				String key = getKey(worda, wordb);
				Float value = cilinComplexwordSimilarity.get(key);
				return value;
			}
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		LoadTongYiCi tongYiCi = LoadTongYiCi.getInstance();
		System.out.println(tongYiCi.getWordsSimilarity("诸位", "各位"));
		System.out.println(tongYiCi.getWordsSimilarity("老朽", "老娘"));
		System.out.println(tongYiCi.getWordsSimilarity("我", "我们"));
		System.out.println(tongYiCi.getWordsSimilarity("老婆", "分神"));
		System.out.println(tongYiCi.getWordsSimilarity("干群", "军民"));
		System.out.println(tongYiCi.getWordsSimilarity("服装", "jimi"));
		System.out.println(tongYiCi.getWordsSimilarity("货到付款", "什么时候"));
	}
}
