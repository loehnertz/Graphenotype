package codes.jakob.graphenotype.clustering

import codes.jakob.graphenotype.clustering.graph.ClusterableGraph


object GraphClusterer {
    fun clusterGraph(graph: ClusterableGraph, vertexClusterMap: Map<Int, Int>): ClusterableGraph {
        graph.vertices.forEach { it.cluster = vertexClusterMap.getValue(it.id) }
        return graph
    }
}
