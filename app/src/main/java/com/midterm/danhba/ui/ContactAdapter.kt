package com.midterm.danhba.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.midterm.danhba.R
import com.midterm.danhba.model.Contact
import com.midterm.danhba.model.ContactGroup

class ContactAdapter(private val open: (Contact) -> Unit) : RecyclerView.Adapter<ContactAdapter.Holder>() {
    private var items = emptyList<Contact>()
    fun submit(rows: List<Contact>) { items = rows; notifyDataSetChanged() }
    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: TextView = view.findViewById(R.id.avatar)
        val name: TextView = view.findViewById(R.id.contactName)
        val phone: TextView = view.findViewById(R.id.contactPhone)
        val group: TextView = view.findViewById(R.id.contactGroup)
        val favorite: TextView = view.findViewById(R.id.favoriteMark)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false))
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val c = items[position]
        holder.avatar.text = c.name.trim().take(1).uppercase()
        holder.name.text = c.name; holder.phone.text = c.phone
        holder.group.text = holder.itemView.resources.getStringArray(R.array.contact_groups)[ContactGroup.index(c.group)]
        holder.favorite.visibility = if (c.isFavorite) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener { open(c) }
    }
}
