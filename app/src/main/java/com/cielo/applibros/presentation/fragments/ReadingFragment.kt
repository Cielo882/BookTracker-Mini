package com.cielo.applibros.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.cielo.applibros.MainActivity
import com.cielo.applibros.R
import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.model.ReadingStatus
import com.cielo.applibros.presentation.adapter.BookAdapter
import com.cielo.applibros.presentation.dialogs.BookDetailDialogFragment
import com.cielo.applibros.presentation.viewmodel.BookViewModelUpdated
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ReadingFragment : Fragment() {

    private lateinit var viewModel: BookViewModelUpdated
    private lateinit var bookAdapter: BookAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var emptyState: View
    private lateinit var emptyAnimation: LottieAnimationView



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener ViewModel desde MainActivity
        viewModel = (activity as MainActivity).getBookViewModel()

        emptyState = view.findViewById(R.id.emptyState)
        emptyAnimation = view.findViewById(R.id.lottieEmpty)

        setupLottie()

        setupSwipeRefresh(view)

        setupRecyclerView(view)
        setupObservers()
    }

    private fun setupLottie() {
        try {
            emptyAnimation.setAnimation(R.raw.books)
            emptyAnimation.repeatCount = LottieDrawable.INFINITE
        } catch (e: Exception) {
            emptyState.visibility = View.GONE
        }

        emptyAnimation.setFailureListener {
            emptyState.visibility = View.GONE
        }
    }

    private fun setupSwipeRefresh(view: View) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        swipeRefresh.apply {
            setColorSchemeResources(
                R.color.brown_dark,
                R.color.brown_medium,
                R.color.brown_light
            )

            setOnRefreshListener {
                // Las LiveData se actualizan automáticamente desde la base de datos
                // Solo necesitamos detener el refresh después de un momento
                view.postDelayed({
                    isRefreshing = false
                }, 800)
            }
        }
    }
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.rvBooks)

        bookAdapter = BookAdapter(
            onBookClick = { book ->
                val dialog = BookDetailDialogFragment.newInstance(book)
                dialog.show(parentFragmentManager, "book_detail")
            },
            onDeleteClick = { book -> // NUEVO
                showDeleteConfirmation(book)
            },
            onRatingChanged = { book, rating ->
                viewModel.updateBookRating(book.id, rating.toInt())
            },
            onFavoriteClick = { book ->
                viewModel.toggleFavorite(book.id, !book.isFavorite)
            }
        )

        recyclerView.apply {
            adapter = bookAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewModel.currentlyReading.observe(viewLifecycleOwner) { books ->
            swipeRefresh.isRefreshing = false
            bookAdapter.submitList(books)

            val isEmpty = books.isEmpty()

            recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE

            if (isEmpty) {
                emptyAnimation.playAnimation()
            } else {
                emptyAnimation.cancelAnimation()
            }
        }
    }

    private fun showDeleteConfirmation(book: Book) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar libro")
            .setMessage("¿Estás seguro de que deseas eliminar \"${book.title}\" de tu biblioteca?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.removeFromRead(book)
                Toast.makeText(requireContext(), "Libro eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        emptyAnimation.pauseAnimation()
    }

    override fun onResume() {
        super.onResume()
        if (emptyState.visibility == View.VISIBLE) {
            emptyAnimation.playAnimation()
        }
    }

    override fun onDestroyView() {
        emptyAnimation.cancelAnimation()
        super.onDestroyView()
    }

}