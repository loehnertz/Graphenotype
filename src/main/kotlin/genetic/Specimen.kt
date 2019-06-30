package codes.jakob.graphenotype.genetic

import codes.jakob.graphenotype.clustering.ModularityCalculator.minimumModularity
import kotlin.random.Random


data class Specimen(
    val vertexClusters: IntArray,
    val vertexAmount: Int = vertexClusters.size,
    var clusterAmount: Int = vertexClusters.distinct().size,
    var fitness: Double = minimumModularity
) {
    init {
        verifyConsecutiveVertexClusters()
    }

    fun mutateVertexCluster(index: Int) {
        vertexClusters[index] = drawRandomVertexCluster()
    }

    fun mutateClusterAmount() {
        val oldClusterAmount: Int = clusterAmount
        val newClusterAmount: Int = Mutations.integerIncrementDecrement(gene = oldClusterAmount, illegalValues = listOf(0, 1, vertexAmount))

        if (oldClusterAmount == newClusterAmount) return
        clusterAmount = newClusterAmount

        if (newClusterAmount < oldClusterAmount) {
            for (vertexCluster: IndexedValue<Int> in vertexClusters.withIndex()) {
                if (vertexCluster.value > newClusterAmount) vertexClusters[vertexCluster.index] = drawRandomVertexCluster()
            }
        } else {
            val vertexClusterAmountToMutate: Int = vertexAmount / 2
            repeat(vertexClusterAmountToMutate) { vertexClusters[drawRandomVertexClusterIndex()] = drawRandomVertexCluster() }
        }
    }

    fun retrieveVertexClusterMap(): Map<Int, Int> {
        return vertexClusters.mapIndexed { vertexId, vertexCluster -> Pair(vertexId, vertexCluster) }.toMap()
    }

    fun clone(): Specimen {
        return Specimen(
            vertexClusters = this.vertexClusters.copyOf(),
            vertexAmount = this.vertexAmount,
            clusterAmount = this.clusterAmount,
            fitness = this.fitness
        )
    }

    private fun verifyConsecutiveVertexClusters() {
        // TODO("Consider implementing this!")
    }

    private fun drawRandomVertexCluster(): Int = Random.nextInt(1, clusterAmount + 1)

    private fun drawRandomVertexClusterIndex(): Int = Random.nextInt(0, vertexAmount)

    private fun drawRandomNewClusterAmount(): Int = Random.nextInt(2, vertexAmount)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Specimen

        if (!vertexClusters.contentEquals(other.vertexClusters)) return false
        if (vertexAmount != other.vertexAmount) return false
        if (clusterAmount != other.clusterAmount) return false

        return true
    }

    override fun hashCode(): Int {
        var result: Int = vertexClusters.contentHashCode()
        result = 31 * result + vertexAmount
        result = 31 * result + clusterAmount
        return result
    }

    companion object {
        fun generateRandom(vertexAmount: Int, clusterAmount: Int): Specimen {
            val vertexClusters: IntArray = (0 until vertexAmount).map { Random.nextInt(1, clusterAmount + 1) }.toIntArray()
            return Specimen(vertexClusters = vertexClusters)
        }
    }
}
