package stryker4jvm.mutator.kotlin.mutators

import io.mockk.clearAllMocks
import stryker4jvm.mutator.kotlin.utility.PsiUtility
import org.junit.jupiter.api.Test
import stryker4jvm.mutator.kotlin.KotlinAST
import stryker4jvm.mutator.kotlin.KotlinCollector
import org.junit.jupiter.api.Assertions.*

class ConditionalExpressionMutatorTest {

    @Test
    fun testConditionalExpressionMutatorMutate() {
        // Arrange
        clearAllMocks()
        val target = MutatorTest.newCollector(ConditionalExpressionMutator)
        val testFile = MutatorTest.parse("""
            fun dummy() { 
                if(0 < 1) print("a")
                if(0 <= 1) print("a")
                if(0 > 1) print("a")
                if(0 >= 1) print("a")
                if(0 == 1) print("a")
                if(0 != 1) print("a")
                if(0 === 1) print("a")
                if(0 !== 1) print("a")
                if(0 || 1) print("a")
                if(0 && 1) print("a")
            }
        """.trimIndent())

        // Act
        val result = target.collect(testFile)
        val ignored = result.ignoredMutations
        val mutations = result.mutations

        // Assert
        assertTrue(ignored.isEmpty())
        assertEquals(10, mutations.size)

        MutatorTest.testName("ConditionalExpression", result)

        val expect = mutableListOf("true", "false") // they all mutate to true and false
        MutatorTest.testMutations(
                mapOf(
                        Pair("0 < 1", expect),
                        Pair("0 <= 1", expect),
                        Pair("0 > 1", expect),
                        Pair("0 >= 1", expect),
                        Pair("0 == 1", expect),
                        Pair("0 != 1", expect),
                        Pair("0 === 1", expect),
                        Pair("0 !== 1", expect),
                        Pair("0 || 1", expect),
                        Pair("0 && 1", expect)
                ),
                result
        )
    }
}
