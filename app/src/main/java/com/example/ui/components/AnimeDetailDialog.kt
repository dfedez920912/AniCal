package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AnimeRelease
import com.example.model.CountdownStatus
import com.example.model.SynopsisLanguage
import com.example.ui.theme.AniGold
import com.example.ui.theme.AniSecondary
import java.time.format.TextStyle
import java.util.Locale

@SuppressLint("NewApi")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailDialog(
    anime: AnimeRelease,
    onDismiss: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onLanguageSelected: (SynopsisLanguage) -> Unit,
    onUpdateReminder: (enabled: Boolean, minutesBefore: Int) -> Unit = { _, _ -> },
    onTestNotification: () -> Unit = {},
    onToggleWatched: () -> Unit = {},
    onToggleWatchlist: () -> Unit = {},
    onRatingChanged: (rating: Int) -> Unit = {},
    watchedEpisodeKeys: Set<String> = emptySet(),
    onToggleWatchedEpisodeNum: (episodeNumber: Int) -> Unit = {},
    zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val countdown = anime.getCountdownStatus()
    val localDate = anime.getAiringLocalDate(zoneId)
    val dayName = localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es"))
        .replaceFirstChar { it.uppercase() }
    val formattedTime = anime.getFormattedTime(zoneId)
    val activeSynopsis = anime.getActiveSynopsis()

    var reminderMinutes by remember { mutableStateOf(anime.reminderMinutesBefore) }
    var reminderEnabled by remember { mutableStateOf(anime.hasReminder) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("anime_detail_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Hero Image with Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(anime.bannerImageUrl ?: anime.coverImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Banner de ${anime.displayTitle}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient Scrim
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.surface
                                        )
                                    )
                                )
                        )

                        // Inset Poster at bottom-left
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(anime.coverImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = anime.displayTitle,
                                modifier = Modifier
                                    .width(86.dp)
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (anime.isSeriesPremiere) AniSecondary else MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = if (anime.isSeriesPremiere) "ESTRENO • EP 1" else "EPISODIO ${anime.episode}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$dayName a las $formattedTime",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Content Body
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Title
                        Text(
                            text = anime.displayTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!anime.titleEnglish.isNullOrBlank() && anime.titleRomaji != anime.titleEnglish) {
                            Text(
                                text = anime.titleRomaji,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Airing Countdown Banner
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = when (countdown) {
                                is CountdownStatus.AiringNow -> AniSecondary.copy(alpha = 0.2f)
                                is CountdownStatus.Upcoming -> MaterialTheme.colorScheme.primaryContainer
                                is CountdownStatus.Aired -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (anime.isSeriesPremiere) "Gran Estreno del Día" else "Episodio Semanal en Emisión",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${countdown.text} ($dayName $formattedTime)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Notification Reminder Settings Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reminder_settings_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                            contentDescription = null,
                                            tint = if (reminderEnabled) AniGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Aviso antes de emitir",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (reminderEnabled) "Te avisaremos $reminderMinutes min antes" else "Alertas desactivadas",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = reminderEnabled,
                                        onCheckedChange = { checked ->
                                            reminderEnabled = checked
                                            onUpdateReminder(checked, reminderMinutes)
                                        },
                                        modifier = Modifier.testTag("reminder_switch")
                                    )
                                }

                                if (reminderEnabled) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tiempo de anticipación:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(15 to "15 min", 30 to "30 min", 60 to "1 hora", 0 to "Al emitir").forEach { (mins, label) ->
                                            val isSel = reminderMinutes == mins
                                            FilterChip(
                                                selected = isSel,
                                                onClick = {
                                                    reminderMinutes = mins
                                                    onUpdateReminder(true, mins)
                                                },
                                                label = { Text(label, fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Button to test notification immediately
                                OutlinedButton(
                                    onClick = onTestNotification,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("test_notification_btn"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Probar notificación de alerta ahora",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Attributes Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DetailStatBox(
                                label = "PUNTUACIÓN",
                                value = if (anime.score != null) "⭐ ${String.format("%.1f", anime.score)}" else "N/A",
                                modifier = Modifier.weight(1f)
                            )
                            DetailStatBox(
                                label = "ESTUDIO",
                                value = anime.studio ?: "Varios",
                                modifier = Modifier.weight(1f)
                            )
                            DetailStatBox(
                                label = "FORMATO",
                                value = anime.format,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // User 5-Star Rating Card Component
                        var currentRating by remember(anime.id, anime.userRating) { mutableStateOf(anime.userRating) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("user_rating_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AniGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Tu Calificación",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = if (currentRating > 0) "$currentRating / 5 ★" else "Sin calificar",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (currentRating > 0) AniGold else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 5 Interactive Stars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    (1..5).forEach { starIndex ->
                                        val isSelected = starIndex <= currentRating
                                        IconButton(
                                            onClick = {
                                                val newRating = if (currentRating == starIndex) 0 else starIndex
                                                currentRating = newRating
                                                onRatingChanged(newRating)
                                            },
                                            modifier = Modifier
                                                .size(38.dp)
                                                .testTag("star_rating_$starIndex")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Calificar $starIndex estrellas",
                                                tint = if (isSelected) AniGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = when (currentRating) {
                                        1 -> "1 estrella • Malo"
                                        2 -> "2 estrellas • Regular"
                                        3 -> "3 estrellas • Bueno"
                                        4 -> "4 estrellas • Muy bueno"
                                        5 -> "5 estrellas • ¡Excelente!"
                                        else -> "Toca una estrella para calificar este anime"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Progress Tracker & Episode List
                        val totalEpCount = anime.totalEpisodes ?: anime.episode.coerceAtLeast(12)
                        val watchedCountForAnime = (1..totalEpCount).count { epNum ->
                            watchedEpisodeKeys.contains("${anime.id}_$epNum") || (epNum == anime.episode && anime.isWatched)
                        }
                        val progressFraction = if (totalEpCount > 0) watchedCountForAnime.toFloat() / totalEpCount.toFloat() else 0f

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("episode_progress_section")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF00E676),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Progreso de Episodios",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "$watchedCountForAnime / $totalEpCount vistos (${(progressFraction * 100).toInt()}%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (progressFraction >= 1f) Color(0xFF00E676) else MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF00E676),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Toca cada episodio para marcarlo o desmarcarlo como visto:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 160.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        (1..totalEpCount).forEach { epNum ->
                                            val isCurrentAiringEp = epNum == anime.episode
                                            val isEpWatched = watchedEpisodeKeys.contains("${anime.id}_$epNum") || (isCurrentAiringEp && anime.isWatched)

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = when {
                                                    isEpWatched -> Color(0xFF00E676).copy(alpha = 0.2f)
                                                    isCurrentAiringEp -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> MaterialTheme.colorScheme.surface
                                                },
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = 1.dp,
                                                    color = when {
                                                        isEpWatched -> Color(0xFF00E676)
                                                        isCurrentAiringEp -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                    }
                                                ),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        if (isCurrentAiringEp) {
                                                            onToggleWatched()
                                                        } else {
                                                            onToggleWatchedEpisodeNum(epNum)
                                                        }
                                                    }
                                                    .testTag("episode_chip_$epNum")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isEpWatched) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                        contentDescription = "Episodio $epNum",
                                                        tint = if (isEpWatched) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = "Ep $epNum" + if (isCurrentAiringEp) " (Hoy)" else "",
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isEpWatched || isCurrentAiringEp) FontWeight.Bold else FontWeight.Medium,
                                                        color = when {
                                                            isEpWatched -> Color(0xFF00E676)
                                                            isCurrentAiringEp -> MaterialTheme.colorScheme.primary
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Genres
                        Text(
                            text = "Géneros",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            anime.genres.forEach { genre ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = genre,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Synopsis Header with Translation Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Sinopsis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (anime.isTranslating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Language Selector Pills (Español | English)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                SynopsisLanguage.values().forEach { lang ->
                                    val isSelected = anime.activeLanguage == lang
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier
                                            .clickable { onLanguageSelected(lang) }
                                            .padding(0.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            if (lang == SynopsisLanguage.SPANISH) {
                                                Icon(
                                                    imageVector = Icons.Default.Translate,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(11.dp),
                                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = lang.shortLabel,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Synopsis Text
                        Text(
                            text = activeSynopsis,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons: Row 1 (Favorite, Watchlist) & Row 2 (Watched, Calendar, Share)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = onFavoriteToggle,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_fav_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (anime.isFavorite) AniSecondary else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (anime.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (anime.isFavorite) "Favorito" else "+ Favorito",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Watchlist Button
                                OutlinedButton(
                                    onClick = onToggleWatchlist,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_watchlist_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (anime.isInWatchlist) AniSecondary else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (anime.isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (anime.isInWatchlist) AniSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (anime.isInWatchlist) "Watchlist" else "+ Watchlist",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Watched Checkbox Button
                                OutlinedButton(
                                    onClick = onToggleWatched,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_watched_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (anime.isWatched) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (anime.isWatched) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Visto",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (anime.isWatched) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (anime.isWatched) "Visto" else "Marcar Visto",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Export to Calendar (Google Calendar / Native Calendar)
                                OutlinedButton(
                                    onClick = {
                                        val startMillis = anime.airingAtSeconds * 1000
                                        val endMillis = startMillis + (25 * 60 * 1000) // 25 min episode
                                        val calIntent = Intent(Intent.ACTION_EDIT).apply {
                                            type = "vnd.android.cursor.item/event"
                                            putExtra("beginTime", startMillis)
                                            putExtra("endTime", endMillis)
                                            putExtra("allDay", false)
                                            putExtra("title", "Estreno: ${anime.displayTitle} Ep ${anime.episode}")
                                            putExtra("description", "Emisión en ${anime.streamingPlatform}. ${anime.getActiveSynopsis().take(120)}...")
                                            putExtra("eventLocation", anime.streamingPlatform)
                                        }
                                        try {
                                            context.startActivity(calIntent)
                                        } catch (e: Exception) {
                                            val sendIntent = Intent(Intent.ACTION_INSERT).apply {
                                                data = android.provider.CalendarContract.Events.CONTENT_URI
                                                putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                                                putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                                                putExtra(android.provider.CalendarContract.Events.TITLE, "Estreno: ${anime.displayTitle} Ep ${anime.episode}")
                                                putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Emisión en ${anime.streamingPlatform}")
                                            }
                                            context.startActivity(sendIntent)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_calendar_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Añadir a Calendario",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Calendario",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Share Button
                                OutlinedButton(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "¡El episodio ${anime.episode} de ${anime.displayTitle} estrena el $dayName a las $formattedTime en ${anime.streamingPlatform}!\n\nCalendario de Anime AniVexa"
                                            )
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Compartir estreno")
                                        context.startActivity(shareIntent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_share_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Compartir",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Compartir",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Close Button Top-Right
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("close_detail_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
