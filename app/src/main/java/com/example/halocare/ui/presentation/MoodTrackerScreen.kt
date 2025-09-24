import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.halocare.BuildConfig
import com.example.halocare.R
import com.example.halocare.ui.models.HaloMoodEntry
import com.example.halocare.ui.presentation.charts.MoodChart
import com.example.halocare.ui.presentation.rememberStatusBarController
import com.example.halocare.ui.presentation.responsive
import com.example.halocare.ui.presentation.responsiveHeight
import com.example.halocare.ui.presentation.responsiveWidth
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
    onBackIconClick: () -> Unit,
    isDarkMode : Boolean
){
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.inversePrimary

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


    LaunchedEffect(Unit){
        statusBarController.updateStatusBar(
            color = statusBarColor,
            darkIcons = isDarkMode
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
                modifier = Modifier.padding(end = 20.dp, bottom = 40.dp.responsiveHeight())
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
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 5.dp, vertical = 5.dp)
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
                        color = if(isDarkMode) MaterialTheme.colorScheme.primaryContainer else
                            MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(
                            bottomStart = 15.dp, bottomEnd = 15.dp,
                            topStart = 15.dp, topEnd = 15.dp
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = if(isDarkMode) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.inversePrimary
                        ,
                        shape = RoundedCornerShape(
                                bottomStart = 15.dp, bottomEnd = 15.dp,
                                topStart = 15.dp, topEnd = 15.dp)
                    )
                    .padding(top = 4.dp, start = 3.dp, end = 3.dp)
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
                     },
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.height(5.dp.responsiveHeight()))

                // Mood Chart
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp.responsiveHeight())
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
                            fontSize = 18.sp.responsiveSp(),
                            fontWeight = FontWeight.Bold,
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.wazirr_mirr),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp.responsiveHeight()),
                            tint = Color.Unspecified
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp.responsiveHeight()))
                    Text("You've logged 'Happy' 5 times this week! Keep it up!", fontSize = 14.sp.responsiveSp())
                }
            }

            var showDialog by remember { mutableStateOf(false) }

            // Dialog shown when user long-presses the wizard icon
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Got it")
                        }
                    },
                    title = { Text("Who is Nimbus the Wise?") },
                    text = {
                        Text(
                            "Nimbus the Wise is a gentle mind-mage who drifts through realms of thought. " +
                                    "He appears when clouds gather in your heart — offering clarity, calm, and the occasional spark of joy."
                        )
                    }
                )
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
                        "Nimbus the Wise",
                        fontSize = 18.sp.responsiveSp(),
                        fontWeight = FontWeight.Bold
                    )

                    Column(modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                showDialog = true
                            }
                        )
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.wzzrd),
                            contentDescription = "Nimbus the Wise",
                            modifier = Modifier
                                .size(30.dp.responsiveHeight())
                                ,
                            tint = Color.Unspecified
                        )
                    }
                }
                Column(modifier = Modifier.padding(
                    bottom = 16.dp, start = 16.dp, end = 16.dp)) {
                    Text(
                        text = dailyAdvice ?: " ",
                        fontSize = 14.sp.responsiveSp(),
                        fontStyle = FontStyle.Italic)
                }
            }
            Column {
                Spacer(modifier = Modifier.height(80.dp.responsiveHeight()
                ))
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
                        },
                        isDarkMode = isDarkMode
                    )
                }
            }

        }
    }
}



