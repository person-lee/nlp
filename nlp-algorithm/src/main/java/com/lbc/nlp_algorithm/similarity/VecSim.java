package com.lbc.nlp_algorithm.similarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbc.nlp_algorithm.common.algorithms.WordToVec;
import com.lbc.nlp_algorithm.cutword.Cutword;
import com.lbc.nlp_algorithm.cutword.ansj.AnjsCutword;
import com.lbc.nlp_algorithm.similarity.simStrategy.WordSimilarity;
import com.lbc.nlp_domain.MaxSim;
import com.lbc.nlp_domain.Word;
import com.lbc.nlp_domain.WordWeight;

public class VecSim extends AbstractSimilarity<String>{
	private final static Logger LOG = LoggerFactory.getLogger(
			VecSim.class.getName());
	
	private VecSim(){
	}
	
	public static VecSim getInstance() {
		return Nested.vecSim;
	}
	
	private static class Nested{
		public static VecSim vecSim = new VecSim();
	}
	
	public double sim(String subStr, String obStr){
		Cutword cutword = AnjsCutword.getInstance();
		List<Word> segWordSub = cutword.doCutword(subStr);
		List<Word> segWordOb = cutword.doCutword(obStr);
		return distance(segWordSub, segWordOb);
	}
	
	private double distance(List<Word> words1, List<Word> words2){
        Map<String, Integer> locOfWords = new HashMap<>(words1.size()+words2.size());
        WordWeight[] vector1 = WordToVec.wordsToVector(words1, locOfWords);
        WordWeight[] vector2 = WordToVec.wordsToVector(words2, locOfWords);
        return vectorDistance(vector1, vector2);
    }
	
	private double vectorDistance(WordWeight[] veca, WordWeight[] vecb){
        int lena=veca.length;
        int lenb=vecb.length;
        List<WordWeight> lwa = new ArrayList<WordWeight>(lena);
        List<WordWeight> lwb = new ArrayList<WordWeight>(lenb);
        
        double sum = arrayFilter(lwa, lwb, veca, vecb);
        
        Collections.sort(lwa, weightComparator);
        Collections.sort(lwb, weightComparator);
        int ind1 = 0, ind2 = 0;
        while (ind1 < lwa.size() && ind2 < lwb.size()){
            WordWeight wwa = lwa.get(ind1);
            WordWeight wwb = lwb.get(ind2);
            if(wwa.getWeight() > wwb.getWeight()){
            	sum = calPara(ind1, ind2, lwa, lwb, wwa, sum);
            	ind1++;
            } else {
            	sum = calPara(ind2, ind1, lwb, lwa, wwb, sum);
            	ind2++;
            }
        }
        return sum;
    }
	
	private Comparator<WordWeight> weightComparator = new Comparator<WordWeight>() {
        @Override
        public int compare(WordWeight o1, WordWeight o2) {
            return o2.getWeight().compareTo(o1.getWeight());
        }
    };
    
    private void calSim(WordWeight word1, WordWeight word2, Integer index, MaxSim maxSim) {
        Float similarity = WordSimilarity.wordSimilar(word1.getWord(), word2.getWord());
        if (similarity != null) {
            Float g = word1.getWeight() * word2.getWeight() * similarity;
            if(maxSim.getRet() == null || g.compareTo(maxSim.getRet()) > 0){
            	maxSim.setRet(g);
            	maxSim.setInd(index);
            	maxSim.setSimilar(similarity);
            }
        }
    }
    
    private double calPara(Integer index1, Integer index2, List<WordWeight> wordList1, List<WordWeight> wordList2, 
    		WordWeight wordWeight, double sum) {
        MaxSim maxSim = new MaxSim();
    	int len = wordList2.size();
    	for(int i = index2; i < len; i++){
        	calSim(wordList2.get(i), wordWeight, i, maxSim);
        }
        if (maxSim.getInd() != null) {
        	LOG.debug("similar pair: " + wordList2.get(maxSim.getInd()).getWord().getTerm() + " " 
        				+ wordList1.get(index1).getWord().getTerm() + " " + maxSim.getSimilar());
            sum += maxSim.getRet();
            wordList2.remove(maxSim.getInd().intValue());
            len--;
        }
        return sum;
    }
    
    /**
     * 筛选出两个词组中的不同词，并对相同词通过其权重计算相似度。
     * @param lwa
     * @param lwb
     * @param veca
     * @param vecb
     * @return
     */
    private double arrayFilter(List<WordWeight> lwa, List<WordWeight> lwb, WordWeight[] veca, WordWeight[] vecb) {
    	int lena = veca.length, lenb = vecb.length;
    	double sum = 0;
    	int ind1 = 0, ind2 = 0;
    	
    	while (true) {
            if(ind1 >= lena){//如果veca的长度小于vecb，则将vecb剩下的词放入lwb。
                while(ind2 < lenb){
                    lwb.add(vecb[ind2++]);
                }
                break;
            } else if(ind2 >= lenb){//如果vecb的长度小于veca，则将veca剩下的词放入lwa。
                while(ind1 < lena){
                    lwa.add(veca[ind1++]);
                }
                break;
            }
            int loc1 = veca[ind1].getIndex();
            int loc2 = vecb[ind2].getIndex();
            while(loc1 < loc2 && ind1 + 1 < lena){//如果veca中下标小于vecb，并且该词不是最后一个词，则将该词加入lwa
                lwa.add(veca[ind1++]);
                loc1 = veca[ind1].getIndex();
            }
            while(loc1 > loc2 && ind2 + 1 < lenb){
                lwb.add(vecb[ind2++]);
                loc2 = vecb[ind2].getIndex();
            }
            if(loc1 == loc2){
                sum += veca[ind1].getWeight() * vecb[ind2].getWeight();
            } else {
                lwa.add(veca[ind1]);
                lwb.add(vecb[ind2]);
            }
            ind1++;
            ind2++;
        }
    	return sum;
    }
    
    public static void main(String[] args) {  
		String[][] testdatas = new String[][]{
                new String[]{"找到了，不好意思", "找到了吗，不好意思啊"},
                new String[]{"您还有货吗", "你有货吗"},
                new String[]{"送礼物吗?", "有礼品送吗？"},
                new String[]{"送礼物吗?", "有礼品送吗?"},
                new String[]{"好吧", "还能便宜是吗？"},
                new String[]{"已经过6天了", "已经过了7天"},
                new String[]{"寄北京几天", "送成都要几天"},
                new String[]{"寄什么快递", "发什么物流"},
                new String[]{"这橙黄色裙子多少钱", "这黄色连衣裙多少钱"},
                new String[]{"这裙子多少钱", "这连衣裙价格"},
                new String[]{"有红色的手机套吗", "有大红色的手机壳吗"},
                new String[]{"支持小米的蓝牙手环", "支持魅族的手环"},
                new String[]{"华为手机", "小米手机"},
                new String[]{"苹果手机", "小米手机"},
                new String[]{"这条西裤多少钱", "这短裤多少钱"},
                new String[]{"你好，我的是联想b50-70的笔记本能用吧", "这个硬盘的大小是多少"},
                new String[]{"可以当光驱使用吗", "怎么使用刻录机"},
        };
        for(String[] testdata : testdatas){
            System.out.println(VecSim.getInstance().sim(testdata[0], testdata[1]) + "\t" + testdata[0] + "\t" + testdata[1]);
        }
        
//      String str1 = "有橙黄色的手机套吗";
//		String str2 = "有黄色的手机壳吗";
//		System.out.println(similarity.sim(str1, str2));
    }
}
