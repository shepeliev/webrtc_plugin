package com.shepeliev.webrtc_plugin.plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultPluginRegistryTest {

    @Mock private lateinit var methodChannelRegistry: MethodChannelRegistry

    private lateinit var fakePlugin1: FakePlugin
    private lateinit var fakePlugin2: FakePlugin
    private lateinit var registry: DefaultPluginRegistry

    @Before
    fun setUp() {
        fakePlugin1 = FakePlugin()
        fakePlugin2 = FakePlugin()
        registry = DefaultPluginRegistry(methodChannelRegistry)
    }

    @Test
    fun add() {
        registry.add(fakePlugin1)

        assertThat(registry.allPlugins).containsExactly(fakePlugin1)
        verify(methodChannelRegistry).addPlugin(fakePlugin1)
    }

    @Test
    fun getAllPlugins() {
        registry.add(fakePlugin1)
        registry.add(fakePlugin2)

        assertThat(registry.allPlugins).containsAnyOf(fakePlugin1, fakePlugin2)
    }

    @Test
    fun remove() {
        registry.add(fakePlugin1)
        registry.add(fakePlugin2)

        registry.remove(fakePlugin1)

        assertThat(registry.allPlugins).containsExactly(fakePlugin2)
        verify(methodChannelRegistry).removePlugin(fakePlugin1)
    }

    @Test
    fun get() {
        registry.add(fakePlugin1)
        registry.add(fakePlugin2)

        val plugin: FakePlugin = registry[fakePlugin1.id]

        assertThat(plugin).isEqualTo(fakePlugin1)
    }
}

private class FakePlugin(
    override val id: PluginId = newId(),
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf()
) : FlutterPlugin

