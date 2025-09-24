package com.example.halocare.ui.presentation.charts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.halocare.ui.presentation.TwoColumnNotebookText
import com.example.halocare.ui.presentation.responsive
import com.example.halocare.ui.presentation.responsiveHeight
import com.example.halocare.ui.presentation.responsiveWidth
import kotlinx.coroutines.delay
import responsiveSp
import java.time.LocalDate

@Preview
@Composable
fun JournalHeatmap(
    entries: List<JournalEntry> = emptyList(),
    modifier: Modifier = Modifier,
    onDateClicked : (List<JournalEntry>) -> Unit = {},
    isDarkMode : Boolean
) {

    val today = LocalDate.now()
    val daysInMonth = today.lengthOfMonth()
    val startOfMonth = today.withDayOfMonth(1).dayOfWeek.value % 7

    val journalDays = entries.groupingBy { it.date.dayOfMonth }.eachCount()

    val containerHeight = 400.dp.responsiveHeight()      // 360dp ≈ 46% of screen height
    val itemSize = 46.dp.responsiveWidth()             // 46dp ≈ 11% of width for 7 columns

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(containerHeight)
            .shadow(elevation = 7.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(11.dp)
    ) {
        Text(
            text = "Journal History",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp.responsiveSp()),
            modifier = Modifier.padding(bottom = 8.dp, start = 13.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if(isDarkMode) MaterialTheme.colorScheme.primaryContainer else
                        MaterialTheme.colorScheme.surfaceTint,
                    shape = RoundedCornerShape(7.dp)
                )
                .padding(2.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = today.month.name,
                style = MaterialTheme.typography.titleLarge.responsive(),
                modifier = Modifier.padding(top = 1.dp, start = 17.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(containerHeight)
            ) {
                items(startOfMonth) {
                    Spacer(modifier = Modifier.size(itemSize))
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

                    val isToday = date == today

                    Box(
                        modifier = Modifier
                            .size(itemSize)
                            .padding(itemSize * 0.086f) // 4dp ≈ 8.6% of itemSize
                            .then(
                                if (isToday) {
                                    Modifier
                                        .border(
                                            BorderStroke(
                                                itemSize * 0.065f,
                                               color = if(!isDarkMode) MaterialTheme.colorScheme.tertiaryContainer
                                                        else MaterialTheme.colorScheme.secondary
                                            ),
                                            shape = RoundedCornerShape(itemSize * 0.087f)
                                        )
                                        .background(
                                            color,
                                            shape = RoundedCornerShape(itemSize * 0.087f)
                                        )
                                        .shadow(
                                            elevation = 17.dp,
                                            shape = RoundedCornerShape(itemSize * 0.087f)
                                        )
                                } else {
                                    Modifier
                                        .background(color, shape = CircleShape)
                                        .shadow(elevation = 17.dp, shape = CircleShape)
                                }
                            )
                            .clickable {
                                val entriesForDay = entries.filter { it.date.dayOfMonth == day + 1 }
                                onDateClicked(entriesForDay)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (day + 1).toString(),
                            style = MaterialTheme.typography.bodySmall.responsive()
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
    abstract val textColor: Color
    abstract val typeName: String

    // Make these composable functions instead of properties
    @Composable
    abstract fun paddingTop(): Dp
    @Composable
    abstract fun paddingStart(): Dp
    @Composable
    abstract fun paddingEnd(): Dp
    @Composable
    abstract fun fontSize(): TextUnit
    @Composable
    abstract fun lineHeight(): TextUnit
    @Composable
    abstract fun dateTextPadding(): Dp
}

// Individual journal type implementations
object ScrollBackground : JournalType() {
    override val backgroundDrawable = R.drawable.scroll_bg
    override val fontFamily = FontFamily(Font(R.font.parisienne))
    override val textColor = Color.DarkGray.copy(alpha = 0.5f)
    override val typeName = "scroll"

    @Composable
    override fun paddingTop() = 150.dp.responsiveHeight()
    @Composable
    override fun paddingStart() = 5.dp.responsiveWidth()
    @Composable
    override fun paddingEnd() = 0.dp.responsiveWidth()
    @Composable
    override fun fontSize() = 17.sp.responsiveSp()
    @Composable
    override fun lineHeight() = 25.sp.responsiveSp()
    @Composable
    override fun dateTextPadding() = 130.dp.responsiveHeight()
}

object NotebookBackground : JournalType() {
    override val backgroundDrawable = R.drawable.paper_bg
    override val fontFamily = FontFamily(Font(R.font.patrick_hand))
    override val textColor = Color.Blue.copy(alpha = 0.5f)
    override val typeName = "notebook"

    @Composable
    override fun paddingTop() = 120.dp.responsiveHeight()
    @Composable
    override fun paddingStart() = 20.dp.responsiveWidth()
    @Composable
    override fun paddingEnd() = 0.dp.responsiveWidth()
    @Composable
    override fun fontSize() = 15.sp.responsiveSp()
    @Composable
    override fun lineHeight() = 15.sp.responsiveSp()
    @Composable
    override fun dateTextPadding() = 85.dp.responsiveHeight()
}

object StickyNoteBackground : JournalType() {
    override val backgroundDrawable = R.drawable.sticky_note_bg
    override val fontFamily = FontFamily(Font(R.font.architects_daughter))
    override val textColor = Color.DarkGray
    override val typeName = "sticky_note"

    @Composable
    override fun paddingTop() = 160.dp.responsiveHeight()
    @Composable
    override fun paddingStart() = 0.dp.responsiveWidth()
    @Composable
    override fun paddingEnd() = 0.dp.responsiveWidth()
    @Composable
    override fun fontSize() = 17.sp.responsiveSp()
    @Composable
    override fun lineHeight() = 25.sp.responsiveSp()
    @Composable
    override fun dateTextPadding() = 160.dp.responsiveHeight()
}

object OpenBookBackground : JournalType() {
    override val backgroundDrawable = R.drawable.open_journal_am_bg_f
    override val fontFamily = FontFamily(Font(R.font.satisfy))
    override val textColor = Color.DarkGray
    override val typeName = "open_book"

    @Composable
    override fun paddingTop() = 195.dp.responsiveHeight()
    @Composable
    override fun paddingStart() = 0.dp.responsiveWidth()
    @Composable
    override fun paddingEnd() = 0.dp.responsiveWidth()
    @Composable
    override fun fontSize() = 13.sp.responsiveSp()
    @Composable
    override fun lineHeight() = 15.sp.responsiveSp()
    @Composable
    override fun dateTextPadding() = 0.dp.responsiveHeight()
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
    val totalEntryAnimDuration = entryText.length * 50L + 1500L
    var showDateText by remember { mutableStateOf(false) }

    LaunchedEffect(entryText) {
        showDateText = false
        delay(totalEntryAnimDuration)
        showDateText = true
    }



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
                .height(600.dp.responsiveHeight())
                .width(450.dp.responsiveWidth())
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.Center),
        )

        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(1500)) + expandVertically(
                animationSpec = tween(1500),
                expandFrom = Alignment.Top
            )
        ) {
            Box(
                modifier = Modifier
                    .height(600.dp.responsiveHeight())
                    .width(450.dp.responsiveWidth())
                    .align(Alignment.Center)
            ) {
                // Main text positioned absolutely
                AnimatedText(
                    text =  formatTextWithLineBreaks(entryText,40),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.parisienne)),
                        fontSize = 18.sp.responsiveSp(),
                        lineHeight = 25.sp.responsiveSp(),
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .width(340.dp.responsiveWidth())
                        .offset(x = 55.dp, y = 330.dp),
                    delayPerChar = 50L, // Adjust speed here
                    startDelay = 800L // Wait for background animation
                )

                // Date positioned absolutely
                if (showDateText) {
                    AnimatedText(
                        text = "$date.",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.parisienne)),
                            fontSize = 18.sp.responsiveSp(),
                            lineHeight = 25.sp.responsiveSp(),
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        ),
                        modifier = Modifier
                            .offset(x = 210.dp, y = 650.dp),
                        delayPerChar = 160L,
                        startDelay = 0L
                    )
                }
            }
        }
    }
}


