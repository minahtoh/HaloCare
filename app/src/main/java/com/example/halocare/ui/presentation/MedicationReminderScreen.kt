package com.example.halocare.ui.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.halocare.R
import com.example.halocare.receivers.MedicationReminderReceiver
import com.example.halocare.ui.models.Medication
import com.example.halocare.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Preview(widthDp = 320, heightDp = 720)
@Composable
fun MedicationReminderScreen(
    mainViewModel: MainViewModel
) {
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



    Scaffold(
        topBar = { MedicationReminderTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_medication_24),
                    contentDescription = "Add Medication")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            MedicationCalendar(medicationSchedule = medicationSchedule)
            Spacer(modifier = Modifier.height(5.dp))
           // MedicationList()
            Column {
                // **TabRow for switching views**
                TabRow(selectedTabIndex = selectedTab) {
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
                // **Filter medication list based on tab selection**
                val medicationsToShow = if (selectedTab == 0) {
                    medicationSchedule[today].orEmpty() // Today's Doses
                } else {
                    medicationSchedule.values.flatten().distinctBy { it.name } // All Medications
                }

                LazyColumn {
                    items(medicationsToShow) { medication ->
                        MedicationCard(
                            medication =  medication,
                            isForToday = selectedTab == 0
                        ) {
                            // Open Dose Logging Dialog on click
                            selectedMedication = medication
                        }
                    }
                }
            }
            // **Show Dose Logging Dialog when a medication is selected**
            selectedMedication?.let { medication ->
                LogMedicationDialog(
                    medication = medication,
                    onDismiss = {  selectedMedication = null },
                    onLogDose = {
                        val updatedMeds = medication.copy(dosesUsedToday = it)
                        mainViewModel.updateMedication(updatedMeds)
                        selectedMedication = null
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            AddMedicationDialog(
                onDismiss = {
                    mainViewModel.saveMedicationData(it)
                    showAddDialog = false
                })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationReminderTopBar() {
    TopAppBar(
        title = { Text("Medication Reminder") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
    )
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


@Composable
fun DoseLoggingDialog(
    medication: Medication,
    loggedDoses: List<LocalTime> = listOf(LocalTime.of(8, 0)), // Doses already logged
    onDismiss: () -> Unit,
   // onLogDose: (LocalTime, Boolean, String) -> Unit // (doseTime, taken, note)
) {
    var note by remember { mutableStateOf("") }
    val currentTime = LocalTime.now()

    val todayDoses = generateDoses(medication.frequency)

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Log Dose for ${medication.name}") },
        text = {
            Column {
                Text("Dosage: ${medication.dosage}")
                Text("Frequency: ${medication.frequency}x per day")


                Spacer(modifier = Modifier.height(12.dp))

                // List doses
                todayDoses.forEach { doseTime ->
                    val isUnlocked = doseTime <= currentTime
                    val isLogged = doseTime in loggedDoses
                    if(!isUnlocked){
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp)) {
                            Text("Next Dose at: ${doseTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp)
                    ) {
                        Checkbox(
                            checked = isLogged,
                            onCheckedChange = null,
                            enabled = isUnlocked && !isLogged
                        )
                        Text(
                            text = "Dose at ${doseTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                            fontWeight = if (isLogged) FontWeight.Light else FontWeight.Bold,
                            color = if (isLogged) Color.Gray else Color.Unspecified
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional note input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Add a note (optional)") },
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()) {
                todayDoses.filter { it <= currentTime && it !in loggedDoses }.forEach { doseTime ->
                    Button(
                        onClick = {
                        //    onLogDose(doseTime, true, note)
                            onDismiss()
                        }
                    ) {
                        Text("Log ${doseTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                    }
                }
            }
        }
    )
}

fun generateDoses(frequency: Int): List<LocalTime> {
    return when (frequency) {
        1 -> listOf(LocalTime.of(8, 0)) // Once daily → 8 AM
        2 -> listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)) // Twice daily → 8 AM, 8 PM
        3 -> listOf(LocalTime.of(6, 0), LocalTime.of(14, 0), LocalTime.of(20, 0)) // Thrice → 6 AM, 2 PM, 8 PM
        else -> emptyList() // Should not happen
    }
}


@Composable
fun MedicationCard(
    medication: Medication,
    isForToday: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { if (isForToday) onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(medication.name, style = MaterialTheme.typography.titleMedium)
                Text("${medication.dosage} mg - ${medication.frequency}x daily",
                    style = MaterialTheme.typography.bodySmall)
                Text("Dose remaining: ${medication.frequency - medication.dosesUsedToday}",
                    style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Switch(
                checked = medication.isReminderOn,
                onCheckedChange = {},
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun AddMedicationDialog(onDismiss: (Medication) -> Unit) {
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
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Add Medication",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
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
            Column {
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
                    fontWeight = FontWeight.Bold
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
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && dosage != 0) {
                        onDismiss(
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
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
@Composable
fun EnableNotifications(medication: Medication){
    val context = LocalContext.current
    val morningTime = LocalTime.of(6, 0)  // 6 AM
    val eveningTime = LocalTime.of(20, 0) // 8 PM
    val afternoonTime = LocalTime.of(14, 0) // 2 PM (only for 3x daily)

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
fun MedicationCalendar(medicationSchedule: Map<LocalDate, List<Medication>>) {
    val today = LocalDate.now()
    val daysInMonth = YearMonth.of(today.year, today.month).lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(today.year, today.month, 1).dayOfWeek.value
    val startOffset = if (firstDayOfMonth == 7) 0 else firstDayOfMonth
    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }

    Column(
    ) {
        Text(
            text = today.month.name.capitalize(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(columns = GridCells.Fixed(7)) {
            // Add empty spaces for days before the first day
            items(startOffset) {
                Box(modifier = Modifier.size(40.dp))
            }

            // Add actual days (Fixed: Ensure day starts from 1)
            items(daysInMonth) { dayIndex ->
                val date = LocalDate.of(today.year, today.month, dayIndex + 1) // ✅ Fixed
                val medications = medicationSchedule[date].orEmpty()

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                        .clickable {
                            dayIndex.let { selectedDay = today.withDayOfMonth(it + 1) }
                        },
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${dayIndex + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp) // ✅ Fixed

                        if (medications.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                medications.forEach { medication ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
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
        selectedDay?.let { day ->
            MedicationBottomSheet(day, medicationSchedule[day] ?: emptyList()) {
                selectedDay = null
            }
        }
    }
}



@Composable
fun MedicationChart(
    medicationSchedule: Map<LocalDate, List<Medication>>
) {
    val today = LocalDate.now()
    val daysInMonth = today.lengthOfMonth()
    val firstDayOfMonth = today.withDayOfMonth(1).dayOfWeek.value % 7 // Adjust for Sunday start
    val daysList = List(firstDayOfMonth) { null } + (1..daysInMonth).map { it }
    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = today.month.name.capitalize(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(columns = GridCells.Fixed(7)) {
            items(daysList) { day ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            color = medicationSchedule[today.withDayOfMonth(day ?: 1)]?.let {
                                blendMedicationColors(it)
                            } ?: Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .clickable {
                            day?.let { selectedDay = today.withDayOfMonth(it) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = day?.toString() ?: "", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    selectedDay?.let { day ->
        MedicationBottomSheet(day, medicationSchedule[day] ?: emptyList()) {
            selectedDay = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationBottomSheet(day: LocalDate, medications: List<Medication>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Medications for ${day.dayOfMonth} ${day.month.name.capitalize()}", style = MaterialTheme.typography.titleMedium)
            medications.forEach { medication ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(medication.color), shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = medication.name, style = MaterialTheme.typography.bodyLarge)
                    if (medication.dosage == medication.frequency){
                        Icon(painter = painterResource(id = R.drawable.baseline_verified_24),
                            contentDescription = "Completed",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

fun blendMedicationColors(medications: List<Medication>): Color {
    return Color(medications.firstOrNull()?.color ?: Color.Transparent.toArgb()) // Simplified blending logic
}

