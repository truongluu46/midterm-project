package com.midterm.danhba.util

import com.midterm.danhba.model.Contact

object ContactValidator {
    fun validPhone(value: String): Boolean {
        if (!Regex("[+0-9()\\s.-]+").matches(value)) return false
        return Regex("\\+?[0-9]{3,15}").matches(ContactSearch.normalizePhone(value))
    }
    fun duplicatePhone(phone: String, id: String, contacts: List<Contact>): Boolean =
        contacts.any { it.id != id && ContactSearch.normalizePhone(it.phone) == ContactSearch.normalizePhone(phone) }
}