@Composable
fun NewScrollJournalView(
    entryText: String,
    date: String,
    modifier: Modifier = Modifier
) {
    var textVisible by remember { mutableStateOf(false) }
    val totalEntryAnimDuration = entryText.length * 50L + 1500L
    var showDateText by remember { mutableStateOf(false) }

    LaunchedEffect(entryText) {
        showDateText = false
        delay(totalEntryAnimDuration)
        showDateText = true
    }

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
                .height(600.dp.responsiveHeight())
                .width(450.dp.responsiveWidth())
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.Center),
        )

        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(1500)) + expandVertically(
                animationSpec = tween(1500),
                expandFrom = Alignment.Top
            )
        ) {
            Box(
                modifier = Modifier
                    .height(600.dp.responsiveHeight())
                    .width(450.dp.responsiveWidth())
                    .align(Alignment.Center)
            ) {
                // Main text positioned absolutely
                AnimatedText(
                    text = formatTextWithLineBreaks(entryText, 40),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.parisienne)),
                        fontSize = 18.sp.responsiveSp(),
                        lineHeight = 25.sp.responsiveSp(),
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .width(340.dp.responsiveWidth())
                        .offset(
                            x = 55.dp.responsiveWidth(),
                            y = 330.dp.responsiveHeight() ) ,
                    delayPerChar = 50L, // Adjust speed here
                    startDelay = 800L // Wait for background animation
                )

                // Date positioned absolutely
                if (showDateText) {
                    AnimatedText(
                        text = "$date.",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.parisienne)),
                            fontSize = 18.sp.responsiveSp(),
                            lineHeight = 25.sp.responsiveSp(),
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        ),
                        modifier = Modifier
                            .offset(
                                x = 200.dp.responsiveWidth(),
                                y = 610.dp.responsiveHeight()
                            ),
                        delayPerChar = 160L,
                        startDelay = 0L
                    )
                }
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
    val totalEntryAnimDuration = entryText.length * 50L + 1500L
    var showDateText by remember { mutableStateOf(false) }

    LaunchedEffect(entryText) {
        showDateText = false
        delay(totalEntryAnimDuration)
        showDateText = true
    }

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
                .height(600.dp.responsiveHeight())
                .width(450.dp.responsiveWidth())
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
                    .height(600.dp.responsiveHeight())
                    .width(450.dp.responsiveWidth())
                    .align(Alignment.Center)
            ) {
                // Main text positioned absolutely
                AnimatedText(
                    text =  formatTextWithLineBreaks(entryText,55),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.patrick_hand)),
                        fontSize = 16.sp.responsiveSp(),
                        lineHeight = 22.sp.responsiveSp(),
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .width(380.dp.responsiveWidth())
                        .offset(x = 70.dp, y = 258.dp),
                    delayPerChar = 50L, // Adjust speed here
                    startDelay = 800L // Wait for background animation
                )

                // Date positioned absolutely
                if (showDateText) {
                    AnimatedText(
                        text = "$date.",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.patrick_hand)),
                            fontSize = 15.sp.responsiveSp(),
                            lineHeight = 15.sp.responsiveSp(),
                            fontWeight = FontWeight.Bold,
                            color =  Color.Blue.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .offset(x = 300.dp, y = 675.dp),
                        delayPerChar = 160L,
                        startDelay = 0L
                    )
                }
            }
        }
    }
}

