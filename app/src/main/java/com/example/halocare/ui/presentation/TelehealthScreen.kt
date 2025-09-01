package com.example.halocare.ui.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.halocare.R
import responsiveSp

@Preview(widthDp = 370, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelehealthScreen() {
    val categories = listOf("General Health", "Mental Health", "Pediatrics & Childcare", "Community Support")
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Telehealth & Community Support") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                categories.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 14.sp.responsiveSp()) }
                    )
                }
            }

            // Coming Soon Effect replaces NewsList
            ComingSoonEffect(category = categories[selectedTab])
        }
    }
}

@Composable
private fun ComingSoonEffect(category: String) {
    val infiniteTransition = rememberInfiniteTransition()

    // Floating animation for the main icon
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Shimmer effect for text
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )

    val shimmerTransition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerBrush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(shimmerTransition, shimmerTransition),
        end = Offset(shimmerTransition + 300f, shimmerTransition + 300f)
    )

    // Pulse animation for background elements
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface
                    ),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative circles
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size((100 + index * 50).dp.responsiveWidth())
                    .scale(pulseScale * (1f - index * 0.1f))
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f - index * 0.01f),
                        CircleShape
                    )
                    .align(Alignment.Center)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp.responsiveWidth())
        ) {
            // Floating main icon
            Icon(
                painter = painterResource(id = R.drawable.baseline_medication_24),
                contentDescription = "Medical Services",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(120.dp.responsiveWidth())
                    .offset(y = floatOffset.dp.responsiveHeight())
                    .shadow(
                        elevation = 8.dp.responsiveHeight(),
                        shape = CircleShape,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
            )

            Spacer(modifier = Modifier.height(40.dp.responsiveHeight()))

            // Animated "Coming Soon" text
            Text(
                text = "Coming Soon",
                style = TextStyle(
                    fontSize = 28.sp.responsiveSp(),
                    fontWeight = FontWeight.Bold,
                    brush = shimmerBrush
                ),
                modifier = Modifier.graphicsLayer(
                    scaleX = 1f + (pulseScale - 1f) * 0.1f,
                    scaleY = 1f + (pulseScale - 1f) * 0.1f
                )
            )

            Spacer(modifier = Modifier.height(16.dp.responsiveHeight()))

            // Category-specific message
            Text(
                text = "We're preparing amazing $category services for you",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp.responsiveSp(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp.responsiveWidth())
            )

            Spacer(modifier = Modifier.height(32.dp.responsiveHeight()))

            // Animated progress indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp.responsiveWidth()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, delayMillis = index * 200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(12.dp.responsiveWidth())
                            .scale(dotScale)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp.responsiveHeight()))

            // Subtle feature preview cards
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 16.dp.responsiveWidth()),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp.responsiveWidth())
            ) {
                Column(
                    modifier = Modifier.padding(20.dp.responsiveWidth()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "What's Coming",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp.responsiveSp(),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp.responsiveHeight()))

                    val features = listOf(
                        "🩺 Virtual Consultations",
                        "💊 Prescription Management",
                        "📊 Health Records Access",
                        "🤝 Community Support Groups"
                    )

                    features.forEach { feature ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp.responsiveHeight()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp.responsiveSp(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun NewsList(category: String) {
    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(5) { index -> // Placeholder count
            NewsCard(
                title = "$category News $index",
                description = "This is a brief description of the news article related to $category.",
                imageUrl = "https://via.placeholder.com/150" // Placeholder image
            )
        }
    }
}

@Composable
fun NewsCard(title: String, description: String, imageUrl: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp.responsiveHeight())
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp.responsiveWidth()))

            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp.responsiveSp())
                Text(description, fontSize = 14.sp.responsiveSp(), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
