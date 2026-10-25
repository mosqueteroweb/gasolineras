package com.gasolineras.app.domain.util

import java.text.Normalizer
import java.util.Locale

/** The Ministry publishes addresses and places in capitals; this makes them easier to read. */
object ReadableText {
    private val spanish = Locale("es", "ES")
    private val connectors = setOf("de", "del", "la", "las", "los", "el", "y", "e", "en", "a", "al")
    private val romanNumeral = Regex("^(I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII)$")

    fun toReadableCase(text: String): String {
        if (text.isBlank() || !mostlyCapitals(text)) return text.trim()
        var first = true
        return text.trim().split(' ').filter { it.isNotEmpty() }.joinToString(" ") { word ->
            val readable = readableWord(word, first)
            first = false
            readable
        }
    }

    // Some Ministry entries mix a lowercase unit into a capitalised address ("AVDA. ... KM 4,700 km").
    private fun mostlyCapitals(text: String): Boolean {
        val letters = text.filter { it.isLetter() }
        return letters.isNotEmpty() && letters.count { it.isUpperCase() } * 10 >= letters.length * 8
    }

    private fun readableWord(word: String, first: Boolean): String {
        val letters = word.filter { it.isLetter() }
        return when {
            // Road codes (M-30, N-II, A6), kilometre points and "S/N" keep their capitals.
            word.any { it.isDigit() } || word.contains('/') || romanNumeral.matches(letters) -> word
            word.none { it.isUpperCase() } -> word
            !first && word.lowercase(spanish) in connectors -> word.lowercase(spanish)
            word.contains('-') -> word.split('-').joinToString("-") { part ->
                // Road prefixes (N-II, A-VI) keep their capitals; compound names (Castilla-La Mancha) do not.
                if (part.length == 1 || romanNumeral.matches(part)) part
                else part.lowercase(spanish).replaceFirstChar { it.titlecase(spanish) }
            }
            else -> word.lowercase(spanish).replaceFirstChar { it.titlecase(spanish) }
        }
    }

    /** "Madrid" for Madrid (MADRID); "Getafe (Madrid)" when the province is a different place. */
    fun place(municipality: String, province: String): String {
        val town = toReadableCase(municipality)
        val region = toReadableCase(province)
        return if (region.isBlank() || sameName(town, region)) town else "$town ($region)"
    }

    private fun sameName(a: String, b: String): Boolean {
        fun plain(s: String) = Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "").lowercase(spanish).trim()
        return plain(a) == plain(b)
    }
}
