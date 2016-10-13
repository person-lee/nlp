package com.lbc.nlp_algorithm.classification.logisticRegression;

/**
 * Logistic regression for non-sparse datasets. 
 * For this case, each data points are array of features.  Every updates will
 * involves each feature in the feature vector..   For sparse datasets, you can 
 * use {@link SparseLogisticRegression} class. 
 * 
 *
 */
public class LogisticRegression extends Regression {
	
	public LogisticRegression(int dimension) {
		super(dimension);
	}
	
	public LogisticRegression(int dimension, double step, double lamda) {
		super(dimension, step, lamda);
	}
	
	
	@Override
	public void updateWeights(Object x, int y){
		double[] featureVector = (double[]) x;
		w0 += step * gradient(x,y);
		for (int i = 0; i < featureVector.length; i++){
			weights[i] += step * (-lamda * weights[i] + featureVector[i] * gradient(x,y));
		}
	}
	
	
	@Override
	public double eval(Object x){
		double[] featureVector = (double[]) x;
		double exp = Math.exp(w0 + innerProduct(featureVector));
		// need to prevent it exceed the max value. 
		exp = Double.isInfinite(exp) ? (Double.MAX_VALUE -1) : exp;
		return exp/(exp+1);
	}
	
	
	@Override
	protected double gradient(Object x, int y){
		double exp = Math.exp(w0 + innerProduct(x));
		exp = Double.isInfinite(exp) ? (Double.MAX_VALUE - 1) : exp;
		if (y == 1)
			return 1/(exp+1);
		else
			return -exp/(exp+1);
		
	}
	
	@Override
	protected double innerProduct(Object x){
		double[] featureVector = (double[]) x;
		double sum = 0;
		for (int i = 0; i < featureVector.length; i++)
			sum += featureVector[i] * weights[i];
		
		return sum;
	}	
}
