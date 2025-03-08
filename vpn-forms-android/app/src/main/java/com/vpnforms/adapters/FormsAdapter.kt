package com.vpnforms.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vpnforms.R
import com.vpnforms.models.Form

class FormsAdapter(
    private val forms: List<Form>,
    private val onFormClick: (String) -> Unit
) : RecyclerView.Adapter<FormsAdapter.FormViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_form, parent, false)
        return FormViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormViewHolder, position: Int) {
        val form = forms[position]
        holder.bind(form)
        holder.itemView.setOnClickListener { onFormClick(form.url) }
    }

    override fun getItemCount(): Int = forms.size

    class FormViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.formTitleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.formDescriptionTextView)

        fun bind(form: Form) {
            titleTextView.text = form.title
            descriptionTextView.text = form.description
        }
    }
}
