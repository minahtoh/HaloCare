package com.example.halocare.ui.presentation

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.tertiaryContainer


    Scaffold(
        topBar = { TrackerTopBar() },
        containerColor = MaterialTheme.colorScheme.inversePrimary
    ) { paddingValues ->

        LaunchedEffect(true){
            statusBarController.updateStatusBar(
                color = statusBarColor,
                darkIcons = true
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AgeGroupSelector(
                pediatricMilestones,
                selectedAgeRange
            ) { newAgeRange ->
                selectedAgeRange = newAgeRange
                selectedTab = 0
            }
           MilestoneCategorySection(
               categories = selectedAgeRange.categories,
               selectedTab = selectedTab,
               onTabSelected = { selectedTab = it },
               modifier = Modifier.weight(1f)
           )
            ProgressSection(
                selectedAgeRange = selectedAgeRange,
                onFullyAchieved = { showCelebration = true }
            )
        }

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
                        .fillMaxWidth()
                        .fillMaxHeight()
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
        ),
        modifier = Modifier.shadow(elevation = 4.dp)
    )
}

@Composable
fun AgeGroupSelector(
    ageGroups: List<MilestoneAgeRange>,
    selected: MilestoneAgeRange,
    onSelect: (MilestoneAgeRange) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Age Range",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 350.dp)
            ) {
                items(ageGroups) { ageGroup ->
                    OutlinedButton(
                        onClick = { onSelect(ageGroup) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected == ageGroup) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            },
                            contentColor = if (selected == ageGroup) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selected == ageGroup) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = ageGroup.ageRange,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MilestoneCategorySection(
    categories: List<MilestoneCategory>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header
            Text(
                text = "Milestone Categories",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
            )

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // Divider between tabs and content
            Divider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Selected category content
            val selectedCategory = categories.getOrNull(selectedTab)
            selectedCategory?.let { category ->
                Column {
                    Text(
                        text = "${category.name} Milestones",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(category.milestones) { milestone ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        milestone.isAchieved.value = !milestone.isAchieved.value
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Checkbox(
                                    checked = milestone.isAchieved.value,
                                    onCheckedChange = { milestone.isAchieved.value = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = milestone.description,
                                    modifier = Modifier.padding(start = 12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (milestone.isAchieved.value) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }

                            if (milestone != category.milestones.last()) {
                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ProgressSection(
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Progress",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${(targetProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$achievedMilestones of $totalMilestones milestones achieved",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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


