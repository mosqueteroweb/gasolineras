package com.gasolineras.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.SortOption

class UserPreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("gasolineras_user_preferences", Context.MODE_PRIVATE)

    fun getVisibleFuels(): List<FuelType> {
        val saved = prefs.getString(KEY_VISIBLE_FUELS, null) ?: return DEFAULT_FUELS
        val names = saved.split(",").filter { it.isNotBlank() }
        val list = names.mapNotNull { name ->
            try { FuelType.valueOf(name) } catch (e: IllegalArgumentException) { null }
        }
        return if (list.isEmpty()) DEFAULT_FUELS else list.take(3)
    }

    fun setVisibleFuels(fuels: List<FuelType>) {
        val validList = fuels.distinct().take(3).ifEmpty { DEFAULT_FUELS }
        val serialized = validList.joinToString(",") { it.name }
        prefs.edit().putString(KEY_VISIBLE_FUELS, serialized).apply()

        // Ensure default selected fuels are within visible fuels
        val currentSelected = getDefaultSelectedFuels()
        val reconciled = currentSelected.filter { validList.contains(it) }.toSet()
        val finalSelected = if (reconciled.isEmpty()) setOf(validList.first()) else reconciled
        setDefaultSelectedFuels(finalSelected)
    }

    fun getDefaultSelectedFuels(): Set<FuelType> {
        val saved = prefs.getString(KEY_DEFAULT_SELECTED_FUELS, null)
        val visible = getVisibleFuels()
        if (saved == null) return visible.toSet()

        val names = saved.split(",").filter { it.isNotBlank() }
        val list = names.mapNotNull { name ->
            try { FuelType.valueOf(name) } catch (e: IllegalArgumentException) { null }
        }.filter { visible.contains(it) }.take(3).toSet()

        return if (list.isEmpty()) setOf(visible.first()) else list
    }

    fun setDefaultSelectedFuels(fuels: Set<FuelType>) {
        val visible = getVisibleFuels()
        val filtered = fuels.filter { visible.contains(it) }.take(3).toSet()
        val finalList = if (filtered.isEmpty()) setOf(visible.first()) else filtered
        val serialized = finalList.joinToString(",") { it.name }
        prefs.edit().putString(KEY_DEFAULT_SELECTED_FUELS, serialized).apply()
    }

    fun getDefaultView(): String {
        return prefs.getString(KEY_DEFAULT_VIEW, "LIST") ?: "LIST"
    }

    fun setDefaultView(view: String) {
        val sanitized = if (view == "MAP") "MAP" else "LIST"
        prefs.edit().putString(KEY_DEFAULT_VIEW, sanitized).apply()
    }

    fun getDefaultRadiusKm(): Double {
        val value = prefs.getFloat(KEY_DEFAULT_RADIUS, 10.0f).toDouble()
        return if (value in listOf(3.0, 10.0, 25.0, 100.0)) value else 10.0
    }

    fun setDefaultRadiusKm(radiusKm: Double) {
        prefs.edit().putFloat(KEY_DEFAULT_RADIUS, radiusKm.toFloat()).apply()
    }

    fun getDefaultSort(): SortOption {
        val name = prefs.getString(KEY_DEFAULT_SORT, SortOption.CHEAPEST.name)
        return try {
            SortOption.valueOf(name ?: SortOption.CHEAPEST.name)
        } catch (e: IllegalArgumentException) {
            SortOption.CHEAPEST
        }
    }

    fun setDefaultSort(sort: SortOption) {
        prefs.edit().putString(KEY_DEFAULT_SORT, sort.name).apply()
    }

    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
    }

    fun setThemeMode(mode: String) {
        val sanitized = when (mode) {
            "LIGHT", "DARK" -> mode
            else -> "SYSTEM"
        }
        prefs.edit().putString(KEY_THEME_MODE, sanitized).apply()
    }

    companion object {
        val DEFAULT_FUELS = listOf(FuelType.GASOLEO_A, FuelType.GASOLINA_95_E5, FuelType.GLP)

        private const val KEY_VISIBLE_FUELS = "pref_visible_fuels"
        private const val KEY_DEFAULT_SELECTED_FUELS = "pref_default_selected_fuels"
        private const val KEY_DEFAULT_VIEW = "pref_default_view"
        private const val KEY_DEFAULT_RADIUS = "pref_default_radius"
        private const val KEY_DEFAULT_SORT = "pref_default_sort"
        private const val KEY_THEME_MODE = "pref_theme_mode"
    }
}
