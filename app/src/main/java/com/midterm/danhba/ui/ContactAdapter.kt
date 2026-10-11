package com.midterm.danhba.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm.danhba.R
import com.midterm.danhba.databinding.ItemContactBinding
import com.midterm.danhba.model.Contact
import com.midterm.danhba.model.ContactGroup

class ContactAdapter(private val open: (Contact) -> Unit) : RecyclerView.Adapter<ContactAdapter.Holder>() {
    private var items = emptyList<Contact>()
    fun submit(rows: List<Contact>) { items = rows; notifyDataSetChanged() }
    class Holder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val c = items[position]
        holder.binding.avatar.text = c.name.trim().take(1).uppercase()
        holder.binding.contactName.text = c.name; holder.binding.contactPhone.text = c.phone
        holder.binding.contactGroup.text = holder.itemView.resources.getStringArray(R.array.contact_groups)[ContactGroup.index(c.group)]
        holder.binding.favoriteMark.visibility = if (c.isFavorite) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener { open(c) }
    }
}
