package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnimeRepository
import com.example.model.AnimeRelease
import com.example.model.AnimeSortOrder
import com.example.model.DayScheduleTab
import com.example.model.EpisodeFilter
import com.example.model.GenreHelper
import com.example.model.SynopsisLanguage
import com.example.notifications.AnimeNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

enum class MainNavScreen(val title: String) {
    SCHEDULE("Calendario"),
    WATCHLIST("Mi Watchlist")
}

data class AnimeUiState(
    val allReleases: List<AnimeRelease> = emptyList(),
    val filteredReleases: List<AnimeRelease> = emptyList(),
    val dayTabs: List<DayScheduleTab> = emptyList(),
    val selectedDate: LocalDate? = LocalDate.now(),
    val searchQuery: String = "",
    val availableGenres: List<String> = emptyList(),
    val selectedGenres: Set<String> = emptySet(),
    val sortOrder: AnimeSortOrder = AnimeSortOrder.AIRING_TIME,
    val isCategorizedByGenre: Boolean = false,
    val releasesByGenre: Map<String, List<AnimeRelease>> = emptyMap(),
    val genreCounts: Map<String, Int> = emptyMap(),
    val onlyFavorites: Boolean = false,
    val episodeFilter: EpisodeFilter = EpisodeFilter.ALL,
    val selectedPlatform: com.example.model.StreamingPlatform = com.example.model.StreamingPlatform.ALL,
    val appThemeMode: com.example.model.AppThemeMode = com.example.model.AppThemeMode.CYBERPUNK,
    val isJapanTimeZone: Boolean = false,
    val quietHoursEnabled: Boolean = false,
    val watchedEpisodeKeys: Set<String> = emptySet(),
    val globalLanguage: SynopsisLanguage = SynopsisLanguage.SPANISH,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val notificationMessage: String? = null,
    val selectedAnimeForDetail: AnimeRelease? = null,
    val favoritesCount: Int = 0,
    val watchedCount: Int = 0,
    val todayPremieresCount: Int = 0,
    val todayOngoingCount: Int = 0,
    val currentNavScreen: MainNavScreen = MainNavScreen.SCHEDULE,
    val watchlistItems: List<com.example.data.local.WatchlistEntity> = emptyList(),
    val filteredWatchlistItems: List<com.example.data.local.WatchlistEntity> = emptyList(),
    val watchlistIds: Set<Int> = emptySet(),
    val watchlistFilterStatus: com.example.data.local.WatchlistStatus? = null,
    val watchlistSearchQuery: String = "",
    val watchlistCount: Int = 0
)

class AnimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AnimeRepository(application.applicationContext)

    private val _rawReleases = MutableStateFlow<List<AnimeRelease>>(emptyList())
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _watchedKeys = MutableStateFlow<Set<String>>(emptySet())
    private val _userRatings = MutableStateFlow<Map<Int, Int>>(emptyMap())
    private val _selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedGenres = MutableStateFlow<Set<String>>(emptySet())
    private val _sortOrder = MutableStateFlow(AnimeSortOrder.AIRING_TIME)
    private val _isCategorizedByGenre = MutableStateFlow(false)
    private val _onlyFavorites = MutableStateFlow(false)
    private val _episodeFilter = MutableStateFlow(EpisodeFilter.ALL)
    private val _selectedPlatform = MutableStateFlow(com.example.model.StreamingPlatform.ALL)
    private val _appThemeMode = MutableStateFlow(com.example.model.AppThemeMode.CYBERPUNK)
    private val _isJapanTimeZone = MutableStateFlow(false)
    private val _quietHoursEnabled = MutableStateFlow(false)
    private val _globalLanguage = MutableStateFlow(SynopsisLanguage.SPANISH)
    private val _isLoading = MutableStateFlow(true)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _notificationMessage = MutableStateFlow<String?>(null)
    private val _selectedAnimeForDetail = MutableStateFlow<AnimeRelease?>(null)
    private val _watchlistItems = MutableStateFlow<List<com.example.data.local.WatchlistEntity>>(emptyList())
    private val _watchlistIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _currentNavScreen = MutableStateFlow(MainNavScreen.SCHEDULE)
    private val _watchlistFilterStatus = MutableStateFlow<com.example.data.local.WatchlistStatus?>(null)
    private val _watchlistSearchQuery = MutableStateFlow("")

    private data class WatchlistParams(
        val items: List<com.example.data.local.WatchlistEntity>,
        val ids: Set<Int>,
        val screen: MainNavScreen,
        val filter: com.example.data.local.WatchlistStatus?,
        val query: String
    )

    private data class FilterParams(
        val raw: List<AnimeRelease>,
        val favIds: Set<Int>,
        val watchedKeys: Set<String>,
        val userRatings: Map<Int, Int>,
        val selDate: LocalDate?,
        val query: String,
        val selGenres: Set<String>,
        val sort: AnimeSortOrder,
        val categorized: Boolean,
        val onlyFavs: Boolean,
        val platform: com.example.model.StreamingPlatform
    )

    private data class ControlParams(
        val epFilter: EpisodeFilter,
        val lang: SynopsisLanguage,
        val themeMode: com.example.model.AppThemeMode,
        val isJpZone: Boolean,
        val quietHours: Boolean,
        val loading: Boolean,
        val refreshing: Boolean,
        val error: String?
    )

    private val filterParamsFlow = combine(
        combine(_rawReleases, _favoriteIds, _watchedKeys, _userRatings) { raw, favIds, watched, ratings ->
            Quad(raw, favIds, watched, ratings)
        },
        _selectedDate,
        _searchQuery,
        combine(_selectedGenres, _sortOrder, _isCategorizedByGenre, _onlyFavorites, _selectedPlatform) { args: Array<Any> ->
            @Suppress("UNCHECKED_CAST")
            FilterOptions(
                genres = args[0] as Set<String>,
                sort = args[1] as AnimeSortOrder,
                cat = args[2] as Boolean,
                favs = args[3] as Boolean,
                platform = args[4] as com.example.model.StreamingPlatform
            )
        }
    ) { (raw, favIds, watched, ratings), selDate, query, opts ->
        FilterParams(raw, favIds, watched, ratings, selDate, query, opts.genres, opts.sort, opts.cat, opts.favs, opts.platform)
    }

    private data class FilterOptions(
        val genres: Set<String>,
        val sort: AnimeSortOrder,
        val cat: Boolean,
        val favs: Boolean,
        val platform: com.example.model.StreamingPlatform
    )

    private val controlParamsFlow = combine(
        _episodeFilter,
        _globalLanguage,
        _appThemeMode,
        _isJapanTimeZone,
        _quietHoursEnabled
    ) { epFilter, lang, theme, jpZone, quiet ->
        ControlGroup(epFilter, lang, theme, jpZone, quiet)
    }

    private data class ControlGroup(
        val epFilter: EpisodeFilter,
        val lang: SynopsisLanguage,
        val theme: com.example.model.AppThemeMode,
        val jpZone: Boolean,
        val quiet: Boolean
    )

    private val finalControlFlow = combine(
        controlParamsFlow,
        _isLoading,
        _isRefreshing,
        _errorMessage
    ) { group, loading, refreshing, error ->
        ControlParams(
            epFilter = group.epFilter,
            lang = group.lang,
            themeMode = group.theme,
            isJpZone = group.jpZone,
            quietHours = group.quiet,
            loading = loading,
            refreshing = refreshing,
            error = error
        )
    }

    private val watchlistParamsFlow = combine(
        _watchlistItems,
        _watchlistIds,
        _currentNavScreen,
        _watchlistFilterStatus,
        _watchlistSearchQuery
    ) { items, ids, screen, filter, query ->
        WatchlistParams(items, ids, screen, filter, query)
    }

    val uiState: StateFlow<AnimeUiState> = combine(
        filterParamsFlow,
        finalControlFlow,
        _selectedAnimeForDetail,
        watchlistParamsFlow
    ) { filterParams, controlParams, detail, watchlistParams ->
        val raw = filterParams.raw
        val favIds = filterParams.favIds
        val watchedKeys = filterParams.watchedKeys
        val watchIds = watchlistParams.ids
        val selDate = filterParams.selDate
        val query = filterParams.query
        val selGenres = filterParams.selGenres
        val sort = filterParams.sort
        val categorized = filterParams.categorized
        val onlyFavs = filterParams.onlyFavs
        val platform = filterParams.platform

        val epFilter = controlParams.epFilter
        val lang = controlParams.lang
        val themeMode = controlParams.themeMode
        val isJpZone = controlParams.isJpZone
        val quietHours = controlParams.quietHours
        val loading = controlParams.loading
        val refreshing = controlParams.refreshing
        val error = controlParams.error

        val userRatings = filterParams.userRatings

        val activeZone = if (isJpZone) java.time.ZoneId.of("Asia/Tokyo") else java.time.ZoneId.systemDefault()
        val today = LocalDate.now(activeZone)

        // Sync favorite, watched, watchlist, and user rating status with Room database
        val syncedRaw = raw.map { item ->
            val isFav = favIds.contains(item.id)
            val isW = watchedKeys.contains("${item.id}_${item.episode}")
            val isWatch = watchIds.contains(item.id)
            val rating = userRatings[item.id] ?: item.userRating
            item.copy(
                isFavorite = isFav,
                isWatched = isW,
                isInWatchlist = isWatch,
                hasReminder = isFav,
                userRating = rating
            )
        }

        // Build 7-day tabs with counts for premieres and ongoing episodes
        val tabs = (0..6).map { offset ->
            val date = today.plusDays(offset.toLong())
            val isToday = offset == 0
            val isTomorrow = offset == 1
            val label = when {
                isToday -> "Hoy"
                isTomorrow -> "Mañana"
                else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")).replaceFirstChar { it.uppercase() }
            }
            val fullDayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }
            val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale("es")).replaceFirstChar { it.uppercase() }

            val dayReleases = syncedRaw.filter { it.getAiringLocalDate() == date }
            val count = dayReleases.size
            val premieres = dayReleases.count { it.isSeriesPremiere }
            val ongoing = dayReleases.count { it.isOngoingEpisode }

            DayScheduleTab(
                date = date,
                label = label,
                dayOfMonth = date.dayOfMonth,
                monthName = monthName,
                fullDayName = fullDayName,
                isToday = isToday,
                isTomorrow = isTomorrow,
                episodeCount = count,
                premieresCount = premieres,
                ongoingCount = ongoing
            )
        }

        // Available genres normalized
        val normalizedGenresList = GenreHelper.FEATURED_GENRES

        // Filter releases (if search query is active, search across all days of the week)
        val filtered = syncedRaw.filter { item ->
            val matchesDate = if (selDate == null || query.isNotBlank()) true else item.getAiringLocalDate() == selDate
            val matchesQuery = query.isBlank() ||
                    item.titleRomaji.contains(query, ignoreCase = true) ||
                    (item.titleEnglish?.contains(query, ignoreCase = true) == true) ||
                    (item.studio?.contains(query, ignoreCase = true) == true) ||
                    item.genres.any { it.contains(query, ignoreCase = true) }
            val matchesGenre = GenreHelper.matchesAnyGenre(item.genres, selGenres)
            val matchesFav = !onlyFavs || item.isFavorite
            val matchesPlatform = platform == com.example.model.StreamingPlatform.ALL ||
                    item.streamingPlatform.equals(platform.displayName, ignoreCase = true)
            val matchesEpisodeType = when (epFilter) {
                EpisodeFilter.ALL -> true
                EpisodeFilter.ONGOING_EPISODES -> item.isOngoingEpisode
                EpisodeFilter.PREMIERES -> item.isSeriesPremiere
            }

            matchesDate && matchesQuery && matchesGenre && matchesFav && matchesPlatform && matchesEpisodeType
        }

        // Apply Sorting
        val sortedReleases = when (sort) {
            AnimeSortOrder.AIRING_TIME -> filtered.sortedBy { it.airingAtSeconds }
            AnimeSortOrder.SCORE -> filtered.sortedByDescending { it.score ?: 0.0 }
            AnimeSortOrder.TITLE_AZ -> filtered.sortedBy { it.displayTitle.lowercase() }
            AnimeSortOrder.EPISODE -> filtered.sortedByDescending { it.episode }
        }

        // Calculate counts for each genre within current date/filter scope
        val dateFilteredRaw = if (selDate == null) syncedRaw else syncedRaw.filter { it.getAiringLocalDate(activeZone) == selDate }
        val countsMap = mutableMapOf<String, Int>()
        for (g in normalizedGenresList) {
            countsMap[g] = dateFilteredRaw.count { item ->
                GenreHelper.matchesAnyGenre(item.genres, setOf(g))
            }
        }

        // Categorize by genre mapping
        val categorizedMap = LinkedHashMap<String, List<AnimeRelease>>()
        if (categorized) {
            for (g in normalizedGenresList) {
                val inGenre = sortedReleases.filter { item ->
                    GenreHelper.matchesAnyGenre(item.genres, setOf(g))
                }
                if (inGenre.isNotEmpty()) {
                    categorizedMap[g] = inGenre
                }
            }
        }

        val favCount = favIds.size
        val watchedCount = watchedKeys.size

        // Counts for current day (or selected date)
        val targetDate = selDate ?: today
        val dayItems = syncedRaw.filter { it.getAiringLocalDate(activeZone) == targetDate }
        val dayPremieres = dayItems.count { it.isSeriesPremiere }
        val dayOngoing = dayItems.count { it.isOngoingEpisode }

        val syncedDetail = detail?.let { d ->
            val isFav = favIds.contains(d.id)
            val isW = watchedKeys.contains("${d.id}_${d.episode}")
            val isWatch = watchIds.contains(d.id)
            val rating = userRatings[d.id] ?: d.userRating
            d.copy(
                isFavorite = isFav,
                isWatched = isW,
                isInWatchlist = isWatch,
                hasReminder = isFav,
                userRating = rating
            )
        }

        // Filter watchlist items by status and search query
        val filteredWatchlist = watchlistParams.items.filter { item ->
            val matchesStatus = watchlistParams.filter == null || item.status == watchlistParams.filter.name
            val q = watchlistParams.query.trim()
            val matchesQuery = q.isEmpty() ||
                    item.titleRomaji.contains(q, ignoreCase = true) ||
                    (item.titleEnglish?.contains(q, ignoreCase = true) == true) ||
                    (item.studio?.contains(q, ignoreCase = true) == true) ||
                    item.genres.contains(q, ignoreCase = true)
            matchesStatus && matchesQuery
        }

        AnimeUiState(
            allReleases = syncedRaw,
            filteredReleases = sortedReleases,
            dayTabs = tabs,
            selectedDate = selDate,
            searchQuery = query,
            availableGenres = normalizedGenresList,
            selectedGenres = selGenres,
            sortOrder = sort,
            isCategorizedByGenre = categorized,
            releasesByGenre = categorizedMap,
            genreCounts = countsMap,
            onlyFavorites = onlyFavs,
            episodeFilter = epFilter,
            selectedPlatform = platform,
            appThemeMode = themeMode,
            isJapanTimeZone = isJpZone,
            quietHoursEnabled = quietHours,
            watchedEpisodeKeys = watchedKeys,
            globalLanguage = lang,
            isLoading = loading,
            isRefreshing = refreshing,
            errorMessage = error,
            notificationMessage = _notificationMessage.value,
            selectedAnimeForDetail = syncedDetail,
            favoritesCount = favCount,
            watchedCount = watchedCount,
            todayPremieresCount = dayPremieres,
            todayOngoingCount = dayOngoing,
            currentNavScreen = watchlistParams.screen,
            watchlistItems = watchlistParams.items,
            filteredWatchlistItems = filteredWatchlist,
            watchlistIds = watchlistParams.ids,
            watchlistFilterStatus = watchlistParams.filter,
            watchlistSearchQuery = watchlistParams.query,
            watchlistCount = watchlistParams.items.size
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AnimeUiState()
    )

    init {
        AnimeNotificationHelper.createNotificationChannel(application.applicationContext)
        viewModelScope.launch {
            repository.favoriteIdsFlow.collect { ids ->
                _favoriteIds.value = ids
                scheduleRemindersForFavorites(_rawReleases.value, ids)
            }
        }
        viewModelScope.launch {
            repository.watchedEpisodeKeysFlow.collect { keys ->
                _watchedKeys.value = keys
            }
        }
        viewModelScope.launch {
            repository.allWatchlistFlow.collect { list ->
                _watchlistItems.value = list
            }
        }
        viewModelScope.launch {
            repository.watchlistIdsFlow.collect { ids ->
                _watchlistIds.value = ids
            }
        }
        viewModelScope.launch {
            repository.userRatingsFlow.collect { ratings ->
                _userRatings.value = ratings
            }
        }
        loadSchedule()
    }

    fun setUserRating(animeId: Int, rating: Int) {
        viewModelScope.launch {
            val targetRating = rating.coerceIn(0, 5)
            repository.setUserRating(animeId, targetRating)
            val current = _userRatings.value.toMutableMap()
            if (targetRating <= 0) current.remove(animeId) else current[animeId] = targetRating
            _userRatings.value = current
            updateAnimeInState(animeId) { it.copy(userRating = targetRating) }
            val msg = if (targetRating > 0) {
                "✓ Puntuación de $targetRating ★ guardada en la base de datos."
            } else {
                "Puntuación eliminada."
            }
            _notificationMessage.value = msg
        }
    }

    fun setNavScreen(screen: MainNavScreen) {
        _currentNavScreen.value = screen
    }

    fun setWatchlistFilter(status: com.example.data.local.WatchlistStatus?) {
        _watchlistFilterStatus.value = status
    }

    fun setWatchlistSearchQuery(query: String) {
        _watchlistSearchQuery.value = query
    }

    fun toggleWatchlist(
        anime: AnimeRelease,
        status: com.example.data.local.WatchlistStatus = com.example.data.local.WatchlistStatus.PLAN_TO_WATCH
    ) {
        viewModelScope.launch {
            val wasAdded = repository.toggleWatchlist(anime, status)
            val msg = if (wasAdded) {
                "✓ '${anime.displayTitle}' guardado en tu Watchlist (${status.displayName})."
            } else {
                "'${anime.displayTitle}' eliminado de tu Watchlist."
            }
            _notificationMessage.value = msg
        }
    }

    fun updateWatchlistStatus(animeId: Int, status: com.example.data.local.WatchlistStatus) {
        viewModelScope.launch {
            repository.updateWatchlistStatus(animeId, status)
            _notificationMessage.value = "Estado de Watchlist: '${status.displayName}'"
        }
    }

    fun updateWatchlistEpisodes(animeId: Int, episodes: Int) {
        viewModelScope.launch {
            repository.updateWatchlistEpisodes(animeId, episodes)
        }
    }

    fun removeFromWatchlist(animeId: Int) {
        viewModelScope.launch {
            repository.removeFromWatchlist(animeId)
            _notificationMessage.value = "Anime eliminado de tu Watchlist"
        }
    }

    fun toggleWatchedEpisode(anime: AnimeRelease) {
        viewModelScope.launch {
            val isNowWatched = repository.toggleWatchedEpisode(anime.id, anime.episode)
            val msg = if (isNowWatched) {
                "✓ Episodio ${anime.episode} de '${anime.displayTitle}' marcado como visto."
            } else {
                "Episodio ${anime.episode} de '${anime.displayTitle}' desmarcado."
            }
            _notificationMessage.value = msg
        }
    }

    fun toggleWatchedEpisodeNum(animeId: Int, episodeNumber: Int, animeTitle: String) {
        viewModelScope.launch {
            val isNowWatched = repository.toggleWatchedEpisode(animeId, episodeNumber)
            val msg = if (isNowWatched) {
                "✓ Episodio $episodeNumber de '$animeTitle' marcado como visto."
            } else {
                "Episodio $episodeNumber de '$animeTitle' desmarcado."
            }
            _notificationMessage.value = msg
        }
    }

    fun setPlatformFilter(platform: com.example.model.StreamingPlatform) {
        _selectedPlatform.value = platform
    }

    fun setAppThemeMode(theme: com.example.model.AppThemeMode) {
        _appThemeMode.value = theme
    }

    fun toggleJapanTimeZone() {
        _isJapanTimeZone.value = !_isJapanTimeZone.value
    }

    fun toggleQuietHours() {
        _quietHoursEnabled.value = !_quietHoursEnabled.value
    }

    fun loadSchedule(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }
            _errorMessage.value = null

            val result = repository.getSchedule(LocalDate.now(), 7)
            result.onSuccess { releases ->
                _rawReleases.value = releases
                scheduleRemindersForFavorites(releases, _favoriteIds.value)
            }.onFailure { ex ->
                _errorMessage.value = "No se pudo actualizar el calendario: ${ex.message}"
            }

            _isLoading.value = false
            _isRefreshing.value = false
        }
    }

    private fun scheduleRemindersForFavorites(releases: List<AnimeRelease>, favIds: Set<Int>) {
        if (favIds.isEmpty() || releases.isEmpty()) return
        val context = getApplication<Application>().applicationContext
        val nowMillis = System.currentTimeMillis()
        releases.filter { favIds.contains(it.id) }.forEach { anime ->
            if (anime.airingAtSeconds * 1000 > nowMillis - (15 * 60 * 1000)) {
                AnimeNotificationHelper.scheduleAnimeReminder(context, anime, anime.reminderMinutesBefore)
            }
        }
    }

    fun selectDate(date: LocalDate?) {
        _selectedDate.value = date
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleGenre(genre: String) {
        val current = _selectedGenres.value.toMutableSet()
        if (current.contains(genre)) {
            current.remove(genre)
        } else {
            current.add(genre)
        }
        _selectedGenres.value = current
    }

    fun clearGenres() {
        _selectedGenres.value = emptySet()
    }

    fun setSortOrder(order: AnimeSortOrder) {
        _sortOrder.value = order
    }

    fun toggleCategorizedByGenre() {
        _isCategorizedByGenre.value = !_isCategorizedByGenre.value
    }

    fun setEpisodeFilter(filter: EpisodeFilter) {
        _episodeFilter.value = filter
    }

    fun toggleOnlyFavorites() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun toggleFavorite(animeId: Int) {
        viewModelScope.launch {
            val anime = _rawReleases.value.find { it.id == animeId }
                ?: _selectedAnimeForDetail.value?.takeIf { it.id == animeId }
                ?: return@launch
            val wasAdded = repository.toggleFavorite(anime)
            if (wasAdded) {
                _notificationMessage.value = "¡${anime.displayTitle} añadido a favoritos! Notificación con WorkManager programada."
            }
        }
    }

    fun updateReminder(animeId: Int, enabled: Boolean, minutesBefore: Int = 15) {
        viewModelScope.launch {
            val anime = _rawReleases.value.find { it.id == animeId }
                ?: _selectedAnimeForDetail.value?.takeIf { it.id == animeId }
                ?: return@launch
            val updated = anime.copy(hasReminder = enabled, reminderMinutesBefore = minutesBefore)
            repository.updateReminder(updated, enabled, minutesBefore)
            updateAnimeInState(animeId) { it.copy(hasReminder = enabled, reminderMinutesBefore = minutesBefore) }
            _notificationMessage.value = if (enabled) {
                "Notificación WorkManager: $minutesBefore min antes de la emisión de '${anime.displayTitle}'"
            } else {
                "Recordatorio desactivado para '${anime.displayTitle}'"
            }
        }
    }

    fun testEpisodeNotification(anime: AnimeRelease) {
        val context = getApplication<Application>().applicationContext
        // Execute direct notification and enqueue via WorkManager
        repository.sendTestNotification(anime, anime.reminderMinutesBefore)
        com.example.notifications.AnimeEpisodeReminderWorker.scheduleImmediateTest(context, anime)
        _notificationMessage.value = "Notificación de WorkManager enviada para ${anime.displayTitle}"
    }

    fun clearNotificationMessage() {
        _notificationMessage.value = null
    }

    fun toggleSynopsisLanguage(animeId: Int) {
        val anime = _rawReleases.value.find { it.id == animeId } ?: return
        val newLang = if (anime.activeLanguage == SynopsisLanguage.SPANISH) {
            SynopsisLanguage.ENGLISH
        } else {
            SynopsisLanguage.SPANISH
        }
        setAnimeLanguage(animeId, newLang)
    }

    fun setAnimeLanguage(animeId: Int, language: SynopsisLanguage) {
        val anime = _rawReleases.value.find { it.id == animeId } ?: return

        if (language == SynopsisLanguage.SPANISH && anime.synopsisEs.isBlank() && anime.synopsisEn.isNotBlank()) {
            translateSynopsis(animeId, language)
            return
        }

        updateAnimeInState(animeId) { it.copy(activeLanguage = language) }
    }

    fun translateSynopsis(animeId: Int, targetLanguage: SynopsisLanguage = SynopsisLanguage.SPANISH) {
        viewModelScope.launch {
            val anime = _rawReleases.value.find { it.id == animeId } ?: return@launch
            updateAnimeInState(animeId) { it.copy(isTranslating = true) }

            val sourceText = if (targetLanguage == SynopsisLanguage.SPANISH) {
                anime.synopsisEn.ifBlank { anime.synopsis }
            } else {
                anime.synopsisEs.ifBlank { anime.synopsis }
            }

            val translated = repository.translateSynopsis(sourceText, targetLanguage)

            updateAnimeInState(animeId) { item ->
                if (targetLanguage == SynopsisLanguage.SPANISH) {
                    item.copy(
                        synopsisEs = translated,
                        activeLanguage = targetLanguage,
                        isTranslating = false
                    )
                } else {
                    item.copy(
                        synopsisEn = translated,
                        activeLanguage = targetLanguage,
                        isTranslating = false
                    )
                }
            }
        }
    }

    private fun updateAnimeInState(animeId: Int, transform: (AnimeRelease) -> AnimeRelease) {
        _rawReleases.value = _rawReleases.value.map { item ->
            if (item.id == animeId) transform(item) else item
        }
        if (_selectedAnimeForDetail.value?.id == animeId) {
            _selectedAnimeForDetail.value = _selectedAnimeForDetail.value?.let(transform)
        }
    }

    fun openDetail(anime: AnimeRelease) {
        _selectedAnimeForDetail.value = anime
    }

    fun closeDetail() {
        _selectedAnimeForDetail.value = null
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