@Composable
fun WeekDateSelector(
    selectedDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit,
    isDarkMode: Boolean
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
                if(isDarkMode) MaterialTheme.colorScheme.onErrorContainer else
                    MaterialTheme.colorScheme.errorContainer // Highlight selected & enabled
            } else {
                if (isDarkMode) MaterialTheme.colorScheme.primaryContainer else
                     MaterialTheme.colorScheme.tertiary
            }

            val dayOfWeekColor = when {
                isSelected && isClickable -> MaterialTheme.colorScheme.onPrimary
                isFutureDate -> Color.Gray.copy(alpha = 0.4f) // Dim future dates
                else -> Color.Gray // Default non-selected day color
            }

            val dayOfMonthColor = when {
                isSelected && isClickable -> if(!isDarkMode) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                isFutureDate -> Color.Gray.copy(alpha = 0.4f) // Dim future dates
                else -> if(!isDarkMode) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.primaryContainer
            }

            val selectwidth = if(!BuildConfig.IS_USER_BUILD) 52.dp else 45.dp


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
                Spacer(modifier = Modifier.height(5.dp.responsiveHeight()))
                Column(
                    modifier = Modifier
                        .height(25.dp.responsiveHeight())
                        .width(selectwidth.responsiveWidth())
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
                        fontSize = 13.sp.responsiveSp(),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(25.dp.responsiveHeight()))
                Column(
                    modifier = Modifier
                        .size(30.dp.responsiveHeight())
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
                        fontSize = 15.sp.responsiveSp(),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(3.dp.responsiveHeight()))
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun MoodEntryLogger(
    onLogUserMood : (HaloMoodEntry) -> Unit = {},
    onClose : () -> Unit = {},
    isDarkMode: Boolean
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
         MoodIconData(R.drawable.scholar, "scholar"),
         MoodIconData(R.drawable.chosenblood, "chosenBld"),
         MoodIconData(R.drawable.santeee, "sANTEE"),
         MoodIconData(R.drawable.undataker, "undataker"),
         MoodIconData(R.drawable.xplora__, "xplora++"),
         MoodIconData(R.drawable.proff, "proff"),
         MoodIconData(R.drawable._uale, "2uale"),
         MoodIconData(R.drawable.timerr, "timerr"),
     )


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isDarkMode) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer
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
                    style = MaterialTheme.typography.titleMedium.responsive(),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "cancel",
                    modifier = Modifier
                        .size(20.dp.responsiveHeight())
                        .clickable {
                            onClose()
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp.responsiveHeight()))

            // Emoji selector using ExposedDropdownMenuBox
            var expanded by remember { mutableStateOf(false) }
            var textFieldWidth by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current

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
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            textFieldWidth = with(density) { coordinates.size.width.toDp() }
                        },
                    label = {
                       if (selectedIcon == null) Text("Select Mood") else Text(selectedIcon!!.iconName)
                            },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = selectedIcon?.icon ?:R.drawable.duhh_icon),
                            contentDescription = "Selected mood",
                            modifier = Modifier.size(24.dp.responsiveWidth())
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle mood dropdown"
                        )
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(textFieldWidth)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        moodIcons.chunked(4).forEach { rowIcons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowIcons.forEach { moodIcon ->
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(60.dp.responsiveWidth())
                                            .clip(RoundedCornerShape(15.dp))
                                            .clickable {
                                                selectedIcon = moodIcon
                                                expanded = false
                                            }
                                            .background(MaterialTheme.colorScheme.inversePrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = moodIcon.icon),
                                            contentDescription = "Mood Icon",
                                            modifier = Modifier.size(40.dp.responsiveHeight())
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp.responsiveHeight()))

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
                            style = MaterialTheme.typography.bodySmall.responsive()
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp.responsiveHeight()))

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
                    .height(48.dp.responsiveHeight()),
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
    onBackIconClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(75.dp.responsiveHeight())
            .shadow(
                elevation = 13.dp,
            )
            .background(MaterialTheme.colorScheme.inversePrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 10.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(40.dp.responsiveHeight())
                    .clip(CircleShape)
                    .clickable(
                        onClick = { onBackIconClick() },
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
                style = MaterialTheme.typography.titleLarge.responsive(),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

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

@Composable
fun TextUnit.responsiveSp(): TextUnit {
    if (!BuildConfig.IS_USER_BUILD) return this
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val density = LocalDensity.current.density
    val fontScale = LocalConfiguration.current.fontScale

    val targetDensity = 3.0f
    val targetFontScale = 0.857f

    val densityFactor = density / targetDensity
    val fontFactor = fontScale / targetFontScale
    val scaleFactor = (screenWidth.toFloat() / 411f) * densityFactor * fontFactor

    return if (this.type == TextUnitType.Sp) {
        val newValue = (this.value * scaleFactor).coerceIn(0.8f * this.value, 1.2f * this.value)
        newValue.sp
    } else this
}