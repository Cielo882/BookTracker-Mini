package com.cielo.applibros.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cielo.applibros.R
import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.model.ReadingStatus

class BookAdapter(
    private val onBookClick: (Book) -> Unit,
    private val onRatingChanged: (Book, Float) -> Unit,
    private val onFavoriteClick: ((Book) -> Unit)? = null,
    private val onDeleteClick: ((Book) -> Unit)? = null
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        private val authorTextView: TextView = itemView.findViewById(R.id.tvAuthor)
        private val statusTextView: TextView = itemView.findViewById(R.id.tvStatus)
        private val coverImageView: ImageView = itemView.findViewById(R.id.ivCover)
        private val statusButton: ImageView = itemView.findViewById(R.id.ivStatus)
        private val favoriteButton: ImageView? = itemView.findViewById(R.id.ivFavorite)
        private val deleteButton: ImageView = itemView.findViewById(R.id.ivDelete)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val ratingText: TextView = itemView.findViewById(R.id.tvRatingText)
        private val reviewText: TextView? = itemView.findViewById(R.id.tvReview)

        fun bind(book: Book) {
            titleTextView.text = book.title
            authorTextView.text = book.authorsString

            // ✅ Obtener el contexto para acceder a strings
            val context = itemView.context

            // ✅ Configurar ribbon con string resources
            val ribbonText: String
            val ribbonColor: Int

            when (book.readingStatus) {
                ReadingStatus.TO_READ -> {
                    ribbonText = context.getString(R.string.status_to_read)
                    ribbonColor = R.color.ribbon_to_read
                }
                ReadingStatus.READING -> {
                    ribbonText = context.getString(R.string.status_reading)
                    ribbonColor = R.color.ribbon_reading
                }
                ReadingStatus.FINISHED -> {
                    ribbonText = context.getString(R.string.status_finished)
                    ribbonColor = R.color.ribbon_finished
                }
            }

            // Actualizar ribbon
            val tvStatusRibbon: TextView = itemView.findViewById(R.id.tvStatusRibbon)
            tvStatusRibbon.text = ribbonText
            tvStatusRibbon.setBackgroundColor(context.getColor(ribbonColor))

            // ✅ Mostrar estado del libro con string resources
            statusTextView.text = when (book.readingStatus) {
                ReadingStatus.TO_READ -> context.getString(R.string.status_to_read)
                ReadingStatus.READING -> context.getString(R.string.status_reading)
                ReadingStatus.FINISHED -> context.getString(R.string.status_finished)
            }

            // Cargar imagen de portada
            book.imageUrl?.let { url ->
                Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.ic_book)
                    .into(coverImageView)
            } ?: run {
                coverImageView.setImageResource(R.drawable.ic_book)
            }

            // Configurar botón de estado
            statusButton.setImageResource(
                when (book.readingStatus) {
                    ReadingStatus.TO_READ -> R.drawable.ic_bookmark_border
                    ReadingStatus.READING -> R.drawable.ic_menu_book
                    ReadingStatus.FINISHED -> R.drawable.ic_check_filled
                }
            )

            // Configurar botón de favorito
            favoriteButton?.let { favBtn ->
                favBtn.setImageResource(
                    if (book.isFavorite) R.drawable.ic_favorite_filled
                    else R.drawable.ic_favorite_border
                )
                favBtn.setOnClickListener {
                    onFavoriteClick?.invoke(book)
                }
            }

            deleteButton.setOnClickListener {
                onDeleteClick?.invoke(book)
            }

            // Sistema de rating y reseña
            setupRatingAndReview(book)

            // Listeners
            itemView.setOnClickListener { onBookClick(book) }
        }

        private fun setupRatingAndReview(book: Book) {
            val context = itemView.context

            if (book.readingStatus == ReadingStatus.FINISHED) {
                ratingBar.visibility = View.VISIBLE
                ratingText.visibility = View.VISIBLE

                val rating = book.rating?.toFloat() ?: 0f
                ratingBar.rating = rating

                // ✅ Usar string resource para "Sin calificar"
                ratingText.text = if (rating > 0) {
                    String.format("%.1f", rating)
                } else {
                    context.getString(R.string.rating_unrated)
                }

                // Listener para cambios en el rating
                ratingBar.setOnRatingBarChangeListener { _, newRating, fromUser ->
                    if (fromUser) {
                        onRatingChanged(book, newRating)
                        // ✅ Actualizar texto con string resource
                        ratingText.text = if (newRating > 0) {
                            String.format("%.1f", newRating)
                        } else {
                            context.getString(R.string.rating_unrated)
                        }
                    }
                }

                // Mostrar reseña si existe
                reviewText?.let { reviewTv ->
                    if (!book.review.isNullOrBlank()) {
                        reviewTv.text = book.review
                        reviewTv.visibility = View.VISIBLE
                    } else {
                        reviewTv.visibility = View.GONE
                    }
                }
            } else {
                ratingBar.visibility = View.GONE
                ratingText.visibility = View.GONE
                reviewText?.visibility = View.GONE
            }
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