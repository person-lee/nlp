package com.lbc.nlp_algorithm.prepocess.core.common;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;

public abstract class AbstractComponent implements Component {

	protected final Context context;
	private Component next;
	
	public AbstractComponent(final Context context) {
		this.context = context;
	}
	
	@Override
	public Component getNext() {
		return next;
	}
	
	@Override
	public Component setNext(Component next) {
		this.next = next;	
		return next;
	}
	
}
