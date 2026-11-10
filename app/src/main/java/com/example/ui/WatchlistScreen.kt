package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.WatchlistEntity
import com.example.data.local.WatchlistStatus
import com.example.ui.components.AnimeDetailDialog
import com.example.ui.theme.AniGold
import com.example.ui.theme.AniSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: AnimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Hardware/Gesture back handler: returns to Schedule screen
    BackHandler {
        viewModel.setNavScreen(MainNavScreen.SCHEDULE)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.setNavScreen(MainNavScreen.SCHEDULE) },
                        modifier = Modifier.testTag("watchlist_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar al Calendario"
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = AniSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Mi Watchlist",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (uiState.watchlistCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AniSecondary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${uiState.watchlistCount}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AniSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
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
            // Search field within Watchlist
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = uiState.watchlistSearchQuery,
                        onValueChange = { viewModel.setWatchlistSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("watchlist_search_bar"),
                        placeholder = {
                            Text(
                                text = "Buscar en tu Watchlist...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar en Watchlist",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (uiState.watchlistSearchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        viewModel.setWatchlistSearchQuery("")
                                        focusManager.clearFocus()
                                    },
                                    modifier = Modifier.testTag("watchlist_clear_search_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("watchlist_status_chips")
                    ) {
                        item {
                            val isAllSelected = uiState.watchlistFilterStatus == null
                            FilterChip(
                                selected = isAllSelected,
                                onClick = { viewModel.setWatchlistFilter(null) },
                                label = { Text("Todos (${uiState.watchlistCount})", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        items(WatchlistStatus.values()) { status ->
                            val isSelected = uiState.watchlistFilterStatus == status
                            val countForStatus = uiState.watchlistItems.count { it.status == status.name }
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setWatchlistFilter(status) },
                                label = { Text("${status.displayName} ($countForStatus)", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (status) {
                                        WatchlistStatus.PLAN_TO_WATCH -> MaterialTheme.colorScheme.primaryContainer
                                        WatchlistStatus.WATCHING -> AniSecondary.copy(alpha = 0.25f)
                                        WatchlistStatus.COMPLETED -> Color(0xFF00E676).copy(alpha = 0.25f)
                                        WatchlistStatus.DROPPED -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    selectedLabelColor = when (status) {
                                        WatchlistStatus.PLAN_TO_WATCH -> MaterialTheme.colorScheme.primary
                                        WatchlistStatus.WATCHING -> AniSecondary
                                        WatchlistStatus.COMPLETED -> Color(0xFF00E676)
                                        WatchlistStatus.DROPPED -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // Watchlist items or Empty State
            if (uiState.filteredWatchlistItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .testTag("watchlist_empty_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = if (uiState.watchlistCount == 0) "Tu Watchlist está vacía" else "Sin resultados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = if (uiState.watchlistCount == 0) {
                                    "Guarda los animes que planeas ver o estés siguiendo desde el Calendario usando el botón de marcador o guardar."
                                } else {
                                    "No se encontraron animes con los filtros de búsqueda aplicados."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    viewModel.setWatchlistSearchQuery("")
                                    viewModel.setWatchlistFilter(null)
                                    viewModel.setNavScreen(MainNavScreen.SCHEDULE)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("explore_schedule_from_watchlist_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Explorar Calendario de Estrenos")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("watchlist_anime_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredWatchlistItems, key = { it.animeId }) { item ->
                        WatchlistAnimeCard(
                            item = item,
                            onClick = {
                                viewModel.openDetail(item.toAnimeRelease())
                            },
                            onStatusChange = { newStatus ->
                                viewModel.updateWatchlistStatus(item.animeId, newStatus)
                            },
                            onIncrementEpisode = {
                                val nextEp = item.watchedEpisodes + 1
                                viewModel.updateWatchlistEpisodes(item.animeId, nextEp)
                            },
                            onRemove = {
                                viewModel.removeFromWatchlist(item.animeId)
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog when clicked
    uiState.selectedAnimeForDetail?.let { selected ->
        AnimeDetailDialog(
            anime = selected,
            onDismiss = { viewModel.closeDetail() },
            onFavoriteToggle = { viewModel.toggleFavorite(selected.id) },
            onLanguageSelected = { lang -> viewModel.setAnimeLanguage(selected.id, lang) },
            onUpdateReminder = { enabled, minutes -> viewModel.updateReminder(selected.id, enabled, minutes) },
            onTestNotification = { viewModel.testEpisodeNotification(selected) },
            onToggleWatched = { viewModel.toggleWatchedEpisode(selected) },
            onToggleWatchlist = { viewModel.toggleWatchlist(selected) },
            onRatingChanged = { rating -> viewModel.setUserRating(selected.id, rating) },
            watchedEpisodeKeys = uiState.watchedEpisodeKeys,
            onToggleWatchedEpisodeNum = { epNum ->
                viewModel.toggleWatchedEpisodeNum(selected.id, epNum, selected.displayTitle)
            },
            zoneId = if (uiState.isJapanTimeZone) java.time.ZoneId.of("Asia/Tokyo") else java.time.ZoneId.systemDefault()
        )
    }
}

@Composable
fun WatchlistAnimeCard(
    item: WatchlistEntity,
    onClick: () -> Unit,
    onStatusChange: (WatchlistStatus) -> Unit,
    onIncrementEpisode: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isStatusMenuOpen by remember { mutableStateOf(false) }

    val totalEpCount = item.totalEpisodes ?: 12
    val progressFraction = if (totalEpCount > 0) {
        (item.watchedEpisodes.toFloat() / totalEpCount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("watchlist_item_${item.animeId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Poster Image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.coverImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Portada de ${item.displayTitle}",
                modifier = Modifier
                    .width(85.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            // Info Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Remove button
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("remove_watchlist_${item.animeId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar de la Watchlist",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Metadata chips: Studio & Score & Format
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = item.format,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        if (item.score != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AniGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = String.format("%.1f", item.score),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        item.studio?.let { st ->
                            Text(
                                text = "• $st",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Episode Progress
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ep. ${item.watchedEpisodes} / ${item.totalEpisodes ?: '?'}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Status selector chip with dropdown menu
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (item.statusEnum) {
                                    WatchlistStatus.PLAN_TO_WATCH -> MaterialTheme.colorScheme.primaryContainer
                                    WatchlistStatus.WATCHING -> AniSecondary.copy(alpha = 0.2f)
                                    WatchlistStatus.COMPLETED -> Color(0xFF00E676).copy(alpha = 0.2f)
                                    WatchlistStatus.DROPPED -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier
                                    .clickable { isStatusMenuOpen = true }
                                    .testTag("status_picker_${item.animeId}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = item.statusEnum.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (item.statusEnum) {
                                            WatchlistStatus.PLAN_TO_WATCH -> MaterialTheme.colorScheme.primary
                                            WatchlistStatus.WATCHING -> AniSecondary
                                            WatchlistStatus.COMPLETED -> Color(0xFF00E676)
                                            WatchlistStatus.DROPPED -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isStatusMenuOpen,
                                onDismissRequest = { isStatusMenuOpen = false }
                            ) {
                                WatchlistStatus.values().forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st.displayName) },
                                        leadingIcon = {
                                            if (st == item.statusEnum) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            onStatusChange(st)
                                            isStatusMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                progressFraction >= 1f -> Color(0xFF00E676)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        // +1 Ep quick increment button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clickable(onClick = onIncrementEpisode)
                                .testTag("increment_ep_${item.animeId}")
                        ) {
                            Text(
                                text = "+1 Ep",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
