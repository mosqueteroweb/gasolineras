package com.gasolineras.app.data.local

import com.gasolineras.app.data.api.FuelApiResponse
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Last successful official response. Kept in filesDir, not Android's disposable cacheDir. */
class StationCache(private val file: File) {
    data class Snapshot(
        @SerializedName("savedAt") val savedAt: Long,
        @SerializedName("response") val response: FuelApiResponse
    )

    private val gson = Gson()

    fun read(): Snapshot? = try {
        if (!file.isFile) null else gson.fromJson(file.readText(), Snapshot::class.java)
            ?.takeIf { it.savedAt > 0 && !it.response.stations.isNullOrEmpty() }
    } catch (_: Exception) {
        null // A damaged cache must never prevent an online recovery.
    }

    fun write(snapshot: Snapshot) {
        file.parentFile?.mkdirs()
        val temporary = File(file.path + ".tmp")
        try {
            temporary.outputStream().use { stream ->
                stream.write(gson.toJson(snapshot).toByteArray(Charsets.UTF_8))
                stream.fd.sync()
            }
            Files.move(temporary.toPath(), file.toPath(),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            temporary.delete()
        }
    }
}
