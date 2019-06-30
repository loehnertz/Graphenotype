package codes.jakob.graphenotype.clustering

import codes.jakob.graphenotype.clustering.graph.ClusterableGraph
import codes.jakob.graphenotype.clustering.graph.ClusterableVertex
import codes.jakob.graphenotype.clustering.graph.WeightedEdge
import kotlin.math.pow


object ModularityCalculator {
    const val minimumModularity: Double = -1.0
    const val maximumModularity: Double = +1.0

    fun calculate(clusteredGraph: ClusterableGraph): Double {
        val modularitySummands: ArrayList<Double> = arrayListOf()
        val accumulatedEdgeWeight: Double = clusteredGraph.edges.sumByDouble { it.weight }
        val clusterMap: Map<Int, List<ClusterableVertex>> = buildClusterMap(clusteredGraph)

        for ((_, vertices: List<ClusterableVertex>) in clusterMap) {
            val edgesStartingAndEndingInCurrentCluster: List<WeightedEdge> = clusteredGraph.edges.filter { vertices.contains(it.connectedVertices.first) && vertices.contains(it.connectedVertices.second) }
            val eii: Double = (edgesStartingAndEndingInCurrentCluster.sumByDouble { it.weight } / accumulatedEdgeWeight)

            val edgesEndingInCurrentCluster: List<WeightedEdge> = clusteredGraph.edges.filter { (vertices.contains(it.connectedVertices.first) || vertices.contains(it.connectedVertices.second)) }
            val aiSquared: Double = (edgesEndingInCurrentCluster.sumByDouble { it.weight } / accumulatedEdgeWeight).pow(2.0)

            modularitySummands += (eii - aiSquared)
        }

        return modularitySummands.sum()
    }

    private fun buildClusterMap(clusteredGraph: ClusterableGraph): Map<Int, List<ClusterableVertex>> {
        val clusterMap: MutableMap<Int, ArrayList<ClusterableVertex>> = mutableMapOf()

        for (vertex: ClusterableVertex in clusteredGraph.vertices) {
            clusterMap.putIfAbsent(vertex.cluster!!, arrayListOf())
            clusterMap.getValue(vertex.cluster!!).add(vertex)
        }

        return clusterMap.toMap()
    }
}
