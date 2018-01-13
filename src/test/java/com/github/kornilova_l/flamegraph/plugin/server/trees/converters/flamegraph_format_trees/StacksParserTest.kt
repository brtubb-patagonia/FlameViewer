package com.github.kornilova_l.flamegraph.plugin.server.trees.converters.flamegraph_format_trees

import org.junit.Test

import org.junit.Assert.*

class StacksParserTest {

    @Test
    fun doCallsContainParametersTest() {
        var stacks = hashMapOf(Pair("1", 1))
        assertFalse(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("1;2", 1), Pair("void hello();void hello()", 1))
        assertFalse(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("void hello()", 1), Pair("1", 1))
        assertFalse(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("void a();void a()", 1))
        assertTrue(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair(" ()", 1))
        assertTrue(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("a ()", 1))
        assertTrue(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("void a(); a()", 1))
        assertTrue(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("void a();void a)(", 1))
        assertFalse(StacksParser.doCallsContainParameters(stacks))
    }
}