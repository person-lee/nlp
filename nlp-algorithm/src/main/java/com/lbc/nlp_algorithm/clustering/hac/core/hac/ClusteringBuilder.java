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
 * HierarchicalAgglomerativeClusterer.cluster() takes a ClusteringBuilder as its argument,
 * calling its merge() method whenever it merges two clusters.
 *
 * @author Matthias.Hauswirth@usi.ch
 */
public interface ClusteringBuilder {

    /**
     * Merge two clusters.
     * @param i the smaller of the two cluster indices
     * @param j the larger of the two cluster indices
     * @param dissimilarity between the two merged clusters
     */
    public void merge(int i, int j, double dissimilarity);

    /**
     * 输出所有cluster，不一定最后只合并成一个cluster，可以有多个，所以这里输出所有cluster
     * 聚类数量小于min的过滤掉
     */
    public String dumpClusters(int min);

    /**
     * 进行初始化,清理所有数据，还原到最初的状态
     */
    public void init(int nObservations);
    public void init(int nObservations, int[] count);

    /**
     * 克隆一个相同类型的实例
     * @return
     */
    public ClusteringBuilder clone(int nObservations);
}
