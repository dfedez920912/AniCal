package com.example.ui

import android.annotation.SuppressLint
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AnimeSortOrder
import com.example.model.EpisodeFilter
import com.example.ui.components.AnimeCard
import com.example.ui.components.AnimeDetailDialog
import com.example.ui.components.DateSelector
import com.example.ui.theme.AniGold
import com.example.ui.theme.AniSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeScheduleScreen(
    viewModel: AnimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var isThemeDialogVisible by remember { mutableStateOf(false) }
    var isFiltersExpanded by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 25) {
            isFiltersExpanded = false
        }
    }

    // Runtime permission launcher for Android 13+ (POST_NOTIFICATIONS)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(uiState.notificationMessage) {
        uiState.notificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotificationMessage()
        }
    }

    val today = remember { LocalDate.now() }
    val todayFormatted = remember {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es"))
        today.format(formatter).replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "AniCal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "CALENDARIO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // TimeZone Switch Button (Local vs Tokyo/JST)
                    IconButton(
                        onClick = { viewModel.toggleJapanTimeZone() },
                        modifier = Modifier.testTag("timezone_toggle_button")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (uiState.isJapanTimeZone) AniSecondary else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (uiState.isJapanTimeZone) "Japón" else "Local",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (uiState.isJapanTimeZone) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Theme and Settings Button
                    IconButton(
                        onClick = { isThemeDialogVisible = true },
                        modifier = Modifier.testTag("theme_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Cambiar tema visual",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Favorites filter button
                    IconButton(
                        onClick = { viewModel.toggleOnlyFavorites() },
                        modifier = Modifier.testTag("favorites_filter_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.favoritesCount > 0) {
                                    Badge(containerColor = AniSecondary) {
                                        Text("${uiState.favoritesCount}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.onlyFavorites) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Filtrar favoritos",
                                tint = if (uiState.onlyFavorites) AniSecondary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Search toggle button (magnifying glass)
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar anime",
                            tint = if (isSearchExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Refresh schedule
                    IconButton(
                        onClick = { viewModel.loadSchedule(isRefresh = true) },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar calendario"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = uiState.currentNavScreen == MainNavScreen.SCHEDULE,
                    onClick = { viewModel.setNavScreen(MainNavScreen.SCHEDULE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendario"
                        )
                    },
                    label = { Text("Calendario", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_schedule_tab")
                )
                NavigationBarItem(
                    selected = uiState.currentNavScreen == MainNavScreen.WATCHLIST,
                    onClick = { viewModel.setNavScreen(MainNavScreen.WATCHLIST) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.watchlistCount > 0) {
                                    Badge(containerColor = AniSecondary) {
                                        Text("${uiState.watchlistCount}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Mi Watchlist"
                            )
                        }
                    },
                    label = { Text("Mi Watchlist", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_watchlist_tab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Expandable Search Bar with smooth AnimatedVisibility transition
            AnimatedVisibility(visible = isSearchExpanded) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("anime_search_bar_container")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("anime_search_bar"),
                            placeholder = {
                                Text(
                                    text = "Buscar anime por título...",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Icono de búsqueda",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                viewModel.setSearchQuery("")
                                                focusManager.clearFocus()
                                            },
                                            modifier = Modifier.testTag("clear_search_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Borrar búsqueda",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { isSearchExpanded = false },
                                        modifier = Modifier.testTag("close_search_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Cerrar búsqueda",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )

                        // Quick suggestion chips when not searching, or active search summary when searching
                        if (uiState.searchQuery.isBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Sugerencias:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    val suggestions = listOf("Solo Leveling", "Bleach", "One Piece", "Dandadan", "Blue Lock")
                                    items(suggestions) { item ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .clickable { viewModel.setSearchQuery(item) }
                                        ) {
                                            Text(
                                                text = item,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Active search feedback banner (non-wrapping)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, start = 4.dp, end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Resultados para \"${uiState.searchQuery}\"",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier
                                            .clickable {
                                                viewModel.setSearchQuery("")
                                                focusManager.clearFocus()
                                            }
                                    ) {
                                        Text(
                                            text = "Limpiar",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${uiState.filteredReleases.size} animes encontrados",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Horizontal Date Tabs Strip (Day of week selector)
            DateSelector(
                tabs = uiState.dayTabs,
                selectedDate = uiState.selectedDate,
                onDateSelected = { date -> viewModel.selectDate(date) }
            )

            // Collapsible Filters Toggle Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { isFiltersExpanded = !isFiltersExpanded },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isFiltersExpanded) "Ocultar filtros avanzados ▴" else "Filtros avanzados y géneros ▾",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (uiState.selectedGenres.isNotEmpty() || uiState.selectedPlatform != com.example.model.StreamingPlatform.ALL || uiState.episodeFilter != EpisodeFilter.ALL) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AniSecondary
                        ) {
                            Text(
                                text = "Filtro activo",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isFiltersExpanded) {
                Column {
                    // Episode Type Filter: All vs Episodios del Día vs Nuevos Estrenos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EpisodeFilter.values().forEach { filter ->
                            val isSelected = uiState.episodeFilter == filter
                            val count = when (filter) {
                                EpisodeFilter.ALL -> uiState.todayPremieresCount + uiState.todayOngoingCount
                                EpisodeFilter.ONGOING_EPISODES -> uiState.todayOngoingCount
                                EpisodeFilter.PREMIERES -> uiState.todayPremieresCount
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setEpisodeFilter(filter) },
                                label = {
                                    Text(
                                        text = "${filter.shortLabel} ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (filter == EpisodeFilter.PREMIERES) AniSecondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = if (filter == EpisodeFilter.PREMIERES) AniSecondary else MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                            )
                        }
                    }

                    // Genre & Sort Control Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Categorize by Genre toggle button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.isCategorizedByGenre) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { viewModel.toggleCategorizedByGenre() }
                                .testTag("toggle_categorized_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = if (uiState.isCategorizedByGenre) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (uiState.isCategorizedByGenre) "Por Géneros: ON" else "Categorizar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isCategorizedByGenre) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Sort Dropdown Button
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { isSortMenuExpanded = true }
                                    .testTag("sort_menu_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Orden: ${uiState.sortOrder.shortLabel}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isSortMenuExpanded,
                                onDismissRequest = { isSortMenuExpanded = false }
                            ) {
                                AnimeSortOrder.values().forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = order.label,
                                                fontWeight = if (uiState.sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                                color = if (uiState.sortOrder == order) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOrder(order)
                                            isSortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Genre Filter Chips Horizontal Scroll
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("genres_filter_row"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Clear all genres chip
                        item {
                            val isAll = uiState.selectedGenres.isEmpty()
                            FilterChip(
                                selected = isAll,
                                onClick = { viewModel.clearGenres() },
                                label = { Text("Todos los géneros", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("genre_chip_all")
                            )
                        }

                        items(uiState.availableGenres) { genre ->
                            val isSelected = uiState.selectedGenres.contains(genre)
                            val count = uiState.genreCounts[genre] ?: 0
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleGenre(genre) },
                                label = {
                                    Text(
                                        text = if (count > 0) "$genre ($count)" else genre,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("genre_chip_$genre")
                            )
                        }
                    }

                    // Streaming Platform Filter Chips Row (Crunchyroll, Netflix, Disney+, Prime Video)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("platform_filter_row"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Dónde ver:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp, top = 8.dp)
                            )
                        }

                        items(com.example.model.StreamingPlatform.values()) { platform ->
                            val isSelected = uiState.selectedPlatform == platform
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setPlatformFilter(platform) },
                                label = {
                                    Text(
                                        text = platform.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("platform_chip_${platform.name}")
                            )
                        }
                    }
                }
            }

            // Section Header Bar
            val currentHeaderTitle = when {
                uiState.searchQuery.isNotBlank() -> "Resultados de Búsqueda"
                uiState.onlyFavorites -> "Mis Animes Favoritos"
                uiState.selectedDate == null -> "Todos los Episodios de la Semana"
                uiState.selectedDate == today -> when (uiState.episodeFilter) {
                    EpisodeFilter.ALL -> "Episodios y Estrenos de Hoy"
                    EpisodeFilter.ONGOING_EPISODES -> "Episodios del Día en Emisión"
                    EpisodeFilter.PREMIERES -> "Nuevos Estrenos de Temporada"
                }
                uiState.selectedDate == today.plusDays(1) -> when (uiState.episodeFilter) {
                    EpisodeFilter.ALL -> "Episodios y Estrenos de Mañana"
                    EpisodeFilter.ONGOING_EPISODES -> "Episodios en Emisión de Mañana"
                    EpisodeFilter.PREMIERES -> "Nuevos Estrenos de Mañana"
                }
                else -> {
                    val dayName = uiState.selectedDate!!.dayOfWeek
                        .getDisplayName(TextStyle.FULL, Locale("es"))
                        .replaceFirstChar { it.uppercase() }
                    "Programación del $dayName ${uiState.selectedDate!!.dayOfMonth}"
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentHeaderTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${uiState.filteredReleases.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                if (uiState.onlyFavorites || uiState.searchQuery.isNotBlank() || uiState.selectedGenres.isNotEmpty()) {
                    Text(
                        text = "Limpiar filtros",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                viewModel.setSearchQuery("")
                                viewModel.clearGenres()
                                if (uiState.onlyFavorites) viewModel.toggleOnlyFavorites()
                            }
                            .padding(4.dp)
                    )
                }
            }

            // Schedule Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading && uiState.allReleases.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando episodios y estrenos...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    uiState.filteredReleases.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (uiState.onlyFavorites) Icons.Default.BookmarkBorder else Icons.Default.Movie,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) "No se encontraron animes para \"${uiState.searchQuery}\""
                                else if (uiState.onlyFavorites) "No tienes animes favoritos guardados"
                                else if (uiState.selectedGenres.isNotEmpty()) "No hay animes para los géneros seleccionados"
                                else "No hay episodios con estos filtros para este día",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) "Verifica la ortografía o intenta buscar por otro título (ej. Solo Leveling, Bleach, One Piece, Dandadan)."
                                else if (uiState.onlyFavorites) "Toca el icono de marcapáginas en cualquier anime para seguir sus estrenos."
                                else "Prueba seleccionando otro género o borrando los filtros activos.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.setSearchQuery("")
                                    viewModel.clearGenres()
                                    viewModel.setEpisodeFilter(EpisodeFilter.ALL)
                                    if (uiState.onlyFavorites) viewModel.toggleOnlyFavorites()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Restablecer todos los filtros")
                            }
                        }
                    }

                    // Categorized by Genre View Mode
                    uiState.isCategorizedByGenre && uiState.releasesByGenre.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("categorized_anime_list"),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            uiState.releasesByGenre.forEach { (genreName, animeList) ->
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = genreName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "${animeList.size}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                items(
                                    items = animeList,
                                    key = { "cat_${genreName}_${it.id}_${it.airingId}" }
                                ) { anime ->
                                    val activeZone = if (uiState.isJapanTimeZone) java.time.ZoneId.of("Asia/Tokyo") else java.time.ZoneId.systemDefault()
                                    AnimeCard(
                                        anime = anime,
                                        onCardClick = { viewModel.openDetail(anime) },
                                        onFavoriteToggle = { viewModel.toggleFavorite(anime.id) },
                                        onToggleLanguage = { viewModel.toggleSynopsisLanguage(anime.id) },
                                        onToggleReminder = {
                                            viewModel.updateReminder(anime.id, !anime.hasReminder, anime.reminderMinutesBefore)
                                        },
                                        onToggleWatched = { viewModel.toggleWatchedEpisode(anime) },
                                        zoneId = activeZone
                                    )
                                }
                            }
                        }
                    }

                    // Standard Sorted List View
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("anime_schedule_list"),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(
                                items = uiState.filteredReleases,
                                key = { "${it.id}_${it.airingId}" }
                            ) { anime ->
                                val activeZone = if (uiState.isJapanTimeZone) java.time.ZoneId.of("Asia/Tokyo") else java.time.ZoneId.systemDefault()
                                AnimeCard(
                                    anime = anime,
                                    onCardClick = { viewModel.openDetail(anime) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(anime.id) },
                                    onToggleLanguage = { viewModel.toggleSynopsisLanguage(anime.id) },
                                    onToggleReminder = {
                                        viewModel.updateReminder(anime.id, !anime.hasReminder, anime.reminderMinutesBefore)
                                    },
                                    onToggleWatched = { viewModel.toggleWatchedEpisode(anime) },
                                    zoneId = activeZone
                                )
                            }
                        }
                    }
                }
            }
        }

        // Theme and Preferences Settings Dialog
        if (isThemeDialogVisible) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { isThemeDialogVisible = false }
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .testTag("theme_settings_dialog")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ajustes y Personalización",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { isThemeDialogVisible = false }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Cerrar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tema Visual de la Aplicación",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        com.example.model.AppThemeMode.values().forEach { mode ->
                            val isSelected = uiState.appThemeMode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.setAppThemeMode(mode) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = mode.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = mode.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Zona Horaria & Notificaciones",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Timezone Toggle Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleJapanTimeZone() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Hora de Japón (JST / Tokio)",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (uiState.isJapanTimeZone) "Activo: Mostrando horarios de emisión japonesa" else "Inactivo: Mostrando según la hora de tu dispositivo",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                androidx.compose.material3.Switch(
                                    checked = uiState.isJapanTimeZone,
                                    onCheckedChange = { viewModel.toggleJapanTimeZone() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quiet hours Toggle Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleQuietHours() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Modo Silencioso Nocturno (23:00 - 07:00)",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Silencia recordatorios durante las horas de descanso",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                androidx.compose.material3.Switch(
                                    checked = uiState.quietHoursEnabled,
                                    onCheckedChange = { viewModel.toggleQuietHours() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full Detail Modal Dialog
        uiState.selectedAnimeForDetail?.let { anime ->
            val activeZone = if (uiState.isJapanTimeZone) java.time.ZoneId.of("Asia/Tokyo") else java.time.ZoneId.systemDefault()
            AnimeDetailDialog(
                anime = anime,
                onDismiss = { viewModel.closeDetail() },
                onFavoriteToggle = { viewModel.toggleFavorite(anime.id) },
                onLanguageSelected = { lang -> viewModel.setAnimeLanguage(anime.id, lang) },
                onUpdateReminder = { enabled, minutes ->
                    viewModel.updateReminder(anime.id, enabled, minutes)
                },
                onTestNotification = {
                    viewModel.testEpisodeNotification(anime)
                },
                onToggleWatched = { viewModel.toggleWatchedEpisode(anime) },
                onToggleWatchlist = { viewModel.toggleWatchlist(anime) },
                watchedEpisodeKeys = uiState.watchedEpisodeKeys,
                onToggleWatchedEpisodeNum = { epNum ->
                    viewModel.toggleWatchedEpisodeNum(anime.id, epNum, anime.displayTitle)
                },
                zoneId = activeZone
            )
        }
    }
}
