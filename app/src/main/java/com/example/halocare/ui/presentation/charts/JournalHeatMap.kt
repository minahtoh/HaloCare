package com.example.halocare.ui.presentation.charts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halocare.R
import com.example.halocare.ui.models.JournalEntry
import com.example.halocare.ui.models.JournalEntryData
import com.example.halocare.ui.presentation.TwoColumnNotebookText
import kotlinx.coroutines.delay
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



// Base sealed class for journal types (keeping this for shared properties)
sealed class JournalType {
    abstract val backgroundDrawable: Int
    abstract val fontFamily: FontFamily
    abstract val paddingTop: Dp
    abstract val paddingStart: Dp
    abstract val paddingEnd: Dp
    abstract val textColor: Color
    abstract val fontSize: TextUnit
    abstract val lineHeight: TextUnit
    abstract val dateTextPadding: Dp
    abstract val typeName: String
}

// Individual journal type implementations
object ScrollBackground : JournalType() {
    override val backgroundDrawable = R.drawable.scroll_bg
    override val fontFamily = FontFamily(Font(R.font.parisienne))
    override val paddingTop = 150.dp
    override val paddingStart = 5.dp
    override val paddingEnd = 0.dp
    override val textColor = Color.DarkGray.copy(alpha = 0.7f)
    override val fontSize = 17.sp
    override val lineHeight = 25.sp
    override val dateTextPadding = 130.dp
    override val typeName = "scroll"
}

object NotebookBackground : JournalType() {
    override val backgroundDrawable = R.drawable.paper_bg
    override val fontFamily = FontFamily(Font(R.font.patrick_hand))
    override val paddingTop = 120.dp
    override val paddingStart = 20.dp
    override val paddingEnd = 0.dp
    override val textColor = Color.Blue.copy(alpha = 0.5f)
    override val fontSize = 15.sp
    override val lineHeight = 15.sp
    override val dateTextPadding = 85.dp
    override val typeName = "notebook"
}

object StickyNoteBackground : JournalType() {
    override val backgroundDrawable = R.drawable.sticky_note_bg
    override val fontFamily = FontFamily(Font(R.font.architects_daughter))
    override val paddingTop = 160.dp
    override val paddingStart = 0.dp
    override val paddingEnd = 0.dp
    override val textColor = Color.DarkGray
    override val fontSize = 17.sp
    override val lineHeight = 25.sp
    override val dateTextPadding = 160.dp
    override val typeName = "sticky_note"
}

object OpenBookBackground : JournalType() {
    override val backgroundDrawable = R.drawable.open_journal_am_bg_f
    override val fontFamily = FontFamily(Font(R.font.satisfy))
    override val paddingTop = 195.dp
    override val paddingStart = 0.dp
    override val paddingEnd = 0.dp
    override val textColor = Color.DarkGray
    override val fontSize = 13.sp
    override val lineHeight = 15.sp
    override val dateTextPadding = 0.dp
    override val typeName = "open_book"
}

// Helper function to get the appropriate journal type
fun String.toJournalType(): JournalType {
    return when (this.lowercase()) {
        "scroll" -> ScrollBackground
        "notebook" -> NotebookBackground
        "sticky_note" -> StickyNoteBackground
        "open_book" -> OpenBookBackground
        else -> ScrollBackground
    }
}

// Separate Composables for each journal type with custom animations

@Composable
fun ScrollJournalView(
    entryText: String,
    date: String,
    modifier: Modifier = Modifier
) {
    var textVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300) // Wait for background to settle
        textVisible = true
    }

    Box(modifier = modifier) {
        // Background - Fixed size
        Image(
            painter = painterResource(id = R.drawable.scroll_bg),
            contentDescription = "Scroll Background",
            modifier = Modifier
                .height(600.dp)
                .width(450.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.Center),
        )

        // Animated text appearing like ink flowing
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(2000)) + slideInVertically(
                animationSpec = tween(2000),
                initialOffsetY = { 50 }
            )
        ) {
            Box(
                modifier = Modifier
                    .height(600.dp)
                    .width(450.dp)
                    .align(Alignment.Center)
            ) {
                // Main text positioned absolutely
                AnimatedText(
                    text =  formatTextWithLineBreaks(entryText,40),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.parisienne)),
                        fontSize = 18.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .width(340.dp)
                        .offset(x = 55.dp, y = 330.dp),
                    delayPerChar = 50L, // Adjust speed here
                    startDelay = 800L // Wait for background animation
                )

                // Date positioned absolutely
                Text(
                    text = "$date.",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.parisienne)),
                        fontSize = 18.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    ),
                    modifier = Modifier
                        .offset(x = 210.dp, y = 650.dp)
                )
            }
        }
    }
}