@Composable
fun NewNotebookJournalView(
    entryText: String,
    date: String,
    modifier: Modifier = Modifier
) {
    var textVisible by remember { mutableStateOf(false) }
    val totalEntryAnimDuration = entryText.length * 50L + 1500L
    var showDateText by remember { mutableStateOf(false) }

    LaunchedEffect(entryText) {
        showDateText = false
        delay(totalEntryAnimDuration)
        showDateText = true
    }

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
                .height(600.dp.responsiveHeight())
                .width(450.dp.responsiveWidth())
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
                    .height(600.dp.responsiveHeight())
                    .width(450.dp.responsiveWidth())
                    .align(Alignment.Center)
            ) {
                // Main text positioned absolutely
                AnimatedText(
                    text = formatTextWithLineBreaks(entryText, 55),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.patrick_hand)),
                        fontSize = 16.sp.responsiveSp(),
                        lineHeight = 22.sp.responsiveSp(),
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .width(380.dp.responsiveWidth()) // ✅ Made responsive
                        .offset(
                            x = 70.dp.responsiveWidth(),   // ✅ Made responsive
                            y = 258.dp.responsiveHeight()  // ✅ Made responsive
                        ),
                    delayPerChar = 50L, // Adjust speed here
                    startDelay = 800L // Wait for background animation
                )

                // Date positioned absolutely
                if (showDateText) {
                    AnimatedText(
                        text = "$date.",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.patrick_hand)),
                            fontSize = 15.sp.responsiveSp(),
                            lineHeight = 15.sp.responsiveSp(),
                            fontWeight = FontWeight.Bold,
                            color = Color.Blue.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .offset(
                                x = 290.dp.responsiveWidth(),  // ✅ Made responsive
                                y = 672.dp.responsiveHeight()  // ✅ Made responsive
                            ),
                        delayPerChar = 160L,
                        startDelay = 0L
                    )
                }
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
    val totalEntryAnimDuration = entryText.length * 50L + 1000L
    var showDateText by remember { mutableStateOf(false) }

    LaunchedEffect(entryText) {
        showDateText = false
        delay(totalEntryAnimDuration)
        showDateText = true
    }

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
                .height(600.dp.responsiveHeight())
                .width(450.dp.responsiveWidth())
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
                    .height(600.dp.responsiveHeight())
                    .width(450.dp.responsiveHeight())
                    .align(Alignment.Center)
            ) {
                // Main text positioned absolutely
                AnimatedText(
                    text =  formatTextWithLineBreaks(entryText,55),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.architects_daughter)),
                        fontSize = 17.sp.responsiveSp(),
                        lineHeight = 25.sp.responsiveSp(),
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
                if (showDateText) {
                    AnimatedText(
                        text = "$date.",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.architects_daughter)),
                            fontSize = 17.sp.responsiveSp(),
                            lineHeight = 25.sp.responsiveSp(),
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        ),
                        modifier = Modifier
                            .offset(x = 250.dp, y = 600.dp),
                        delayPerChar = 160L,
                        startDelay = 0L
                    )
                }
            }
        }
    }
}

