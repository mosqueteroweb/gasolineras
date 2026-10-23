package com.gasolineras.app.ui.map

import android.content.Context
import com.gasolineras.app.BuildConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.offline.OfflineManager
import java.io.File

/** Separate HTTP pool: never cancel the Ministry client's requests when pausing the map. */
object MapResources {
    private var client: OkHttpClient? = null
    @Synchronized fun initialize(context: Context) {
        if (client != null) return
        MapLibre.getInstance(context.applicationContext)
        val http = OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "map-http"), 64L * 1024 * 1024))
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("User-Agent",
                    "Gass/${BuildConfig.VERSION_NAME} (https://github.com/mosqueteroweb/gasolineras)").build())
            }.build()
        HttpRequestUtil.setOkHttpClient(http)
        HttpRequestUtil.setLogEnabled(false)
        client = http
    }

    fun pauseRequests() { client?.dispatcher?.cancelAll() }

    fun clearCache(context: Context, done: (Boolean) -> Unit) {
        initialize(context)
        pauseRequests()
        val httpCleared = runCatching { client?.cache?.evictAll() }.isSuccess
        OfflineManager.getInstance(context).clearAmbientCache(object : OfflineManager.FileSourceCallback {
            override fun onSuccess() {
                // Also remove the obsolete cache left by previous versions.
                val legacy = File(context.cacheDir, "osmdroid")
                done(httpCleared && (!legacy.exists() || legacy.deleteRecursively()))
            }
            override fun onError(message: String) { done(false) }
        })
    }
}
