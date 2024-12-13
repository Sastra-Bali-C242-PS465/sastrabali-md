package com.example.sabi.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sabi.R
import com.example.sabi.data.response.QuizGroup

class QuizAdapter(
    private var quizList: List<QuizGroup>,
    private val onItemClick: ((QuizGroup) -> Unit)? = null
) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumber: TextView = itemView.findViewById(R.id.tv_quiz_number)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_quiz_title)
        val tvTotalQuestions: TextView = itemView.findViewById(R.id.tv_total_quiz)
        val ivPhoto: ImageView = itemView.findViewById(R.id.iv_quiz_photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_quiz, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizList[position]
        holder.tvNumber.text = (position + 1).toString()
        holder.tvTitle.text = quiz.title
        holder.tvTotalQuestions.text = "5 Soal"

        Glide.with(holder.ivPhoto.context)
            .load(quiz.thumbnailUrl)
            .placeholder(R.drawable.image_placeholder)
            .into(holder.ivPhoto)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(quiz)
        }
    }

    override fun getItemCount(): Int = quizList.size

    fun updateData(newQuizList: List<QuizGroup>) {
        quizList = newQuizList
        notifyDataSetChanged()
    }
}
