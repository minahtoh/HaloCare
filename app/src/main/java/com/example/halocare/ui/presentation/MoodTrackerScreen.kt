import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.halocare.R
import com.example.halocare.ui.models.HaloMoodEntry
import com.example.halocare.ui.presentation.charts.MoodChart
import com.example.halocare.viewmodel.MainViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

//@Preview(widthDp = 320, heightDp = 720)
@Composable
fun MoodTrackerScreen(
    mainViewModel: MainViewModel,
    onBackIconClick: () -> Unit
){
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val zoneId = ZoneId.systemDefault()

    val startOfDayMillis = selectedDate
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()

    val endOfDayMillis = selectedDate
        .plusDays(1)
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli() - 1
    var showMoodDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val moodsList by mainViewModel.dailyMoods.collectAsState()
    val dailyAdvice by mainViewModel.todayAdvice.collectAsState()
    val context = LocalContext.current


    // Track whether the user has scrolled down
    val isTopBarVisible by remember {
        derivedStateOf { scrollState.value < 50 }  // Adjust threshold as needed
    }
    LaunchedEffect(Unit){
        mainViewModel.apply {
            getTodaysMood(startOfDayMillis,endOfDayMillis)
            getTodayAdvice()
            toastMessage.collect { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showMoodDialog = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_draw_24),
                    contentDescription = "Log Mood",
                    tint = Color.White)
            }
        },
        topBar = {
            AnimatedVisibility(
                visible = isTopBarVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
            ) {
                MoodTrackerTopBar(onBackIconClick)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(scrollState)
        ) {
            // Date Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                WeekDateSelector(
                    selectedDate = selectedDate,
                     onDateSelected = {
                         selectedDate = it
                         val startOfDay = it
                             .atStartOfDay(zoneId)
                             .toInstant()
                             .toEpochMilli()

                         val endOfDay = it
                             .plusDays(1)
                             .atStartOfDay(zoneId)
                             .toInstant()
                             .toEpochMilli() - 1
                         mainViewModel.getTodaysMood(startOfDay,endOfDay)
                     }
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            // Mood Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                MoodChart(moodEntries = moodsList ?: emptyList())
            }

            // Mood Insights Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Mood Insights",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.emoji_adviser),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .rotate(360f),
                            tint = Color.Unspecified
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You've logged 'Happy' 5 times this week! Keep it up!", fontSize = 14.sp)
                }
            }

            // Inspirational Quote Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = dailyAdvice ?: " ",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic)
                }
            }

            if (showMoodDialog) {
                Dialog(onDismissRequest = { showMoodDialog = false }) {
                    MoodEntryLogger(
                        onLogUserMood = {
                            mainViewModel.logMoodEntry(it)
                            showMoodDialog = false
                        }
                    )
                }
            }

        }
    }
}



@Composable
fun WeekDateSelector(
    selectedDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = remember(selectedDate) {
        val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }
    val today = remember { LocalDate.now() }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        contentPadding = PaddingValues()
    ) {
        items(daysOfWeek) { date ->
            val isSelected = date == selectedDate
            val isFutureDate = date.isAfter(today)
            val isClickable = !isFutureDate


            val backgroundColor = if (isSelected && isClickable) {
                MaterialTheme.colorScheme.primary // Highlight selected & enabled
            } else {
                Color.Transparent
            }

            val dayOfWeekColor = when {
                isSelected && isClickable -> MaterialTheme.colorScheme.onPrimary
                isFutureDate -> Color.Gray.copy(alpha = 0.4f) // Dim future dates
                else -> Color.Gray // Default non-selected day color
            }

            val dayOfMonthColor = when {
                isSelected && isClickable -> MaterialTheme.colorScheme.onPrimary
                isFutureDate -> Color.Gray.copy(alpha = 0.4f) // Dim future dates
                else -> MaterialTheme.colorScheme.onSurface // Default non-selected date color
            }


            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = isClickable) {
                        onDateSelected(date)
                    }
                    .background(
                        backgroundColor
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()),
                    color = dayOfWeekColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = date.dayOfMonth.toString(),
                    color = dayOfMonthColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun MoodEntryLogger(
    onLogUserMood : (HaloMoodEntry) -> Unit = {}
) {
    var selectedIcon by remember {
        mutableStateOf<MoodIconData?>(null)
    }
    var entryText by remember { mutableStateOf("") }
    val maxCharacters = 80

     val moodIcons = listOf(
        MoodIconData(R.drawable.angry_icon,"angry"),
        MoodIconData(R.drawable.bandss_icon, "bandss af"),
        MoodIconData(R.drawable.aristotle_icon, "im saying"),
        MoodIconData(R.drawable.nowayyy_icon, "aint no way"),
        MoodIconData(R.drawable.smartguy_icon, "nerdd"),
        MoodIconData(R.drawable.stoned_icon, "normalll"),
        MoodIconData(R.drawable.duhh_icon, "duhh"),
        MoodIconData(R.drawable.tearss_icon, "crying"),
        MoodIconData(R.drawable.owkay_icon, "okay"),
        MoodIconData(R.drawable.lmfaoo_icon, "lmfaooo"),
        MoodIconData(R.drawable.traumatised_icon, "jesus"),
        MoodIconData(R.drawable.depressed_icon, "depressed")
    )


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Emoji selector using ExposedDropdownMenuBox
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    label = {
                       if (selectedIcon == null) Text("Select Mood") else Text(selectedIcon!!.iconName)
                            },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = selectedIcon?.icon ?:R.drawable.duhh_icon),
                            contentDescription = "Selected mood",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle mood dropdown"
                        )
                    }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .height(250.dp)
                        .width(250.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .height(250.dp)
                                .width(250.dp)
                                .padding(8.dp),
                            userScrollEnabled = true
                        ) {
                            items(moodIcons) { moodIcon ->
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            selectedIcon = moodIcon
                                            expanded = false
                                        }
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Image(
                                            painter = painterResource(id = moodIcon.icon),
                                            contentDescription = "Mood Icon",
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text input for mood description with character limit
            OutlinedTextField(
                value = entryText,
                onValueChange = {
                    if (it.length <= maxCharacters) entryText = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Wassup...") },
                label = { Text("Gist") },
                minLines = 2,
                maxLines = 3,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${entryText.length}/$maxCharacters",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            Button(
                onClick = {
                    if (entryText.isNotBlank() && selectedIcon != null) {
                        // Reset form after submission
                        val moodEntry = HaloMoodEntry(
                            iconRes = selectedIcon!!.icon,
                            gist = entryText,
                            timeLogged = System.currentTimeMillis()
                        )
                        onLogUserMood(moodEntry)
                        entryText = ""

                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                enabled = entryText.isNotBlank()
            ) {
                Text("Log Mood")
            }
        }
    }
}

data class MoodIconData(
    val icon : Int,
    val iconName : String
)




@Composable
fun MoodTrackerTopBar(
    onBackIconClick : ()-> Unit
){
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier,
        shadowElevation = 7.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp, start = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50),
                shadowElevation = 5.dp,
                modifier = Modifier.size(30.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_keyboard_double_arrow_right_24),
                    contentDescription = "back",
                    modifier = Modifier
                        .rotate(180f)
                        .clickable { onBackIconClick() },
                    colorFilter = ColorFilter.tint(
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                )
            }
            Text(
                text = "Mood Tracker",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.surfaceTint,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                shadowElevation = 5.dp,
                modifier = Modifier.size(30.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_psychology_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    }
}