@Composable
fun NewStickyNoteJournalView(
    entryText: String,
    date: String,
    modifier: Modifier = Modifier
) {
    var textVisible by remember { mutableStateOf(false) }
    val totalEntryAnimDuration = entryText.length * 50L + 1000L
    var showDateText by remember { mutableStateOf(false) }

    LaunchedEffect(entryText) {
        showDateText = false
        delay(totalEntryAnimDuration)
        showDateText = true
    }

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
                .height(600.dp.responsiveHeight())
                .width(450.dp.responsiveWidth())
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
                    .height(600.dp.responsiveHeight())
                    .width(450.dp.responsiveWidth()) // ✅ Fixed: was responsiveHeight()
                    .align(Alignment.Center)
            ) {
                // Main text positioned absolutely
                AnimatedText(
                    text = formatTextWithLineBreaks(entryText, 55),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.architects_daughter)),
                        fontSize = 17.sp.responsiveSp(),
                        lineHeight = 25.sp.responsiveSp(),
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    ),
                    modifier = Modifier
                        .width(380.dp.responsiveWidth()) // ✅ Made responsive
                        .offset(
                            x = 32.dp.responsiveWidth(),   // ✅ Made responsive
                            y = 310.dp.responsiveHeight()  // ✅ Made responsive
                        ),
                    delayPerChar = 50L, // Adjust speed here
                    startDelay = 800L // Wait for background animation
                )

                // Date positioned absolutely
                if (showDateText) {
                    AnimatedText(
                        text = "$date.",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.architects_daughter)),
                            fontSize = 17.sp.responsiveSp(),
                            lineHeight = 25.sp.responsiveSp(),
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        ),
                        modifier = Modifier
                            .offset(
                                x = 250.dp.responsiveWidth(),  // ✅ Made responsive
                                y = 585.dp.responsiveHeight()  // ✅ Made responsive
                            ),
                        delayPerChar = 160L,
                        startDelay = 0L
                    )
                }
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
                .height(600.dp.responsiveHeight())
                .width(450.dp.responsiveWidth())
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.Center),
        )

        // Text appearing like pages turning
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(1500)) + expandVertically(
                animationSpec = tween(1500),
                expandFrom = Alignment.Top
            )
        ) {
            Box(
                modifier = Modifier
                    .height(600.dp.responsiveHeight())
                    .width(450.dp.responsiveWidth())
                    .align(Alignment.Center)
            ) {
                // Position TwoColumnNotebookText absolutely
                Box(
                    modifier = Modifier
                        .width(450.dp.responsiveWidth())
                        .height(400.dp.responsiveHeight())
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


@Composable
fun NewOpenBookJournalView(
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
                .height(600.dp.responsiveHeight())
                .width(450.dp.responsiveWidth())
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.Center),
        )

        // Text appearing like pages turning
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(1500)) + expandVertically(
                animationSpec = tween(1500),
                expandFrom = Alignment.Top
            )
        ) {
            Box(
                modifier = Modifier
                    .height(600.dp.responsiveHeight())
                    .width(450.dp.responsiveWidth())
                    .align(Alignment.Center)
            ) {
                // Position TwoColumnNotebookText absolutely
                Box(
                    modifier = Modifier
                        .width(450.dp.responsiveWidth())  // ✅ Already responsive
                        .height(400.dp.responsiveHeight()) // ✅ Already responsive
                        .offset(
                            x = 32.dp.responsiveWidth(),   // ✅ Made responsive
                            y = 395.dp.responsiveHeight()  // ✅ Made responsive
                        )
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