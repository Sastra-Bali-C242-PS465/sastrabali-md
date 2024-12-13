package com.example.sabi.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sabi.R
import com.example.sabi.data.response.DictionaryItem
import com.example.sabi.data.response.QuizGroup

class DictionaryAdapter(
    private val dictionaries: List<DictionaryItem>,
    private val onItemClick: (DictionaryItem) -> Unit
) : RecyclerView.Adapter<DictionaryAdapter.DictionaryViewHolder>() {

    inner class DictionaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_dictionary_title)
        val thumbnail: ImageView = itemView.findViewById(R.id.iv_dictionary_photo)

        fun bind(dictionary: DictionaryItem) {
            title.text = dictionary.title
            Glide.with(itemView.context).load(dictionary.thumbnailUrl).into(thumbnail)
            itemView.setOnClickListener {
                onItemClick(dictionary)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DictionaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_dictionary, parent, false)
        return DictionaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DictionaryViewHolder, position: Int) {
        holder.bind(dictionaries[position])
    }

    override fun getItemCount(): Int = dictionaries.size
}
