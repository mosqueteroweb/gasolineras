package com.gasolineras.app.ui.map

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/** Exactly one balanced start/stop and resume/pause, including disposal while resumed. */
internal class MapLifecycle(private val target: Target) : LifecycleEventObserver {
    interface Target {
        fun start()
        fun resume()
        fun pause()
        fun stop()
        fun destroy()
    }
    private var started = false
    private var resumed = false
    private var destroyed = false

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) = sync(source.lifecycle.currentState)

    fun sync(state: Lifecycle.State) {
        if (destroyed) return
        val shouldStart = state.isAtLeast(Lifecycle.State.STARTED)
        val shouldResume = state.isAtLeast(Lifecycle.State.RESUMED)
        if (resumed && !shouldResume) { target.pause(); resumed = false }
        if (started && !shouldStart) { target.stop(); started = false }
        if (!started && shouldStart) { target.start(); started = true }
        if (!resumed && shouldResume) { target.resume(); resumed = true }
        if (state == Lifecycle.State.DESTROYED) dispose()
    }

    fun dispose() {
        if (destroyed) return
        if (resumed) target.pause()
        if (started) target.stop()
        resumed = false
        started = false
        destroyed = true
        target.destroy()
    }
}
