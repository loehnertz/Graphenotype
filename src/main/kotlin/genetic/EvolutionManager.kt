package codes.jakob.graphenotype.genetic

import codes.jakob.graphenotype.utility.Utilities.calculateGradientOfCurve
import codes.jakob.graphenotype.utility.Utilities.drawPercentageChance
import codes.jakob.graphenotype.utility.toArrayList
import com.mitchtalmadge.asciidata.graph.ASCIIGraph
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.random.Random


class EvolutionManager(
    private val vertexAmount: Int,
    private val maxGenerations: Int = MaxGenerations,
    private val populationSize: Int = PopulationSize,
    private val baseChanceToMutate: Int = ChanceToMutatePercentage,
    private val allowShortCircuiting: Boolean = AllowShortCircuiting,
    private val fitnessFunction: (Specimen) -> Double
) {
    private var currentGeneration = 0
    private var generationsWithoutImprovement = 0
    private var mutationChance: Int = baseChanceToMutate
    private val specimen2FitnessMap: MutableMap<Specimen, Pair<Double, Int>> = mutableMapOf()
    private val bestFitnessProgression: ArrayList<Double> = arrayListOf()
    private lateinit var population: ArrayList<Specimen>
    private lateinit var bestEverSpecimen: Specimen

    fun start(): Specimen {
        initializePopulation()

        while (currentGeneration <= maxGenerations) {
            if (allowShortCircuiting) {
                if (noImprovementsForTooLong()) break
                if (currentFitnessGradientTooLow()) break
            }

            currentGeneration++

            select()
            crossover()
            mutate()

            evaluate()
        }

        finalReport()

        return bestEverSpecimen
    }

    private fun evaluate() {
        val deferredEvaluations: ArrayList<Pair<Specimen, Deferred<Double>>> = arrayListOf()

        for (specimen: Specimen in population) {
            if (specimen2FitnessMap.containsKey(specimen)) {
                specimen.fitness = specimen2FitnessMap[specimen]!!.first
            } else {
                deferredEvaluations += Pair(specimen, GlobalScope.async { fitnessFunction(specimen) })
            }
        }

        runBlocking {
            val completedEvaluations: List<Pair<Specimen, Double>> = deferredEvaluations.map { Pair(it.first, it.second.await()) }
            for ((specimen: Specimen, calculatedFitness: Double) in completedEvaluations) {
                specimen.fitness = calculatedFitness
                specimen2FitnessMap[specimen] = Pair(calculatedFitness, currentGeneration)
            }
        }

        updateBestSpecimen()

        report()
    }

    private fun report() {
        println("Generation #$currentGeneration")
        println("Best fitness of current generation: ${retrieveCurrentBestSpecimen().fitness}")
        println("Average fitness of current generation: ${population.map { it.fitness }.average()}")
        println("Maximum fitness gradient of current generation: ${calculateCurrentFitnessGradient()}")
        println("Distinct cluster amounts in current generation: ${population.map { it.clusterAmount }.distinct()}")
        println("Amount of unique specimen born: ${specimen2FitnessMap.size}")
        println("Generations without improvement: $generationsWithoutImprovement")
        println("Current mutation chance: $mutationChance%")
        println()
    }

    private fun finalReport() {
        println("\n")
        println("The evolution has concluded:\n")
        println("The best performing specimen of the whole evolution:")
        println("Fitness: ${bestEverSpecimen.fitness}")
        println("Cluster amount: ${bestEverSpecimen.clusterAmount}")
        println("\n")
        println(ASCIIGraph.fromSeries(bestFitnessProgression.toDoubleArray()).withNumRows(10).plot())
        println()
    }

    private fun select() {
        population = population.sortedByDescending { it.fitness }.toSet().take(((populationSize / 2) + 1)).toArrayList()
    }

    private fun crossover() {
        while (population.size < populationSize) {
            val parentPair: Pair<Specimen, Specimen> = retrieveParentPair()
            val offspring: Specimen = crossoverParents(parentPair)
            population.add(offspring)
        }
    }

    private fun mutate() {
        adjustMutationChance()

        for (specimen: Specimen in population) {
            if (drawPercentageChance() <= baseChanceToMutate) {
                specimen.mutateClusterAmount()
                continue
            }

            for (vertexCluster: IndexedValue<Int> in specimen.vertexClusters.withIndex()) {
                if (drawPercentageChance() <= baseChanceToMutate) specimen.mutateVertexCluster(vertexCluster.index)
            }
        }
    }

    private fun adjustMutationChance() {
        mutationChance = (generationsWithoutImprovement + 1) * baseChanceToMutate / 2

        if (mutationChance < baseChanceToMutate) mutationChance = baseChanceToMutate
        if (mutationChance > MaxChanceToMutatePercentage) mutationChance = MaxChanceToMutatePercentage
    }

    private fun retrieveParentPair(): Pair<Specimen, Specimen> {
        return Pair(selectRandomSpecimenViaTournament(), selectRandomSpecimenViaTournament())
    }

    private fun selectRandomSpecimenViaTournament(): Specimen {
        val tournamentParticipants: List<Specimen> = population.shuffled().take((populationSize / SelectionPercentage) + 1)
        return tournamentParticipants.maxBy { it.fitness }!!
    }

    private fun crossoverParents(parentPair: Pair<Specimen, Specimen>): Specimen {
        val crossoverPoint: Int = Random.nextInt(0, vertexAmount)
        val firstParentVertexClusters: IntArray = parentPair.first.vertexClusters.sliceArray(0 until crossoverPoint)
        val secondParentVertexClusters: IntArray = parentPair.second.vertexClusters.sliceArray(crossoverPoint until vertexAmount)
        return Specimen(vertexClusters = (firstParentVertexClusters + secondParentVertexClusters))
    }

    private fun updateBestSpecimen() {
        val currentBestSpecimen: Specimen = retrieveCurrentBestSpecimen().also { bestFitnessProgression.add(it.fitness) }

        if (currentGeneration == 0) bestEverSpecimen = currentBestSpecimen.clone()

        if (currentBestSpecimen.fitness > bestEverSpecimen.fitness) {
            bestEverSpecimen = currentBestSpecimen.clone()
            generationsWithoutImprovement = 0
        } else {
            generationsWithoutImprovement++
        }
    }

    private fun noImprovementsForTooLong(): Boolean {
        return generationsWithoutImprovement > (maxGenerations / 10)
    }

    private fun currentFitnessGradientTooLow(): Boolean {
        val currentFitnessGradient: Double = calculateCurrentFitnessGradient()
        if (currentFitnessGradient.isNaN() || currentFitnessGradient <= 0.0) return false
        if (currentFitnessGradient < MinimumFitnessGradient) return true
        return false
    }

    private fun calculateCurrentFitnessGradient(): Double {
        val x1: Int = bestFitnessProgression.size / 2
        val y1: Double = bestFitnessProgression[x1]
        val x2: Int = bestFitnessProgression.size - 1
        val y2: Double = bestFitnessProgression[x2]

        return calculateGradientOfCurve(x1.toDouble(), y1, x2.toDouble(), y2)
    }

    private fun initializePopulation() {
        population = (2..vertexAmount).map { generateRandomSpecimen(it) }.toArrayList()
        evaluate()
    }

    private fun generateRandomSpecimen(clusterAmount: Int = Random.nextInt(2, vertexAmount)): Specimen {
        return Specimen.generateRandom(
            clusterAmount = clusterAmount,
            vertexAmount = vertexAmount
        )
    }

    private fun retrieveCurrentBestSpecimen(): Specimen = population.maxBy { it.fitness }!!

    companion object Constants {
        private const val MaxGenerations = 1000
        private const val PopulationSize = 1000
        private const val SelectionPercentage = 10
        private const val MaxChanceToMutatePercentage = 10
        private const val ChanceToMutatePercentage = 1
        private const val MinimumFitnessGradient = 0.001
        private const val AllowShortCircuiting = true
    }
}
