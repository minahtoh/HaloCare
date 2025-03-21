package com.example.halocare.ui.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.halocare.R
import com.example.halocare.ui.models.Professional
import java.time.LocalDate
import java.util.Calendar

@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    availableSpecialties: List<String> = emptyList(),  // List of specialties
    availableProfessionals: List<Professional> = emptyList(), // List of doctors/specialists
    //onConfirmAppointment: (Professional, LocalDate) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedSpecialty by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val filteredProfessionals = availableProfessionals.filter {
        it.specialty == selectedSpecialty
    }
    var selectedProfessional by remember { mutableStateOf<Professional?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showBookButton by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppointmentsTopBar()
        },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 🔹 1. Specialty Dropdown
            Text("Select Specialty", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            DropdownSelector(
                options = availableSpecialties,
                selectedOption = selectedSpecialty,
                onOptionSelected = {
                    selectedSpecialty = it
                    showBookButton = false
                    selectedProfessional = null
                }
            )

            // 🔹 2. Date Picker
            Spacer(Modifier.height(16.dp))
            Text("Select Date", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            DatePickerButton(
                selectedDate = selectedDate,
                onDateSelected = {
                    selectedDate = it
                    showBookButton = false
                }
            )

            // 🔹 3. Available Professionals List
            Spacer(Modifier.height(16.dp))
            Text("Available Professionals", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            filteredProfessionals.forEach { professional ->
                val isAvailable = selectedDate?.let { professional.isAvailable(it) } ?: true

                ProfessionalCard(
                    professional = professional,
                    isEnabled = isAvailable,
                    triggered = !showBookButton,
                    onSelect = {
                        if (isAvailable) {
                          //  onConfirmAppointment(professional, selectedDate!!)
                            selectedProfessional = professional
                            showBookButton = !showBookButton
                        }
                    }
                )
            }

            if (showBookButton && selectedProfessional != null) {
                Button(
                    onClick = {
                        if (selectedDate == null){
                            Toast.makeText(
                                context,
                                "Select a date for your appointment!",
                                    Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        showDialog = true
                              },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Book Appointment")
                }
            }

            if (showDialog && selectedProfessional != null) {
                AppointmentConfirmationDialog(
                    professional = selectedProfessional!!,
                    onDismiss = { showDialog = false }
                )
            }
        }

    }
}

@Composable
fun DropdownSelector(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = selectedOption ?: "Select Specialty")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { specialty ->
                DropdownMenuItem(
                    text = { Text(specialty) },
                    onClick = {
                        onOptionSelected(specialty)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DatePickerButton(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                onDateSelected(LocalDate.of(year, month + 1, day))
            },
            LocalDate.now().year,
            LocalDate.now().monthValue - 1,
            LocalDate.now().dayOfMonth
        )
    }

    OutlinedButton(
        onClick = { datePicker.show() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = selectedDate?.toString() ?: "Pick a Date")
    }
}

@Composable
fun ProfessionalCard(
    professional: Professional,
    isEnabled: Boolean,
    triggered: Boolean,
    onSelect: () -> Unit,
) {
    var isSelected by remember { mutableStateOf(false) }
    if (triggered) isSelected = false
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = isEnabled) {
                onSelect()
                isSelected = !isSelected
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Faded if unavailable
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.inversePrimary
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_person_3_24), // Placeholder icon
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(professional.name, fontWeight = FontWeight.Bold)
                Text(professional.specialty, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentConfirmationDialog(professional: Professional, onDismiss: () -> Unit) {
    var note by remember { mutableStateOf("") }

    var selectedTime by remember { mutableStateOf<String?>(null) }
    val timeOptions = listOf("10:00 AM", "12:00 PM", "2:00 PM", "4:00 PM")
    var customTimeSelected by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var customTime by remember { mutableStateOf("Choose Custom Time") }

    val timePickerDialog = remember {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                customTime = String.format("%02d:%02d %s",
                    if (selectedHour == 0 || selectedHour == 12) 12 else selectedHour % 12,
                    selectedMinute,
                    if (selectedHour < 12) "AM" else "PM"
                )
            },
            hour,
            minute,
            false // 12-hour format
        )
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { /* Confirm Appointment Logic */ onDismiss() }) {
                Text("Confirm Appointment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Confirm Appointment") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = professional.picture),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(professional.name, fontWeight = FontWeight.Bold)
                        Text(professional.specialty, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "$--${professional.price}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Select a time:")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ){
                    items(timeOptions){
                            time ->
                        OutlinedButton(
                            onClick = {
                                selectedTime = time
                                customTimeSelected = false
                                      },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedTime == time) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent
                            )
                        ) {
                            Text(time)
                        }
                    }
                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        OutlinedButton(
                            onClick = {
                                timePickerDialog.show()
                                selectedTime = customTime
                                customTimeSelected = true
                                      },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (customTimeSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(customTime)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Add Notes (Symptoms, Concerns)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 3.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { /* Open Image Picker */ },
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Attach Image")
                    }
                }
            }
        }
    )
}

@Composable
fun AppointmentsTopBar(){
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier,
        shadowElevation = 7.dp
    ){
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
                    painter = painterResource(id = R.drawable.baseline_keyboard_arrow_left_24),
                    contentDescription = "back",
                    colorFilter = ColorFilter.tint(
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                )
            }
            Text(
                text = "Book an Appointment",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.surfaceTint,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                shadowElevation = 5.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_meeting_room_24),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            color = MaterialTheme.colorScheme.surfaceTint
                        )
                    )
                }
            }
        }
    }
}