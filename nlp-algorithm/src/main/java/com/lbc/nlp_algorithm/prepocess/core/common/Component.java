package com.lbc.nlp_algorithm.prepocess.core.common;

public interface Component {
	public void fire();
	public Component getNext();
	public Component setNext(Component next);
}
