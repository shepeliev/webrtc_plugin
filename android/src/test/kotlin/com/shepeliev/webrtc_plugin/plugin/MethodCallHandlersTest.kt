package com.shepeliev.webrtc_plugin.plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MethodCallHandlersTest {
    @Mock private lateinit var methodHandler: MethodHandler<String>
    @Mock private lateinit var result: MethodChannel.Result

    private lateinit var methodCallHandlers: MethodCallHandlers

    @Before
    fun setUp() {
        whenever(methodHandler.invoke(any())) doReturn "42"
        val handlers = mapOf("method" to methodHandler)
        methodCallHandlers = MethodCallHandlers(handlers)
    }

    @Test
    fun onMethodCall() {
        methodCallHandlers.onMethodCall(MethodCall("method", mapOf("foo" to 42)), result)

        verify(result).success("42")
        argumentCaptor<MethodCall>().apply {
            verify(methodHandler).invoke(capture())
            assertThat(firstValue.method).isEqualTo("method")
            assertThat(firstValue.arguments).isEqualTo(mapOf("foo" to 42))
        }
    }

    @Test
    fun onMethodCall_not_implemented_method() {
        methodCallHandlers.onMethodCall(MethodCall("notImplemented", null), result)

        verify(result).notImplemented()
    }

    @Test
    fun onMethodCall_method_throws_error() {
        val exception = RuntimeException("error")
        whenever(methodHandler.invoke(any())) doThrow exception
        methodCallHandlers.onMethodCall(MethodCall("method", mapOf("foo" to 42)), result)

        verify(result).error(
            "UNHANDLED_EXCEPTION",
            "{method=\"method\", arguments={foo=42}, errorMessage=\"error\"}",
            exception
        )
    }
}
