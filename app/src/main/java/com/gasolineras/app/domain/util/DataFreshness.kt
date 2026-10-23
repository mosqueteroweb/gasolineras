package com.gasolineras.app.domain.util

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DataFreshness {
    fun isPublicationOld(value: String?, now: Long = System.currentTimeMillis()): Boolean {
        if (value.isNullOrBlank()) return false
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "ES")).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("Europe/Madrid")
        }
        val position = ParsePosition(0)
        val date = formatter.parse(value, position) ?: return false
        return position.index == value.length && now - date.time > 86_400_000L
    }
}
