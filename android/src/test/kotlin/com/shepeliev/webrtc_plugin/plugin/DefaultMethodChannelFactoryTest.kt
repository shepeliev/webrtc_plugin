package com.shepeliev.webrtc_plugin.plugin

import com.nhaarman.mockitokotlin2.*
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultMethodChannelFactoryTest {
    @Mock private lateinit var registrar: Registrar
    @Mock private lateinit var messenger: BinaryMessenger

    @Before
    fun setUp() {
        whenever(registrar.messenger()) doReturn messenger
    }

    @Test
    fun createMethodChannel() {
        val factory = DefaultMethodChannelFactory()

        factory.createMethodChannel(registrar, "method_channel", mapOf())

        verify(messenger).setMessageHandler(eq("method_channel"), any())
    }
}
