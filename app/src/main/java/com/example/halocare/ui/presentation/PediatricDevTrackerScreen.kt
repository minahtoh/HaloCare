package com.example.halocare.ui.presentation

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.halocare.ui.models.Milestone
import com.example.halocare.ui.models.MilestoneAgeRange
import com.example.halocare.ui.models.MilestoneCategory
import com.example.halocare.ui.models.pediatricMilestones
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import org.checkerframework.checker.units.qual.Speed
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@Preview(widthDp = 320, heightDp = 720)
@Composable
fun PediatricTrackerScreen() {
    var selectedAgeGroup by remember { mutableStateOf("0-3 months") }
    var selectedTab by remember { mutableStateOf(0) }
    var milestoneToggleTrigger by remember { mutableStateOf(false) }
    var selectedAgeRange by remember { mutableStateOf(pediatricMilestones.first()) }
    var targetAgeRange by remember{ mutableStateOf( selectedAgeRange.ageRange)}
    var showCelebration by remember { mutableStateOf(false) }


    Scaffold(
        topBar = { TrackerTopBar() },
        containerColor = MaterialTheme.colorScheme.inversePrimary
    ) { paddingValues ->

        if (showCelebration) {
            LaunchedEffect(Unit){
                delay(2000)
                showCelebration = false
            }
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = remember {
                    listOf(
                        Party(
                            emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(50),
                            spread = 360,
                            colors = listOf(Color.Yellow.toArgb(), Color.Green.toArgb(), Color.Magenta.toArgb()),
                            speed = 20f,
                            position = Position.Relative(0.5, 0.3)
                        )
                    )
                }
            )
            AnimatedVisibility(visible = showCelebration) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().fillMaxHeight()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉 All age appropriate milestones achieved! 🎉",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.inversePrimary,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(10.dp)
        ) {
            AgeGroupSelector(
                pediatricMilestones,
                selectedAgeRange
            ) { newAgeRange ->
                selectedAgeRange = newAgeRange
                selectedTab = 0 // Reset tab when age range changes
            }

            val categories = selectedAgeRange.categories

            TabRow(
                containerColor = MaterialTheme.colorScheme.inversePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTabIndex = selectedTab
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }) {
                        Text(
                            category.name,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            val selectedCategory = categories.getOrNull(selectedTab)

            selectedCategory?.let {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    LazyColumn {
                        items(it.milestones) { milestone ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        milestone.isAchieved.value = !milestone.isAchieved.value
                                    }
                            ) {
                                Checkbox(
                                    checked = milestone.isAchieved.value,
                                    onCheckedChange = { milestone.isAchieved.value = it }
                                )
                                Text(
                                    text = milestone.description,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            ProgressBar(
                selectedAgeRange,
                onFullyAchieved = {
                    showCelebration = true
                }
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerTopBar() {
    TopAppBar(
        title = { Text(
            "Pediatric Development Tracker",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        ) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            titleContentColor = MaterialTheme.colorScheme.tertiary
        )
    )
}

@Composable
fun AgeGroupSelector(
    ageGroups: List<MilestoneAgeRange>,
    selected: MilestoneAgeRange, onSelect: (MilestoneAgeRange) -> Unit)
{
    LazyRow(
        contentPadding = PaddingValues(bottom = 10.dp),
    ) {
        items(ageGroups) { ageGroup ->
            OutlinedButton(
                modifier = Modifier.padding(3.dp),
                onClick = { onSelect(ageGroup) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor =
                    if (selected == ageGroup)
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(ageGroup.ageRange)
            }
        }
    }
}


@Composable
fun ProgressBar(
    selectedAgeRange: MilestoneAgeRange,
    onFullyAchieved: () -> Unit
) {
    val totalMilestones = selectedAgeRange.categories.sumOf { it.milestones.size }
    val achievedMilestones = selectedAgeRange.categories.sumOf { category ->
        category.milestones.count { it.isAchieved.value }
    }

    val targetProgress = if (totalMilestones > 0) {
        achievedMilestones / totalMilestones.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "MilestoneProgress"
    )

    LaunchedEffect(targetProgress) {
        if (targetProgress >= 1f) {
            onFullyAchieved()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Milestone Progress",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$achievedMilestones of $totalMilestones achieved",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}




fun showDatePicker(context: Context, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val year = selectedDate.year
    val month = selectedDate.monthValue - 1 // Month index starts from 0
    val day = selectedDate.dayOfMonth

    DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
        onDateSelected(LocalDate.of(selectedYear, selectedMonth + 1, selectedDay))
    }, year, month, day).show()
}

val milestoneData = listOf(
    MilestoneAgeRange(
        ageRange = "0-3 Months",
        categories = listOf(
            MilestoneCategory(
                name = "Motor Skills",
                milestones = listOf(
                    Milestone(description = "Lifts head briefly when on tummy"),
                    Milestone(description = "Moves arms and legs actively")
                )
            ),
            MilestoneCategory(
                name = "Cognitive Skills",
                milestones = listOf(
                    Milestone(description = "Responds to sounds"),
                    Milestone(description = "Follows moving objects with eyes")
                )
            )
        )
    ),
    MilestoneAgeRange(
        ageRange = "4-6 Months",
        categories = listOf(
            MilestoneCategory(
                name = "Motor Skills",
                milestones = listOf(
                    Milestone(description = "Rolls from back to tummy"),
                    Milestone(description = "Sits with support")
                )
            ),
            MilestoneCategory(
                name = "Social Skills",
                milestones = listOf(
                    Milestone(description = "Smiles at familiar people"),
                    Milestone(description = "Laughs when playing")
                )
            )
        )
    )
)


