package com.shepeliev.webrtc_plugin.plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.shepeliev.webrtc_plugin.METHOD_CHANNEL_NAME
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultMethodChannelRegistryTest {
    @Mock private lateinit var registrar: Registrar
    @Mock private lateinit var binaryMessenger: BinaryMessenger
    @Mock private lateinit var methodChannelFactory: MethodChannelFactory

    @Before
    fun setUp() {
        whenever(methodChannelFactory.createMethodChannel(any(), any(), any())) doAnswer {
            val channelName = it.arguments[1] as String
            MethodChannel(binaryMessenger, channelName)
        }
    }

    @Test
    fun constructor() {
        val fooPlugin = FooBackend()
        val bazPlugin = BarBackend()
        val registry = DefaultMethodChannelRegistry(
            registrar,
            listOf(fooPlugin, bazPlugin),
            methodChannelFactory
        )

        assertThat(registry.methodChannels).hasSize(1)
        assertThat(registry.methodChannels.keys).containsExactly(METHOD_CHANNEL_NAME)
        argumentCaptor<Map<String, MethodHandler<Int>>>().apply {
            verify(methodChannelFactory).createMethodChannel(
                eq(registrar),
                eq(METHOD_CHANNEL_NAME),
                capture()
            )
            val dummyMethodCall = MethodCall("", null)
            assertThat(firstValue.getValue("foo").invoke(dummyMethodCall)).isEqualTo(42)
            assertThat(firstValue.getValue("bar").invoke(dummyMethodCall)).isEqualTo(42)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun constructor_duplicate_global_method() {
        val fooPlugin = FooBackend()
        val bazPlugin = BarBackend()
        val fooBarPlugin = FooBarBackend()
        DefaultMethodChannelRegistry(
            registrar,
            listOf(fooPlugin, bazPlugin, fooBarPlugin),
            methodChannelFactory
        )
    }

    @Test
    fun addPlugin() {
        val registry = DefaultMethodChannelRegistry(
            registrar,
            emptyList(),
            methodChannelFactory
        )

        val bazPlugin = BazBackend()
        registry.addPlugin(bazPlugin)

        assertThat(registry.methodChannels).hasSize(1)
        assertThat(registry.methodChannels.keys).containsExactly(bazPlugin.channelName)
        argumentCaptor<Map<String, MethodHandler<Int>>>().apply {
            verify(methodChannelFactory).createMethodChannel(
                eq(registrar),
                eq(bazPlugin.channelName),
                capture()
            )
            val dummyMethodCall = MethodCall("", null)
            assertThat(firstValue.getValue("baz").invoke(dummyMethodCall)).isEqualTo(42)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun addPlugin_duplicate_plugin() {
        val registry = DefaultMethodChannelRegistry(
            registrar,
            emptyList(),
            methodChannelFactory
        )

        val bazPlugin = BazBackend()
        registry.addPlugin(bazPlugin)
        registry.addPlugin(bazPlugin)
    }

    @Test
    fun removePlugin() {
        val registry = DefaultMethodChannelRegistry(
            registrar,
            emptyList(),
            methodChannelFactory
        )
        val bazPlugin = BazBackend()
        registry.addPlugin(bazPlugin)

        registry.removePlugin(bazPlugin)

        assertThat(registry.methodChannels).isEmpty()
        verify(binaryMessenger).setMessageHandler(bazPlugin.channelName, null)
    }
}

private class FooBackend : GlobalFlutterBackend {
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "foo" to ::foo
    )

    @Suppress("UNUSED_PARAMETER")
    private fun foo(methodCall: MethodCall): Int = 42
}

private class BarBackend : GlobalFlutterBackend {
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "bar" to ::bar
    )

    @Suppress("UNUSED_PARAMETER")
    private fun bar(methodCall: MethodCall): Int = 42
}

private class FooBarBackend : GlobalFlutterBackend {
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "bar" to ::bar
    )

    @Suppress("UNUSED_PARAMETER")
    private fun bar(methodCall: MethodCall): Int = 42
}

private class BazBackend(override val id: PluginId = newId()) :
    FlutterBackend {
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "baz" to ::baz
    )

    @Suppress("UNUSED_PARAMETER")
    private fun baz(methodCall: MethodCall): Int = 42
}
