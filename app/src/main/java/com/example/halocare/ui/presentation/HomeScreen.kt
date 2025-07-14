package com.example.halocare.ui.presentation

import androidx.collection.emptyLongSet
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.halocare.R
import com.example.halocare.network.models.WeatherResponse
import com.example.halocare.network.models.WeatherResponseHourly
import com.example.halocare.ui.utils.FabCutoutShape
import com.example.halocare.viewmodel.AuthViewModel
import com.example.halocare.viewmodel.LoadingState
import com.example.halocare.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


//@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick : () -> Unit = {},
    onCategoryClick : () -> Unit ={},
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    scrollState: ScrollState
) {
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.inversePrimary
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
    val loggedUser by authViewModel.haloCareUser.collectAsState()
    val weatherData by mainViewModel.weatherData.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val hourlyWeatherData by mainViewModel.hourlyWeatherData.collectAsState()
    val hourlyWeatherLoadingState by mainViewModel.hourlyWeatherLoadingState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, ${loggedUser.name}") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inversePrimary,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = {
                        onProfileClick()
                    }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                },
                modifier = Modifier.shadow(elevation = 10.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer
    ) { paddingValues ->

        val pagerState = rememberPagerState {images.size }
        val coroutineScope = rememberCoroutineScope()
        var lastUserInteraction by remember { mutableStateOf(System.currentTimeMillis()) }

        LaunchedEffect(Unit) {
            statusBarController.updateStatusBar(
                color = statusBarColor,
                darkIcons = true
            )
            while (true) {
                delay(3000)
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
            Column(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .verticalScroll(scrollState)
            ) {
                var isDaytimeColor by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                        //     beyondViewportPageCount = 1,
                        pageSpacing = 15.dp
                    ) { page ->
                        UserDashboardCard(
                            imageRes = images[page],
                            modifier = Modifier
                                .graphicsLayer {
                                    val pageOffset =
                                        calculateCurrentOffsetForPage(page, pagerState).absoluteValue
                                    scaleX = 1f - (pageOffset * 0.1f) // Slight zoom-out effect
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
                Spacer(modifier = Modifier.height(10.dp))
                CentreCircleDivider(
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .height(310.dp)
                            .padding(1.dp),
                        contentPadding = PaddingValues(7.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)

                    ) {
                        items(features.size) { index ->
                            Card(
                                modifier = Modifier
                                    //.padding(8.dp)
                                    .width(150.dp)
                                    .height(145.dp)
                                    .clickable {
                                        onCategoryClick()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
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
                Spacer(modifier = Modifier.height(3.dp))
                CentreCircleDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 7.dp, end = 7.dp))
                {
                    if (weatherData != null){
                        DashboardWeatherCard(
                            weatherData = weatherData!!,
                            onClick = {
                                mainViewModel.getHourlyWeather("Lagos")
                                showBottomSheet = true
                                isDaytimeColor = it
                            }
                        )
                    }else{
                        WeatherCard()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.height(350.dp))
                if (showBottomSheet){
                    HourlyWeatherBottomSheet(
                        hourlyWeatherList = hourlyWeatherData?.forecast?.days?.get(0)?.hourly,
                        loadingState = hourlyWeatherLoadingState,
                        onDismiss = {showBottomSheet = false},
                        isDayTime = isDaytimeColor
                    )
                }
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
            .height(250.dp),
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
fun CentreCircleDivider(
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    lineThickness: Dp = 2.dp,
    circleRadius: Dp = 3.dp,
    circleColor: Color = MaterialTheme.colorScheme.primary
){
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left line
        Divider(
            color = lineColor,
            thickness = lineThickness,
            modifier = Modifier
                .weight(1f)
        )

        // Circle spacer
        Spacer(modifier = Modifier.width(8.dp))

        // Circle in center
        Box(
            modifier = Modifier
                .size(circleRadius * 2)
                .background(circleColor, CircleShape)
        )

        // Circle spacer
        Spacer(modifier = Modifier.width(8.dp))

        // Right line
        Divider(
            color = lineColor,
            thickness = lineThickness,
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
fun HaloCareBottomBarCurved(
    navController: NavController,
    isVisible: Boolean,
    onFabClick: () -> Unit
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
                .height(160.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val outlineColor = MaterialTheme.colorScheme.inversePrimary
            val borderColor = MaterialTheme.colorScheme.secondary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .graphicsLayer {
                        shadowElevation = 20f
                        shape = FabCutoutShape()
                        clip = true
                    }
                    .background(MaterialTheme.colorScheme.inversePrimary)
                    .border(2.dp, Color.Black.copy(alpha = 0.1f), FabCutoutShape())
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = {
                        HaloCareHomeIcon(
                            size = 30.dp,
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
                            modifier = Modifier.size(30.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                        )
                    },
                    label = "Consults",
                    isSelected = currentRoute == ConsultsScreen.route,
                    onClick = {
                        navController.navigateSingleTopTo(ConsultsScreen.route)
                    }
                )
                Spacer(Modifier.width(60.dp)) // Space for FAB
                BottomNavItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_monitor_heart_24),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
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
                            modifier = Modifier.size(30.dp),
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
                onClick = { onFabClick() },
                containerColor = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(60.dp)
                    .offset(y = (30).dp) // Lifts the FAB above the cutout
                    .shadow(elevation = 2.dp, shape = CircleShape, clip = false)

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
            .width(65.dp)
            .height(90.dp)
            .padding(1.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onClick) {
                icon()
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
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
            .padding(10.dp)
            ){
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

@Composable
fun HomeWeatherCard(
    weatherData: WeatherResponse,
    onClick: (Boolean) -> Unit
) {
    val current = weatherData.current
    val isDaytime = current.isDay

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(isDaytime) }
           ,
        colors = CardDefaults.cardColors(
            containerColor = if (isDaytime)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.inversePrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Location and Time Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = weatherData.location.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDaytime)
                            MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${weatherData.location.country} | ${weatherData.location.localtime}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDaytime)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }

                // Day/Night Icon
                Icon(
                    painter = if (isDaytime) painterResource(id = R.drawable.baseline_wb_sunny_24)
                    else painterResource(id = R.drawable.baseline_nights_stay_24),
                    contentDescription = if (isDaytime) "Daytime" else "Nighttime",
                    tint = if (isDaytime) Color.Yellow else Color.Blue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weather Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Temperature
                WeatherDetailItem(
                    icon = painterResource(id = R.drawable.baseline_thermostat_24),
                    primaryText = "${current.tempCelsius}°C",
                    secondaryText = "Feels like ${current.feelsLikeCelsius}°C",
                    isDaytime = isDaytime
                )

                // Wind
                WeatherDetailItem(
                    icon = painterResource(id = R.drawable.baseline_air_24),
                    primaryText = "${current.windSpeed} km/h",
                    secondaryText = current.windDirection,
                    isDaytime = isDaytime
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cloud Coverage
                WeatherDetailItem(
                    icon = painterResource(id = R.drawable.baseline_cloud_24),
                    primaryText = "${current.cloudCoverage}%",
                    secondaryText = "Cloud Cover",
                    isDaytime = isDaytime
                )

                // Precipitation
                WeatherDetailItem(
                    icon = painterResource(id = R.drawable.baseline_water_drop_24),
                    primaryText = "${current.precipitationMm} mm",
                    secondaryText = "Precipitation",
                    isDaytime = isDaytime
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weather Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model =current.condition.icon),
                    contentDescription = current.condition.text,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = current.condition.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDaytime)
                        MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
private fun WeatherDetailItem(
    icon: Painter,
    primaryText: String,
    secondaryText: String,
    isDaytime: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = if (isDaytime) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.errorContainer
        )

        Column {
            Text(
                text = primaryText,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDaytime)
                    MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.secondary
            )
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDaytime)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun DashboardWeatherCard(
    weatherData: WeatherResponse,
    onClick: (Boolean) -> Unit
) {
    val current = weatherData.current
    val isDaytime = current.isDay

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable { onClick(isDaytime) },
        colors = CardDefaults.cardColors(
            containerColor = if (isDaytime)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Row: Location + Time + Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = weatherData.location.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = weatherData.location.localtime,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Icon(
                    painter = painterResource(
                        id = if (isDaytime)
                            R.drawable.baseline_wb_sunny_24
                        else R.drawable.baseline_nights_stay_24
                    ),
                    contentDescription = null,
                    tint = if (isDaytime) Color.Yellow else Color.Cyan,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Temperature (centered)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${current.tempCelsius}°C",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(current.condition.icon),
                    contentDescription = current.condition.text,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = current.condition.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Weather Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherMetric(icon = R.drawable.baseline_air_24, value = "${current.windSpeed} km/h", label = "Wind")
                WeatherMetric(icon = R.drawable.baseline_cloud_24, value = "${current.cloudCoverage}%", label = "Clouds")
                WeatherMetric(icon = R.drawable.baseline_water_drop_24, value = "${current.precipitationMm} mm", label = "Rain")
            }
        }
    }
}

@Composable
fun WeatherMetric(icon: Int, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourlyWeatherBottomSheet(
    hourlyWeatherList: List<WeatherResponseHourly.HourlyForecast>?,
    modifier: Modifier = Modifier,
    loadingState: LoadingState,
    onDismiss: () -> Unit,
    isDayTime: Boolean
) {
    val isLoading = loadingState == LoadingState.LOADING

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .padding(16.dp)
                .padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )

        ) {
            Text(
                text = "Hourly Forecast",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                //contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(if (isLoading) 5 else hourlyWeatherList?.size ?: 0) { hourlyData ->
                    VerticalWeatherCard(
                        hourlyData = if (isLoading) null else hourlyWeatherList?.get(hourlyData),
                        isDaytime = isDayTime
                    )
                }
            }
        }
    }
}

@Composable
fun VerticalWeatherCard(
    hourlyData: WeatherResponseHourly.HourlyForecast?,
    modifier: Modifier = Modifier,
    isDaytime: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDaytime) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.inversePrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp, vertical = 3.dp)
    ) {
        if (hourlyData == null) {
            VerticalShimmerWeatherCard(isDaytime)
        } else {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Time Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_schedule_24),
                        contentDescription = "Time",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = hourlyData.time.substring(11, 16),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weather Details Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Weather Icon
                    AsyncImage(
                        model = hourlyData.condition.iconUrl,
                        contentDescription = hourlyData.condition.text,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Temperature Column
                    Column {
                        Text(
                            text = "${hourlyData.tempCelsius}°C",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${hourlyData.tempFahrenheit}°F",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Stats Column
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        WeatherStatItem(
                            icon = painterResource(id = R.drawable.baseline_air_24),
                            value = "${hourlyData.windSpeed} kph",
                            label = hourlyData.windDirection
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        WeatherStatItem(
                            icon = painterResource(id = R.drawable.baseline_water_drop_24),
                            value = "${hourlyData.chanceOfRain}%",
                            label = "Chance",
                            tint = if (hourlyData.chanceOfRain > 50) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherStatItem(
    icon: Painter,
    value: String,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = tint.copy(alpha = 0.7f)
            )
        }
    }
}
//@Preview
@Composable
private fun VerticalShimmerWeatherCard(
    isDaytime: Boolean
) {
    val colorList = listOf(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
    val shimmerColors = remember {
        colorList
    }

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 500f, translateAnim + 500f)
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDaytime)MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.inversePrimary
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(brush)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column {
                    repeat(2) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(brush)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(brush)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(30.dp)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(brush)
                                )
                            }
                        }
                        if (it == 0) Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureGridPopup(
    features: List<Feature> = featureList,
    onFeatureClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
        Box {
            // Pointer triangle
            PointerTriangle(
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = 180f
                        alpha = 0.5f
                    }
                    .offset(y = (-1).dp) // Slight overlap
                    .align(Alignment.BottomCenter)
            )
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inversePrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(features) { feature ->
                            FeatureGridItem(feature, onFeatureClick)
                        }
                    }
                }
            }

        }
}

@Composable
fun FeatureGridItem(
    feature: Feature,
    onClick: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick(feature.route) }
            .padding(8.dp)

    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    clip = false
                )
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.name,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = feature.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
@Composable
fun PointerTriangle(modifier: Modifier = Modifier) {
    val triangleColor = MaterialTheme.colorScheme.surface
    Canvas(modifier = modifier.size(32.dp)) {
        val path = Path().apply {
            moveTo(size.width / 2, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = path,
            color = triangleColor,
            style = Fill
        )
    }
}

// Sample feature list
val featureList = listOf(
    Feature("Health", Icons.Filled.Favorite),
    Feature("Weather", Icons.Filled.Warning),
    Feature("News", Icons.Filled.List),
    Feature("Schedule", Icons.Filled.DateRange),
    Feature("Community", Icons.Filled.Place),
    Feature("Pediatrics", Icons.Filled.Person),
    Feature("Mood", Icons.Filled.ThumbUp),
    Feature("Daily Habits", Icons.Filled.CheckCircle),
    Feature("Settings", Icons.Filled.Settings)
)

data class Feature(val name: String, val icon: ImageVector, val route: String = AppointmentsScreen.route)

// ---- Shimmer Effect Helper ---- //
@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    this.background(
        color = Color.Gray.copy(alpha = alpha),
        shape = RoundedCornerShape(4.dp)
    )
}