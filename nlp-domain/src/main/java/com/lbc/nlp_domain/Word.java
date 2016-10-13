package com.lbc.nlp_domain;

import java.io.Serializable;

public class Word implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3264624093961223016L;
	/**
	 * 词
	 */
	private String term;
	/**
	 * 词性
	 */
	private String speech;
	/**
	 * 权重
	 */
	private Integer weight;
	
	/**
	 * 在原句中的开始位置
	 */
	private Integer begPos;
	
	/**
	 * 在原句中的结束位置
	 */
	private Integer endPos;
	
	/**
	 * 某些类型的词在切词的时候进行过调整，需要保存原串
	 */
	private String normedTerm;
	
	/**
	 * 该词是否可用于识别。
	 * 有的词在前一步骤识别中已识别出来，并且被确定，则可标记为已使用，避免后面的步骤重复使用该词进行识别。
	 */
	private boolean isUsable;
	
	/**
	 * crf识别结果
	 */
	private String crf;

	public Word(){}
	
	public Word(String term) {
		this.term = term;
		this.speech = "un";
		this.weight = 1;
		this.normedTerm = term;
		//"O"表示其他实体，在EntityType.java中定义，这里没有引用brain-common，所以直接使用常量
		this.crf = "O";
	}

	public Word(String term, String speech, Integer begPos, Integer endPos) {
		this.term = term;
		this.speech = speech;
		this.weight = 1;
		this.begPos = begPos;
		this.endPos = endPos;
		this.isUsable = true;
		this.normedTerm = term;
		this.crf = "O";
	}
	
	public Word(String term, String speech, Integer begPos, Integer endPos, String normedTerm) {
		this.term = term;
		this.speech = speech;
		this.weight = 1;
		this.begPos = begPos;
		this.endPos = endPos;
		this.isUsable = true;
		this.normedTerm = normedTerm;
		this.crf = "O";
	}

	public Word(String term, String speech, Integer weight, Integer begPos, Integer endPos) {
		this.term = term;
		this.speech = speech;
		this.weight = weight;
		this.begPos = begPos;
		this.endPos = endPos;
		this.isUsable = true;
		this.crf = "O";
	}
	
	public String toString() {
		String desc = "term[";
		desc += term;
		desc += "] speech[";
		desc += speech;
		desc += "] weight[";
		desc += weight;
		desc += "] begPos[";
		desc += begPos;
		desc += "] endPos[";
		desc += endPos;
		desc += "] usable[";
		desc += isUsable;
		desc += "] normed[";
		desc += normedTerm;
		desc += "] crf[";
		desc += crf;
		desc += "]";
		
		return desc;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getSpeech() {
		return speech;
	}

	public void setSpeech(String speech) {
		this.speech = speech;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public boolean isUsable() {
		return isUsable;
	}

	public void setUsable(boolean isUsable) {
		this.isUsable = isUsable;
	}

	public Integer getBegPos() {
		return begPos;
	}

	public void setBegPos(Integer begPos) {
		this.begPos = begPos;
	}

	public Integer getEndPos() {
		return endPos;
	}

	public void setEndPos(Integer endPos) {
		this.endPos = endPos;
	}

	public String getNormedTerm() {
		return normedTerm;
	}

	public void setNormedTerm(String normedTerm) {
		this.normedTerm = normedTerm;
	}

	public String getCrf() {
		return crf;
	}

	public void setCrf(String crf) {
		this.crf = crf;
	}

	@Override
	public Word clone(){
		Word word = new Word();
		word.setTerm(term);
		word.setCrf(crf);
		word.setNormedTerm(normedTerm);
		word.setSpeech(speech);
		word.setBegPos(begPos);
		word.setEndPos(endPos);
		word.setUsable(isUsable);
		word.setWeight(weight);
		return word;
	}
}
