package com.example.halocare.ui.presentation

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke

import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.halocare.R
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

import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Preview(widthDp = 320, heightDp = 720)
@Composable
fun PediatricTrackerScreen(
    onBackIconClick : () -> Unit
) {
    var selectedAgeGroup by remember { mutableStateOf("0-3 months") }
    var selectedTab by remember { mutableStateOf(0) }
    var milestoneToggleTrigger by remember { mutableStateOf(false) }
    var selectedAgeRange by remember { mutableStateOf(pediatricMilestones.first()) }
    var targetAgeRange by remember{ mutableStateOf( selectedAgeRange.ageRange)}
    var showCelebration by remember { mutableStateOf(false) }
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.tertiaryContainer


    Scaffold(
        topBar = { TrackerTopBar(
            onBackIconClick = {onBackIconClick()}
        ) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
            Spacer(modifier = Modifier.height(5.dp))
            NewAgeGroupSelector(
                modifier = Modifier,
                pediatricMilestones,
                selectedAgeRange,
            ) { newAgeRange ->
                selectedAgeRange = newAgeRange
                selectedTab = 0
            }
            Spacer(modifier = Modifier.height(3.dp))
            ProgressSection(
                selectedAgeRange = selectedAgeRange,
                onFullyAchieved = { showCelebration = true }
            )
            Spacer(modifier = Modifier.height(3.dp))
           MilestoneCategorySection(
               categories = selectedAgeRange.categories,
               selectedTab = selectedTab,
               onTabSelected = { selectedTab = it },
           )

            Spacer(modifier = Modifier.height(550.dp))
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
                        .padding(16.dp).padding(bottom = 25.dp),
                    contentAlignment = Alignment.BottomCenter
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
fun TrackerTopBar(
    onBackIconClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 17.dp,
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
            Text(
                text = "Pediatric Development Tracker",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

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
                        painter = painterResource(id = R.drawable.ped_dev_tracker_ic),
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
                modifier = Modifier.heightIn(max = 450.dp)
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
fun NewAgeGroupSelector(
    modifier: Modifier = Modifier,
    ageGroups: List<MilestoneAgeRange>,
    selected: MilestoneAgeRange,
    onSelect: (MilestoneAgeRange) -> Unit,

) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {

        Spacer(modifier = Modifier.height(4.dp))

        // Dropdown Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (expanded) 8.dp else 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {

            // Label
            Column(
                modifier = Modifier.fillMaxWidth().padding(7.dp)
            ) {
                Text(
                    text = "How old is your child?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 3.dp)
                )
            }

            Column {
                // Dropdown Trigger
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .background(
                            color = if (expanded) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                            } else Color.Transparent
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Age Icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getAgeIcon(selected),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = selected.ageRange,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Development milestones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Dropdown Arrow
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                                        else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(
                                    animateFloatAsState(
                                        targetValue = if (expanded) 180f else 0f,
                                        animationSpec = tween(300)
                                    ).value
                                )
                        )
                    }
                }

                // Dropdown Menu
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        itemsIndexed(ageGroups) { index, ageGroup ->
                            val isSelected = selected == ageGroup
                            val isLast = index == ageGroups.size - 1

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(ageGroup)
                                        expanded = false
                                    }
                                    .background(
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        } else Color.Transparent
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Age Icon
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getAgeIcon(ageGroup),
                                            contentDescription = null,
                                            tint = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = ageGroup.ageRange,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        Text(
                                            text = getAgeDescription(ageGroup),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Checkmark for selected item
                                    AnimatedVisibility(
                                        visible = isSelected,
                                        enter = scaleIn(tween(200)) + fadeIn(tween(200)),
                                        exit = scaleOut(tween(200)) + fadeOut(tween(200))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Divider (except for last item)
                            if (!isLast) {
                                Divider(
                                    modifier = Modifier.padding(horizontal = 64.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to get appropriate icon based on age range
@Composable
private fun getAgeIcon(ageRange: MilestoneAgeRange): ImageVector {
    // You'll need to adjust this based on your MilestoneAgeRange structure
    return when {
        ageRange.ageRange.contains("month") && !ageRange.ageRange.contains("12") -> Icons.Default.Face
        ageRange.ageRange.contains("year") || ageRange.ageRange.contains("12") -> Icons.Default.Person
        else -> Icons.Default.Face
    }
}

// Helper function to get age description
private fun getAgeDescription(ageRange: MilestoneAgeRange): String {
    return when {
        ageRange.ageRange.contains("month") -> "Early development phase"
        ageRange.ageRange.contains("year") -> "Active learning phase"
        else -> "Key milestone period"
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                PalmCheckbox(
                                    checked = milestone.isAchieved.value,
                                    onCheckedChange = { milestone.isAchieved.value = it }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.onTertiaryContainer
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

@Composable
fun PalmCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(400)
    )

    Box(
        modifier = modifier
            .size(34.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCheckedChange(!checked) }
    ) {

        val palmColor = if (checked) MaterialTheme.colorScheme.tertiary else Color.Gray

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val palmSize = size.minDimension / 6
            val palmCenter = Offset(size.width / 2, size.height * 0.7f)
            val adjust = 6.dp.toPx()
            // Draw fingers with proper spread angles for open palm
            val fingers = listOf(
                // Thumb - angled left and shorter
                Triple(-145f, palmSize * 1.8f, palmCenter.copy(x = palmCenter.x - palmSize * 0.7f)),
                // Index finger - angled slightly left
                Triple(-110f, palmSize * 2.6f, palmCenter.copy(x = palmCenter.x - palmSize * 0.4f)),
                // Middle finger - straight up (longest)
                Triple(-90f, palmSize * 2.8f, palmCenter),
                // Ring finger - angled slightly right
                Triple(-70f, palmSize * 2.5f, palmCenter.copy(x = palmCenter.x + palmSize * 0.4f)),
                // Pinky - angled more right and shortest
                Triple(-35f, palmSize * 1.9f, palmCenter.copy(x = palmCenter.x + palmSize * 0.7f, ))
            )

            fingers.forEach { (angle, length, startPos) ->
                val angleRad = angle * (PI / 180f).toFloat()
                val endX = startPos.x + cos(angleRad) * length * animatedProgress
                val endY = (startPos.y - 15f) + sin(angleRad) * length * animatedProgress

                drawLine(
                    color = palmColor,
                    start = startPos,
                    end = Offset(endX, endY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw squircle palm
            val squircleSize = palmSize * 1.5f
            val squircleRect = Rect(
                offset = Offset(20f,39f - adjust),
                size = Size(squircleSize * 2, squircleSize * 2)
            )

            drawRoundRect(
                color = palmColor,
                topLeft = squircleRect.topLeft,
                size = squircleRect.size,
                cornerRadius = CornerRadius(squircleSize * 0.4f),
                alpha = 0.3f + (animatedProgress * 0.7f)
            )

            // Optional: Add inner squircle when checked

        }
    }
}