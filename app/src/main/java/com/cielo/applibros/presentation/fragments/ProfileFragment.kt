package com.cielo.applibros.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cielo.applibros.MainActivity
import com.cielo.applibros.R
import com.cielo.applibros.presentation.adapter.CompactBookAdapter
import com.cielo.applibros.presentation.dialogs.BookDetailDialogFragment
import com.cielo.applibros.presentation.viewmodel.ProfileViewModel
import androidx.core.graphics.toColorInt
import com.cielo.applibros.presentation.viewmodel.BookViewModelUpdated

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var bookViewModel: BookViewModelUpdated

    private lateinit var currentlyReadingAdapter: CompactBookAdapter
    private lateinit var recentlyFinishedAdapter: CompactBookAdapter

    private lateinit var sectionCurrentlyReading: View
    private lateinit var sectionRecentlyFinished: View





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).getProfileViewModel()
        bookViewModel = (activity as MainActivity).getBookViewModel()

        applyOverlay(view)

        sectionCurrentlyReading = view.findViewById(R.id.sectionCurrentlyReading)
        sectionRecentlyFinished = view.findViewById(R.id.sectionRecentlyFinished)

        setupViews(view)
        setupObservers()

        viewModel.loadUserStats()
    }

    private fun applyOverlay(view: View) {
        val overlay = view.findViewById<View>(R.id.profileOverlay)

        val nightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK

        if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            // Modo oscuro → overlay negro suave
            overlay.setBackgroundColor(
                "#CC000000".toColorInt()
            )
        } else {
            // Modo claro → overlay blanco suave
            overlay.setBackgroundColor(
                "#B3FFFDF5".toColorInt()
            )
        }
    }

    private fun setupViews(view: View) {

        // Crear adaptadores compactos
        currentlyReadingAdapter = CompactBookAdapter { book ->
            val dialog = BookDetailDialogFragment.newInstance(book)
            dialog.show(parentFragmentManager, "book_detail")
        }

        recentlyFinishedAdapter = CompactBookAdapter { book ->
            val dialog = BookDetailDialogFragment.newInstance(book)
            dialog.show(parentFragmentManager, "book_detail")
        }


        // Configurar RecyclerView para "Leyendo Actualmente"
        view.findViewById<RecyclerView>(R.id.rvCurrentlyReading).apply {
            adapter = currentlyReadingAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }

        // Configurar RecyclerView para "Mis Favoritos"
        view.findViewById<RecyclerView>(R.id.rvFavorites).apply {
            adapter = recentlyFinishedAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }


    }

    private fun setupObservers() {

        bookViewModel.currentlyReading.observe(viewLifecycleOwner) { books ->
            currentlyReadingAdapter.submitList(books)

            sectionCurrentlyReading.visibility =
                if (books.isEmpty()) View.GONE else View.VISIBLE
        }

        bookViewModel.finishedBooks.observe(viewLifecycleOwner) { books ->
            recentlyFinishedAdapter.submitList(books.take(10))

            sectionRecentlyFinished.visibility =
                if (books.isEmpty()) View.GONE else View.VISIBLE
        }


        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            view?.let { v ->
                v.findViewById<TextView>(R.id.tvTotalRead).text = stats.totalBooksRead.toString()
                v.findViewById<TextView>(R.id.tvTotalToRead).text = stats.totalBooksToRead.toString()
                v.findViewById<TextView>(R.id.tvAverageRating).text =
                    String.format("%.1f", stats.averageRating)

                currentlyReadingAdapter.submitList(stats.currentlyReading)
                recentlyFinishedAdapter.submitList(stats.recentlyFinishedBooks)
                sectionCurrentlyReading.visibility =
                    if (stats.currentlyReading.isEmpty()) View.GONE else View.VISIBLE

                sectionRecentlyFinished.visibility =
                    if (stats.recentlyFinishedBooks.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }
}