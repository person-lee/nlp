package com.lbc.nlp_algorithm.similarity.simHash;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.lbc.nlp_algorithm.similarity.simHash.pojo.MyFingerprint;
import com.lbc.nlp_modules.common.MD5;
import com.lbc.nlp_modules.common.StringUtil;
import com.lbc.nlp_modules.common.tree.GetWord;

public class FingerprintService extends AbsService {


    /**
     * 根据一个 文章的正文.计算文章的指纹
     * 	
     * @param content
     * @return
     */
    public String fingerprint(String content) {

        content = StringUtil.rmHtmlTag(content);

        GetWord word = new GetWord(forest, content);

        String temp = null;

        int middleLength = content.length() / 2;

        double factory;

        HashMap<String, MyFingerprint> hm = new HashMap<String, MyFingerprint>();

        MyFingerprint myFingerprint = null;
        while ((temp = word.getFrontWords()) != null) {
            if (temp != null && temp.length() == 0) {
                continue;
            }
            temp = temp.toLowerCase();
            factory = 1 - (Math.abs(middleLength - word.offe) / (double) middleLength);
            if ((myFingerprint = hm.get(temp)) != null) {
                myFingerprint.updateScore(factory);
            } else {
                hm.put(temp, new MyFingerprint(temp, Double.parseDouble(word.getParam(1)), factory));
            }
        }

        Set<MyFingerprint> set = new TreeSet<MyFingerprint>();
        set.addAll(hm.values());

        int size = Math.min(set.size() / 10, 4) + 1;

        Iterator<MyFingerprint> iterator = set.iterator();
        int j = 0;
        HashSet<String> hs = new HashSet<String>();
        for (; j < size && iterator.hasNext(); j++) {
            myFingerprint = iterator.next();
            hs.add(myFingerprint.getName() + " ");
        }
        String finger = MD5.code(hs.toString());

        return finger;
    }

}
