package com.midterm.danhba.model

/*
MSSV: 123220163
Ten: Truong Dinh Luu
Lop: 22PFIEV3
 */

data class Contact(val id: String, val name: String, val phone: String, val email: String = "", val group: String = "other", val note: String = "", val isFavorite: Boolean = false, val isBlocked: Boolean = false)

enum class ContactGroup(val key: String) {
    FAMILY("family"), FRIENDS("friends"), WORK("work"), OTHER("other");
    companion object { fun index(key: String) = entries.indexOfFirst { it.key == key }.coerceAtLeast(0) }
}
