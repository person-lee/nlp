package com.lbc.nlp_algorithm.clustering.hac.core.hac;


public class DendrogramBuilder implements ClusteringBuilder {

	private DendrogramNode[] nodes;
    private MergeNode lastMergeNode;

    public DendrogramBuilder(final int nObservations) {
    	init(nObservations);
    }

	@Override
    public final void merge(final int i, final int j, final double dissimilarity) {
        final MergeNode node = new MergeNode(nodes[i], nodes[j], dissimilarity);
        nodes[i] = node;
        lastMergeNode = node;
    }

    public final Dendrogram getDendrogram() {
        if (nodes.length==1) {
            return new Dendrogram(nodes[0]);
        } else {
            return new Dendrogram(lastMergeNode);
        }
    }

	@Override
	public String dumpClusters(int min) {
		String description = "";
		for (int i = 0; i < nodes.length; i++) {
			DendrogramNode node = nodes[i];
			if (false == node.isTop())
				continue;
			if (node.getObservationCount() < min)
				continue;
			Dendrogram dendrogram = new Dendrogram(node);
			description += (dendrogram.dumpTree() + "\n");
		}
		return description;
	}

	@Override
	public void init(int nObservations) {
        this.nodes = new DendrogramNode[nObservations];
        for (int i = 0; i<nObservations; i++) {
            nodes[i] = new ObservationNode(i);
        }
	}
	@Override
	public void init(int nObservations, int[] count) {
        this.nodes = new DendrogramNode[nObservations];
        for (int i = 0; i<nObservations; i++) {
            nodes[i] = new ObservationNode(i, count[i]);
        }
	}

	@Override
	public ClusteringBuilder clone(int nObservations) {
		return new DendrogramBuilder(nObservations);
	}
}
