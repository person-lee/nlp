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
 * A DendrogramNode is a node in a Dendrogram.
 * It represents a subtree of the dendrogram tree.
 * It has two children (left and right), 
 * and it can provide the number of leaf nodes (ObservationNodes) in this subtree.
 * 
 * @author Matthias.Hauswirth@unisi.ch
 */
public interface DendrogramNode {
	
	//设置该节点是否是top节点，在merge过程中会根据dissimilary阈值跳出，不会最终合并到只有一个节点,这里
	//设置当前节点是否是顶层节点
	public void setTop(boolean isTop);
	//判断当前节点是否是顶层节点
	public boolean isTop();
	public DendrogramNode getLeft();
	public DendrogramNode getRight();
	public int getObservationCount();

}