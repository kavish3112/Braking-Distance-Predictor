package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrakingPrediction
import com.example.data.model.RoadSurface
import com.example.data.model.SafetyLevel
import kotlin.math.max

@Composable
fun StoppingDistanceTrackView(
    prediction: BrakingPrediction,
    modifier: Modifier = Modifier
) {
    // Smooth animated fractions for visualization
    val animatedTotalDist by animateFloatAsState(
        targetValue = prediction.totalStoppingDistanceM.toFloat(),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "totalDistance"
    )

    val animatedBrakingDist by animateFloatAsState(
        targetValue = prediction.brakingDistanceM.toFloat(),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "brakingDistance"
    )

    val animatedReactionDist by animateFloatAsState(
        targetValue = prediction.reactionDistanceM.toFloat(),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "reactionDistance"
    )

    val surfaceColor = when (prediction.roadSurface) {
        RoadSurface.DRY -> Color(0xFF334155) // Slate tarmac
        RoadSurface.WET -> Color(0xFF1E3A8A) // Deep wet navy
        RoadSurface.SNOW -> Color(0xFF64748B) // Frosty grey
        RoadSurface.ICE -> Color(0xFF0E7490) // Glacial cyan
    }

    val reactionColor = Color(0xFFF59E0B) // Amber
    val brakingColor = Color(0xFFEF4444) // Bright Brake Red

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stopping_distance_track"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Car",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real-Time Stopping Track",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Safety badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(prediction.safetyLevel.hexColor).copy(alpha = 0.2f))
                        .border(1.dp, Color(prediction.safetyLevel.hexColor), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = prediction.safetyLevel.label,
                        color = Color(prediction.safetyLevel.hexColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Stopping Track Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw Road Surface Background
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                surfaceColor.copy(alpha = 0.85f),
                                surfaceColor
                            )
                        ),
                        size = size,
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )

                    // 2. Draw Road center lane dash lines
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 16f), 0f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.22f),
                        start = Offset(0f, h * 0.45f),
                        end = Offset(w, h * 0.45f),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dashPathEffect
                    )

                    // 3. Max distance scaling for visual normalization
                    // Max track scale is at least 150m or current total + 10%
                    val maxDisplayM = max(130f, animatedTotalDist * 1.15f)
                    val reactionWidth = (animatedReactionDist / maxDisplayM) * w
                    val brakingWidth = (animatedBrakingDist / maxDisplayM) * w
                    val totalWidth = reactionWidth + brakingWidth

                    val trackY = h * 0.62f
                    val trackHeight = 18.dp.toPx()

                    // Draw Reaction Phase Bar
                    drawRoundRect(
                        color = reactionColor,
                        topLeft = Offset(8.dp.toPx(), trackY),
                        size = Size(max(8f, reactionWidth), trackHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw Braking Phase Bar (Skid friction segment)
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            listOf(brakingColor.copy(alpha = 0.85f), brakingColor)
                        ),
                        topLeft = Offset(8.dp.toPx() + reactionWidth, trackY),
                        size = Size(max(8f, brakingWidth), trackHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw Stop Finish Marker Flag / Line
                    val finishX = (8.dp.toPx() + totalWidth).coerceAtMost(w - 14.dp.toPx())
                    drawLine(
                        color = Color.White,
                        start = Offset(finishX, 10.dp.toPx()),
                        end = Offset(finishX, h - 10.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )

                    // Stop line circle marker
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(finishX, trackY + trackHeight / 2)
                    )
                }

                // Car indicator badge positioning
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "START @ ${prediction.speedKmh.toInt()} km/h",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two-part Distance breakdown legend & metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Perception-Reaction phase
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(reactionColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Reaction (${prediction.reactionTimeSeconds}s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${prediction.reactionDistanceM} m",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = "+",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Pure braking phase
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(brakingColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Braking Friction",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${prediction.brakingDistanceM} m",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = "=",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Total Distance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Stopping",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${prediction.totalStoppingDistanceM} m",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-life Equivalency Bar
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🚗 Approx ${prediction.carLengths} car lengths (${(prediction.totalStoppingDistanceM * 3.28084).toInt()} ft)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (prediction.extraDistancePercentOverBaseline > 5.0) {
                        Text(
                            text = "+${prediction.extraDistancePercentOverBaseline.toInt()}% vs Ideal",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(prediction.safetyLevel.hexColor)
                        )
                    } else {
                        Text(
                            text = "Near Optimal",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}
