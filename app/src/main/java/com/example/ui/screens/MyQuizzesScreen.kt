package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Quiz
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassEmptyState
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassScaffold
import com.example.ui.components.GlassSearchBar
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun MyQuizzesScreen(
    viewModel: QuizViewModel,
    onStartQuiz: (quizId: String) -> Unit,
    onOpenQuizDetails: (quizId: String) -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val quizzes by viewModel.filteredQuizzes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var showActionSheet by remember { mutableStateOf(false) }

    GlassScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header matching Screen 7: "My Quizzes", Filter button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Quizzes",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )

                GlassIconButton(
                    icon = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    onClick = { /* toggles filter row */ }
                )
            }

            // Search Bar matching Screen 7
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                GlassSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    placeholder = "Search quizzes..."
                )
            }

            // Filter Tabs matching Screen 7: "All", "Favorites", "Recent", + Categories
            val filterTabs = listOf("All", "Favorites", "Recent", "Cat:General Knowledge", "Cat:Science", "Cat:Mathematics")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterTabs) { tab ->
                    val label = tab.removePrefix("Cat:")
                    GlassFilterChip(
                        text = label,
                        isSelected = selectedFilter == tab,
                        onClick = { viewModel.setSelectedFilter(tab) }
                    )
                }
            }

            // Quizzes List matching Screen 7
            if (quizzes.isEmpty()) {
                GlassEmptyState(
                    title = if (searchQuery.isNotEmpty()) "No Quizzes Found" else "No Quizzes Yet",
                    description = if (searchQuery.isNotEmpty()) "Try searching for a different keyword or category."
                    else "Import a JSON quiz or create your own to get started.",
                    buttonText = "Import Quiz (JSON)",
                    onButtonClick = onNavigateToImport,
                    icon = Icons.Default.Public,
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(quizzes, key = { it.id }) { quiz ->
                        QuizItemCard(
                            quiz = quiz,
                            onCardClick = { onOpenQuizDetails(quiz.id) },
                            onPlayClick = { onStartQuiz(quiz.id) },
                            onFavoriteClick = { viewModel.toggleFavorite(quiz) },
                            onDeleteClick = { viewModel.deleteQuiz(quiz.id) },
                            onDuplicateClick = { viewModel.duplicateQuiz(quiz.id) {} }
                        )
                    }
                }
            }
        }

        // Floating Action Button matching Screen 7
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 24.dp, bottom = 90.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(16.dp, CircleShape, spotColor = Color(0xFF2E86DE))
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF54A0FF), Color(0xFF2E86DE))
                        )
                    )
                    .border(BorderStroke(1.5.dp, Color(0x80FFFFFF)), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                        onClick = onNavigateToImport
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Import or Add Quiz",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun QuizItemCard(
    quiz: Quiz,
    onCardClick: () -> Unit,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDuplicateClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val (icon, iconColor) = when {
        quiz.category.contains("Science", ignoreCase = true) -> Icons.Default.Science to Color(0xFF10AC84)
        quiz.category.contains("Math", ignoreCase = true) -> Icons.Default.Calculate to Color(0xFF5F27CD)
        quiz.category.contains("English", ignoreCase = true) -> Icons.Default.MenuBook to Color(0xFF3867D6)
        quiz.category.contains("ICT", ignoreCase = true) || quiz.category.contains("Tech", ignoreCase = true) -> Icons.Default.Computer to Color(0xFF0FB9B1)
        quiz.category.contains("General", ignoreCase = true) -> Icons.Default.Public to Color(0xFF00CEC9)
        else -> Icons.Default.AutoAwesome to Color(0xFFFFA502)
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        backgroundColor = Color(0x30112442),
        borderStroke = BorderStroke(1.dp, Color(0x55FFFFFF)),
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge matching Screen 7
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconColor.copy(alpha = 0.25f))
                    .border(BorderStroke(1.2.dp, iconColor.copy(alpha = 0.7f)), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title and Metadata matching Screen 7
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quiz.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${quiz.questions.size} Questions · ${quiz.difficulty}",
                        color = Color(0xB3FFFFFF),
                        fontSize = 13.sp
                    )

                    if (quiz.attemptsCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x3010AC84))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${quiz.bestScore}%",
                                color = Color(0xFF2ED573),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Favorite Button
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (quiz.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (quiz.isFavorite) Color(0xFFFF4757) else Color(0x99FFFFFF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Play Button (Circle with ▶) matching Screen 7
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF54A0FF), Color(0xFF2E86DE))
                        )
                    )
                    .border(BorderStroke(1.dp, Color(0x80FFFFFF)), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                        onClick = onPlayClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Quiz",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