@Composable
fun NotebookJournalView(
    entryText: String,
    date: String,
    modifier: Modifier = Modifier
) {
    var textVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        textVisible = true
    }

    Box(modifier = modifier) {
        // Background - Fixed size
        Image(
            painter = painterResource(id = R.drawable.paper_bg),
            contentDescription = "Notebook Background",
            modifier = Modifier
                .height(600.dp)
                .width(450.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.Center),
        )

        // Text appearing like handwriting
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(1500)) + expandVertically(
                animationSpec = tween(1500),
                expandFrom = Alignment.Top
            )
        ) {
            Box(
                modifier = Modifier
                    .height(600.dp)
                    .width(450.dp)
                    .align(Alignment.Center)
            ) {
                // Main text positioned absolutely
                AnimatedText(
                    text =  formatTextWithLineBreaks(entryText,55),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.patrick_hand)),
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .width(380.dp)
                        .offset(x = 70.dp, y = 258.dp),
                    delayPerChar = 50L, // Adjust speed here
                    startDelay = 800L // Wait for background animation
                )

                // Date positioned absolutely
                Text(
                    text = "$date.",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.patrick_hand)),
                        fontSize = 15.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .offset(x = 300.dp, y = 675.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    delayPerChar: Long = 50L, // Milliseconds between each character
    startDelay: Long = 0L
) {
    var visibleChars by remember { mutableStateOf(0) }

    LaunchedEffect(text) {
        delay(startDelay)
        for (i in 0..text.length) {
            visibleChars = i
            delay(delayPerChar)
        }
    }

    Text(
        text = text.take(visibleChars),
        style = style,
        modifier = modifier
    )
}

@Composable
fun StickyNoteJournalView(
    entryText: String,
    date: String,
    modifier: Modifier = Modifier
) {
    var textVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        textVisible = true
    }

    Box(modifier = modifier) {
        // Background - Fixed size
        Image(
            painter = painterResource(id = R.drawable.sticky_note_bg),
            contentDescription = "Sticky Note Background",
            modifier = Modifier
                .height(600.dp)
                .width(450.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.Center),
        )

        // Text appearing quickly like jotting down notes
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(800)) + scaleIn(
                animationSpec = tween(800),
                initialScale = 0.8f
            )
        ) {
            Box(
                modifier = Modifier
                    .height(600.dp)
                    .width(450.dp)
                    .align(Alignment.Center)
            ) {
                // Main text positioned absolutely
                AnimatedText(
                    text =  formatTextWithLineBreaks(entryText,55),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.architects_daughter)),
                        fontSize = 17.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    ),
                    modifier = Modifier
                        .width(380.dp)
                        .offset(x = 32.dp, y = 310.dp),
                    delayPerChar = 50L, // Adjust speed here
                    startDelay = 800L // Wait for background animation
                )

                // Date positioned absolutely
                Text(
                    text = "$date.",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.architects_daughter)),
                        fontSize = 17.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    ),
                    modifier = Modifier
                        .offset(x = 250.dp, y = 600.dp)
                )
            }
        }
    }
}
@Composable
fun OpenBookJournalView(
    entryText: String,
    date: String,
    modifier: Modifier = Modifier
) {
    var textVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500) // Longer delay for book opening effect
        textVisible = true
    }

    Box(modifier = modifier) {
        // Background - Fixed size
        Image(
            painter = painterResource(id = R.drawable.open_journal_am_bg_f),
            contentDescription = "Open Book Background",
            modifier = Modifier
                .height(600.dp)
                .width(450.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.Center),
        )

        // Text appearing like pages turning
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(2500)) + slideInHorizontally(
                animationSpec = tween(2500),
                initialOffsetX = { -it / 4 }
            )
        ) {
            Box(
                modifier = Modifier
                    .height(600.dp)
                    .width(450.dp)
                    .align(Alignment.Center)
            ) {
                // Position TwoColumnNotebookText absolutely
                Box(
                    modifier = Modifier
                        .width(450.dp)
                        .height(400.dp)
                        .offset(x = 32.dp, y = 395.dp)
                ) {
                    TwoColumnNotebookText(
                        fullText = formatTextWithLineBreaks(entryText, 35),
                        date = "$date.",
                        font = FontFamily(Font(R.font.satisfy)),
                        maxCharsPerColumn = 100
                    )
                }
            }
        }
    }
}

fun formatTextWithLineBreaks(text: String, maxCharsPerLine: Int): String {
    return text.split(" ").fold("" to "") { (result, currentLine), word ->
        if (currentLine.length + word.length + 1 <= maxCharsPerLine) {
            result to if (currentLine.isEmpty()) word else "$currentLine $word"
        } else {
            val newResult = if (result.isEmpty()) currentLine else "$result\n$currentLine"
            newResult to word
        }
    }.let { (result, lastLine) ->
        if (result.isEmpty()) lastLine else "$result\n$lastLine"
    }
}