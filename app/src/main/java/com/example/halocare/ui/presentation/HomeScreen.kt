package com.example.halocare.ui.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.halocare.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController = rememberNavController(),
    onProfileClick : () -> Unit = {}
) {
    val features = listOf(
        "Development Tracker", "Medication Reminder", "Health Insights",
        "Symptom Checker"
    )
    val icons = listOf(
        Icons.Default.Favorite, Icons.Default.DateRange, Icons.Default.ShoppingCart,
        Icons.Default.Build
    )
    val images = listOf(
        R.drawable.supplements,
        R.drawable.screenshot_2025_03_07_071855,
        R.drawable.screenshot_2025_03_07_071929,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, User") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = {
                        onProfileClick()
                    }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { paddingValues ->

        val pagerState = rememberPagerState {images.size }
        val coroutineScope = rememberCoroutineScope()
        var lastUserInteraction by remember { mutableStateOf(System.currentTimeMillis()) }



        LaunchedEffect(Unit) {
            while (true) {
                delay(3000) // Wait for 3 seconds
                val now = System.currentTimeMillis()
                if (now - lastUserInteraction >= 3000) { // Only scroll if no interaction
                    val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                item {
                    Column {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentPadding = PaddingValues(32.dp),
                            pageSpacing = 3.dp
                        ) { page ->
                            UserDashboardCard(
                                imageRes = images[page],
                                modifier = Modifier
                                    .graphicsLayer {
                                        val pageOffset =
                                            calculateCurrentOffsetForPage(
                                                page,
                                                pagerState
                                            ).absoluteValue
                                        scaleX = 1f - (pageOffset * 0.1f)
                                        scaleY = 1f - (pageOffset * 0.1f)
                                    }
                            )

                            // Detect user swipe & reset timer
                            LaunchedEffect(pagerState.currentPageOffsetFraction) {
                                if (pagerState.currentPageOffsetFraction != 0f) {
                                    lastUserInteraction = System.currentTimeMillis()
                                }
                            }
                        }

                        // Indicator Dots
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 3.dp)
                        ) {
                            repeat(pagerState.pageCount) { index ->
                                val selected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .size(if (selected) 12.dp else 8.dp)
                                        .padding(4.dp)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary
                                            else Color.Gray,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }

                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(400.dp).padding(10.dp)
                    ) {
                        items(features.size) { index ->
                            Card(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .clickable { /* Navigate to feature */ },
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        icons[index],
                                        contentDescription = features[index],
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(features[index], fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                item { WeatherCard() }
            }
        }
    }
}

@Composable
fun UserDashboardCard(
    imageRes : Int,
    modifier: Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Vitamin D",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay (Right to Half)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f), // Dark gradient start (Right side)
                                Color.Transparent // Fade to transparent towards the left
                            ),
                            startX = 0f, // Adjust gradient width
                            endX = 400f
                        )
                    )
            )

            // Text Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Vitamin D",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "20% Off",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}


@Composable
fun HaloCareBottomBar(){
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                HaloCareHomeIcon(
                    size = 30.dp,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_healing_24),
                    contentDescription = null,
                    Modifier.size(30.dp)
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_monitor_heart_24),
                    contentDescription = null,
                    Modifier.size(30.dp)
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_settings_24),
                    contentDescription = null,
                    Modifier.size(30.dp)
                )
            }
        }
    }
}
@Composable
fun HaloCareBottomBarCurved(
    navController: NavController,
    isVisible: Boolean
) {

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route


    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val outlineColor = MaterialTheme.colorScheme.secondaryContainer
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                val width = size.width
                val height = size.height

                val fabRadius = 40.dp.toPx() // Adjust based on FAB size
                val fabCenterX = width / 2
                val fabBottomY = height - fabRadius / 2

                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(fabCenterX - fabRadius * 1.5f, 0f)

                    // Create the curved cutout for the FAB
                    cubicTo(
                        fabCenterX - fabRadius, 0f,
                        fabCenterX - fabRadius * 0.5f, fabBottomY,
                        fabCenterX, fabBottomY
                    )
                    cubicTo(
                        fabCenterX + fabRadius * 0.5f, fabBottomY,
                        fabCenterX + fabRadius, 0f,
                        fabCenterX + fabRadius * 1.5f, 0f
                    )

                    lineTo(width, 0f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path,
                    color = outlineColor
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(end = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = {
                        HaloCareHomeIcon(
                            size = 25.dp,
                           // isSelected = true,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    },
                    label = "Home",
                    isSelected = currentRoute == HomeScreen.route,
                    onClick = {
                        navController.navigateSingleTopTo(HomeScreen.route)
                    }
                )
                BottomNavItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_healing_24),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                        )
                    },
                    label = "Consults",
                    isSelected = currentRoute == ConsultsScreen.route,
                    onClick = {
                        navController.navigateSingleTopTo(ConsultsScreen.route)
                    }
                )
                Spacer(Modifier.width(56.dp)) // Space for FAB
                BottomNavItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_monitor_heart_24),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                        )
                    },
                    label = "Health Tracking",
                    isSelected = currentRoute == HealthTrackingScreen.route,
                    onClick = {
                        navController.navigateSingleTopTo(HealthTrackingScreen.route)
                    }
                )
                BottomNavItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_settings_24),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                        )
                    },
                    label = "Settings",
                    isSelected = currentRoute == SettingsScreen.route,
                    onClick = {
                        navController.navigateSingleTopTo(SettingsScreen.route)
                    }
                )
            }

            FloatingActionButton(
                onClick = { /*TODO*/ },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-24).dp) // Lifts the FAB above the cutout
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

private fun NavController.navigateSingleTopTo(route: String,popBackStack: Boolean = false) {
    this.navigate(route) {
        launchSingleTop = true
        if (popBackStack){popBackStack()}
    }
}

@Composable
fun BottomNavItem(
    icon: @Composable () -> Unit,
    label: String,
    isSelected : Boolean,
    onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onClick) {
                icon()
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray

            )
        }
    }
}

@Preview
@Composable
fun WeatherCard(){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ){
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)){
            Row(
                modifier = Modifier.align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_wb_sunny_24),
                    contentDescription = "weather_icon",
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Today's Weather",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "19:42 pm",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomStart)
            )
            Text(
                text = "28* C",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
            )
        }
    }
}

fun calculateCurrentOffsetForPage(page: Int, pagerState: PagerState): Float {
    return (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
}
