package com.midterm.danhba
/*
MSSV: 123220163
Ten: Truong Dinh Luu
Lop: 22PFIEV3
 */

import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm.danhba.model.Contact
import com.midterm.danhba.model.ContactGroup
import com.midterm.danhba.ui.*
import com.midterm.danhba.util.ContactSearch
import com.midterm.danhba.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton

class MainActivity : BaseActivity() {
    private var contacts = emptyList<Contact>()
    private lateinit var adapter: ContactAdapter
    private lateinit var binding: ActivityMainBinding
    private val search get() = binding.search
    private val groups get() = binding.groups
    private var selectedTab = R.id.navContacts
    private val dialNumber get() = binding.dialNumber
    private val status get() = binding.status
    private var loading = true
    private var failed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        insets(binding.root)
        dialNumber.text = savedInstanceState?.getString("dialNumber") ?: ""
        selectedTab = savedInstanceState?.getInt("tab") ?: intent.getIntExtra("tab", R.id.navContacts)
        setupDialpad()
        groups.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.filter_groups))
        adapter = ContactAdapter { startActivity(Intent(this, ContactDetailActivity::class.java).putExtra("id", it.id).putExtra("tab", selectedTab)) }
        binding.contacts.apply { layoutManager = LinearLayoutManager(this@MainActivity); adapter = this@MainActivity.adapter }
        search.setText(savedInstanceState?.getString("query") ?: "")
        groups.setSelection(savedInstanceState?.getInt("group") ?: 0)
        search.doAfterTextChanged { render() }
        groups.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = render()
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.addContact.setOnClickListener { startActivity(Intent(this, ContactFormActivity::class.java)) }
        binding.retry.setOnClickListener { reload() }
        binding.bottomNavigation.apply {
            selectedItemId = selectedTab
            setOnItemSelectedListener {
                selectedTab = it.itemId
                render()
                true
            }
        }
        render()
    }
    override fun onResume() { super.onResume(); reload() }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasExtra("tab")) {
            selectedTab = intent.getIntExtra("tab", R.id.navContacts)
            binding.bottomNavigation.selectedItemId = selectedTab
            render()
        }
    }
    private fun reload() {
        loading = true; failed = false; render()
        repository.list { result -> if (alive()) {
            loading = false; failed = result.isFailure
            result.onSuccess { contacts = it }; render()
        } }
    }
    private fun render() {
        val isDialpad = selectedTab == R.id.navDialpad
        val isFavorites = selectedTab == R.id.navFavorites
        binding.contactContent.visibility = if (isDialpad) View.GONE else View.VISIBLE
        binding.dialpadContent.visibility = if (isDialpad) View.VISIBLE else View.GONE
        binding.addContact.visibility = if (selectedTab == R.id.navContacts) View.VISIBLE else View.GONE
        binding.title.setText(when (selectedTab) {
            R.id.navFavorites -> R.string.tab_favorites
            R.id.navDialpad -> R.string.tab_dialpad
            else -> R.string.title
        })
        val selected = groups.selectedItemPosition
        val rows = contacts.filter { ContactSearch.matches(it, search.text.toString()) &&
            (selected <= 0 || it.group == ContactGroup.entries[selected - 1].key) && (!isFavorites || it.isFavorite) }
            .sortedWith(compareBy<Contact> { ContactSearch.normalizeName(it.name) }.thenBy { it.id })
        adapter.submit(if (failed || loading) emptyList() else rows)
        status.visibility = if (loading || failed || rows.isEmpty()) View.VISIBLE else View.GONE
        status.setText(when { loading -> R.string.loading; failed -> R.string.load_error; isFavorites && contacts.none { it.isFavorite } -> R.string.empty_favorites; contacts.isEmpty() -> R.string.empty_contacts; else -> R.string.no_results })
        binding.retry.visibility = if (failed) View.VISIBLE else View.GONE
        binding.addContact.isEnabled = !loading && !failed
        binding.count.text = getString(R.string.contact_count, if (failed || loading) 0 else rows.size)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("query", search.text.toString()); outState.putInt("group", groups.selectedItemPosition)
        outState.putInt("tab", selectedTab)
        outState.putString("dialNumber", dialNumber.text.toString())
        super.onSaveInstanceState(outState)
    }
    private fun setupDialpad() {
        val keys = binding.dialKeys
        val call = binding.dialCall
        val delete = binding.dialDelete
        fun updateActions() {
            call.isEnabled = dialNumber.text.isNotEmpty()
            delete.isEnabled = dialNumber.text.isNotEmpty()
        }
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("*", "0", "#")).forEach { digits ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            digits.forEach { digit ->
                row.addView(MaterialButton(this).apply {
                    text = digit
                    textSize = 24f
                    layoutParams = LinearLayout.LayoutParams(0, (64 * resources.displayMetrics.density).toInt(), 1f)
                    setOnClickListener {
                        if (dialNumber.text.length < 30) dialNumber.append(digit)
                        updateActions()
                    }
                    if (digit == "0") setOnLongClickListener {
                        if (dialNumber.text.isEmpty()) dialNumber.text = "+"
                        updateActions()
                        true
                    }
                })
            }
            keys.addView(row)
        }
        delete.setOnClickListener {
            dialNumber.text = dialNumber.text.dropLast(1)
            updateActions()
        }
        delete.setOnLongClickListener {
            dialNumber.text = ""
            updateActions()
            true
        }
        call.setOnClickListener {
            if (loading || failed) { message(if (failed) R.string.load_error else R.string.loading); return@setOnClickListener }
            val number = ContactSearch.normalizePhone(dialNumber.text.toString())
            if (contacts.any { it.isBlocked && ContactSearch.normalizePhone(it.phone) == number }) {
                message(R.string.blocked_action)
                return@setOnClickListener
            }
            try {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", dialNumber.text.toString(), null)))
            } catch (_: ActivityNotFoundException) {
                message(R.string.no_handler)
            }
        }
        updateActions()
    }
}
