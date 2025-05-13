package com.example.halocare.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.halocare.R
import com.example.halocare.ui.models.Appointment
import com.example.halocare.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultsScreen(
    onBackPressed: () -> Unit = {},
    onAppointmentsClick : () -> Unit = {},
    scrollState: ScrollState,
    mainViewModel : MainViewModel
){
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.inversePrimary
    val currentUserId by mainViewModel.currentUserId.collectAsState()
    val appointmentsList by mainViewModel.appointmentsList.collectAsState()

    val pastAppointments = appointmentsList?.filter {
        it.status.equals("completed", ignoreCase = true) ||
                it.status.equals("rescheduled", ignoreCase = true)
    }

    val upcomingAppointments = appointmentsList?.filterNot {
        it.status.equals("completed", ignoreCase = true) ||
                it.status.equals("rescheduled", ignoreCase = true)
    }

    Surface(
        //modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Scaffold(
            topBar = {
                ConsultationsTopBar(
                    onBackPressed
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { paddingValues ->

            LaunchedEffect(true ){
                statusBarController.updateStatusBar(
                    color = statusBarColor,
                    darkIcons = true
                )
                mainViewModel.getUserAppointments(currentUserId)
            }

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(10.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Appointment",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${upcomingAppointments?.size ?: "0"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth()
                ){
                    when{
                        upcomingAppointments == null ->{
                            items(5){
                                UpcomingAppointmentCard(appointment = null)
                            }
                        }
                        upcomingAppointments.isEmpty() -> {
                            item {
                                EmptyAppointmentPlaceholder(
                                    title = "No Upcoming Appointments",
                                    message = "You haven't booked any appointments yet. Once you do, they’ll show up here."
                                )
                            }
                        }
                        else -> {
                            items(upcomingAppointments.size){
                                UpcomingAppointmentCard(
                                    appointment = upcomingAppointments[it]
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onAppointmentsClick() },
                        modifier = Modifier.padding(start = 30.dp, end = 30.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text(
                            text = "Book new appointment",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Past Appointments",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LazyColumn(
                    modifier = Modifier.height(400.dp)
                ) {
                    when{
                        pastAppointments == null ->{
                            items(5){
                                UserAppointmentCard(appointment = null)
                            }
                        }
                        pastAppointments.isEmpty() -> {
                            item {
                                EmptyAppointmentPlaceholder(
                                    title = "No Past Appointments",
                                    message = "It looks like you haven’t completed any appointments yet."
                                )
                            }
                        }
                        else -> {
                            items(pastAppointments.size){
                                UserAppointmentCard(
                                    appointment = pastAppointments[it]
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun EmptyAppointmentPlaceholder(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_event_busy_24),
            contentDescription = "Empty Icon",
            tint = Color.Gray,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ConsultationsTopBar(
    onBackPressed : () -> Unit
){
    Surface(
        color = MaterialTheme.colorScheme.inversePrimary,
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
                color = MaterialTheme.colorScheme.inversePrimary,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .size(5.dp)
                    .clickable {
                        onBackPressed()
                    }
            ) {
            }
            Text(
                text = "Consultations",
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
                    Text(
                        text = "...",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.surfaceTint,
                    )
                }
            }
        }
    }
}

@Composable
fun UserAppointmentCard(
    appointment: Appointment?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = appointment?.profilePicture,
                contentDescription = "${appointment?.professionalName}'s photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape).addForShimmer(appointment)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f).addForShimmer(appointment)
            ) {
                Text(
                    text = appointment?.professionalName ?: "                ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = appointment?.occupation ?: "            ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = if(appointment != null) "${appointment.date} at ${appointment.time}" else "                ",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if(appointment != null){
                StatusBadge(status = appointment.status)
            }
        }
    }
}
@Composable
fun StatusBadge(status: String) {
    val backgroundColor = when (status.lowercase()) {
        "booked" -> Color(0xFF4CAF50) // green
        "pending" -> Color(0xFFFFC107) // amber
        "cancelled" -> Color(0xFFF44336) // red
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}


@Composable
fun UpcomingAppointmentCard(
    appointment: Appointment?,
    modifier: Modifier = Modifier,
    onPayClick: () -> Unit = {}
) {
    Card(
    modifier = modifier
        .fillMaxWidth()
        .height(280.dp)  // Fixed height
        .padding(horizontal = 16.dp, vertical = 8.dp),
    elevation = CardDefaults.cardElevation(4.dp),
    shape = RoundedCornerShape(12.dp)
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(72.dp)
        ) {
            AsyncImage(
                model = appointment?.profilePicture,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .addForShimmer(appointment)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier
                .fillMaxHeight()
                .addForShimmer(appointment)
            ) {
                Text(
                    text = appointment?.professionalName ?:"                         ",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = appointment?.occupation ?:"                         ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))  // Push status to bottom
                StatusPill(appointment?.status ?:"                        ",
                appointment)
            }
            Spacer(
                Modifier
                    .width(16.dp)
                    .align(Alignment.Top))
            Column {
                if (appointment != null){
                    Text(
                        text = "₦${appointment.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .addForShimmer(appointment)
                    )
                } else{
                    Text(
                        text = "             ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .addForShimmer(null)
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }

        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(80.dp)
        ) {
            Column(
                modifier = Modifier
                    .height(48.dp)
                    .addForShimmer(appointment)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (appointment != null) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .addForShimmer(null)
                        )
                    }

                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = appointment?.date ?: "                         ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (appointment != null) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_schedule_24),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .addForShimmer(null)
                        )
                    }

                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = appointment?.time ?: "                         ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (
                    !appointment?.status.isNullOrBlank() &&
                    appointment?.status?.trim().equals("awaiting confirmation", ignoreCase = true)
                ) {
                    Spacer(Modifier.height(2.dp))
                    Button(
                        onClick = { onPayClick?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Pay Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Note
        appointment?.note?.takeIf { it.isNotBlank() }?.let { note ->
            Column(modifier = Modifier
                .height(40.dp)
                .addForShimmer(appointment)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_notes_24),
                        contentDescription = "notes"
                    )
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Text(
            text = if (appointment != null) "Booked on ${formatTimestamp(appointment.bookedAt)}" else "    ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .height(24.dp)
                .addForShimmer(appointment)
            )
        }
    }
}
@Composable
private fun StatusPill(status: String, appointment: Appointment?) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                when (status.lowercase()) {
                    "confirmed" -> Color.Green.copy(alpha = 0.2f)
                    "cancelled" -> Color.Red.copy(alpha = 0.2f)
                    else -> Color.Gray.copy(alpha = 0.2f)
                }
            )
            .addForShimmer(appointment)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = when (status.lowercase()) {
                "confirmed" -> Color.Green
                "cancelled" -> Color.Red
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

// Helper function to format timestamp
private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        .format(Date(timestamp))
}
@Composable
fun Modifier.addForShimmer(appointment: Appointment?): Modifier {
    return if (appointment == null) {
        this.then(Modifier.shimmerEffect())
    } else {
        this
    }
}
