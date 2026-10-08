package com.midterm.danhba

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.midterm.danhba.model.Contact
import com.midterm.danhba.model.ContactGroup
import com.midterm.danhba.ui.*
import com.midterm.danhba.util.ContactSearch

class MainActivity : BaseActivity() {
    private var contacts = emptyList<Contact>()
    private lateinit var adapter: ContactAdapter
    private lateinit var search: EditText
    private lateinit var groups: Spinner
    private lateinit var favorites: CheckBox
    private lateinit var status: TextView
    private var loading = true
    private var failed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main); insets()
        search = findViewById(R.id.search); groups = findViewById(R.id.groups)
        favorites = findViewById(R.id.favoritesOnly); status = findViewById(R.id.status)
        groups.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.filter_groups))
        adapter = ContactAdapter { startActivity(Intent(this, ContactDetailActivity::class.java).putExtra("id", it.id)) }
        findViewById<RecyclerView>(R.id.contacts).apply { layoutManager = LinearLayoutManager(this@MainActivity); adapter = this@MainActivity.adapter }
        search.setText(savedInstanceState?.getString("query") ?: "")
        groups.setSelection(savedInstanceState?.getInt("group") ?: 0)
        favorites.isChecked = savedInstanceState?.getBoolean("favorites") ?: false
        search.doAfterTextChanged { render() }; favorites.setOnCheckedChangeListener { _, _ -> render() }
        groups.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = render()
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        findViewById<View>(R.id.addContact).setOnClickListener { startActivity(Intent(this, ContactFormActivity::class.java)) }
        findViewById<View>(R.id.retry).setOnClickListener { reload() }
    }
    override fun onResume() { super.onResume(); reload() }
    private fun reload() {
        loading = true; failed = false; render()
        repository.list { result -> if (alive()) {
            loading = false; failed = result.isFailure
            result.onSuccess { contacts = it }; render()
        } }
    }
    private fun render() {
        val selected = groups.selectedItemPosition
        val rows = contacts.filter { ContactSearch.matches(it, search.text.toString()) &&
            (selected <= 0 || it.group == ContactGroup.entries[selected - 1].key) && (!favorites.isChecked || it.isFavorite) }
            .sortedWith(compareBy<Contact> { ContactSearch.normalizeName(it.name) }.thenBy { it.id })
        adapter.submit(if (failed || loading) emptyList() else rows)
        status.visibility = if (loading || failed || rows.isEmpty()) View.VISIBLE else View.GONE
        status.setText(when { loading -> R.string.loading; failed -> R.string.load_error; contacts.isEmpty() -> R.string.empty_contacts; else -> R.string.no_results })
        findViewById<View>(R.id.retry).visibility = if (failed) View.VISIBLE else View.GONE
        findViewById<View>(R.id.addContact).isEnabled = !loading && !failed
        findViewById<TextView>(R.id.count).text = getString(R.string.contact_count, if (failed || loading) 0 else rows.size)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("query", search.text.toString()); outState.putInt("group", groups.selectedItemPosition)
        outState.putBoolean("favorites", favorites.isChecked); super.onSaveInstanceState(outState)
    }
}
