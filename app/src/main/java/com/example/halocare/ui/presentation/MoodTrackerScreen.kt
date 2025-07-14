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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.halocare.R
import com.example.halocare.ui.models.HaloMoodEntry
import com.example.halocare.ui.presentation.charts.MoodChart
import com.example.halocare.ui.presentation.rememberStatusBarController
import com.example.halocare.viewmodel.MainViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

//@Preview(widthDp = 320, heightDp = 720)
@Composable
fun MoodTrackerScreen(
    mainViewModel: MainViewModel,
    onBackIconClick: () -> Unit
){
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.errorContainer

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

    val isTopBarVisible by remember {
        derivedStateOf { scrollState.value < 50 }  // Adjust threshold as needed
    }
    LaunchedEffect(Unit){
        statusBarController.updateStatusBar(
            color = statusBarColor,
            darkIcons = true
        )
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
                containerColor = MaterialTheme.colorScheme.inversePrimary,
                modifier = Modifier.padding(end = 20.dp, bottom = 50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_draw_24),
                    contentDescription = "Log Mood",
                    tint = Color.White)
            }
        },
        topBar = {
            MoodTrackerTopBar(onBackIconClick)
        },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 5 .dp, vertical = 5.dp)
            .verticalScroll(scrollState)
        ) {

            Spacer(modifier = Modifier.height(3.dp))
            // Date Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(
                            bottomStart = 15.dp, bottomEnd = 15.dp,
                            topStart = 15.dp, topEnd = 15.dp
                        )
                    )
                    .background(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(
                            bottomStart = 15.dp, bottomEnd = 15.dp,
                            topStart = 15.dp, topEnd = 15.dp
                        )
                    )
                    .padding(top = 4.dp, start = 2.dp, end = 2.dp)
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
                Spacer(modifier = Modifier.height(5.dp))

                // Mood Chart
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                        .shadow(elevation = 1.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(
                                bottomStart = 15.dp, bottomEnd = 15.dp,
                                topStart = 2.dp, topEnd = 2.dp
                            )
                        )
                        .padding(bottom = 4.dp, top = 2.dp, start = 2.dp, end = 2.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(Color.LightGray, shape = RoundedCornerShape(11.dp))
                        ,
                    ) {
                        MoodChart(moodEntries = moodsList ?: emptyList())
                    }
                }
            }


            // Mood Insights Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Mood Insights",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.wazirr_mirr),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp),
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
                    .padding(3.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Gandalf's says",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.wzzrd),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp),
                        tint = Color.Unspecified
                    )
                }
                Column(modifier = Modifier.padding(
                    bottom = 16.dp, start = 16.dp, end = 16.dp)) {
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
                        },
                        onClose = {
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
        modifier = Modifier
            .fillMaxWidth()
            .background(
                shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
                color = Color.Unspecified
            ),
        horizontalArrangement = Arrangement.SpaceAround,
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(daysOfWeek) { date ->
            val isSelected = date == selectedDate
            val isFutureDate = date.isAfter(today)
            val isClickable = !isFutureDate


            val backgroundColor = if (isSelected && isClickable) {
                MaterialTheme.colorScheme.errorContainer // Highlight selected & enabled
            } else {
                MaterialTheme.colorScheme.tertiary
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
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(enabled = isClickable) {
                        onDateSelected(date)
                    }
                    .background(
                        backgroundColor
                    )
                    .padding(vertical = 2.dp, horizontal = 13.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                Column(
                    modifier = Modifier
                        .height(25.dp)
                        .width(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceTint,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        color = dayOfWeekColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(25.dp))
                Column(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            color = MaterialTheme.colorScheme.inversePrimary,
                            shape = RoundedCornerShape(7.dp)
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = dayOfMonthColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(7.dp))
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun MoodEntryLogger(
    onLogUserMood : (HaloMoodEntry) -> Unit = {},
    onClose : () -> Unit = {}
) {
    var selectedIcon by remember {
        mutableStateOf<MoodIconData?>(null)
    }
    var entryText by remember { mutableStateOf("") }
    val maxCharacters = 80

     val moodIcons = listOf(
        MoodIconData(R.drawable.angry_icon,"angry"),
        MoodIconData(R.drawable.bandss_icon, "bandss af"),
       // MoodIconData(R.drawable.aristotle_icon, "im saying"),
        MoodIconData(R.drawable.nowayyy_icon, "aint no way"),
       // MoodIconData(R.drawable.smartguy_icon, "nerdd"),
        MoodIconData(R.drawable.stoned_icon, "normalll"),
        MoodIconData(R.drawable.duhh_icon, "duhh"),
        MoodIconData(R.drawable.tearss_icon, "crying"),
        MoodIconData(R.drawable.owkay_icon, "okay"),
        MoodIconData(R.drawable.lmfaoo_icon, "lmfaooo"),
        MoodIconData(R.drawable.traumatised_icon, "jesus"),
        MoodIconData(R.drawable.depressed_icon, "depressed"),
         MoodIconData(R.drawable.sheff, "cooking"),
        MoodIconData(R.drawable.shockrr, "shocker"),
        MoodIconData(R.drawable.too_sippin, "sipping"),
        MoodIconData(R.drawable.micro_man, "announcement"),
         MoodIconData(R.drawable.clawn, "clown"),
        MoodIconData(R.drawable.wzzrd, "sorcerer"),
         MoodIconData(R.drawable.socreates, "socrates"),
         MoodIconData(R.drawable.robo_cop, "RoboCop"),
         MoodIconData(R.drawable.bord_super, "superNO"),
         MoodIconData(R.drawable.insp_cookie, "inspectorCookie"),
         MoodIconData(R.drawable.zoomzoomzoom, "lockin"),
         MoodIconData(R.drawable.general_ra, "general"),
         MoodIconData(R.drawable.sleep_wud, "denger"),
         MoodIconData(R.drawable.noodle_goat, "noodleGoat"),
         MoodIconData(R.drawable.first_tuch, "lessgo"),
         MoodIconData(R.drawable.mr_him, "sipper"),
         MoodIconData(R.drawable.present_father, "presentFather"),
         MoodIconData(R.drawable.explora, "explora"),
         MoodIconData(R.drawable.lover_man, "loverMan"),
         MoodIconData(R.drawable.yeatt, "AH"),
         MoodIconData(R.drawable.darwin, "letsSee"),
         MoodIconData(R.drawable.spit_it, "spitIt"),
     )


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "How are you feeling?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "cancel",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            onClose()
                        }
                )
            }

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
                        .width(375.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .height(250.dp)
                                .width(375.dp)
                                .padding(8.dp),
                            userScrollEnabled = true
                        ) {
                            items(moodIcons) { moodIcon ->
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(15.dp))
                                        .clickable {
                                            selectedIcon = moodIcon
                                            expanded = false
                                        }
                                        .background(MaterialTheme.colorScheme.inversePrimary),
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodTrackerTopBar(
    onBackIconClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 17.dp,
            )
            .background(MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable (
                        onClick = {onBackIconClick()},
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Back",
                    modifier = Modifier
                        .rotate(180f)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.surfaceTint
                )
            }

            // Title
            Text(
                text = "Mood Tracker",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            // Right-side Icon
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                shadowElevation = 2.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_psychology_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

