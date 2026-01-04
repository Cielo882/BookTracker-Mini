package com.cielo.applibros

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat.applyTheme
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.cielo.applibros.data.local.database.AppDatabase
import com.cielo.applibros.data.local.preferences.SettingsPreferences
import com.cielo.applibros.data.remote.UnifiedBookSearchService
import com.cielo.applibros.data.remote.api.GoogleBooksApiService
import com.cielo.applibros.data.remote.api.GutendexApiService
import com.cielo.applibros.data.remote.api.OpenLibraryApiService
import com.cielo.applibros.data.repository.BookRepositoryImpl
import com.cielo.applibros.domain.model.ReadingStatus
import com.cielo.applibros.domain.model.ThemeMode
import com.cielo.applibros.domain.usecase.*
import com.cielo.applibros.presentation.fragments.*
import com.cielo.applibros.presentation.viewmodel.BookViewModelUpdated
import com.cielo.applibros.presentation.viewmodel.ProfileViewModel
import com.cielo.applibros.presentation.viewmodel.SettingsViewModel
import com.cielo.applibros.utils.AnalyticsHelper
import com.cielo.applibros.utils.CrashlyticsHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // Views
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbar: Toolbar

    // ViewModels
    private lateinit var bookViewModel: BookViewModelUpdated
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var settingsViewModel: SettingsViewModel // NUEVO

    private lateinit var settingsPreferences: SettingsPreferences // NUEVO

    // ✅ AGREGAR: Firebase Helpers
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var crashlyticsHelper: CrashlyticsHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeFirebase()

        // AGREGAR: Cargar tema antes de setContentView
        settingsPreferences = SettingsPreferences(this)
        applyTheme(settingsPreferences.getSettings().themeMode)

        setContentView(R.layout.activity_main)

        analyticsHelper.logScreenView("MainActivity")

        setupDependencies()
        setupViews()
        setupToolbar()
        setupDrawer()
        setupBottomNavigation()

        // Cargar el fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(ToReadFragment())
            bottomNavigation.selectedItemId = R.id.nav_to_read
        }
    }

    private fun initializeFirebase() {
        val app = application as BookTrackerApplication
        analyticsHelper = AnalyticsHelper(app.analytics)
        crashlyticsHelper = CrashlyticsHelper(app.crashlytics)

        // Log inicio de sesión
        crashlyticsHelper.logUserAction("App Started")
    }

    private fun setupDependencies() {

        try {
        // Configurar OkHttpClient
        val okHttpClient = NetworkHelper.createUnsafeOkHttpClient()

        // Retrofit para Gutendex
        val gutendexRetrofit = Retrofit.Builder()
            .baseUrl(GutendexApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Retrofit para Open Library
        val openLibraryRetrofit = Retrofit.Builder()
            .baseUrl(OpenLibraryApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Retrofit para Google Books
        val googleBooksRetrofit = Retrofit.Builder()
            .baseUrl(GoogleBooksApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Crear servicios de API
        val gutendexApi = gutendexRetrofit.create(GutendexApiService::class.java)
        val openLibraryApi = openLibraryRetrofit.create(OpenLibraryApiService::class.java)
        val googleBooksApi = googleBooksRetrofit.create(GoogleBooksApiService::class.java)


        // Crear servicio unificado
        val unifiedSearchService = UnifiedBookSearchService(
            gutendexApi,
            openLibraryApi,
            googleBooksApi
        )

        val database = AppDatabase.getDatabase(this)
        val bookDao = database.bookDao()

        val settingsPreferences = SettingsPreferences(applicationContext)

        // Crear repository con el servicio unificado
        val repository = BookRepositoryImpl(unifiedSearchService, bookDao, settingsPreferences)

        // Use cases existentes
        val getBooksUseCase = GetBooksUseCase(repository)
        val getReadBooksUseCase = GetReadBooksUseCase(repository)
        val addToReadUseCase = AddToReadUseCase(repository)
        val removeFromReadUseCase = RemoveFromReadUseCase(repository)
        val updateBookRatingUseCase = UpdateBookRatingUseCase(repository)

        // Nuevos use cases
        val updateBookStatusUseCase = UpdateBookStatusUseCase(repository)
        val updateBookReviewUseCase = UpdateBookReviewUseCase(repository)
        val toggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
        val getBooksToReadUseCase = GetBooksToReadUseCase(repository)
        val getCurrentlyReadingUseCase = GetCurrentlyReadingUseCase(repository)
        val getFinishedBooksUseCase = GetFinishedBooksUseCase(repository)
        val getUserStatsUseCase = GetUserStatsUseCase(repository)
        val updateStartDateUseCase = UpdateStartDateUseCase(repository)
        val updateFinishDateUseCase = UpdateFinishDateUseCase(repository)

        // ViewModels
        bookViewModel = BookViewModelUpdated(
            getBooksUseCase,
            getReadBooksUseCase,
            addToReadUseCase,
            removeFromReadUseCase,
            updateBookRatingUseCase,
            updateBookStatusUseCase,
            updateBookReviewUseCase,
            toggleFavoriteUseCase,
            getBooksToReadUseCase,
            getCurrentlyReadingUseCase,
            getFinishedBooksUseCase,
            updateStartDateUseCase,
            updateFinishDateUseCase,
            analyticsHelper,  // ✅ AGREGAR
            crashlyticsHelper  // ✅ AGREGAR

        )

        profileViewModel = ProfileViewModel(getUserStatsUseCase)

        settingsViewModel = SettingsViewModel(settingsPreferences)
        } catch (e: Exception) {
            // ✅ AGREGAR: Log de error en Crashlytics
            crashlyticsHelper.logException(e, "setupDependencies")
        }
    }

    private fun applyTheme(theme: ThemeMode) {
        val mode = when (theme) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setupViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_to_read -> {
                    loadFragment(ToReadFragment())
                    true
                }
                R.id.nav_reading -> {
                    loadFragment(ReadingFragment())
                    true
                }
                R.id.nav_search -> {
                    loadFragment(SearchFragment())
                    true
                }
                R.id.nav_finished -> {
                    loadFragment(FinishedFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            // ✅ AGREGAR: Log de navegación
            val fragmentName = fragment::class.simpleName ?: "Unknown"
            analyticsHelper.logFragmentOpened(fragmentName)
            crashlyticsHelper.logNavigationEvent(fragmentName)

        } catch (e: Exception) {
            crashlyticsHelper.logException(e, "loadFragment")
        }
    }

    // Implementación del NavigationView.OnNavigationItemSelectedListener
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearBottomNavigationSelection()
        when (item.itemId) {
            R.id.nav_profile -> {
                loadFragment(ProfileFragment())
                analyticsHelper.logFeatureUsed("profile")
            }
            R.id.nav_statistics -> {
                loadFragment(StatisticsFragment())
                analyticsHelper.logFeatureUsed("statistics")
            }
            R.id.nav_favorites -> {
                loadFragment(FavoritesFragment())
                analyticsHelper.logFeatureUsed("favorites")
            }
            R.id.nav_reading_log -> {
                loadFragment(ReadingLogFragment())
                analyticsHelper.logFeatureUsed("reading_log")
            }
            R.id.nav_settings -> {
                loadFragment(SettingsFragment())
                analyticsHelper.logFeatureUsed("settings")
            }
            R.id.nav_about -> {
                showAboutDialog()
                analyticsHelper.logFeatureUsed("about")
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Métodos públicos para compartir helpers
    fun getAnalyticsHelper(): AnalyticsHelper = analyticsHelper
    fun getCrashlyticsHelper(): CrashlyticsHelper = crashlyticsHelper

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // Métodos públicos para compartir ViewModels con fragments
    fun getBookViewModel(): BookViewModelUpdated = bookViewModel
    fun getProfileViewModel(): ProfileViewModel = profileViewModel
    fun getSettingsViewModel(): SettingsViewModel = settingsViewModel


    // Método para navegar a una sección específica desde otros fragments
    fun navigateToStatus(status: ReadingStatus) {
        val itemId = when (status) {
            ReadingStatus.TO_READ -> R.id.nav_to_read
            ReadingStatus.READING -> R.id.nav_reading
            ReadingStatus.FINISHED -> R.id.nav_finished
        }
        bottomNavigation.selectedItemId = itemId
    }

    private fun showComingSoon(feature: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Próximamente")
            .setMessage("La función '$feature' estará disponible en una próxima actualización.")
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Acerca de BookTracker")
            .setMessage(
                "BookTracker v2.0\n\n" +
                        "Tu biblioteca personal para organizar y rastrear tus lecturas.\n\n" +
                        "Desarrollado con ❤️ usando Clean Architecture y Kotlin"
            )
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun clearBottomNavigationSelection() {
        bottomNavigation.menu.setGroupCheckable(0, true, false)
        for (i in 0 until bottomNavigation.menu.size()) {
            bottomNavigation.menu.getItem(i).isChecked = false
        }
        bottomNavigation.menu.setGroupCheckable(0, true, true)
    }
}