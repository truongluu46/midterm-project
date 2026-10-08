package com.midterm.danhba.util

import com.midterm.danhba.model.Contact
import java.text.Normalizer
import java.util.Locale

object ContactSearch {
    fun normalizeName(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).replace('đ', 'd').trim()
    fun normalizePhone(value: String): String = value.filter { it.isDigit() || it == '+' }
    fun matches(contact: Contact, query: String): Boolean {
        if (query.isBlank()) return true
        val digits = normalizePhone(query)
        return normalizeName(contact.name).contains(normalizeName(query)) ||
            (digits.isNotEmpty() && normalizePhone(contact.phone).contains(digits))
    }
}
