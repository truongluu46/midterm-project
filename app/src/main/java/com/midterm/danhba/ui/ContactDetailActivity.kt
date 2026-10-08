package com.midterm.danhba.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.midterm.danhba.R
import com.midterm.danhba.model.Contact
import com.midterm.danhba.model.ContactGroup

class ContactDetailActivity : BaseActivity() {
    private var contact: Contact? = null
    private var busy = false
    private val id get() = intent.getStringExtra("id") ?: ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_contact_detail); insets()
        findViewById<View>(R.id.back).setOnClickListener { if (!busy) finish() }
        findViewById<View>(R.id.edit).setOnClickListener { startActivity(Intent(this, ContactFormActivity::class.java).putExtra("id", id)) }
        findViewById<View>(R.id.delete).setOnClickListener { contact?.let { c ->
            MaterialAlertDialogBuilder(this).setTitle(R.string.delete_contact).setMessage(getString(R.string.delete_message, c.name))
                .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.delete_contact) { _, _ ->
                    if (!busy) { busy = true; controls(false); repository.delete(id) { result -> if (alive()) {
                        busy = false; controls(true); result.onSuccess { message(R.string.deleted); finish() }.onFailure { message(R.string.save_error) }
                    } } }
                }.show()
        } }
        findViewById<View>(R.id.favorite).setOnClickListener { if (!busy) {
            busy = true; controls(false); repository.toggleFavorite(id) { result -> if (alive()) {
                busy = false; controls(true); result.onSuccess { rows -> contact = rows.firstOrNull { it.id == id }; render() }.onFailure { message(R.string.save_error) }
            } }
        } }
        findViewById<View>(R.id.call).setOnClickListener { launch(Intent.ACTION_DIAL, "tel") }
        findViewById<View>(R.id.sms).setOnClickListener { launch(Intent.ACTION_SENDTO, "smsto") }
        controls(false)
    }
    override fun onResume() {
        super.onResume()
        repository.list { result -> if (alive()) {
            result.onSuccess { contact = it.firstOrNull { c -> c.id == id }; render(); controls(!busy) }
                .onFailure { message(R.string.load_error); finish() }
        } }
    }
    private fun controls(enabled: Boolean) { listOf(R.id.edit, R.id.delete, R.id.favorite, R.id.call, R.id.sms).forEach { findViewById<View>(it).isEnabled = enabled } }
    private fun render() {
        val c = contact ?: run { message(R.string.contact_missing); finish(); return }
        findViewById<TextView>(R.id.contactName).text = c.name
        findViewById<TextView>(R.id.avatar).text = c.name.take(1).uppercase()
        findViewById<TextView>(R.id.contactPhone).text = c.phone
        findViewById<TextView>(R.id.contactEmail).text = c.email.ifBlank { getString(R.string.not_provided) }
        findViewById<TextView>(R.id.contactGroup).text = resources.getStringArray(R.array.contact_groups)[ContactGroup.index(c.group)]
        findViewById<TextView>(R.id.contactNote).text = c.note.ifBlank { getString(R.string.not_provided) }
        findViewById<TextView>(R.id.favorite).setText(if (c.isFavorite) R.string.remove_favorite else R.string.add_favorite)
    }
    private fun launch(action: String, scheme: String) {
        val c = contact ?: return
        try { startActivity(Intent(action, Uri.fromParts(scheme, c.phone, null))) }
        catch (_: ActivityNotFoundException) { message(R.string.no_handler) }
        catch (_: SecurityException) { message(R.string.no_handler) }
    }
}
