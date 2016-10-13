package com.lbc.nlp_algorithm.classification.domain;

public class PredictResult {
	private Integer label;
	private Double probility;
	
	public static class Builder{
		private Integer label;
		private Double probility;
		
		public Builder label(Integer label) {
			this.label = label;
			return this;
		}
		
		public Builder probility(Double probility) {
			this.probility = probility;
			return this;
		}
		
		public PredictResult build() {
			return new PredictResult(this);
		}
	}
	
	public PredictResult(Builder builder) {
		this.label = builder.label;
		this.probility = builder.probility;
	}

	public Integer getLabel() {
		return label;
	}

	public void setLabel(Integer label) {
		this.label = label;
	}

	public Double getProbility() {
		return probility;
	}

	public void setProbility(Double probility) {
		this.probility = probility;
	}

}
