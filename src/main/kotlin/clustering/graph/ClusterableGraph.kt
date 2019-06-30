package codes.jakob.graphenotype.clustering.graph


data class ClusterableGraph(
    val edges: Set<WeightedEdge>,
    val vertices: Set<ClusterableVertex> = inferVerticesFromEdges(edges)
) {
    fun clone(): ClusterableGraph {
        return ClusterableGraph(
            vertices = this.vertices.map { it.copy() }.toSet(),
            edges = this.edges.map { it.copy() }.toSet()
        )
    }

    companion object {
        fun inferVerticesFromEdges(edges: Set<WeightedEdge>): Set<ClusterableVertex> {
            return edges.flatMap { listOf(it.connectedVertices.first, it.connectedVertices.second) }.toSet()
        }
    }
}
