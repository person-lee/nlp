package com.lbc.nlp_domain;

public class PositiveProbility{
	private Double probability;
	private boolean positive;
	
	public PositiveProbility(double probability, boolean positive){
		this.probability = probability;
		this.positive = positive;
	}
	public Double getProbability() {
		return probability;
	}
	public void setProbability(double probability) {
		this.probability = probability;
	}
	public boolean isPositive() {
		return positive;
	}
	public void setPositive(boolean positive) {
		this.positive = positive;
	}
}
