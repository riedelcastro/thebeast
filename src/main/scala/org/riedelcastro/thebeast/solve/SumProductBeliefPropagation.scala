package org.riedelcastro.thebeast.solve


import env._
import doubles.{Multiplication, Sum, DoubleTerm}
/**
 * @author Sebastian Riedel
 */

class SumProductBeliefPropagation extends MarginalInference {
  class SPBPFactorGraph extends DoubleFactorGraph {
    case class SPBPEdge(override val node: NodeType, override val factor: FactorType) extends Edge(node, factor) {
      var node2factor: Belief[Any] = Ignorance(node.variable.values)
      var factor2node: Belief[Any] = Ignorance(node.variable.values)

      def updateNode2Factor = {
        node2factor = node.belief / factor2node
      }

    }

    case class SPBPNode(override val variable: EnvVar[Any]) extends Node(variable) {
      var belief: Belief[Any] = Ignorance(variable.values)

      def updateBelief = {
        belief = edges.foldLeft[Belief[Any]](Ignorance(variable.values)) {
          (r, e) => r * e.factor2node
        }
      }

    }

    case class SPBPFactor(override val term: TermType) extends Factor(term) {
      def updateOutgoingMessages = {
        val incomingBeliefs = new MutableBeliefs
        for (edge <- edges) incomingBeliefs.setBelief(edge.node.variable, edge.node2factor)
        val outgoingBeliefs = term.marginalize(incomingBeliefs)
        for (edge <- edges) edge.factor2node = outgoingBeliefs.belief(edge.node.variable) / edge.node2factor
      }
    }



    type FactorType = SPBPFactor
    type NodeType = SPBPNode
    type EdgeType = SPBPEdge

    protected def createFactor(term: TermType) = SPBPFactor(term)

    protected def createNode(variable: EnvVar[_]) = SPBPNode(variable)

    protected def createEdge(node: NodeType, factor: FactorType) = SPBPEdge(node, factor)
  }


  def infer(term: DoubleTerm) = {
    val graph = new SPBPFactorGraph
    term match {
      case Sum(args) => graph.addTerms(args)
      case Multiplication(args) => graph.addTerms(args)
      case _ => graph.addTerm(term)
    }

    println(graph.factors)


    //synchronous edge processing
    for (factor <- graph.factors)
      factor.updateOutgoingMessages

    for (node <- graph.nodes)
      node.updateBelief

    for (edge <- graph.edges)
      edge.updateNode2Factor

    val result = new MutableBeliefs
    for (node <-graph.nodes)
      result.setBelief(node.variable,node.belief)

    result
  }
}