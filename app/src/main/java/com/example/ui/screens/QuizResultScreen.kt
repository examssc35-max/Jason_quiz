package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuizResult
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffold
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.SecondaryGlassButton

@Composable
fun QuizResultScreen(
    result: QuizResult,
    onReviewAnswers: () -> Unit,
    onTryAgain: () -> Unit,
    onBackHome: () -> Unit
) {
    val animatedPercent = remember { Animatable(0f) }

    LaunchedEffect(result) {
        animatedPercent.animateTo(
            targetValue = result.percentage.toFloat(),
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    GlassScaffold {
        // Confetti effect if good score!
        if (result.percentage >= 60) {
            ConfettiOverlay()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Title matching Screen 5: "Great Job!"
            val titleText = when {
                result.percentage >= 90 -> "Outstanding!"
                result.percentage >= 70 -> "Great Job!"
                result.percentage >= 50 -> "Well Done!"
                else -> "Keep Practicing!"
            }

            Text(
                text = titleText,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Glowing Radial Circular Progress Ring matching Screen 5
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(190.dp)) {
                    val strokeWidth = 14.dp.toPx()

                    // Background track circle
                    drawArc(
                        color = Color(0x35FFFFFF),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Glowing progress arc
                    val sweep = (animatedPercent.value / 100f) * 360f
                    if (sweep > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFF00CEC9),
                                    Color(0xFF10AC84),
                                    Color(0xFF2ED573),
                                    Color(0xFF00CEC9)
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Inner Stats: "8 / 10" and "80%"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${result.correctCount} / ${result.totalQuestions}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result.percentage}%",
                        color = Color(0xD9FFFFFF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 3-Column Glass Stats Card: Correct | Wrong | Skipped matching Screen 5
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color(0x30112442),
                borderStroke = BorderStroke(1.2.dp, Color(0x55FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatColumn(
                        count = "${result.correctCount}",
                        label = "Correct",
                        countColor = Color(0xFF2ED573)
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color(0x30FFFFFF))
                    )

                    StatColumn(
                        count = "${result.wrongCount}",
                        label = "Wrong",
                        countColor = Color(0xFFFF4757)
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color(0x30FFFFFF))
                    )

                    StatColumn(
                        count = "${result.skippedCount}",
                        label = "Skipped",
                        countColor = Color(0xB3FFFFFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Elegant Quote Card matching Screen 5
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0x250C1B33),
                borderStroke = BorderStroke(1.dp, Color(0x40FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "“Every question makes you smarter!”",
                        color = Color(0xE6FFFFFF),
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Action: "🔍 Review Answers" matching Screen 5
            PrimaryGradientButton(
                text = "Review Answers",
                icon = Icons.Default.Search,
                onClick = onReviewAnswers,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Secondary Actions Row: "Try Again" & "Back Home"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryGlassButton(
                    text = "Try Again",
                    icon = Icons.Default.Refresh,
                    onClick = onTryAgain,
                    modifier = Modifier.weight(1f)
                )

                SecondaryGlassButton(
                    text = "Back Home",
                    icon = Icons.Default.Home,
                    onClick = onBackHome,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    count: String,
    label: String,
    countColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = countColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color(0xB3FFFFFF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
