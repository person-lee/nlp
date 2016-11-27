/*
 * This file is licensed to You under the "Simplified BSD License".
 * You may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/bsd-license.php
 *
 * See the COPYRIGHT file distributed with this work for information
 * regarding copyright ownership.
 */
package com.lbc.nlp_algorithm.clustering.hac.core.hac;


/**
 * An ObservationNode represents a leaf node in a Dendrogram.
 * It corresponds to a singleton cluster of one observation.
 *
 * @author Matthias.Hauswirth@usi.ch
 * 2016.08.29：支持传入计数
 */
public final class ObservationNode implements DendrogramNode {

	private boolean top = true;
	private final int observation;
	private final int count;

	public ObservationNode(int observation, int count) {
		this.observation = observation;
		this.count = count;
	}
	public ObservationNode(int observation) {
		this.observation = observation;
		this.count = 1;
	}

	@Override
	public final DendrogramNode getLeft() {
		return null;
	}

	@Override
	public final DendrogramNode getRight() {
		return null;
	}

	@Override
	public int getObservationCount() {
		return count;
	}

	public final int getObservation() {
		return observation;
	}

	@Override
	public String toString() {
		return String.valueOf(observation);
	}

	@Override
	public void setTop(boolean isTop) {
		this.top = isTop;
	}

	@Override
	public boolean isTop() {
		return top;
	}
}