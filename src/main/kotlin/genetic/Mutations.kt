package codes.jakob.graphenotype.genetic

import kotlin.random.Random


object Mutations {
    fun integerIncrementDecrement(gene: Int, illegalValues: List<Int>? = null): Int {
        val mutatedGene: Int = if (Random.nextBoolean()) {
            gene + 1
        } else {
            gene - 1
        }

        return if (illegalValues == null) {
            mutatedGene
        } else {
            if (!illegalValues.contains(mutatedGene)) {
                mutatedGene
            } else {
                gene
            }
        }
    }
}
