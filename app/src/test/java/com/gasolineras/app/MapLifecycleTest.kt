package com.gasolineras.app

import androidx.lifecycle.Lifecycle
import com.gasolineras.app.ui.map.MapLifecycle
import org.junit.Assert.assertEquals
import org.junit.Test

class MapLifecycleTest {
    private class Recording : MapLifecycle.Target {
        val calls = mutableListOf<String>()
        override fun start() { calls += "start" }
        override fun resume() { calls += "resume" }
        override fun pause() { calls += "pause" }
        override fun stop() { calls += "stop" }
        override fun destroy() { calls += "destroy" }
    }
    @Test fun backgroundAndScreenLockStopRenderingAndReturningRestartsOnce() {
        val target = Recording()
        val lifecycle = MapLifecycle(target)
        lifecycle.sync(Lifecycle.State.RESUMED)
        lifecycle.sync(Lifecycle.State.RESUMED)
        lifecycle.sync(Lifecycle.State.STARTED)
        lifecycle.sync(Lifecycle.State.CREATED)
        lifecycle.sync(Lifecycle.State.CREATED)
        lifecycle.sync(Lifecycle.State.RESUMED)
        lifecycle.dispose()
        lifecycle.dispose()
        assertEquals(listOf("start","resume","pause","stop","start","resume","pause","stop","destroy"), target.calls)
    }
    @Test fun leavingMapWhileVisibleDestroysOnlyAfterStoppingAndNeverRestarts() {
        val target = Recording()
        val lifecycle = MapLifecycle(target)
        lifecycle.sync(Lifecycle.State.RESUMED)
        lifecycle.dispose()
        lifecycle.sync(Lifecycle.State.RESUMED)
        assertEquals(listOf("start","resume","pause","stop","destroy"), target.calls)
    }
    @Test fun mapCreatedWhileBackgroundedDoesNotStart() {
        val target = Recording()
        val lifecycle = MapLifecycle(target)
        lifecycle.sync(Lifecycle.State.CREATED)
        lifecycle.sync(Lifecycle.State.DESTROYED)
        assertEquals(listOf("destroy"), target.calls)
    }
}
