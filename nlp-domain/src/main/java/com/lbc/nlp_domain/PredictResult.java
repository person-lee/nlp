package com.lbc.nlp_domain;

public class PredictResult {
	private String question;
	private Integer index;
	private double predictProb;
	private Integer label;
	private double[] probility;
	
	public static class Builder {
		private String question;
		private Integer index;
		private double predictProb;
		private Integer label;
		private double[] probility;
		
		public Builder question(String question) {
			this.question = question;
			return this;
		}
		
		public Builder index(Integer index) {
			this.index = index;
			return this;
		}
		
		public Builder predictProb(double predictProb) {
			this.predictProb = predictProb;
			return this;
		}
		
		public Builder label(Integer label) {
			this.label = label;
			return this;
		}
		
		public Builder probility(double[] probility) {
			this.probility = probility;
			return this;
		}
		
		public PredictResult build() {
			return new PredictResult(this);
		}
	}
	
	public PredictResult() {
		
	}
	
	public PredictResult(Builder builder) {
		this.question = builder.question;
		this.index = builder.index;
		this.predictProb = builder.predictProb;
		this.label = builder.label;
		this.probility = builder.probility;
	}
	
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}

	public double getPredictProb() {
		return predictProb;
	}

	public void setPredictProb(double predictProb) {
		this.predictProb = predictProb;
	}

	public Integer getLabel() {
		return label;
	}

	public void setLabel(Integer label) {
		this.label = label;
	}

	public double[] getProbility() {
		return probility;
	}

	public void setProbility(double[] probility) {
		this.probility = probility;
	}

}
