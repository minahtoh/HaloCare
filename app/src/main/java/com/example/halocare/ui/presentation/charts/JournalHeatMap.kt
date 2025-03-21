package com.example.halocare.ui.presentation.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.halocare.ui.models.JournalEntryData
import java.time.LocalDate

@Preview
@Composable
fun JournalHeatmap(
    //entries: List<JournalEntryData>,
    modifier: Modifier = Modifier) {
    val dummyJournals = listOf(
        JournalEntryData(LocalDate.now().minusDays(5), 3),
        JournalEntryData(LocalDate.now().minusDays(4), 2),
        JournalEntryData(LocalDate.now().minusDays(3), 1),
        JournalEntryData(LocalDate.now().minusDays(2), 0),
        JournalEntryData(LocalDate.now().minusDays(1), 6),
        JournalEntryData(LocalDate.now().minusDays(6), 4),
    )
    val today = LocalDate.now()
    val daysInMonth = today.lengthOfMonth()
    val startOfMonth = today.withDayOfMonth(1).dayOfWeek.value % 7 // Adjust for Sunday start
    val journalDays = dummyJournals.associateBy { it.date.dayOfMonth }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = today.month.name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            //contentPadding = PaddingValues(3.dp)
        ) {
            items(startOfMonth) { Spacer(modifier = Modifier.size(36.dp)) } // Empty spaces for alignment
            items(daysInMonth) { day ->
                val date = today.withDayOfMonth(day + 1)
                val entry = journalDays[day + 1]
                val color = when (entry?.entriesCount ?: 0) {
                    0 -> Color.LightGray // No entry
                    in 1..2 -> Color(0xFFB0E57C) // Light green
                    in 3..5 -> Color(0xFF7CC576) // Medium green
                    else -> Color(0xFF4CAF50) // Dark green (frequent journaling)
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .padding(4.dp)
                        .background(color, shape = CircleShape)
                        .clickable { /* Show journal preview */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = (day + 1).toString(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}