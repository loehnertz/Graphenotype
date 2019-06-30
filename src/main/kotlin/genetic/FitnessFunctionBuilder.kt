package genetic

import codes.jakob.graphenotype.clustering.GraphClusterer
import codes.jakob.graphenotype.clustering.ModularityCalculator
import codes.jakob.graphenotype.clustering.graph.ClusterableGraph
import codes.jakob.graphenotype.genetic.Specimen


object FitnessFunctionBuilder {
    fun build(graph: ClusterableGraph): (Specimen) -> Double {
        return { specimen: Specimen ->
            val clonedGraph: ClusterableGraph = graph.clone()
            val clusteredGraph: ClusterableGraph = GraphClusterer.clusterGraph(clonedGraph, specimen.retrieveVertexClusterMap())
            ModularityCalculator.calculate(clusteredGraph)
        }
    }
}
