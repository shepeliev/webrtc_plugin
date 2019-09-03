package com.shepeliev.webrtc_plugin.plugin

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch

private val handler = Handler(Looper.getMainLooper())

fun uiThread(block: () -> Unit): CountDownLatch {
    val countDownLatch = CountDownLatch(1)
    if (Thread.currentThread() == handler.looper.thread) {
        block()
        countDownLatch.countDown()
    } else {
        handler.post {
            block()
            countDownLatch.countDown()
        }
    }
    return countDownLatch
}
