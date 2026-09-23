package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalGlassColors
import kotlin.math.sin
import kotlin.random.Random

/**
 * Fullscreen glass scaffold that renders the misty landscape wallpaper
 * with frosted atmospheric overlay adapting to light/dark themes.
 */
@Composable
fun GlassScaffold(
    modifier: Modifier = Modifier,
    blurOverlayAlpha: Float = 0.45f,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = LocalGlassColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (glassColors.isDark) Color(0xFF0A111F) else Color(0xFF1E2D4A))
    ) {
        // Nature backdrop image
        Image(
            painter = painterResource(id = R.drawable.bg_nature_glass),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Frosted atmospheric overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (glassColors.isDark) {
                        Color(0x8A070E1B)
                    } else {
                        Color(0x400C1B33)
                    }
                )
        )

        // Subtle gradient sheen from top to bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x20FFFFFF),
                            Color(0x00FFFFFF),
                            Color(0x40000000)
                        )
                    )
                )
        )

        content()
    }
}

/**
 * Frosted translucent glass card with delicate border and soft depth.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color? = null,
    borderStroke: BorderStroke? = null,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = LocalGlassColors.current
    val bg = backgroundColor ?: glassColors.cardBackground
    val border = borderStroke ?: BorderStroke(1.dp, glassColors.cardBorderGradient)

    val boxModifier = modifier
        .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape) else Modifier)
        .clip(shape)
        .background(bg)
        .border(border, shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                    onClick = onClick
                )
            } else Modifier
        )

    Box(modifier = boxModifier, content = content)
}

/**
 * Primary action pill button with vibrant gradient and glow.
 */
@Composable
fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val glassColors = LocalGlassColors.current
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(12.dp, shape, spotColor = glassColors.primaryAccent, ambientColor = glassColors.primaryAccent.copy(alpha = 0.4f))
            .clip(shape)
            .background(
                if (enabled) glassColors.primaryGradient
                else Brush.horizontalGradient(listOf(Color(0x55555555), Color(0x33555555)))
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (enabled) Color(0x80FFFFFF) else Color(0x20FFFFFF)
                ),
                shape
            )
            .clickable(
                enabled = enabled && !isLoading,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

/**
 * Secondary translucent glass pill button.
 */
@Composable
fun SecondaryGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val glassColors = LocalGlassColors.current
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(Color(0x30FFFFFF))
            .border(BorderStroke(1.dp, Color(0x60FFFFFF)), shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Circular frosted glass icon button (for Back, Bookmark, Close, Help, etc.).
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    isActive: Boolean = false,
    activeTint: Color? = null
) {
    val glassColors = LocalGlassColors.current
    val shape = CircleShape
    val activeColor = activeTint ?: glassColors.primaryAccent

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                if (isActive) activeColor.copy(alpha = 0.35f)
                else Color(0x35FFFFFF)
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (isActive) activeColor.copy(alpha = 0.8f)
                    else Color(0x55FFFFFF)
                ),
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.3f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) activeColor else tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Question option button matching Screen 4 of the reference image.
 * Shows option letter badge (A, B, C, D) + Option text.
 * When selected, lights up with glowing electric blue glass and white border.
 */
@Composable
fun GlassOptionButton(
    letter: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCorrect: Boolean? = null, // Used in review/practice mode
    enabled: Boolean = true
) {
    val glassColors = LocalGlassColors.current
    val shape = RoundedCornerShape(20.dp)

    val (bg, border) = when {
        isCorrect == true -> glassColors.optionCorrectBg to BorderStroke(1.5.dp, Color(0xFF10AC84))
        isCorrect == false -> glassColors.optionWrongBg to BorderStroke(1.5.dp, Color(0xFFFF4757))
        isSelected -> glassColors.optionSelectedBg to BorderStroke(1.5.dp, Color(0xFF70A1FF))
        else -> glassColors.optionDefaultBg to BorderStroke(1.dp, glassColors.optionDefaultBorder)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.shadow(8.dp, shape, spotColor = Color(0xFF2E86DE))
                else Modifier
            )
            .clip(shape)
            .background(bg)
            .border(border, shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Letter Badge pill (A, B, C, D)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCorrect == true -> Color(0xFF10AC84)
                            isCorrect == false -> Color(0xFFFF4757)
                            isSelected -> Color.White
                            else -> Color(0x40FFFFFF)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    color = if (isSelected && isCorrect == null) glassColors.primaryAccent else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Option text
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Translucent Search Bar for filtering quizzes.
 */
@Composable
fun GlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search quizzes...",
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(shape)
            .background(Color(0x30FFFFFF))
            .border(BorderStroke(1.dp, Color(0x4DFFFFFF)), shape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xB3FFFFFF),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0x8AFFFFFF),
                        fontSize = 15.sp
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = Color(0xB3FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Filter Chip for categories and tabs.
 */
@Composable
fun GlassFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val glassColors = LocalGlassColors.current
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (isSelected) Color.White
                else Color(0x2BFFFFFF)
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (isSelected) Color.White else Color(0x45FFFFFF)
                ),
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF1E2D4A) else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (isSelected) Color(0xFF1E2D4A) else Color.White,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Beautiful empty state with frosted card, icon, and actionable button.
 */
@Composable
fun GlassEmptyState(
    title: String,
    description: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0x30FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = Color(0xB3FFFFFF),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (buttonText != null && onButtonClick != null) {
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryGradientButton(
                    text = buttonText,
                    onClick = onButtonClick,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }
}

/**
 * Festive confetti particle canvas for Quiz Result celebration.
 */
@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier) {
    val confettiColors = listOf(
        Color(0xFFFF4757),
        Color(0xFF2ED573),
        Color(0xFF1E90FF),
        Color(0xFFFFA502),
        Color(0xFFFF6B81),
        Color(0xFF70A1FF),
        Color(0xFF2ED573)
    )

    val particles = remember {
        List(45) {
            ConfettiParticle(
                xNorm = Random.nextFloat(),
                initialYNorm = -Random.nextFloat() * 0.8f,
                speed = 0.3f + Random.nextFloat() * 0.5f,
                size = 6.dp + (Random.nextFloat() * 8).dp,
                color = confettiColors.random(),
                rotation = Random.nextFloat() * 360f
            )
        }
    }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (p in particles) {
            val curY = ((p.initialYNorm + animProgress.value * p.speed) % 1.2f) * h
            val curX = p.xNorm * w + (sin(animProgress.value * 6.28f + p.xNorm * 10f) * 20f)
            val pSizePx = p.size.toPx()

            drawRoundRect(
                color = p.color,
                topLeft = androidx.compose.ui.geometry.Offset(curX, curY),
                size = androidx.compose.ui.geometry.Size(pSizePx, pSizePx * 0.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
        }
    }
}

private data class ConfettiParticle(
    val xNorm: Float,
    val initialYNorm: Float,
    val speed: Float,
    val size: Dp,
    val color: Color,
    val rotation: Float
)
