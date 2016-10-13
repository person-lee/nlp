package com.lbc.nlp_domain;

public class WordWeight {
    private Word word;
    private Float weight;
    private Integer index;
    
    public static class Builder{
    	private Word word;
        private Float weight;
        private Integer index;
        
        public Builder word(Word word) {
        	this.word = word;
        	return this;
        }
        public Builder weight(Float weight) {
        	this.weight = weight;
        	return this;
        }
        public Builder index(Integer index) {
        	this.index = index;
        	return this;
        }
        public WordWeight build(){
        	return new WordWeight(this);
        }
    }
    
    public WordWeight(Builder builder){
        this.word = builder.word;
        this.weight = builder.weight;
        this.index = builder.index;
    }
    
	public Word getWord() {
		return word;
	}
	public void setWord(Word word) {
		this.word = word;
	}
	public Float getWeight() {
		return weight;
	}
	public void setWeight(Float weight) {
		this.weight = weight;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
    
    
}