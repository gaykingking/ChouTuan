package com.example.myapplication.data.remote

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * 纯 Kotlin 协程指数退避网络重试执行器。
 *
 * 专为外卖骑手/用户在电梯、地下室等弱网环境下的门店加载与订单状态同步场景设计，
 * 支持配置最大重试次数、初始退避毫秒数、退避倍数与可重试异常过滤，零新增第三方依赖。
 */
class NetworkRetryExecutor(
    val maxRetries: Int = DEFAULT_MAX_RETRIES,
    val initialDelayMillis: Long = DEFAULT_INITIAL_DELAY_MILLIS,
    val backoffMultiplier: Double = DEFAULT_BACKOFF_MULTIPLIER,
    val shouldRetry: (Throwable) -> Boolean = { it is IOException }
) {
    init {
        require(maxRetries >= 0) { "maxRetries must be >= 0" }
        require(initialDelayMillis >= 0L) { "initialDelayMillis must be >= 0" }
        require(backoffMultiplier >= 1.0) { "backoffMultiplier must be >= 1.0" }
    }

    suspend fun <T> execute(
        maxRetries: Int = this.maxRetries,
        initialDelayMillis: Long = this.initialDelayMillis,
        backoffMultiplier: Double = this.backoffMultiplier,
        shouldRetry: (Throwable) -> Boolean = this.shouldRetry,
        block: suspend () -> T
    ): T {
        require(maxRetries >= 0) { "maxRetries must be >= 0" }
        require(initialDelayMillis >= 0L) { "initialDelayMillis must be >= 0" }
        require(backoffMultiplier >= 1.0) { "backoffMultiplier must be >= 1.0" }

        var currentDelay = initialDelayMillis
        var attempt = 0
        while (true) {
            try {
                return block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                if (attempt >= maxRetries || !shouldRetry(e)) {
                    throw e
                }
                if (currentDelay > 0L) {
                    delay(currentDelay)
                }
                currentDelay = (currentDelay * backoffMultiplier).toLong()
                attempt++
            }
        }
    }

    companion object {
        const val DEFAULT_MAX_RETRIES = 3
        const val DEFAULT_INITIAL_DELAY_MILLIS = 300L
        const val DEFAULT_BACKOFF_MULTIPLIER = 2.0
    }
}
