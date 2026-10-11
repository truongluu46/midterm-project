package com.midterm.danhba.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AtomicFile
import com.midterm.danhba.model.Contact
import com.midterm.danhba.model.ContactGroup
import com.midterm.danhba.util.ContactValidator
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors

/*
MSSV: 123220163
Ten: Truong Dinh Luu
Lop: 22PFIEV3
 */
class ContactRepository internal constructor(context: Context) {
    private val file = AtomicFile(File(context.applicationContext.filesDir, "contacts.json"))
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    @Volatile private var snapshot: List<Contact>? = null
    fun list(callback: (Result<List<Contact>>) -> Unit) = task(callback) { load() }
    fun save(contact: Contact, callback: (Result<List<Contact>>) -> Unit) = task(callback) {
        val old = load()
        require(contact.name.isNotBlank() && ContactValidator.validPhone(contact.phone))
        require(!ContactValidator.duplicatePhone(contact.phone, contact.id, old)) { "duplicate" }
        commit(old.filterNot { it.id == contact.id } + contact)
    }
    fun delete(id: String, callback: (Result<List<Contact>>) -> Unit) = task(callback) {
        commit(load().filterNot { it.id == id })
    }
    fun toggleFavorite(id: String, callback: (Result<List<Contact>>) -> Unit) = task(callback) {
        commit(load().map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it })
    }
    fun toggleBlocked(id: String, callback: (Result<List<Contact>>) -> Unit) = task(callback) {
        val old = load()
        require(old.any { it.id == id })
        commit(old.map { if (it.id == id) it.copy(isBlocked = !it.isBlocked) else it })
    }
    private fun task(callback: (Result<List<Contact>>) -> Unit, action: () -> List<Contact>) {
        executor.execute { val result = runCatching(action); main.post { callback(result) } }
    }
    private fun load(): List<Contact> {
        snapshot?.let { return it }
        // openRead restores an interrupted AtomicFile write before checking absence.
        val raw = try { file.openRead().use { it.readBytes().toString(Charsets.UTF_8) } }
        catch (e: java.io.FileNotFoundException) {
            if (file.baseFile.exists() || File(file.baseFile.path + ".bak").exists()) throw e
            return emptyList<Contact>().also { snapshot = it }
        }
        val root = JSONObject(raw)
        require(root.getInt("schemaVersion") == 1)
        val rows = root.getJSONArray("contacts")
        val loaded = (0 until rows.length()).map { i ->
            val row = rows.getJSONObject(i)
            Contact(row.getString("id"), row.getString("name"), row.getString("phone"),
                row.optString("email", ""), row.getString("group"), row.getString("note"), row.getBoolean("isFavorite"), row.optBoolean("isBlocked", false))
        }
        require(loaded.map { it.id }.distinct().size == loaded.size)
        require(loaded.all { it.id.isNotBlank() && it.name.isNotBlank() && ContactValidator.validPhone(it.phone) && ContactGroup.entries.any { g -> g.key == it.group } })
        require(loaded.none { ContactValidator.duplicatePhone(it.phone, it.id, loaded) })
        return loaded.also { snapshot = it }
    }
    private fun commit(contacts: List<Contact>): List<Contact> {
        val rows = JSONArray()
        contacts.forEach { c -> rows.put(JSONObject().put("id", c.id).put("name", c.name).put("phone", c.phone)
            .put("email", c.email).put("group", c.group).put("note", c.note).put("isFavorite", c.isFavorite).put("isBlocked", c.isBlocked)) }
        val bytes = JSONObject().put("schemaVersion", 1).put("contacts", rows).toString().toByteArray(Charsets.UTF_8)
        val stream = file.startWrite()
        try { stream.write(bytes); file.finishWrite(stream) }
        catch (e: Exception) { file.failWrite(stream); throw e }
        snapshot = contacts.toList()
        return snapshot!!
    }
    companion object {
        @Volatile private var instance: ContactRepository? = null
        fun get(context: Context): ContactRepository = instance ?: synchronized(this) {
            instance ?: ContactRepository(context).also { instance = it }
        }
    }
}
