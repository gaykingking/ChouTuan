package com.example.myapplication.sdk

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 丑团原生 TTS 语音播报管理器（单一职责：商家接单与骑手派单语音播报）。
 * 支持静音开关与 Lifecycle 生命周期安全释放。
 */
class VoiceBroadcastManager(
    context: Context
) : TextToSpeech.OnInitListener, DefaultLifecycleObserver {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private val isReady = AtomicBoolean(false)
    private val isReleased = AtomicBoolean(false)

    @Volatile
    var isMuted: Boolean = false
        private set

    @Volatile
    private var pendingMessage: String? = null

    override fun onInit(status: Int) {
        if (isReleased.get()) return
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.SIMPLIFIED_CHINESE
            tts?.setSpeechRate(1.0f)
            isReady.set(true)
            pendingMessage?.let {
                pendingMessage = null
                speakInternal(it)
            }
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            tts?.stop()
        }
    }

    fun toggleMute(): Boolean {
        setMuted(!isMuted)
        return isMuted
    }

    /** 商家端：丑团新订单0抽成到账 */
    fun broadcastMerchantOrder(amountYuan: String? = null) {
        val text = if (amountYuan.isNullOrBlank()) {
            "丑团新订单0抽成到账"
        } else {
            "丑团新订单0抽成到账，实收${amountYuan}元"
        }
        speakInternal(text)
    }

    /** 骑手端：新配送任务全额运费到账 */
    fun broadcastRiderTask(deliveryFeeYuan: String? = null) {
        val text = if (deliveryFeeYuan.isNullOrBlank()) {
            "新配送任务全额运费到账"
        } else {
            "新配送任务全额运费到账，运费${deliveryFeeYuan}元"
        }
        speakInternal(text)
    }

    private fun speakInternal(message: String) {
        if (isReleased.get() || isMuted) return
        if (!isReady.get()) {
            pendingMessage = message
            return
        }
        tts?.speak(message, TextToSpeech.QUEUE_ADD, null, "ct_${System.currentTimeMillis()}")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        release()
    }

    fun release() {
        if (isReleased.compareAndSet(false, true)) {
            isReady.set(false)
            pendingMessage = null
            tts?.stop()
            tts?.shutdown()
            tts = null
        }
    }
}
