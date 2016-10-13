package com.lbc.nlp_modules.common.dic;

import java.io.BufferedReader;
import java.util.HashMap;

import com.lbc.nlp_modules.common.tree.Forest;
import com.lbc.nlp_modules.common.tree.library.Library;

/**
 * Created by ansj on 4/1/14.
 */
public class DicManager {

	private static final HashMap<String, Forest> forestMap = new HashMap<String, Forest>();

	/**
	 * 构建一个tire书辞典
	 * 
	 * @param dicName
	 * @param dicName
	 * @return
	 * @throws Exception
	 */
	public synchronized static Forest makeForest(String dicName, BufferedReader br) throws Exception {
		Forest forest = null;
		if ((forest = forestMap.get(dicName)) != null) {
			return forest;
		}
		forest = Library.makeForest(br);

		if (dicName != null) {
			forestMap.put(dicName, forest);
		}

		return forest;
	}

	/**
	 * 从内存中移除
	 * 
	 * @param dicName
	 * @return
	 */
	public static Forest remove(String dicName) {
		return forestMap.remove(dicName);
	}

	/**
	 * 获得一本辞典
	 * 
	 * @param dicName
	 * @return
	 */
	public static Forest getForest(String dicName) {
		return forestMap.get(dicName);
	}
}
