package com.example.halocare.ui.presentation.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.halocare.ui.models.JournalEntry
import com.example.halocare.ui.models.JournalEntryData
import java.time.LocalDate

@Preview
@Composable
fun JournalHeatmap(
    entries: List<JournalEntry> = emptyList(), // Use the actual data class now
    modifier: Modifier = Modifier,
    onDateClicked : (List<JournalEntry>) -> Unit = {}
) {
    val today = LocalDate.now()
    val daysInMonth = today.lengthOfMonth()
    val startOfMonth = today.withDayOfMonth(1).dayOfWeek.value % 7

    // Group entries by day and count how many entries per date
    val journalDays = entries.groupingBy { it.date.dayOfMonth }.eachCount()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .shadow(elevation = 7.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(11.dp)
    ) {
        Text(
            text = "Journal History",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp, start = 13.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceTint, shape = RoundedCornerShape(7.dp))
                .padding(5.dp)
        ) {
            Text(
            text = today.month.name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp, start = 27.dp))


            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                items(startOfMonth) {
                    Spacer(modifier = Modifier.size(36.dp))
                }

                items(daysInMonth) { day ->
                    val date = today.withDayOfMonth(day + 1)
                    val count = journalDays[day + 1] ?: 0

                    val color = when (count) {
                        0 -> Color.LightGray
                        in 1..2 -> Color(0xFFB0E57C)
                        in 3..5 -> Color(0xFF7CC576)
                        else -> Color(0xFF4CAF50)
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .padding(4.dp)
                            .background(color, shape = CircleShape)
                            .shadow(elevation = 17.dp, shape = CircleShape)
                            .clickable {
                                val entriesForDay = entries.filter { it.date.dayOfMonth == day + 1 }
                                onDateClicked(entriesForDay)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (day + 1).toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
