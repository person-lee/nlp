package com.lbc.nlp_algorithm.classification.libsvm;
public class SvmProblem implements java.io.Serializable
{
    private static final long serialVersionUID = 4608183423226513475L;
    public int l;
	public double[] y;
	public SvmNode[][] x;
}
