package lr_test;

import junit.framework.Assert;

import org.junit.Test;

import com.lbc.nlp_algorithm.classification.logisticRegression.LogisticRegression;

public class LogisticRegressionTest {
    
    private LogisticRegression lr = null;
    public void setup(){
       
    }

    @Test
    public void test() {
        lr = new LogisticRegression(2);
        // construct the input features
        double[][] x = new double[][]{
                {1,3}, {4,6}, {2,5}, {6,10}, {1,0.5}, {5,1}, {5,2}, {6,3}
        };

        int[] y = new int[]{1,1,1,1,0,0,0,0};

        // run ten paths
        for (int itr = 0; itr < 10; itr++){
            for (int i = 0; i < x.length; i++){
                lr.updateWeights(x[i], y[i]);
            }
        }     
        
        double[][] test = new double[][]{
                {1,5},{6,4},{5,0}
        };
        
        double prob = lr.eval(test[0]);
        Assert.assertTrue(prob > 0.5);
        prob = lr.eval(test[1]);
        Assert.assertFalse(prob > 0.5);
        prob = lr.eval(test[2]);
        Assert.assertFalse(prob > 0.5);   
    }
}
