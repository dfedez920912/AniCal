package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DayScheduleTab
import com.example.ui.theme.AniGold
import com.example.ui.theme.AniSecondary
import java.time.LocalDate

@Composable
fun DateSelector(
    tabs: List<DayScheduleTab>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("date_selector_row"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // "Todos" (Full week) pill
        item {
            val isAllSelected = selectedDate == null
            val backgroundColor by animateColorAsState(
                targetValue = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                label = "all_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "all_text"
            )

            Surface(
                modifier = Modifier
                    .height(72.dp)
                    .width(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDateSelected(null) }
                    .testTag("date_tab_all"),
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = if (isAllSelected) 6.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Todos los días",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Semana",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Completa",
                        fontSize = 9.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Each day of the week
        items(tabs, key = { it.date.toString() }) { tab ->
            val isSelected = selectedDate == tab.date
            val isToday = tab.isToday

            val containerColor by animateColorAsState(
                targetValue = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                label = "card_container"
            )

            val contentColor by animateColorAsState(
                targetValue = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "card_content"
            )

            val borderColor = if (isToday && !isSelected) {
                MaterialTheme.colorScheme.primary
            } else if (isSelected) {
                Color.White.copy(alpha = 0.4f)
            } else {
                Color.Transparent
            }

            Surface(
                modifier = Modifier
                    .height(72.dp)
                    .width(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = if (isToday || isSelected) 1.5.dp else 0.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onDateSelected(tab.date) }
                    .testTag("date_tab_${tab.date}"),
                color = containerColor,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = if (isSelected) 6.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header tag (HOY / MAÑANA / Day Name)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) AniGold else AniSecondary)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "HOY",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Text(
                                text = tab.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Day of month number
                    Text(
                        text = tab.dayOfMonth.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor
                    )

                    // Episode count badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${tab.episodeCount} ep",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}
