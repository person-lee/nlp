package com.lbc.nlp_algorithm.classification.logisticRegression;

/**
 * Binary logistic regression, by which we assume the data sets contains ONLY two outcomes.
 * For optimization, we use stochastic gradient descent, so it can easily scale to very large
 * data sets. 
 * 
 * For regularization, the regression class uses L2 norm.  Also, please note that 
 * we don't regularize the intercept- "w0"
 * 
 * 
 *
 */
public abstract class Regression<T> {
    // weight vector, [w0, w1,....wn]
    protected double w0;    
    protected double[] weights;   

    // configurations. 
    protected double step = 0.01;    // step size of gradient descent
    protected double lamda = 0.1;     // regularization factor.

    public Regression(int dimension, double step, double lamda){
        this.step = step;
        this.lamda = lamda;
        weights = new double[dimension]; // initialize the weight vector
    }

    public Regression(int dimension){
        weights = new double[dimension];
    }

    /**
     * Stochastic weight updates for each data point. 
     */
    public abstract void updateWeights(T x, int y);

    /**
     * Evaluation function for testing data point.
     * @param x    input feature vector
     * @return     probability of outcome. For (0,1) cases, it outputs 
     *             the probability of outcome becomes "1"
     */
    public abstract double eval(T x);

    /**
     * Calculate the gradient, that is y-P(Y=1|x,W). 
     */
    protected abstract double gradient(T x, int y);

    /**
     * Calculate the inner product of vector x and weight vector. 
     */
    protected abstract double innerProduct(T x);

    /**
     * calcualte the l2 norm, which is Math.sqrt(w_{0}^w_{0} + ....w_{n}*w_{n})
     */
    public double getL2Norm(){
        double sum = 0;
        sum += w0 * w0;
        for(int i = 0; i < weights.length; i++)
            sum += weights[i] * weights[i];

        return Math.sqrt(sum);
    }
}
