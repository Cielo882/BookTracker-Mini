package com.cielo.applibros.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cielo.applibros.R
import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.model.ReadingStatus

class SearchBookAdapter(
    private val onAddBook: (Book, ReadingStatus) -> Unit,
    private val existingBookIds: Set<Int> = emptySet()
) : ListAdapter<Book, SearchBookAdapter.SearchBookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchBookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_book, parent, false)
        return SearchBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchBookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SearchBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        private val authorTextView: TextView = itemView.findViewById(R.id.tvAuthor)
        private val subjectsTextView: TextView = itemView.findViewById(R.id.tvSubjects)
        private val coverImageView: ImageView = itemView.findViewById(R.id.ivCover)
        private val btnAdd: Button = itemView.findViewById(R.id.btnAdd)
        private val statusLabel: TextView? = itemView.findViewById(R.id.tvStatus)

        fun bind(book: Book) {
            val context = itemView.context

            titleTextView.text = book.title
            authorTextView.text = book.authorsString
            subjectsTextView.text = book.subjects.take(3).joinToString(", ")

            book.imageUrl?.let { url ->
                Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.ic_book)
                    .into(coverImageView)
            } ?: run {
                coverImageView.setImageResource(R.drawable.ic_book)
            }

            val bookExists = existingBookIds.contains(book.id)

            if (bookExists) {
                // ✅ Libro ya existe - texto traducido
                btnAdd.text = context.getString(R.string.adapter_book_added)
                btnAdd.isEnabled = false
                btnAdd.alpha = 0.6f
                btnAdd.setBackgroundColor(context.getColor(R.color.text_secondary))

                // ✅ Label de estado traducido
                statusLabel?.text = context.getString(R.string.adapter_already_added)
                statusLabel?.visibility = View.VISIBLE
            } else {
                // ✅ Libro no existe - botón habilitado
                btnAdd.text = context.getString(R.string.adapter_add_button)
                btnAdd.isEnabled = true
                btnAdd.alpha = 1f
                btnAdd.setBackgroundColor(context.getColor(R.color.brown_dark))
                statusLabel?.visibility = View.GONE
            }

            btnAdd.setOnClickListener { view ->
                if (!bookExists) {
                    showAddMenuPopup(view, book)
                }
            }
        }

        // ✅ MÉTODO CORREGIDO con string resources
        private fun showAddMenuPopup(view: View, book: Book) {
            val context = itemView.context
            val popupMenu = PopupMenu(context, view)

            popupMenu.menu.apply {
                // ✅ Opciones del menú traducidas
                add(context.getString(R.string.menu_want_to_read)).setOnMenuItemClickListener {
                    onAddBook(book, ReadingStatus.TO_READ)
                    true
                }
                add(context.getString(R.string.menu_reading)).setOnMenuItemClickListener {
                    onAddBook(book, ReadingStatus.READING)
                    true
                }
                add(context.getString(R.string.menu_finished)).setOnMenuItemClickListener {
                    onAddBook(book, ReadingStatus.FINISHED)
                    true
                }
            }
            popupMenu.show()
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}