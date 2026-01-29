package com.cielo.applibros.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cielo.applibros.R
import com.cielo.applibros.domain.model.Book

class BookStatsAdapter : ListAdapter<Pair<Book, Int>, BookStatsAdapter.StatsViewHolder>(StatsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_stats, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDays: TextView = itemView.findViewById(R.id.tvDays)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)

        fun bind(data: Pair<Book, Int>) {
            val (book, days) = data

            tvTitle.text = book.title

            // ✅ Usando string resource con parámetro
            tvDays.text = itemView.context.getString(R.string.stats_days, days)

            // ✅ Usando string resources
            tvRating.text = book.rating?.let {
                itemView.context.getString(R.string.stats_rating, it)
            } ?: itemView.context.getString(R.string.stats_no_rating)
        }
    }

    class StatsDiffCallback : DiffUtil.ItemCallback<Pair<Book, Int>>() {
        override fun areItemsTheSame(oldItem: Pair<Book, Int>, newItem: Pair<Book, Int>): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(oldItem: Pair<Book, Int>, newItem: Pair<Book, Int>): Boolean {
            return oldItem == newItem
        }
    }
}