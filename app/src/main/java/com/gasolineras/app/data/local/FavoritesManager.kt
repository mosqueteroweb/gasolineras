package com.gasolineras.app.data.local

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("gasolineras_preferences", Context.MODE_PRIVATE)

    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(stationId: String): Boolean {
        return getFavoriteIds().contains(stationId)
    }

    fun toggleFavorite(stationId: String): Boolean {
        val current = getFavoriteIds().toMutableSet()
        val isNowFavorite = if (current.contains(stationId)) {
            current.remove(stationId)
            false
        } else {
            current.add(stationId)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return isNowFavorite
    }

    companion object {
        private const val KEY_FAVORITES = "favorite_station_ids"
    }
}
