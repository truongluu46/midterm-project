package com.midterm.danhba.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.midterm.danhba.R
import com.midterm.danhba.databinding.ActivityContactFormBinding
import com.midterm.danhba.model.Contact
import com.midterm.danhba.model.ContactGroup
import com.midterm.danhba.util.ContactValidator
import java.util.UUID

class ContactFormActivity : BaseActivity() {
    private lateinit var binding: ActivityContactFormBinding
    private val name get() = binding.name
    private val phone get() = binding.phone
    private val note get() = binding.note
    private val group get() = binding.group
    private val favorite get() = binding.favorite
    private val save get() = binding.save
    private lateinit var id: String
    private var original: Contact? = null
    private var contacts = emptyList<Contact>()
    private var ready = false
    private var saving = false
    private var restored: Bundle? = null
    private fun draft() = Contact(id, name.text.toString().trim(), phone.text.toString().trim(), original?.email ?: "",
        ContactGroup.entries[group.selectedItemPosition.coerceAtLeast(0)].key, note.text.toString().trim(), favorite.isChecked, original?.isBlocked ?: false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        insets(binding.root)
        restored = savedInstanceState
        id = savedInstanceState?.getString("id") ?: intent.getStringExtra("id") ?: UUID.randomUUID().toString()
        group.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.contact_groups))
        binding.title.setText(if (intent.hasExtra("id")) R.string.edit_contact else R.string.add_contact)
        binding.back.setOnClickListener { leave() }
        save.setOnClickListener { submit() }; save.isEnabled = false
        listOf<View>(name, phone, note, group, favorite).forEach { it.isEnabled = false }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) { override fun handleOnBackPressed() = leave() })
        repository.list { result -> if (alive()) {
            result.onSuccess { rows ->
                contacts = rows; original = rows.firstOrNull { it.id == id }
                if (intent.hasExtra("id") && original == null) { message(R.string.contact_missing); finish(); return@onSuccess }
                val b = restored
                val value = if (b != null) Contact(id, b.getString("name", ""), b.getString("phone", ""), b.getString("email", ""),
                    b.getString("group", "other"), b.getString("note", ""), b.getBoolean("favorite")) else original ?: Contact(id, "", "")
                name.setText(value.name); phone.setText(value.phone); note.setText(value.note)
                group.setSelection(ContactGroup.index(value.group)); favorite.isChecked = value.isFavorite
                ready = true; save.isEnabled = true
                listOf<View>(name, phone, note, group, favorite).forEach { it.isEnabled = true }
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
        val c = draft(); name.error = null; phone.error = null
        var valid = true
        if (c.name.isBlank()) { name.error = getString(R.string.name_required); valid = false }
        if (!ContactValidator.validPhone(c.phone)) { phone.error = getString(R.string.phone_invalid); valid = false }
        else if (ContactValidator.duplicatePhone(c.phone, c.id, contacts)) { phone.error = getString(R.string.phone_duplicate); valid = false }
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
