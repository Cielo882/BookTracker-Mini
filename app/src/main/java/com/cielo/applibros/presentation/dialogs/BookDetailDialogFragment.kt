package com.cielo.applibros.presentation.dialogs

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cielo.applibros.MainActivity
import com.google.android.material.textfield.TextInputEditText
import com.cielo.applibros.R
import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.model.ReadingStatus
import com.cielo.applibros.presentation.viewmodel.BookViewModelUpdated
import com.cielo.applibros.utils.ShareBookHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BookDetailDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: BookViewModelUpdated
    private var book: Book? = null

    // Fechas
    private var startDateMillis: Long? = null
    private var finishDateMillis: Long? = null

    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var spinnerStatus: Spinner

    // Campos condicionales
    private lateinit var ratingContainer: LinearLayout
    private lateinit var ratingBar: RatingBar

    private lateinit var reviewContainer: LinearLayout
    private lateinit var tilReview: TextInputLayout
    private lateinit var etReview: TextInputEditText

    private lateinit var cbFavorite: CheckBox

    // Contenedores de fechas
    private lateinit var startDateContainer: LinearLayout
    private lateinit var finishDateContainer: LinearLayout

    private lateinit var tvStartDateLabel: TextView
    private lateinit var tvStartDate: TextView
    private lateinit var btnEditStartDate: ImageButton

    private lateinit var tvFinishDateLabel: TextView
    private lateinit var tvFinishDate: TextView
    private lateinit var btnEditFinishDate: ImageButton

    private lateinit var btnSave: Button
    private lateinit var btnClose: Button

    //  favorito
    private lateinit var favoriteContainer: LinearLayout
    private lateinit var ivFavoriteIcon: ImageView
    private var isFavorite: Boolean = false

    companion object {
        private const val ARG_BOOK_ID = "book_id"

        fun newInstance(book: Book): BookDetailDialogFragment {
            val fragment = BookDetailDialogFragment()
            val bundle = Bundle().apply {
                putInt(ARG_BOOK_ID, book.id)
            }
            fragment.arguments = bundle
            fragment.book = book
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_book_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as? BottomSheetDialog
            val bottomSheet = bottomSheetDialog?.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true

                val layoutParams = it.layoutParams
                layoutParams.height = (resources.displayMetrics.heightPixels * 0.9).toInt()
                it.layoutParams = layoutParams
            }
        }

        viewModel = (activity as MainActivity).getBookViewModel()

        setupViews(view)
        setupStatusSpinner()
        setupListeners()

        book?.let { populateViews(it) }
    }

    private fun setupViews(view: View) {
        ivCover = view.findViewById(R.id.ivCover)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvAuthor = view.findViewById(R.id.tvAuthor)
        spinnerStatus = view.findViewById(R.id.spinnerStatus)

        ratingContainer = view.findViewById(R.id.ratingContainer)
        ratingBar = view.findViewById(R.id.ratingBar)

        reviewContainer = view.findViewById(R.id.reviewContainer)
        tilReview = view.findViewById(R.id.tilReview)
        etReview = view.findViewById(R.id.etReview)

        //cbFavorite = view.findViewById(R.id.cbFavorite)

        favoriteContainer = view.findViewById(R.id.favoriteContainer)
        ivFavoriteIcon = view.findViewById(R.id.ivFavoriteIcon)
        // Contenedores de fechas
        startDateContainer = view.findViewById(R.id.startDateContainer)
        finishDateContainer = view.findViewById(R.id.finishDateContainer)

        tvStartDateLabel = view.findViewById(R.id.tvStartDateLabel)
        tvStartDate = view.findViewById(R.id.tvStartDate)
        btnEditStartDate = view.findViewById(R.id.btnEditStartDate)

        tvFinishDateLabel = view.findViewById(R.id.tvFinishDateLabel)
        tvFinishDate = view.findViewById(R.id.tvFinishDate)
        btnEditFinishDate = view.findViewById(R.id.btnEditFinishDate)

        btnSave = view.findViewById(R.id.btnSave)
        btnClose = view.findViewById(R.id.btnClose)

        // ✅ NUEVO: Botón compartir (solo visible si está terminado)
        view.findViewById<Button>(R.id.btnShareBook)?.apply {
            if (book?.readingStatus == ReadingStatus.FINISHED) {
                visibility = View.VISIBLE
                setOnClickListener {
                    if (hasUnsavedChanges()) {
                        showSaveFirstDialog()
                    } else {
                        shareBook(book!!)
                    }
                }
            }
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        val book = this.book ?: return false

        // Verificar si el estado cambió
        val currentStatus = when (spinnerStatus.selectedItemPosition) {
            0 -> ReadingStatus.TO_READ
            1 -> ReadingStatus.READING
            2 -> ReadingStatus.FINISHED
            else -> ReadingStatus.TO_READ
        }

        if (currentStatus != book.readingStatus) return true

        // Verificar si cambió el rating
        if (ratingBar.rating.toInt() != (book.rating ?: 0)) return true

        // Verificar si cambió la reseña
        val currentReview = etReview.text?.toString()?.trim() ?: ""
        val originalReview = book.review ?: ""
        if (currentReview != originalReview) return true

        // Verificar si cambió favorito
        //if (cbFavorite.isChecked != book.isFavorite) return true
        if (isFavorite != book.isFavorite) return true

        return false
    }

    private fun showSaveFirstDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Cambios sin guardar")
            .setMessage("Debes guardar los cambios antes de compartir.\n\n¿Deseas guardar ahora?")
            .setPositiveButton("Guardar y compartir") { _, _ ->
                book?.let {
                    saveBookChanges(it)
                    // Esperar un momento para que se guarde
                    view?.postDelayed({
                        shareBook(it)
                    }, 500)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun setupStatusSpinner() {
        val statusOptions = arrayOf("Por leer", "Leyendo", "Terminado")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statusOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            book?.let { saveBookChanges(it) }
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateVisibilityBasedOnStatus(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // DatePickers
        btnEditStartDate.setOnClickListener {
            showStartDatePicker()
        }

        btnEditFinishDate.setOnClickListener {
            showFinishDatePicker()
        }

        favoriteContainer.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon()
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            ivFavoriteIcon.setImageResource(R.drawable.ic_favorite_filled)
            // Animación opcional
            ivFavoriteIcon.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .withEndAction {
                    ivFavoriteIcon.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                }
                .start()
        } else {
            ivFavoriteIcon.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    private fun updateVisibilityBasedOnStatus(position: Int) {
        when (position) {
            0 -> { // Por leer
                ratingContainer.visibility = View.GONE
                reviewContainer.visibility = View.GONE
                favoriteContainer.visibility = View.GONE  // ✅ CAMBIAR
                startDateContainer.visibility = View.GONE
                finishDateContainer.visibility = View.GONE
            }
            1 -> { // Leyendo
                ratingContainer.visibility = View.GONE
                reviewContainer.visibility = View.GONE
                favoriteContainer.visibility = View.GONE  // ✅ CAMBIAR
                startDateContainer.visibility = View.VISIBLE
                finishDateContainer.visibility = View.GONE
            }
            2 -> { // Terminado
                ratingContainer.visibility = View.VISIBLE
                reviewContainer.visibility = View.VISIBLE
                favoriteContainer.visibility = View.VISIBLE  // ✅ CAMBIAR
                startDateContainer.visibility = View.VISIBLE
                finishDateContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun showStartDatePicker() {
        val calendar = Calendar.getInstance()
        startDateMillis?.let { calendar.timeInMillis = it }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                startDateMillis = selectedCalendar.timeInMillis
                updateStartDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showFinishDatePicker() {
        val calendar = Calendar.getInstance()
        finishDateMillis?.let { calendar.timeInMillis = it }

        // Validar que la fecha de fin no sea antes de la fecha de inicio
        val minDate = startDateMillis ?: calendar.timeInMillis

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                finishDateMillis = selectedCalendar.timeInMillis
                updateFinishDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.show()
    }

    private fun updateStartDateDisplay() {
        startDateMillis?.let { millis ->
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvStartDate.text = formatter.format(Date(millis))
        } ?: run {
            tvStartDate.text = "Sin fecha"
        }
    }

    private fun updateFinishDateDisplay() {
        finishDateMillis?.let { millis ->
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvFinishDate.text = formatter.format(Date(millis))

            // Calcular días de lectura
            startDateMillis?.let { startMillis ->
                val days = ((millis - startMillis) / (1000 * 60 * 60 * 24)).toInt()
                tvFinishDate.text = "${tvFinishDate.text} ($days días)"
            }
        } ?: run {
            tvFinishDate.text = "Sin fecha"
        }
    }

    private fun populateViews(book: Book) {
        tvTitle.text = book.title
        tvAuthor.text = book.authorsString

        book.imageUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_book)
                .into(ivCover)
        }

        val statusPosition = when (book.readingStatus) {
            ReadingStatus.TO_READ -> 0
            ReadingStatus.READING -> 1
            ReadingStatus.FINISHED -> 2
        }
        spinnerStatus.setSelection(statusPosition)

        ratingBar.rating = book.rating?.toFloat() ?: 0f
        etReview.setText(book.review ?: "")
        //cbFavorite.isChecked = book.isFavorite
        isFavorite = book.isFavorite
        updateFavoriteIcon()

        // Cargar fechas
        startDateMillis = book.startDate
        finishDateMillis = book.finishDate

        updateStartDateDisplay()
        updateFinishDateDisplay()

        updateVisibilityBasedOnStatus(statusPosition)
    }

    private fun saveBookChanges(book: Book) {
        val newStatus = when (spinnerStatus.selectedItemPosition) {
            0 -> ReadingStatus.TO_READ
            1 -> ReadingStatus.READING
            2 -> ReadingStatus.FINISHED
            else -> ReadingStatus.TO_READ
        }

        viewModel.updateBookStatus(book.id, newStatus)

        // Guardar fechas según el estado
        when (newStatus) {
            ReadingStatus.READING -> {
                startDateMillis?.let {
                    viewModel.updateStartDate(book.id, it)
                }
            }
            ReadingStatus.FINISHED -> {
                startDateMillis?.let {
                    viewModel.updateStartDate(book.id, it)
                }
                finishDateMillis?.let {
                    viewModel.updateFinishDate(book.id, it)
                }

                val ratingValue = ratingBar.rating.toInt()
                if (ratingValue > 0) {
                    viewModel.updateBookRating(book.id, ratingValue)
                }

                val review = etReview.text?.toString()?.trim()
                if (!review.isNullOrBlank()) {
                    viewModel.updateBookReview(book.id, review)
                }

                //viewModel.toggleFavorite(book.id, cbFavorite.isChecked)
                viewModel.toggleFavorite(book.id, isFavorite)

            }
            ReadingStatus.TO_READ -> {
                // Si vuelve a "Por leer", limpiar fechas (usar 0L como marcador de 'sin fecha')
                viewModel.updateStartDate(book.id, 0L)
                viewModel.updateFinishDate(book.id, 0L)
            }

            else -> {}
        }

        dismiss()
    }

    private fun shareBook(book: Book) {
        lifecycleScope.launch {
            try {
                // Mostrar loading
                val progressDialog = AlertDialog.Builder(requireContext())
                    .setMessage("Creando imagen...")
                    .setCancelable(false)
                    .create()
                progressDialog.show()

                // Crear imagen
                val shareHelper = ShareBookHelper(requireContext())
                val imageUri = shareHelper.createShareImage(book)

                progressDialog.dismiss()

                if (imageUri != null) {
                    shareHelper.shareBook(imageUri, book)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al crear imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }
}