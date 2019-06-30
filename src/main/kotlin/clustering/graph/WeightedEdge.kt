package codes.jakob.graphenotype.clustering.graph


data class WeightedEdge(
    val connectedVertices: Pair<ClusterableVertex, ClusterableVertex>,
    val weight: Double
)
