package com.shepeliev.webrtc_plugin.plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MethodCallHandlersTest {
    @get:Rule val mockitoRule = MockitoJUnit.rule()!!
    @Mock private lateinit var result: MethodChannel.Result

    private var methodCall: MethodCall? = null
    private lateinit var methodCallHandlers: MethodCallHandlers

    @Before
    fun setUp() {
        val handlers = mapOf("method" to ::methodHandler)
        methodCallHandlers = MethodCallHandlers(handlers)
    }

    @After
    fun tearDown() {
        methodCall = null
    }

    private fun methodHandler(methodCall: MethodCall): String {
        this.methodCall = methodCall
        methodCall.argument<Throwable>("throw")?.let { throw it }
        return "42"
    }

    @Test
    fun onMethodCall() {
        val expectedMethodCall = MethodCall("method", mapOf("foo" to 42))
        methodCallHandlers.onMethodCall(expectedMethodCall, result)

        verify(result).success("42")
        assertThat(methodCall).isEqualTo(expectedMethodCall)
    }

    @Test
    fun onMethodCall_not_implemented_method() {
        methodCallHandlers.onMethodCall(MethodCall("notImplemented", null), result)

        verify(result).notImplemented()
    }

    @Test
    fun onMethodCall_method_throws_error() {
        val exception = RuntimeException("error message")
        methodCallHandlers.onMethodCall(
            MethodCall("method", mapOf("throw" to exception)),
            result
        )

        verify(result).error("UNHANDLED_EXCEPTION", "error message", exception)
    }
}
