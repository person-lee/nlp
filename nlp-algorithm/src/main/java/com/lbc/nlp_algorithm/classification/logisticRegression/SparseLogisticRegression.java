package com.lbc.nlp_algorithm.classification.logisticRegression;

import java.util.Map;

/**
 * Sparse logistic regression class is used for training the classifier for sparse 
 * data sets. For example, if you treat the text data set as binary vector, the dimensional 
 * will likely be millions. However, the high dimensional vector is very sparse, by only 
 * containing few values, while all others are zero. In this case, we can represent each 
 * feature vector as a map, where the key is the index of the features, and value is the 
 * actual value. 
 * 
 *
 */
public class SparseLogisticRegression extends Regression{

    public SparseLogisticRegression(int dimension) {
        super(dimension);
    }

    public SparseLogisticRegression(int dimension, double step, double lamda) {
        super(dimension, step, lamda);
    }

    @Override
    public void updateWeights(Object x, int y){
        Map<Integer, Double> featureVector = (Map<Integer, Double>) x;
        w0 += step * gradient(x,y);
        for (Integer idx : featureVector.keySet()){
            weights[idx] += step * (-lamda * weights[idx] + featureVector.get(idx) * gradient(x,y));
        }
    }

    @Override
    protected double gradient(Object x, int y) {
        Map<Integer, Double> featureVector = (Map<Integer, Double>) x;
        double exp = Math.exp(w0 + innerProduct(featureVector));
        exp = Double.isInfinite(exp) ? (Double.MAX_VALUE -1) : exp;
        if (y == 1)
            return 1/(exp+1);
        else
            return -exp/(exp+1);
    }

    @Override
    protected double innerProduct(Object x){
        Map<Integer, Double> featureVector = (Map<Integer, Double>) x;
        double sum = 0;
        for (Integer idx : featureVector.keySet())
            sum += featureVector.get(idx) * weights[idx];
        return sum;
    }

    @Override
    public double eval(Object x) {
        Map<Integer, Double> featureVector = (Map<Integer, Double>) x;
        double exp = Math.exp(w0 + innerProduct(featureVector));
        exp = Double.isInfinite(exp) ? (Double.MAX_VALUE -1) : exp;
        return exp/(exp+1);
    }	
}
