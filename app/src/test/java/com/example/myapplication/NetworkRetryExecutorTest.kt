package com.example.myapplication

import com.example.myapplication.data.remote.NetworkRetryExecutor
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class NetworkRetryExecutorTest {

    @Test
    fun execute_succeedsOnFirstAttempt_returnsResultWithoutRetry() = runBlocking {
        val executor = NetworkRetryExecutor(
            maxRetries = 2,
            initialDelayMillis = 1L,
            backoffMultiplier = 2.0
        )
        var attempts = 0

        val result = executor.execute {
            attempts++
            "store-list-ok"
        }

        assertEquals("store-list-ok", result)
        assertEquals(1, attempts)
    }

    @Test
    fun execute_failsTwiceThenSucceedsOnThirdAttempt_retriesAndReturnsResult() = runBlocking {
        val executor = NetworkRetryExecutor(
            maxRetries = 2,
            initialDelayMillis = 1L,
            backoffMultiplier = 2.0
        )
        var attempts = 0

        val result = executor.execute {
            attempts++
            if (attempts < 3) {
                throw IOException("Weak network in elevator (attempt $attempts)")
            }
            "order-synced"
        }

        assertEquals("order-synced", result)
        assertEquals(3, attempts)
    }

    @Test
    fun execute_exceedsMaxRetries_throwsFinalException() = runBlocking {
        val executor = NetworkRetryExecutor(
            maxRetries = 2,
            initialDelayMillis = 1L,
            backoffMultiplier = 2.0
        )
        var attempts = 0
        val finalException = IOException("Basement network unreachable (final)")

        try {
            executor.execute {
                attempts++
                if (attempts <= 2) {
                    throw IOException("Transient failure $attempts")
                }
                throw finalException
            }
            fail("Expected finalException to be thrown after exceeding maxRetries")
        } catch (e: IOException) {
            assertSame(finalException, e)
            assertEquals(3, attempts)
        }
    }
}
