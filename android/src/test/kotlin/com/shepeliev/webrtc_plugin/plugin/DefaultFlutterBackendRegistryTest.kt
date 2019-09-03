package com.shepeliev.webrtc_plugin.plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultFlutterBackendRegistryTest {

    @Mock private lateinit var methodChannelRegistry: MethodChannelRegistry

    private lateinit var fakePlugin1: FakeBackend
    private lateinit var fakePlugin2: FakeBackend
    private lateinit var registry: DefaultFlutterBackendRegistry

    @Before
    fun setUp() {
        fakePlugin1 = FakeBackend()
        fakePlugin2 = FakeBackend()
        registry = DefaultFlutterBackendRegistry()
            .also { it.methodChannelRegistry = methodChannelRegistry }
    }

    @Test
    fun add() {
        registry.add(fakePlugin1)

        assertThat(registry.all).containsExactly(fakePlugin1)
        verify(methodChannelRegistry).addFlutterBackend(fakePlugin1)
    }

    @Test
    fun getAllPlugins() {
        registry.add(fakePlugin1)
        registry.add(fakePlugin2)

        assertThat(registry.all).containsAnyOf(fakePlugin1, fakePlugin2)
    }

    @Test
    fun remove() {
        registry.add(fakePlugin1)
        registry.add(fakePlugin2)

        registry.remove(fakePlugin1)

        assertThat(registry.all).containsExactly(fakePlugin2)
        verify(methodChannelRegistry).removePlugin(fakePlugin1)
    }

    @Test
    fun get() {
        registry.add(fakePlugin1)
        registry.add(fakePlugin2)

        val plugin: FakeBackend = registry[fakePlugin1.id]

        assertThat(plugin).isEqualTo(fakePlugin1)
    }
}

private class FakeBackend(
    override val id: BackendId = newStringId(),
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf()
) : FlutterBackend

