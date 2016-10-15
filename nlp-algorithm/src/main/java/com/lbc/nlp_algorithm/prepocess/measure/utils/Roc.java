package com.lbc.nlp_algorithm.prepocess.measure.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.lbc.nlp_domain.PositiveProbility;

public class Roc {
	private int REGION_NUM = 100;			// 划分的区间数
	private List<PositiveProbility> positiveProbilities = Lists.newArrayList();
	private int actual_positive_num = 0;	//实际的正例数
	private int actual_negative_num = 0;	//实际的负例数
	

	/**
	 * 添加预测的结果
	 * @param probability 预测为正例的概率
	 * @param positive	true表示实际为正例，false表示实际为负例
	 */
	public void addResult(double probability, boolean positive){
		positiveProbilities.add(new PositiveProbility(probability, positive));
		if(positive){
			actual_positive_num++;
		} else {
			actual_negative_num++;
		}
	}
	
	/**
	 * 获取AUC(Area Under the ROC Curve)，ROC曲线下的面积
	 * @return
	 */
	public double calAuc(){
		if(positiveProbilities.size() < REGION_NUM){
			REGION_NUM = positiveProbilities.size();
		}
		if(actual_positive_num==0){
			// 全为负例
			return 0;
		}
		if(actual_negative_num==0){
			// 全为正例
			return 1;
		}
		Collections.sort(positiveProbilities, new Comparator<PositiveProbility>() {
			@Override
			public int compare(PositiveProbility o1, PositiveProbility o2) {
				return o2.getProbability().compareTo(o1.getProbability());
			}
		});
		
		/**
		 * 将结果分为REGION_NUM份，计算TPR(true positive rate)和FPR(false positive rate)。
		 * x轴为FPR, y轴为TPR
		 */
		int region_size = positiveProbilities.size()/REGION_NUM + 1;
		double tpr[] = new double[REGION_NUM];
		double fpr[] = new double[REGION_NUM];
		int cur_true_positive = 0;
		int cur_false_positive = 0;
		for(int i=0; i<REGION_NUM; i++){
			for(int k=i*region_size; k<(i+1)*region_size && k<positiveProbilities.size(); k++){
				PositiveProbility pr = positiveProbilities.get(k);
				if(pr.isPositive()){
					cur_true_positive++;
				}
				else if(!pr.isPositive()){
					cur_false_positive++;
				}
			}
			tpr[i] = 1.0 * cur_true_positive / actual_positive_num;
			fpr[i] = 1.0 * cur_false_positive / actual_negative_num;
		}
		double area = 0;
		for(int i = 0; i < REGION_NUM; i++){
			double lastx = i==0? 0 : fpr[i-1];
			double lasty = i==0? 0 : tpr[i-1];
			area += (fpr[i] - lastx) * lasty + (fpr[i] - lastx) * (tpr[i] - lasty) / 2;
		}
		return area;
	}
	
	public static void main(String[] args) {
		// 测试随机值，面积约为0.5
		Roc roc = new Roc();
		Random r = new Random();
		for(int i=0; i<1000; i++){
			roc.addResult(r.nextDouble(), r.nextInt(2)==1?true:false);
		}
		System.out.println(roc.calAuc());
	}
}
