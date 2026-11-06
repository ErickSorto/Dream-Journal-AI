package org.ballistic.dreamjournalai.shared.dream_add_edit.data

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class TextGenerationChargeTest {
    @Test
    fun failedRetriesPreserveBalance() = runBlocking {
        var balance = 100
        val failure = AIResult.Error("Request failed")
        repeat(3) {
            assertSame(failure, failure.chargeForGeneratedText(5) { balance -= it })
        }
        assertEquals(100, balance)
    }

    @Test
    fun emptyCompletionsAreErrorsAndPreserveBalance() = runBlocking {
        var balance = 100
        for (text in listOf("", " \n\t")) {
            assertIs<AIResult.Error>(AIResult.Success(text).chargeForGeneratedText(5) { balance -= it })
        }
        assertEquals(100, balance)
    }

    @Test
    fun successAfterFailureChargesOnceForDeliveredText() = runBlocking {
        val charges = mutableListOf<Int>()
        AIResult.Error("Timeout").chargeForGeneratedText(5) { charges += it }
        val success = AIResult.Success("A useful interpretation")
        assertSame(success, success.chargeForGeneratedText(5) { charges += it })
        assertEquals(listOf(5), charges)
    }

    @Test
    fun freeGenerationDoesNotCallBilling() = runBlocking {
        val success = AIResult.Success("A free interpretation")
        assertSame(success, success.chargeForGeneratedText(0) { error("Must not bill free generation") })
    }
}
