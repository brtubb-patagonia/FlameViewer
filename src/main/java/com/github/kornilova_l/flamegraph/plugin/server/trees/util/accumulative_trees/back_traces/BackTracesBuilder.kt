package com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.back_traces

import com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.tree.maximumNodesCount
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeBuilder
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil.getSelfTime
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node
import java.util.*

/**
 * This class is used only if calltraces tree contains less than [maximumNodesCount]
 */
class BackTracesBuilder(callTraces: TreeProtos.Tree) : TreeBuilder {
    private val backTraces: TreeProtos.Tree

    init {
        val treeBuilder = TreeProtos.Tree.newBuilder()
                .setBaseNode(Node.newBuilder())
        buildTreeRecursively(callTraces.baseNode, treeBuilder.baseNodeBuilder, ArrayList(callTraces.depth))
        TreesUtil.setNodesOffsetRecursively(treeBuilder.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(treeBuilder)
        TreesUtil.setNodesCount(treeBuilder)
        treeBuilder.depth = callTraces.depth
        backTraces = treeBuilder.build()
    }

    private fun buildTreeRecursively(node: Node,
                                     baseNode: Node.Builder,
                                     currentStack: MutableList<Node>) {
        currentStack.add(node)
        for (i in 0 until node.nodesList.size) {
            val child = node.nodesList[i]
            buildTreeRecursively(child, baseNode, currentStack)
        }
        val selfTime = getSelfTime(node)
        if (selfTime > 0) {
            addStack(baseNode, currentStack, selfTime)
        }
        currentStack.removeAt(currentStack.size - 1)
    }

    private fun addStack(baseNode: Node.Builder, currentStack: MutableList<Node>, width: Long) {
        var currentBuilder = baseNode
        for (i in currentStack.size - 1 downTo 1) { // first node is base node
            val nodeInfo = currentStack[i].nodeInfo
            currentBuilder = TreesUtil.updateNodeList(currentBuilder, nodeInfo.className,
                    nodeInfo.methodName, nodeInfo.description, width)
        }
    }

    override fun getTree(): TreeProtos.Tree = backTraces
}
