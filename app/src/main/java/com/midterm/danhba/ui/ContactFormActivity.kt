package com.midterm.danhba.ui

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.midterm.danhba.R
import com.midterm.danhba.model.Contact
import com.midterm.danhba.model.ContactGroup
import com.midterm.danhba.util.ContactValidator
import java.util.UUID

class ContactFormActivity : BaseActivity() {
    private lateinit var name: EditText
    private lateinit var phone: EditText
    private lateinit var email: EditText
    private lateinit var note: EditText
    private lateinit var group: Spinner
    private lateinit var favorite: CheckBox
    private lateinit var save: View
    private lateinit var id: String
    private var original: Contact? = null
    private var contacts = emptyList<Contact>()
    private var ready = false
    private var saving = false
    private var restored: Bundle? = null
    private fun draft() = Contact(id, name.text.toString().trim(), phone.text.toString().trim(), email.text.toString().trim(),
        ContactGroup.entries[group.selectedItemPosition.coerceAtLeast(0)].key, note.text.toString().trim(), favorite.isChecked)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_contact_form); insets()
        restored = savedInstanceState
        id = savedInstanceState?.getString("id") ?: intent.getStringExtra("id") ?: UUID.randomUUID().toString()
        name = findViewById(R.id.name); phone = findViewById(R.id.phone); email = findViewById(R.id.email)
        note = findViewById(R.id.note); group = findViewById(R.id.group); favorite = findViewById(R.id.favorite); save = findViewById(R.id.save)
        group.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.contact_groups))
        findViewById<TextView>(R.id.title).setText(if (intent.hasExtra("id")) R.string.edit_contact else R.string.add_contact)
        findViewById<View>(R.id.back).setOnClickListener { leave() }
        save.setOnClickListener { submit() }; save.isEnabled = false
        listOf<View>(name, phone, email, note, group, favorite).forEach { it.isEnabled = false }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) { override fun handleOnBackPressed() = leave() })
        repository.list { result -> if (alive()) {
            result.onSuccess { rows ->
                contacts = rows; original = rows.firstOrNull { it.id == id }
                if (intent.hasExtra("id") && original == null) { message(R.string.contact_missing); finish(); return@onSuccess }
                val b = restored
                val value = if (b != null) Contact(id, b.getString("name", ""), b.getString("phone", ""), b.getString("email", ""),
                    b.getString("group", "other"), b.getString("note", ""), b.getBoolean("favorite")) else original ?: Contact(id, "", "")
                name.setText(value.name); phone.setText(value.phone); email.setText(value.email); note.setText(value.note)
                group.setSelection(ContactGroup.index(value.group)); favorite.isChecked = value.isFavorite
                ready = true; save.isEnabled = true
                listOf<View>(name, phone, email, note, group, favorite).forEach { it.isEnabled = true }
            }.onFailure { message(R.string.load_error); finish() }
        } }
    }
    private fun leave() {
        if (saving) return
        if (ready && draft() != (original ?: Contact(id, "", ""))) {
            MaterialAlertDialogBuilder(this).setTitle(R.string.unsaved_title).setMessage(R.string.unsaved_message)
                .setNegativeButton(R.string.stay, null).setPositiveButton(R.string.discard) { _, _ -> finish() }.show()
        } else finish()
    }
    private fun submit() {
        if (!ready || saving) return
        val c = draft(); name.error = null; phone.error = null; email.error = null
        var valid = true
        if (c.name.isBlank()) { name.error = getString(R.string.name_required); valid = false }
        if (!ContactValidator.validPhone(c.phone)) { phone.error = getString(R.string.phone_invalid); valid = false }
        else if (ContactValidator.duplicatePhone(c.phone, c.id, contacts)) { phone.error = getString(R.string.phone_duplicate); valid = false }
        if (c.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(c.email).matches()) { email.error = getString(R.string.email_invalid); valid = false }
        if (!valid) return
        saving = true; save.isEnabled = false
        repository.save(c) { result -> if (alive()) {
            saving = false; save.isEnabled = true
            result.onSuccess { message(R.string.saved); finish() }.onFailure {
                if (it.message == "duplicate") phone.error = getString(R.string.phone_duplicate) else message(R.string.save_error)
            }
        } }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        val c = if (ready) draft() else null
        if (c != null) {
            outState.putString("id", c.id); outState.putString("name", c.name); outState.putString("phone", c.phone)
            outState.putString("email", c.email); outState.putString("group", c.group); outState.putString("note", c.note)
            outState.putBoolean("favorite", c.isFavorite)
        } else restored?.let { outState.putAll(it) }
        super.onSaveInstanceState(outState)
    }
}
