package com.midterm.danhba.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.midterm.danhba.databinding.ActivityContactDetailBinding
import com.midterm.danhba.MainActivity
import com.midterm.danhba.R
import com.midterm.danhba.model.Contact
import com.midterm.danhba.model.ContactGroup

class ContactDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityContactDetailBinding
    private var contact: Contact? = null
    private var busy = false
    private val id get() = intent.getStringExtra("id") ?: ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        insets(binding.root)
        binding.bottomNavigation.apply {
            selectedItemId = intent.getIntExtra("tab", R.id.navContacts)
            fun navigate(tab: Int) {
                startActivity(Intent(this@ContactDetailActivity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra("tab", tab))
                finish()
            }
            setOnItemSelectedListener { item -> navigate(item.itemId); true }
            setOnItemReselectedListener { item -> navigate(item.itemId) }
        }
        binding.back.setOnClickListener { if (!busy) finish() }
        binding.edit.setOnClickListener { startActivity(Intent(this, ContactFormActivity::class.java).putExtra("id", id)) }
        binding.delete.setOnClickListener { contact?.let { c ->
            MaterialAlertDialogBuilder(this).setTitle(R.string.delete_contact).setMessage(getString(R.string.delete_message, c.name))
                .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.delete_contact) { _, _ ->
                    if (!busy) { busy = true; controls(false); repository.delete(id) { result -> if (alive()) {
                        busy = false; controls(true); result.onSuccess { message(R.string.deleted); finish() }.onFailure { message(R.string.save_error) }
                    } } }
                }.show()
        } }
        binding.favorite.setOnClickListener { if (!busy) {
            busy = true; controls(false); repository.toggleFavorite(id) { result -> if (alive()) {
                busy = false; controls(true); result.onSuccess { rows -> contact = rows.firstOrNull { it.id == id }; render() }.onFailure { message(R.string.save_error) }
            } }
        } }
        binding.call.setOnClickListener { launch(Intent.ACTION_DIAL, "tel") }
        binding.block.setOnClickListener {
            val c = contact ?: return@setOnClickListener
            if (busy) return@setOnClickListener
            if (c.isBlocked) toggleBlocked() else {
                MaterialAlertDialogBuilder(this).setTitle(R.string.block_number)
                    .setMessage(getString(R.string.block_message, c.phone))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.block_number) { _, _ -> toggleBlocked() }.show()
            }
        }
        binding.sms.setOnClickListener { launch(Intent.ACTION_SENDTO, "smsto") }
        controls(false)
    }
    override fun onResume() {
        super.onResume()
        repository.list { result -> if (alive()) {
            result.onSuccess { contact = it.firstOrNull { c -> c.id == id }; render(); controls(!busy) }
                .onFailure { message(R.string.load_error); finish() }
        } }
    }
    private fun controls(enabled: Boolean) {
        listOf(binding.edit, binding.delete, binding.favorite, binding.block).forEach { it.isEnabled = enabled }
        listOf(binding.call, binding.sms).forEach { it.isEnabled = enabled && contact?.isBlocked != true }
    }
    private fun toggleBlocked() {
        if (busy || contact == null) return
        busy = true
        controls(false)
        repository.toggleBlocked(id) { result -> if (alive()) {
            busy = false
            result.onSuccess { rows -> contact = rows.firstOrNull { it.id == id }; render() }
                .onFailure { message(R.string.save_error) }
            controls(true)
        } }
    }
    private fun render() {
        val c = contact ?: run { message(R.string.contact_missing); finish(); return }
        binding.contactName.text = c.name
        binding.avatar.text = c.name.take(1).uppercase()
        binding.contactPhone.text = c.phone
        binding.contactGroup.text = resources.getStringArray(R.array.contact_groups)[ContactGroup.index(c.group)]
        binding.contactNote.text = c.note.ifBlank { getString(R.string.not_provided) }
        binding.favorite.setText(if (c.isFavorite) R.string.remove_favorite else R.string.add_favorite)
        binding.block.setText(if (c.isBlocked) R.string.unblock_number else R.string.block_number)
        binding.blockedStatus.visibility = if (c.isBlocked) View.VISIBLE else View.GONE
    }
    private fun launch(action: String, scheme: String) {
        val c = contact ?: return
        if (c.isBlocked) { message(R.string.blocked_action); return }
        try { startActivity(Intent(action, Uri.fromParts(scheme, c.phone, null))) }
        catch (_: ActivityNotFoundException) { message(R.string.no_handler) }
        catch (_: SecurityException) { message(R.string.no_handler) }
    }
}
