package codes.jakob.graphenotype.utility

import kotlin.random.Random


object Utilities {
    fun drawPercentageChance() = Random.nextInt(0, 101)

    fun calculateGradientOfCurve(x1: Double, y1: Double, x2: Double, y2: Double): Double = (y2 - y1) / (x2 - x1)
}
