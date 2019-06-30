package codes.jakob.graphenotype.clustering.graph


data class ClusterableVertex(
    val id: Int,
    var cluster: Int? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClusterableVertex

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}
