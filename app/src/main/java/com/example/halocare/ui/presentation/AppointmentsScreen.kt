package com.example.halocare.ui.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.halocare.R
import com.example.halocare.ui.models.Appointment
import com.example.halocare.ui.models.Professional
import com.example.halocare.viewmodel.LoadingState
import com.example.halocare.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.util.Calendar

//@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    mainViewModel: MainViewModel,
    navigateToConsultsScreen : () -> Unit
    //onConfirmAppointment: (Professional, LocalDate) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedSpecialty by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val availableSpecialties by mainViewModel.availableSpecialties.collectAsState()
    val availableSpecialtiesList : List<String>? = availableSpecialties?.map{ professionalSpecialty -> professionalSpecialty.specialtyName }

    val chosenSpecialty = availableSpecialties?.find { it.specialtyName == selectedSpecialty }

    val professionalsList = chosenSpecialty?.professionals ?: emptyList()

    val sortedProfessionals = professionalsList.sortedBy { professional ->
        selectedDate?.let { professional.isAvailable(it) } ?: true
    }.reversed()

    var selectedProfessional by remember { mutableStateOf<Professional?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showBookButton by remember { mutableStateOf(false) }
    val currentUserId by mainViewModel.currentUserId.collectAsState()
    val bookingLoadingState by mainViewModel.appointmentBookingState.collectAsState()


    LaunchedEffect(Unit) {
        mainViewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

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
            AppointmentBookingDialog(
                loadingState = bookingLoadingState,
                onDismiss = {},
                onComplete = { isSuccessful ->
                    if (isSuccessful){
                        navigateToConsultsScreen()
                        mainViewModel.resetBookingState()
                    }
                }
            )
            // 🔹 1. Specialty Dropdown
            Text("Select Specialty", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            DropdownSelector(
                options = availableSpecialtiesList ?: emptyList(),
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

            sortedProfessionals.forEach { professional ->
                val isAvailable = selectedDate?.let { professional.isAvailable(it) } ?: true
                ProfessionalListCard(
                    professional = professional,
                    isEnabled = isAvailable,
                    triggered = !showBookButton,
                    onClick = {
                        if (isAvailable) {
                            selectedProfessional = professional
                            showBookButton = !showBookButton
                        }
                    }
                )
            }

            if (showDialog && selectedProfessional != null) {
                AppointmentConfirmationDialog(
                    professional = selectedProfessional!!,
                    selectedDate = selectedDate,
                    onDismiss = { showDialog = false },
                    onAppointmentSubmitted = {
                        appointment -> mainViewModel.bookUserAppointment(currentUserId, appointment)
                    }
                )
            }
            selectedProfessional?.let {
                ModalBottomSheet(
                    onDismissRequest = { selectedProfessional = null },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    ProfessionalDetailSheet(professional = it)
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
                            .padding(16.dp)
                            .padding(bottom = 10.dp)
                    ) {
                        Text("Book Appointment")
                    }
                }
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentConfirmationDialog(
    professional: Professional,
    selectedDate: LocalDate?,
    onAppointmentSubmitted: (Appointment) -> Unit,
    onDismiss: () -> Unit) {
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
            {  _, selectedHour, selectedMinute ->
                val formatted = String.format(
                    "%02d:%02d %s",
                    if (selectedHour == 0 || selectedHour == 12) 12 else selectedHour % 12,
                    selectedMinute,
                    if (selectedHour < 12) "AM" else "PM"
                )
                customTime = formatted
                selectedTime = formatted
                customTimeSelected = true
            },
            hour,
            minute,
            false
        )
    }

    fun bookAppointment(
        professional: Professional,
        selectedTime: String,
        selectedDate: String,
        note: String?,
    ): Appointment {
        return Appointment(
            professionalId = professional.id,
            professionalName = professional.name,
            profilePicture = professional.picture,
            occupation = professional.specialty,
            price = professional.consultationPrice.toDouble(),
            time = selectedTime,
            note = note,
            status = "Awaiting Confirmation",
            date = selectedDate
        )
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (selectedTime != null){
                    onAppointmentSubmitted(bookAppointment(
                        professional = professional,
                        selectedTime = selectedTime!!,
                        selectedDate = selectedDate.toString(),
                        note = note
                    ))
                    onDismiss()
                } else{
                    Toast.makeText(context, "Select a time for your Appointment!", Toast.LENGTH_SHORT)
                        .show()
                }

            }) {
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
                    AsyncImage(
                        model  = professional.picture,
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
                            text = "₦--${professional.consultationPrice}",
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
                        Icon(
                            painter = painterResource(id = R.drawable.round_mic_none_24),
                            contentDescription = "Record audio")
                    }
                    IconButton(
                        onClick = { /* Open Image Picker */ },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_image_24),
                            contentDescription = "Attach Image")
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


@Composable
fun ProfessionalListCard(
    professional: Professional,
    isEnabled: Boolean,
    triggered: Boolean,
    onClick: () -> Unit = {}
) {
    var isSelected by remember { mutableStateOf(false) }
    if (triggered) isSelected = false
    Card(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Faded if unavailable
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.inversePrimary
            }
        )
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = professional.picture,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = professional.name, fontWeight = FontWeight.Bold)
                Text(text = professional.specialty, color = Color.Gray)
                Text(text = professional.location, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Text(text = "${professional.rating}", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ProfessionalDetailSheet(
    professional: Professional
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = professional.picture,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = professional.name, style = MaterialTheme.typography.titleMedium)
        Text(text = professional.specialty, color = Color.Gray)
        Text(text = professional.location, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Bio", fontWeight = FontWeight.SemiBold)
        Text(text = professional.bio, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Languages: ${professional.language.joinToString(", ")}")
        Text(text = "Rating: ${professional.rating}")
        Text(text = "Consultation Fee: ₦${professional.consultationPrice}")

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Next Available Dates:")
        LazyRow {
            items(professional.availableDates) { date ->
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = date, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppointmentBookingDialog(
    loadingState: LoadingState,
    onComplete: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    if (loadingState != LoadingState.IDLE) {
        var animationComplete by remember { mutableStateOf(false) }

        LaunchedEffect(loadingState) {
            if (loadingState == LoadingState.SUCCESSFUL || loadingState == LoadingState.ERROR) {
                delay(1500)
                animationComplete = true
            }
        }

        if (animationComplete) {
            onComplete(loadingState == LoadingState.SUCCESSFUL)
            animationComplete = false
            onDismiss()
        }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = loadingState,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "Loading State Change"
                    ) { state ->
                        when (state) {
                            LoadingState.LOADING -> LoadingDots()
                            LoadingState.SUCCESSFUL -> Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(64.dp)
                            )
                            LoadingState.ERROR -> Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(64.dp)
                            )
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingDots() {
    var dotCount by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            dotCount = if (dotCount < 3) dotCount + 1 else 1
        }
    }

    Text(
        text = "Booking" + ".".repeat(dotCount),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    )
}
