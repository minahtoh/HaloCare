package com.example.halocare.ui.presentation

import android.app.AlarmManager
import android.app.PendingIntent

import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.KeyboardArrowRight
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColor
import com.example.halocare.R
import com.example.halocare.receivers.MedicationReminderReceiver
import com.example.halocare.ui.models.Medication
import com.example.halocare.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

//@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MedicationReminderScreen(
    mainViewModel: MainViewModel,
    onBackIconClick : () -> Unit
) {
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.tertiaryContainer

    var showAddDialog by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    val medicationsList by mainViewModel.allMedications.collectAsState()
    val medicationSchedule: Map<LocalDate, List<Medication>> = medicationsList
        .flatMap { medication ->
            medication.prescribedDays.map { date -> date to medication }
        }
        .groupBy({ it.first }, { it.second })

    var showMedicationBottomSheet by remember { mutableStateOf(false) }
    var selectedDayForSheet by remember { mutableStateOf<LocalDate?>(null) }
    val medicationForSheet = selectedDayForSheet?.let { medicationSchedule[it] }.orEmpty()



    LaunchedEffect(Unit){
        statusBarController.updateStatusBar(
            color = statusBarColor,
            darkIcons = true
        )
    }
    if (showMedicationBottomSheet && selectedDayForSheet != null) {

            MedicationBottomSheet(
                day = selectedDayForSheet!!,
                medications = medicationForSheet,
                onDismiss = { showMedicationBottomSheet = false }
            )

    }

    Scaffold(
        topBar = {
            MedicationReminderTopBar(onBackIconClick = { onBackIconClick() })
                 },
        floatingActionButton = {
            FloatingActionButton(
            onClick = { showAddDialog = true },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 50.dp.responsiveHeight(), end = 15.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_medication_24),
                contentDescription = "Add Medication")
        } },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            item {
                Column(modifier = Modifier.padding(6.dp)) {
                    MedicationCalendar(
                        medicationSchedule = medicationSchedule,
                        onDayClick = { date ->
                            selectedDayForSheet = date
                            showMedicationBottomSheet = true
                        }
                    )
                }
                Spacer(modifier = Modifier.height(5.dp.responsiveHeight()))
            }

            stickyHeader {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Today's Doses") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("All Medications") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp.responsiveHeight()))
                }
            }

            val today = LocalDate.now()
            val medicationsToShow = if (selectedTab == 0) {
                medicationSchedule[today].orEmpty()
            } else {
                medicationSchedule.values.flatten().distinctBy { it.name }
            }

            if (medicationsToShow.isNotEmpty()) {
                items(medicationsToShow) { medication ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SwipeToConfirmDeleteContainer(
                            medication = medication,
                            onDelete = { mainViewModel.deleteMedication(it) }
                        ) {
                            MedicationCard(medication = medication) {
                                selectedMedication = medication
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add Medication",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp.responsiveHeight())
                            )
                            Spacer(modifier = Modifier.height(12.dp.responsiveHeight()))
                            Text(
                                text = if (selectedTab == 0)
                                    "No doses scheduled for today."
                                else
                                    "No medications found.\nStart by adding one.",
                                style = MaterialTheme.typography.bodyMedium.responsive(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        selectedMedication?.let { medication ->
            LogMedicationDialog(
                medication = medication,
                onDismiss = { selectedMedication = null },
                onLogDose = { doses ->
                    val updated = medication.copy(dosesUsedToday = doses)
                    mainViewModel.updateMedication(updated)
                    selectedMedication = null
                }
            )
        }
    }


    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            AddMedicationDialog(
                onSubmit = {
                    mainViewModel.saveMedicationData(it)
                    showAddDialog = false
                },
                onClose = {
                    showAddDialog = false
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationReminderTopBar(
    onBackIconClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(75.dp)
            .shadow(
                elevation = 2.dp,
            )
            .background(MaterialTheme.colorScheme.tertiaryContainer)
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
            // Auto-resizing Title
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val maxWidth = maxWidth
                val fontSize = when {
                    maxWidth < 280.dp -> 16.sp
                    maxWidth < 340.dp -> 18.sp
                    else -> 20.sp
                }

                Text(
                    text = "Medication Reminder",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = fontSize),
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

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
                        painter = painterResource(id = R.drawable.baseline_medication_24),
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
fun LogMedicationDialog(
    medication: Medication,
    onDismiss: () -> Unit,
    onLogDose: (Int) -> Unit
) {
    var currentLogged by remember { mutableIntStateOf(medication.dosesUsedToday) }
    val doseTimes = remember(medication.firstDoseTime, medication.frequency) {
        List(medication.frequency) { i ->
            val calculatedTime = medication.firstDoseTime.plusHours((i * (24 / medication.frequency)).toLong())

            // Check if the calculated time exceeds midnight, and if so, set it to 23:00
            if (calculatedTime.hour == 0 && calculatedTime.minute == 0) {
                calculatedTime.withHour(23).withMinute(0)
            } else {
                calculatedTime
            }
        }
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(medication.color).copy(alpha = 0.9f),
        confirmButton = {
            TextButton(
                onClick = {
                    onLogDose(currentLogged)
                    onDismiss()
                }
            ) {
                Text("Done")
            }
        },
        title = {
            Text("Log Doses for ${medication.name}")
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tap to log doses. Remaining: ${medication.frequency - currentLogged}")
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    doseTimes.forEachIndexed { index, time ->
                        val isLogged = index < currentLogged
                        val now = LocalTime.now()
                        val isTimeReached = now >= time

                        val circleColor = when {
                            isLogged -> Color(medication.color)
                            isTimeReached -> Color.LightGray
                            else -> Color.Gray.copy(alpha = 0.3f)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(circleColor)
                                    .clickable(
                                        enabled = isTimeReached && !isLogged && currentLogged < medication.frequency
                                    ) {
                                        currentLogged++
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = time.toString().substring(0, 5),
                                    color = if (isLogged) Color.White else Color.Black,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            when {
                                isLogged -> AnimatedVisibility(true) {
                                    Text("✔", color = Color.Green)
                                }
                                isTimeReached -> AnimatedVisibility(true) {
                                    Text("•••", color = Color.DarkGray)
                                }
                                else -> Spacer(modifier = Modifier.height(18.dp)) // keeps spacing consistent
                            }
                        }
                    }
                }
            }
        }
    )
}




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MedicationCard(
    medication: Medication,
    isForToday: Boolean = false,
    onClick: () -> Unit = {}
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val cardColor = Color(medication.color)
    val surfaceColor = if (isSystemInDarkTheme) {
        cardColor
    } else {
        cardColor.copy(alpha = 0.8f)
    }

    var showInfoDialog by remember { mutableStateOf(false) }

    val dosesRemaining = medication.frequency - medication.dosesUsedToday
    val progressPercentage = (medication.dosesUsedToday.toFloat() / medication.frequency.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 1.dp, end = 1.dp, bottom = 0.dp)
            .combinedClickable(
                onClick = {
                    if (isForToday) onClick()
                },
                onLongClick = {
                    showInfoDialog = true
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isForToday) 6.dp else 4.dp,
            pressedElevation = 8.dp
        ),
        shape = RoundedCornerShape(7.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = cardColor.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main content row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left content
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Medication icon/indicator
                    Box(
                        modifier = Modifier
                            .size(48.dp.responsiveHeight())
                            .background(
                                cardColor.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_medication_24),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp.responsiveHeight())
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp.responsiveWidth()))

                    // Medication details
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${medication.dosage} mg",
                                style = MaterialTheme.typography.bodyMedium.responsive(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )

                            Text(
                                text = "${medication.frequency}x daily",
                                style = MaterialTheme.typography.bodyMedium.responsive(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Doses remaining with color coding
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val remainingColor = when {
                                dosesRemaining == 0 -> MaterialTheme.colorScheme.error
                                dosesRemaining == 1 -> Color(0xFFFF9800) // Orange
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Icon(
                                imageVector = when {
                                    dosesRemaining == 0 -> Icons.Default.CheckCircle
                                    dosesRemaining <= 1 -> Icons.Default.Warning
                                    else -> Icons.Default.DateRange
                                },
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(16.dp.responsiveHeight())
                            )

                            Text(
                                text = when (dosesRemaining) {
                                    0 -> "All doses taken"
                                    1 -> "1 dose remaining"
                                    else -> "$dosesRemaining doses remaining"
                                },
                                style = MaterialTheme.typography.bodySmall.responsive(),
                                color = remainingColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Switch with better styling
                Switch(
                    checked = medication.isReminderOn,
                    onCheckedChange = {},
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = cardColor.copy(alpha = 0.8f),
                        checkedThumbColor = cardColor,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Progress indicator for doses taken
            if (medication.frequency > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Progress",
                            style = MaterialTheme.typography.labelMedium.responsive(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${medication.dosesUsedToday}/${medication.frequency}",
                            style = MaterialTheme.typography.labelMedium.responsive(),
                            color = cardColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp.responsiveHeight()))

                    LinearProgressIndicator(
                        progress = progressPercentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = cardColor
                    )
                }
            }

            // Subtle bottom accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                cardColor.copy(alpha = 0.8f),
                                cardColor.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
    if (showInfoDialog) {
        MedicationInfoDialog(
            medication = medication,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
fun AddMedicationDialog(
    onSubmit: (Medication) -> Unit,
    onClose: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf(1) }
    var frequency by remember { mutableStateOf(1) } // Default: Once Daily
    var time by remember { mutableStateOf(LocalTime.of(8, 0)) }
    val timePickerDialog = rememberTimePickerDialog(time) { selectedTime -> time = selectedTime }
    var shouldNotify by remember { mutableStateOf(true) }
    var showDateDialog by remember { mutableStateOf(false) }
    var prescribedDates by remember { mutableStateOf<List<LocalDate>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color(0xFFE57373)) }
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Add Medication",
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

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medication Name") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = dosage.toString(),
                onValueChange = { dosage = it.toInt() },
                label = { Text("Dosage") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "How many times per day?"
            )
            Spacer(modifier = Modifier.height(5.dp))
            // Frequency Selector (Dropdown)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(1 to "Once", 2 to "Twice", 3 to "Thrice").forEach { (value, label) ->
                    OutlinedButton(
                        onClick = { frequency = value },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (frequency == value) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (frequency == value) MaterialTheme.colorScheme.primary else Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(label, fontWeight = if (frequency == value) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Prescribed For",
                modifier = Modifier
                    .padding(10.dp)
                    .clickable {
                        showDateDialog = true
                    }
            )
            Spacer(modifier = Modifier.height(5.dp))
            if (showDateDialog) {
                DatesSelectorDialog(
                    selectedDates = prescribedDates,
                    onDateToggle = { date ->
                        prescribedDates = prescribedDates.toMutableList().apply {
                            if (contains(date)) remove(date) else add(date)
                        }
                    },
                    onDismiss = { showDateDialog = false },
                    onConfirm = { showDateDialog = false }
                )
            }
            val sortedDates = prescribedDates.sorted()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text(text = "Days:", fontWeight = FontWeight.SemiBold)
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        sortedDates.size <= 3 -> {
                            sortedDates.forEach { date ->
                                Text(text = date.format(formatter))
                            }
                        }

                        else -> {
                            Text(text = sortedDates.first().format(formatter))
                            Text(text = "...")
                            Text(text = sortedDates.last().format(formatter))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1st dose time?"
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Time Selector (TimePickerDialog)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { timePickerDialog.show() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Time: ${time.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Text("Choose a color:",
                style = MaterialTheme.typography.bodyLarge
            )
            ColorChooserRow(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
            ) {
                Checkbox(
                    checked = shouldNotify,
                    onCheckedChange = {shouldNotify = it},
                )
                Text(
                    text = "Notify",
                    fontWeight =  FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(7.dp))
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "notify",
                    tint = MaterialTheme.colorScheme.inversePrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && dosage != 0) {
                        onSubmit(
                            Medication(
                                name = name.capitalize(),
                                dosage = dosage,
                                frequency = frequency,
                                isReminderOn = shouldNotify,
                                dosesUsedToday = 0,
                                prescribedDays = prescribedDates,
                                color = selectedColor.toArgb(),
                                firstDoseTime = time
                            )
                        )
                    }
                    if (shouldNotify){
                    //TODO : implement notification enabler
                        scheduleMedicationReminder(
                            context = context,
                            medicationName = name.capitalize(),
                            medicationDose = dosage.toString(),
                            time = time
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text("Add Medication")
            }
        }
    }
}

@Composable
fun ColorChooserRow(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colorOptions = listOf(
        Color(0xFFE57373), // Red
        Color(0xFF64B5F6), // Blue
        Color(0xFF81C784), // Green
        Color(0xFFFFD54F), // Yellow
        Color(0xFFBA68C8)  // Purple
    )

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        colorOptions.forEach { color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (color == selectedColor) 3.dp else 1.dp,
                        color = if (color == selectedColor) Color.Black else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@Composable
fun DatesSelectorDialog(
    selectedDates: List<LocalDate>,
    onDateToggle: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
      var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with month and navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
                    }
                    Text(
                        text = currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() } + " ${currentMonth.year}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar grid
                val daysInMonth = currentMonth.lengthOfMonth()
                val firstDayOffset = LocalDate.of(currentMonth.year, currentMonth.month, 1).dayOfWeek.value % 7

                LazyVerticalGrid(columns = GridCells.Fixed(7)) {
                    items(firstDayOffset) {
                        Box(modifier = Modifier.size(40.dp)) // empty cells for alignment
                    }

                    items(daysInMonth) { index ->
                        val date = LocalDate.of(currentMonth.year, currentMonth.month, index + 1)
                        val isSelected = selectedDates.contains(date)

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF4CAF50) else Color.LightGray)
                                .clickable {
                                    onDateToggle(date)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm + Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = { onConfirm() }) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun rememberTimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
): TimePickerDialog {
    val context = LocalContext.current
    return TimePickerDialog(
        context,
        { _, hour, minute -> onTimeSelected(LocalTime.of(hour, minute)) },
        initialTime.hour,
        initialTime.minute,
        false
    )
}

fun scheduleMedicationReminder(context: Context, medicationName: String, medicationDose: String, time: LocalTime) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
        putExtra("medication_name", medicationName)
        putExtra("medication_dosage", medicationDose)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        time.hashCode(), // Unique ID per time
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, time.hour)
        set(Calendar.MINUTE, time.minute)
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_MONTH, 1) // Move to next day if time has passed
        }
    }

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}




// Dummy Data Model


val dummyMedications = listOf(
    Medication(
        medicationId = 1,
        name = "Paracetamol",
        dosage = 500,
        frequency = 3,
        dosesUsedToday = 1,
        prescribedDays = listOf(
            LocalDate.now(),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2)
        ),
        isReminderOn = true,
        color = Color(0xFFE57373).toArgb() // Soft Red
    ),
    Medication(
        medicationId = 2,
        name = "Amoxicillin",
        dosage = 250,
        frequency = 2,
        dosesUsedToday = 0,
        prescribedDays = listOf(
            LocalDate.now().minusDays(1),
            LocalDate.now(),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2)
        ),
        isReminderOn = true,
        color = Color(0xFF64B5F6).toArgb() // Light Blue
    ),
    Medication(
        medicationId = 3,
        name = "Ibuprofen",
        dosage = 400,
        frequency = 1,
        dosesUsedToday = 1,
        prescribedDays = listOf(
            LocalDate.now(),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(4)
        ),
        isReminderOn = false,
        color = Color(0xFF81C784).toArgb() // Soft Green
    ),
    Medication(
        medicationId = 4,
        name = "Cetirizine",
        dosage = 10,
        frequency = 1,
        dosesUsedToday = 0,
        prescribedDays = listOf(
            LocalDate.now(),
            LocalDate.now().plusWeeks(1)
        ),
        isReminderOn = true,
        color = Color(0xFFFFD54F).toArgb() // Yellow
    ),
    Medication(
        medicationId = 5,
        name = "Vitamin D",
        dosage = 1000,
        frequency = 1,
        dosesUsedToday = 0,
        prescribedDays = (0..6).map { LocalDate.now().plusDays(it.toLong()) },
        isReminderOn = false,
        color = Color(0xFFBA68C8).toArgb() // Purple
    )
)

val dummyMedicationSchedule: Map<LocalDate, List<Medication>> =
    dummyMedications
        .flatMap { medication ->
            medication.prescribedDays.map { date -> date to medication }
        }
        .groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        )

@Composable
fun MedicationCalendar(
    medicationSchedule: Map<LocalDate, List<Medication>>,
    onDayClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val daysInMonth = YearMonth.of(today.year, today.month).lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(today.year, today.month, 1).dayOfWeek.value
    val startOffset = if (firstDayOfMonth == 7) 0 else firstDayOfMonth

    Column {
        Text(
            text = today.month.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            },
            style = MaterialTheme.typography.titleLarge.responsive(),
            modifier = Modifier.padding(bottom = 8.dp.responsiveHeight())
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .padding(5.dp)
                .height(300.dp.responsiveHeight()),
            verticalArrangement = Arrangement.spacedBy(6.dp.responsiveHeight()),
            horizontalArrangement = Arrangement.spacedBy(6.dp.responsiveWidth()),
            userScrollEnabled = false
        ) {
            items(startOffset) {
                Box(modifier = Modifier.size(48.dp.responsiveHeight()))
            }

            items(daysInMonth) { dayIndex ->
                val date = LocalDate.of(today.year, today.month, dayIndex + 1)
                val medications = medicationSchedule[date].orEmpty()
                val isToday = date == today

                Box(
                    modifier = Modifier
                        .size(48.dp.responsiveWidth())
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(3.dp)
                        .clickable { onDayClick(date) },
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.inversePrimary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(start = 3.dp, end = 3.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(2.dp)
                                .background(
                                    color = if (isToday) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .padding(horizontal = 7.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${dayIndex + 1}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall.responsive(),
                                color = if (isToday) Color.White else Color.Unspecified
                            )
                        }

                        if (medications.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                medications.take(3).forEach { medication ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp.responsiveHeight())
                                            .background(
                                                Color(medication.color),
                                                RoundedCornerShape(50)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationBottomSheet(day: LocalDate, medications: List<Medication>, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(start = 14.dp)) {
            if (medications.isEmpty()){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Text(
                        text ="Medications for ${day.dayOfMonth} ${day.month.name.capitalize()}",
                        style = MaterialTheme.typography.titleLarge.responsive().copy(fontWeight = FontWeight.Bold),
                    )
                    Spacer(modifier = Modifier.height(13.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 15.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_do_not_disturb_24),
                            contentDescription = null
                        )
                        Text(
                            text = "No medications available",
                            style = MaterialTheme.typography.bodyLarge.responsive().copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(75.dp))
            }else{
                Text(
                    text ="Medications for ${day.dayOfMonth} ${day.month.name.capitalize()}",
                    style = MaterialTheme.typography.titleLarge.responsive().copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(13.dp))

                medications.forEach { medication ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(medication.color), shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = medication.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        if (medication.dosage == medication.frequency){
                            Icon(painter = painterResource(id = R.drawable.baseline_verified_24),
                                contentDescription = "Completed",
                                modifier = Modifier.size(25.dp.responsiveWidth())
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}


@OptIn( ExperimentalMaterial3Api::class)
@Composable
fun SwipeToConfirmDeleteContainer(
    medication: Medication, // Changed from NotificationData
    onDelete: (Medication) -> Unit,
    animationDuration: Int = 500,
    content: @Composable () -> Unit
) {
    var isDeleted by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val state = rememberDismissState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> {
                    // Stop at 75% and show confirmation
                    showConfirmDialog = true
                    false // Don't dismiss yet
                }
                else -> false
            }
        }
    )
    // Reset swipe state when dialog is dismissed
    LaunchedEffect(showConfirmDialog) {
        if (!showConfirmDialog) {
            state.reset()
        }
    }

    LaunchedEffect(key1 = isDeleted) {
        if (isDeleted) {
            delay(animationDuration.toLong())
            onDelete(medication)
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Delete Medication") },
            text = { Text("Are you sure you want to delete this medication?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        isDeleted = true
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    AnimatedVisibility(
        visible = !isDeleted,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismiss(
            state = state,
            directions = setOf(DismissDirection.EndToStart),
            background = {
                deleteBackground(swipeDismissState = state)
            },
            dismissContent = {
                content()
            }
        )
    }
}

@OptIn( ExperimentalMaterial3Api::class)
@Composable
fun deleteBackground(
    swipeDismissState: DismissState
) {
    val color = if (swipeDismissState.dismissDirection == DismissDirection.EndToStart) {
        MaterialTheme.colorScheme.error
    } else {
        Color.Transparent
    }

    val iconAlpha by animateFloatAsState(
        targetValue = if (swipeDismissState.dismissDirection == DismissDirection.EndToStart) 1f else 0f,
        label = "iconAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier.alpha(iconAlpha)
        )
    }
}
@Composable
fun MedicationInfoDialog(
    medication: Medication,
    onDismiss: () -> Unit
) {
    val cardColor = Color(medication.color)
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with medication icon and name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp.responsiveHeight())
                            .background(
                                cardColor.copy(alpha = 0.2f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_medication_24),
                            contentDescription = null,
                            tint = cardColor,
                            modifier = Modifier.size(32.dp.responsiveHeight())
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.headlineSmall.responsive(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${medication.dosage} mg",
                            style = MaterialTheme.typography.titleMedium.responsive(),
                            color = cardColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp.responsiveHeight()))

                // Medication Details
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Schedule Information
                    InfoRow(
                        icon = painterResource(id = R.drawable.baseline_schedule_24),
                        iconColor = cardColor,
                        label = "Schedule",
                        value = "${medication.frequency} times daily"
                    )

                    InfoRow(
                        icon = painterResource(id = R.drawable.baseline_access_alarm_24),
                        iconColor = cardColor,
                        label = "First Dose",
                        value = medication.firstDoseTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                    )

                    // Daily Progress
                    InfoRow(
                        icon = painterResource(id = R.drawable.baseline_trending_up_24),
                        iconColor = cardColor,
                        label = "Today's Progress",
                        value = "${medication.dosesUsedToday} of ${medication.frequency} doses"
                    )

                    // Reminder Status
                    InfoRow(
                        icon = if (medication.isReminderOn) painterResource(id = R.drawable.baseline_notifications_active_24)
                        else painterResource(id = R.drawable.baseline_notifications_off_24),
                        iconColor = if (medication.isReminderOn) Color(0xFF4CAF50) else Color(0xFF757575),
                        label = "Reminders",
                        value = if (medication.isReminderOn) "Enabled" else "Disabled"
                    )

                    // Prescription Period
                    if (medication.prescribedDays.isNotEmpty()) {
                        InfoRow(
                            icon = painterResource(id = R.drawable.baseline_date_schedule_24),
                            iconColor = cardColor,
                            label = "Prescription Period",
                            value = "${medication.prescribedDays.size} days"
                        )

                        // Date range if available
                        if (medication.prescribedDays.size > 1) {
                            val sortedDates = medication.prescribedDays.sorted()
                            InfoRow(
                                icon = painterResource(id = R.drawable.baseline_date_schedule_24),
                                iconColor = cardColor,
                                label = "Date Range",
                                value = "${sortedDates.first().format(formatter)} - ${sortedDates.last().format(formatter)}"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp.responsiveHeight()))

                // Progress Bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Progress",
                            style = MaterialTheme.typography.labelLarge.responsive(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(medication.dosesUsedToday.toFloat() / medication.frequency * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge.responsive(),
                            color = cardColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp.responsiveHeight()))

                    LinearProgressIndicator(
                        progress = (medication.dosesUsedToday.toFloat() / medication.frequency.toFloat()).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = cardColor,
                        trackColor = cardColor.copy(alpha = 0.2f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp.responsiveHeight()))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp.responsiveHeight()),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: Painter,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp.responsiveHeight())
                .background(
                    iconColor.copy(alpha = 0.15f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp.responsiveHeight())
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.responsive(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.responsive(),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
@Composable
fun TextStyle.responsive(): TextStyle {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val fontScale = configuration.fontScale
    val screenWidth = configuration.screenWidthDp

    val targetDensity = 3.0f
    val targetFontScale = 0.857f

    val densityFactor = density / targetDensity
    val fontFactor = fontScale / targetFontScale

    val scaleFactor = (screenWidth.toFloat() / 411f) * densityFactor * fontFactor

    return this.copy(
        fontSize = (this.fontSize.value * scaleFactor).coerceIn(
            0.8f * this.fontSize.value,
            1.2f * this.fontSize.value
        ).sp,
        lineHeight = if (this.lineHeight != TextUnit.Unspecified) {
            (this.lineHeight.value * scaleFactor).coerceIn(
                0.8f * this.lineHeight.value,
                1.2f * this.lineHeight.value
            ).sp
        } else TextUnit.Unspecified
    )
}
@Composable
fun Dp.responsiveWidth(): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val density = LocalDensity.current.density
    val fontScale = LocalConfiguration.current.fontScale
    val targetDensity = 3.0f
    val targetFontScale = 0.857f

    val densityFactor = density / targetDensity
    val fontFactor = fontScale / targetFontScale

    val scaleFactor = (screenWidth.toFloat() / 411f) * densityFactor * fontFactor

    return (this.value * scaleFactor).dp.coerceIn(0.8f * this, 1.2f * this)
}

@Composable
fun Dp.responsiveHeight(): Dp {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val fontScale = configuration.fontScale
    val usableHeight = configuration.screenHeightDp

    val targetDensity = 3.0f
    val targetFontScale = 0.857f

    val densityFactor = density / targetDensity
    val fontFactor = fontScale / targetFontScale

    val scaleFactor = (usableHeight.toFloat() / 891f) * densityFactor * fontFactor

    return (this.value * scaleFactor).dp.coerceIn(0.8f * this, 1.2f * this)
}