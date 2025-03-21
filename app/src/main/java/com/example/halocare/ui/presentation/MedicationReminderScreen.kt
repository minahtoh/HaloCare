package com.example.halocare.ui.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.halocare.R
import com.example.halocare.receivers.MedicationReminderReceiver
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Preview(widthDp = 320, heightDp = 720)
@Composable
fun MedicationReminderScreen() {
    var showAddDialog by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Today's Doses, 1 = All Medications
    var selectedMedication by remember { mutableStateOf<Medication?>(null) } // Store selected medication


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
            MedicationCalendar(medicationSchedule = dummyMedicationSchedule)
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
                    dummyMedicationSchedule[today].orEmpty() // Today's Doses
                } else {
                    dummyMedicationSchedule.values.flatten() // All Medications
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
                DoseLoggingDialog(
                    medication = medication,
                    onDismiss = { selectedMedication = null },

                )
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            AddMedicationDialog(onDismiss = { showAddDialog = false })
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
fun MedicationList() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(dummyMedications) { medication ->
            MedicationCard(medication)
        }
    }
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

    // Generate today’s doses based on realFrequency
    val todayDoses = generateDoses(medication.timeFrequency)

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Log Dose for ${medication.name}") },
        text = {
            Column {
                Text("Dosage: ${medication.dosage}")
                Text("Frequency: ${medication.timeFrequency}x per day")


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
                Text("${medication.dosage} - ${medication.frequency}",
                    style = MaterialTheme.typography.bodySmall)
                Text("Next Dose: ${medication.nextDose}",
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
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(1) } // Default: Once Daily
    var time by remember { mutableStateOf(LocalTime.of(8, 0)) }
    val timePickerDialog = rememberTimePickerDialog(time) { selectedTime -> time = selectedTime }
    var shouldNotify by remember { mutableStateOf(true) }

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
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage") }
            )
            Spacer(modifier = Modifier.height(8.dp))

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

            // Time Selector (TimePickerDialog)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { timePickerDialog.show() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Time: ${time.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                    fontWeight = FontWeight.Bold)
            }
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
                    if (name.isNotBlank() && dosage.isNotBlank()) {
                        onDismiss(
                            Medication(
                                name = name,
                                dosage = dosage,
                                frequency = "$frequency times daily",
                                nextDose = time.format(DateTimeFormatter.ofPattern("hh:mm a")),
                                isReminderOn = shouldNotify,
                                timeFrequency = frequency
                            )
                        )
                    }
                    if (shouldNotify){
                    //TODO : implement notification enabler
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

    scheduleMedicationReminder(context, medication, morningTime)
    if (medication.timeFrequency >= 2) {
        scheduleMedicationReminder(context, medication, eveningTime)
    }
    if (medication.timeFrequency == 3) {
        scheduleMedicationReminder(context, medication, afternoonTime)
    }
}

fun scheduleMedicationReminder(context: Context, medication: Medication, time: LocalTime) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
        putExtra("medication_name", medication.name)
        putExtra("medication_dosage", medication.dosage)
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
data class Medication(
    val name: String,
    val dosage: String,
    val frequency: String,
    val nextDose: String,
    val isReminderOn: Boolean,
    val color: Color = Color.Red,
    val isCompleted : Boolean = false,
    val timeFrequency : Int = 2
)

val dummyMedications = listOf(
    Medication("Paracetamol", "500mg", "Twice Daily", "08:00 PM", true),
    Medication("Vitamin C", "1000mg", "Once Daily", "09:00 AM", false)
)

val dummyMedicationSchedule: Map<LocalDate, List<Medication>> = mapOf(
    LocalDate.now() to listOf(
        Medication("Paracetamol", "500mg", "Twice a day", "08:00 AM", true, Color.Blue),
        Medication("Ibuprofen", "200mg", "Once a day", "12:00 PM", true, Color.Green)
    ),
    LocalDate.now().plusDays(1) to listOf(
        Medication("Metformin", "850mg", "Twice a day", "07:30 AM", true, Color.Magenta),
        Medication("Atorvastatin", "10mg", "Every night", "09:00 PM", true, Color.Yellow)
    ),
    LocalDate.now().plusDays(2) to listOf(
        Medication("Amoxicillin", "250mg", "Three times a day", "10:00 AM", true, Color.Cyan),
        Medication("Paracetamol", "500mg", "Twice a day", "08:00 AM", true, Color.Blue,true,3)
    ),
    LocalDate.now().plusDays(3) to listOf(
        Medication("Ibuprofen", "200mg", "Once a day", "12:00 PM", true, Color.Green)
    ),
    LocalDate.now().plusDays(4) to listOf(
        Medication("Atorvastatin", "10mg", "Every night", "09:00 PM", true, Color.Yellow),
        Medication("Metformin", "850mg", "Twice a day", "07:30 AM", true, Color.Magenta)
    )
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
                                            .background(medication.color, RoundedCornerShape(50))
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
                            .background(medication.color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = medication.name, style = MaterialTheme.typography.bodyLarge)
                    if (medication.isCompleted){
                        Icon(painter = painterResource(id = R.drawable.baseline_verified_24),
                            contentDescription = "Completed",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
        }
    }
}

fun blendMedicationColors(medications: List<Medication>): Color {
    return medications.firstOrNull()?.color ?: Color.Transparent // Simplified blending logic
}

