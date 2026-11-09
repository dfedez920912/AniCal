package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AnimeRelease
import com.example.model.CountdownStatus
import com.example.model.SynopsisLanguage
import com.example.ui.theme.AniGold
import com.example.ui.theme.AniSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeCard(
    anime: AnimeRelease,
    onCardClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleReminder: () -> Unit = {},
    onToggleWatched: () -> Unit = {},
    zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault(),
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val countdown = anime.getCountdownStatus()
    val formattedTime = anime.getFormattedTime(zoneId)
    val activeSynopsis = anime.getActiveSynopsis()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick)
            .testTag("anime_card_${anime.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Estreno vs Episodio del Día badge & Time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge for Estreno vs Episodio del Día
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (anime.isSeriesPremiere) {
                        AniSecondary.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (anime.isSeriesPremiere) AniSecondary else MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = if (anime.isSeriesPremiere) "NUEVO ESTRENO" else "EPISODIO DEL DÍA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (anime.isSeriesPremiere) AniSecondary else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Airing time chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = when (countdown) {
                            is CountdownStatus.AiringNow -> AniSecondary
                            is CountdownStatus.Upcoming -> MaterialTheme.colorScheme.primary
                            is CountdownStatus.Aired -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "$formattedTime • ${countdown.text}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (countdown) {
                            is CountdownStatus.AiringNow -> AniSecondary
                            is CountdownStatus.Upcoming -> MaterialTheme.colorScheme.primary
                            is CountdownStatus.Aired -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Main Row: Poster on left + Info on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cover Image with Badge
                Box(
                    modifier = Modifier
                        .width(105.dp)
                        .height(148.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(anime.coverImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Carátula de ${anime.displayTitle}",
                        modifier = Modifier
                            .width(105.dp)
                            .height(148.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Episode badge
                    Surface(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.TopStart),
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                    ) {
                        Text(
                            text = "EP ${anime.episode}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Format badge (TV / Movie)
                    Surface(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.BottomEnd),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Text(
                            text = anime.format,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Info Section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(148.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Title and Favorite + Reminder buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = anime.displayTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Watched Episode Checkbox Button
                                IconButton(
                                    onClick = onToggleWatched,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("watched_btn_${anime.id}")
                                ) {
                                    Icon(
                                        imageVector = if (anime.isWatched) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = if (anime.isWatched) "Marcar como no visto" else "Marcar como visto",
                                        tint = if (anime.isWatched) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Reminder Bell (active if favorite)
                                if (anime.isFavorite) {
                                    IconButton(
                                        onClick = onToggleReminder,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("reminder_btn_${anime.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (anime.hasReminder) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                            contentDescription = "Recordatorio de emisión",
                                            tint = if (anime.hasReminder) AniGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Favorite Bookmark button
                                IconButton(
                                    onClick = onFavoriteToggle,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("favorite_btn_${anime.id}")
                                ) {
                                    Icon(
                                        imageVector = if (anime.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = if (anime.isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                                        tint = if (anime.isFavorite) AniSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Romaji Title + Platform Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (anime.streamingPlatform.lowercase()) {
                                    "netflix" -> Color(0xFFE50914).copy(alpha = 0.2f)
                                    "crunchyroll" -> Color(0xFFF47521).copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                }
                            ) {
                                Text(
                                    text = anime.streamingPlatform,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (anime.streamingPlatform.lowercase()) {
                                        "netflix" -> Color(0xFFFF5252)
                                        "crunchyroll" -> Color(0xFFFF9800)
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            if (anime.isWatched) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF00E676).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Visto ✓",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E676),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Metadata row: Score + Studio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (anime.score != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Puntuación",
                                    tint = AniGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = String.format("%.1f", anime.score),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (!anime.studio.isNullOrBlank()) {
                            Text(
                                text = "•  ${anime.studio}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Genres chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                anime.genres.take(4).forEach { genre ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = genre,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Synopsis Header with Language Switcher Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sinopsis:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Translation language pill button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable(enabled = !anime.isTranslating, onClick = onToggleLanguage)
                        .testTag("translate_btn_${anime.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (anime.isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Traducir",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Text(
                            text = if (anime.activeLanguage == SynopsisLanguage.SPANISH) "Idioma: ES (Cambiar a EN)" else "Language: EN (Cambiar a ES)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Synopsis Preview with Expand / Collapse
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Text(
                    text = activeSynopsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                    lineHeight = 18.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Mostrar menos" else "Leer sinopsis completa",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .clickable(onClick = onCardClick)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Detalles",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Ficha",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
