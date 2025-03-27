package com.example.halocare.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.halocare.R
import com.example.halocare.ui.models.Appointment

//@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultsScreen(
    onBackPressed: () -> Unit = {},
    onAppointmentsClick : () -> Unit = {},
    scrollState: ScrollState
){
    Surface(
        //modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        val upcomingAppointment = Appointment(
            doctorName = "Dr. Jane Smith",
            doctorPicture = R.drawable.baseline_person_3_24,
            date = "March 10, 2025",
            time = "10:30 AM",
            price = 49.99,
            occupation = "Occupational Therapist",
            status = "Confirmed"
        )

        val pastAppointments = listOf(
            Appointment(
                doctorName = "Dr. Alex Brown",
                doctorPicture = R.drawable.baseline_person_3_24,
                date = "Feb 20, 2025",
                time = "2:00 PM",
                price = 24.9,
                status = "Completed"),
            Appointment(
                doctorName = "Dr. Emily Davis",
                doctorPicture = R.drawable.baseline_person_3_24,
                date = "Jan 15, 2025",
                time = "11:00 AM",
                price = 15.00,
                status = "Completed"),
            Appointment(
                doctorName = "Dr. Michael Johnson",
                doctorPicture = R.drawable.baseline_person_3_24,
                date = "Dec 10, 2024",
                time = "9:30 AM",
                price = 99.99,
                status = "Completed")
        )

        Scaffold(
            topBar = {
                ConsultationsTopBar(
                    onBackPressed
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                              onAppointmentsClick()
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Appointment")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
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
                                text = "1",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                UpcomingAppointmentCard(appointment = upcomingAppointment)

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
                    modifier = Modifier.height(200.dp)
                ) {
                    items(pastAppointments.size) { appointment ->
                        AppointmentCard(appointment = pastAppointments[appointment])
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                UserDashboardCard(
                    imageRes = R.drawable.supplements,
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AppointmentCard(appointment: Appointment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.inversePrimary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = appointment.doctorPicture
                    ),
                    contentDescription = "doctor_picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = appointment.doctorName, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${appointment.date} at ${appointment.time}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = appointment.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(20.dp)
                    .width(70.dp),
                shadowElevation = 3.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "$" + "${appointment.price}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingAppointmentCard(appointment: Appointment){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Berlin, Germany 23",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = appointment.date,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = appointment.time,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = appointment.doctorPicture),
                    contentDescription = "profile_picture" )
                Spacer(modifier = Modifier.width(5.dp))
                Column() {
                    Text(
                        text = appointment.doctorName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = appointment.occupation,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                elevation = ButtonDefaults.buttonElevation(5.dp)

              //  shape = ButtonDefaults.shape(RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Pay now --$ ${appointment.price}")
                    Image(
                        painter = painterResource(id = R.drawable.baseline_keyboard_double_arrow_right_24),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
fun ConsultationsTopBar(
    onBackPressed : () -> Unit
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
                modifier = Modifier.size(30.dp).clickable {
                    onBackPressed()
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_keyboard_double_arrow_right_24),
                    contentDescription = "back",
                    modifier = Modifier.rotate(180f),
                    colorFilter = ColorFilter.tint(
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                )
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