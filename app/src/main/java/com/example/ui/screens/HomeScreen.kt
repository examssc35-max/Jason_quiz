package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffold
import com.example.ui.theme.LocalGlassColors
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun HomeScreen(
    viewModel: QuizViewModel,
    onNavigateToImport: () -> Unit,
    onNavigateToQuizzes: (filter: String?) -> Unit,
    onNavigateToStats: () -> Unit,
    onStartQuiz: (quizId: String) -> Unit,
    onOpenQuizDetails: (quizId: String) -> Unit
) {
    val allQuizzes by viewModel.allQuizzes.collectAsState()
    val favoriteQuizzes by viewModel.favoriteQuizzes.collectAsState()
    val recentQuizzes by viewModel.recentQuizzes.collectAsState()
    val activeProgressList by viewModel.activeProgressList.collectAsState()
    val glassColors = LocalGlassColors.current

    GlassScaffold {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Top Bar / Greeting matching Screen 2
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hi, Learner 👋",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ready for a new challenge?",
                            color = Color(0xB3FFFFFF),
                            fontSize = 14.sp
                        )
                    }

                    // Avatar profile element
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .border(BorderStroke(2.dp, Color(0x80FFFFFF)), CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.learner_avatar),
                            contentDescription = "User profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Continue Quiz Banner (if any unfinished quiz progress exists!)
            if (activeProgressList.isNotEmpty()) {
                val latestProgress = activeProgressList.first()
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color(0x4010AC84),
                        onClick = { onStartQuiz(latestProgress.quizId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10AC84)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Continue Quiz",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${latestProgress.quizTitle} • Q${latestProgress.currentQuestionIndex + 1}",
                                    color = Color(0xE6FFFFFF),
                                    fontSize = 13.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Resume",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Primary Hero Glass Card: "Import Quiz (JSON)"
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0x3512294E),
                    borderStroke = BorderStroke(1.2.dp, Color(0x60FFFFFF)),
                    onClick = onNavigateToImport
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Glowing Document Box
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF2E86DE))
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF54A0FF), Color(0xFF2E86DE))
                                    )
                                )
                                .border(BorderStroke(1.dp, Color(0x80FFFFFF)), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Import Quiz (JSON)",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap to select a quiz file",
                                color = Color(0xB3FFFFFF),
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x25FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2x2 Quick Access Cards matching Screen 2
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1: My Quizzes & Recent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickStatsCard(
                            title = "My Quizzes",
                            subtitle = "${allQuizzes.size} quizzes",
                            icon = Icons.Default.Folder,
                            iconBgColor = Color(0xFF10AC84),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToQuizzes("All") }
                        )

                        QuickStatsCard(
                            title = "Recent",
                            subtitle = "${recentQuizzes.size} quizzes",
                            icon = Icons.Default.AccessTime,
                            iconBgColor = Color(0xFF5F27CD),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToQuizzes("Recent") }
                        )
                    }

                    // Row 2: Favorites & Statistics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickStatsCard(
                            title = "Favorites",
                            subtitle = "${favoriteQuizzes.size} quizzes",
                            icon = Icons.Default.Favorite,
                            iconBgColor = Color(0xFFFF4757),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToQuizzes("Favorites") }
                        )

                        QuickStatsCard(
                            title = "Statistics",
                            subtitle = "View progress",
                            icon = Icons.Default.BarChart,
                            iconBgColor = Color(0xFF2E86DE),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToStats
                        )
                    }
                }
            }

            // Popular Categories Section matching Screen 2
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular Categories",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See All",
                        color = Color(0xFF54A0FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToQuizzes("All") }
                    )
                }
            }

            // 6 Popular Categories Grid matching Screen 2
            item {
                val categories = listOf(
                    CategoryItem("General Knowledge", Icons.Default.Public, Color(0xFF00CEC9)),
                    CategoryItem("Science", Icons.Default.Science, Color(0xFF10AC84)),
                    CategoryItem("Mathematics", Icons.Default.Calculate, Color(0xFF5F27CD)),
                    CategoryItem("English", Icons.Default.MenuBook, Color(0xFF3867D6)),
                    CategoryItem("ICT", Icons.Default.Computer, Color(0xFF0FB9B1)),
                    CategoryItem("Custom", Icons.Default.AutoAwesome, Color(0xFF778CA3))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (row in categories.chunked(3)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (cat in row) {
                                CategoryTile(
                                    item = cat,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigateToQuizzes("Cat:${cat.name}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun QuickStatsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = Color(0x3510223D),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor.copy(alpha = 0.85f))
                    .border(BorderStroke(1.dp, Color(0x60FFFFFF)), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xB3FFFFFF),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CategoryTile(
    item: CategoryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = Color(0x28122644),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.25f))
                    .border(BorderStroke(1.dp, item.color.copy(alpha = 0.6f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}
