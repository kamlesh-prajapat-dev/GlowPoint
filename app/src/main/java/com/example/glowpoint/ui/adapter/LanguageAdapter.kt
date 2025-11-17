package com.example.glowpoint.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.glowpoint.databinding.ItemLanguageBinding

class LanguageAdapter(private val languages: List<Pair<String, String>>, private val onLanguageSelected: (String) -> Unit) :
    RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(languages[position])
    }

    override fun getItemCount(): Int = languages.size

    inner class LanguageViewHolder(private val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(language: Pair<String, String>) {
            binding.languageName.text = language.first
            binding.root.setOnClickListener {
                onLanguageSelected(language.second)
            }
        }
    }
}