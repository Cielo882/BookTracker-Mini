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
    private val onDeleteClick: ((Book) -> Unit)? = null // NUEVO

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
        private val statusTextView: TextView = itemView.findViewById(R.id.tvStatus) // Nuevo
        private val coverImageView: ImageView = itemView.findViewById(R.id.ivCover)
        private val statusButton: ImageView = itemView.findViewById(R.id.ivStatus) // Cambió de ivRead
        private val favoriteButton: ImageView? = itemView.findViewById(R.id.ivFavorite) // Nuevo, opcional
        private val deleteButton: ImageView = itemView.findViewById(R.id.ivDelete) // NUEVO

        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val ratingText: TextView = itemView.findViewById(R.id.tvRatingText)
        private val reviewText: TextView? = itemView.findViewById(R.id.tvReview) // Nuevo, opcional

        fun bind(book: Book) {
            titleTextView.text = book.title
            authorTextView.text = book.authorsString

            // Mostrar estado del libro
            statusTextView.text = when (book.readingStatus) {
                ReadingStatus.TO_READ -> "Por leer"
                ReadingStatus.READING -> "Leyendo"
                ReadingStatus.FINISHED -> "Terminado"
            }

            // Cargar imagen de portada
            book.imageUrl?.let { url ->
                Glide.with(itemView.context)
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

            // Configurar botón de favorito (si existe)
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
            //statusButton.setOnClickListener { onStatusClick(book) }
        }

        private fun setupRatingAndReview(book: Book) {
            if (book.readingStatus == ReadingStatus.FINISHED) {
                // Solo mostrar rating si fue terminado
                ratingBar.visibility = View.VISIBLE
                ratingText.visibility = View.VISIBLE

                // Configurar el rating actual
                val rating = book.rating?.toFloat() ?: 0f
                ratingBar.rating = rating
                ratingText.text = if (rating > 0) {
                    String.format("%.1f", rating)
                } else {
                    "Sin calificar"
                }

                // Listener para cambios en el rating
                ratingBar.setOnRatingBarChangeListener { _, newRating, fromUser ->
                    if (fromUser) {
                        onRatingChanged(book, newRating)
                        ratingText.text = if (newRating > 0) {
                            String.format("%.1f", newRating)
                        } else {
                            "Sin calificar"
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
                // Ocultar rating si no se ha terminado
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