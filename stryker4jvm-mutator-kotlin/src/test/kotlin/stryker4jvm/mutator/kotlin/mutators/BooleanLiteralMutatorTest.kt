package stryker4jvm.mutator.kotlin.mutators

import io.mockk.clearAllMocks
import stryker4jvm.mutator.kotlin.utility.PsiUtility
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import stryker4jvm.mutator.kotlin.KotlinAST
import stryker4jvm.mutator.kotlin.KotlinCollector
import stryker4jvm.mutator.kotlin.mutators.MutatorTest.newCollector

class BooleanLiteralMutatorTest {
    @Test
    fun testBooleanMutator() {
        // Arrange
        clearAllMocks()
        val target = newCollector(BooleanLiteralMutator)
        val testFile = KotlinAST(PsiUtility.createPsiFile("fun dummy() { print(true && false) }"))

        // Act
        val result = target.collect(testFile)
        val ignored = result.ignoredMutations
        val mutations = result.mutations

        // Assert
        assertTrue(ignored.isEmpty())
        assertEquals(2, mutations.size) // we have two mutations

        MutatorTest.testName("BooleanLiteral", result)
        MutatorTest.testMutations(
                mapOf(
                        Pair("true", mutableListOf("false")),  // all trues map to false
                        Pair("false", mutableListOf("true"))), // all false map to trues
                result
        )
    }
}
