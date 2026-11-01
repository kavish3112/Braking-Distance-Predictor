package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrakingPrediction
import com.example.data.model.RoadSurface
import com.example.data.model.TireCondition
import com.example.ml.BrakingPredictorEngine
import com.example.ui.viewmodel.BrakingViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ScenarioCompareScreen(
    viewModel: BrakingViewModel,
    modifier: Modifier = Modifier
) {
    val scenarioA by viewModel.compareA.collectAsState()
    val scenarioB by viewModel.compareB.collectAsState()
    val obstacleDistanceM by viewModel.obstacleDistanceM.collectAsState()

    val collisionSpeedA = BrakingPredictorEngine.calculateCollisionSpeedKmh(
        initialSpeedKmh = scenarioA.speedKmh,
        prediction = scenarioA,
        obstacleDistanceM = obstacleDistanceM
    )

    val collisionSpeedB = BrakingPredictorEngine.calculateCollisionSpeedKmh(
        initialSpeedKmh = scenarioB.speedKmh,
        prediction = scenarioB,
        obstacleDistanceM = obstacleDistanceM
    )

    val distanceDeltaM = scenarioB.totalStoppingDistanceM - scenarioA.totalStoppingDistanceM

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Scenario Safety Comparison",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Side-by-side condition & impact analysis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Side-by-Side Cards (Scenario A vs Scenario B)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Scenario A
                ScenarioCard(
                    scenarioLabel = "Scenario A (Base)",
                    prediction = scenarioA,
                    badgeColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onApplyCurrent = { viewModel.copyCurrentToScenarioA() }
                )

                // Scenario B
                ScenarioCard(
                    scenarioLabel = "Scenario B (Hazard)",
                    prediction = scenarioB,
                    badgeColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onApplyCurrent = { viewModel.copyCurrentToScenarioB() }
                )
            }
        }

        // Comparison Difference Summary Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (distanceDeltaM > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (distanceDeltaM > 0) "⚠️ Scenario B Requires +${distanceDeltaM.roundToInt()}m More Distance"
                        else "Scenario B Stops ${abs(distanceDeltaM).roundToInt()}m Sooner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (distanceDeltaM > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Braking difference is ${abs(scenarioB.brakingDistanceM - scenarioA.brakingDistanceM).roundToInt()}m (${abs(distanceDeltaM / 4.5).roundToInt()} car lengths equivalent).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Virtual Crash & Obstacle Hazard Simulator
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("crash_hazard_simulator_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Emergency Obstacle Simulator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${obstacleDistanceM.toInt()}m ahead",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Simulate an unexpected stationary hazard ahead. See if either scenario stops in time or crashes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = obstacleDistanceM.toFloat(),
                        onValueChange = { viewModel.setObstacleDistance(it.toDouble()) },
                        valueRange = 15f..120f,
                        steps = 20,
                        modifier = Modifier.testTag("obstacle_distance_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Crash Outcomes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ObstacleOutcomeCard(
                            label = "Car A",
                            stoppingDistance = scenarioA.totalStoppingDistanceM,
                            obstacleDist = obstacleDistanceM,
                            impactSpeedKmh = collisionSpeedA,
                            modifier = Modifier.weight(1f)
                        )

                        ObstacleOutcomeCard(
                            label = "Car B",
                            stoppingDistance = scenarioB.totalStoppingDistanceM,
                            obstacleDist = obstacleDistanceM,
                            impactSpeedKmh = collisionSpeedB,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Detailed Parameter Comparison Table
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Parameter Specs Comparison",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ComparisonRow("Initial Speed", "${scenarioA.speedKmh.toInt()} km/h", "${scenarioB.speedKmh.toInt()} km/h")
                    ComparisonRow("Road Surface", scenarioA.roadSurface.displayName, scenarioB.roadSurface.displayName)
                    ComparisonRow("Weather", scenarioA.weather.displayName, scenarioB.weather.displayName)
                    ComparisonRow("Tyre Tread", scenarioA.tireCondition.displayName, scenarioB.tireCondition.displayName)
                    ComparisonRow("Vehicle Weight", scenarioA.vehicleLoad.displayName, scenarioB.vehicleLoad.displayName)
                    ComparisonRow("Road Incline", scenarioA.roadSlope.displayName, scenarioB.roadSlope.displayName)
                    ComparisonRow("Friction (µ)", String.format("%.2f", scenarioA.effectiveFriction), String.format("%.2f", scenarioB.effectiveFriction))
                    ComparisonRow("Deceleration", "${scenarioA.decelerationMs2} m/s²", "${scenarioB.decelerationMs2} m/s²")
                    ComparisonRow("Stop Time", "${scenarioA.stoppingTimeSeconds} s", "${scenarioB.stoppingTimeSeconds} s")
                }
            }
        }
    }
}

@Composable
private fun ScenarioCard(
    scenarioLabel: String,
    prediction: BrakingPrediction,
    badgeColor: Color,
    onApplyCurrent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = scenarioLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${prediction.totalStoppingDistanceM.roundToInt()}m",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Braking: ${prediction.brakingDistanceM}m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "• ${prediction.speedKmh.toInt()} km/h", fontSize = 11.sp)
                    Text(text = "• ${prediction.roadSurface.displayName}", fontSize = 11.sp)
                    Text(text = "• ${prediction.tireCondition.displayName}", fontSize = 11.sp)
                    Text(text = "• ${prediction.weather.displayName}", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onApplyCurrent,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = badgeColor.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "Copy Current",
                    fontSize = 11.sp,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ObstacleOutcomeCard(
    label: String,
    stoppingDistance: Double,
    obstacleDist: Double,
    impactSpeedKmh: Double,
    modifier: Modifier = Modifier
) {
    val stopsInTime = stoppingDistance <= obstacleDist
    val outcomeColor = if (stopsInTime) Color(0xFF10B981) else Color(0xFFEF4444)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = outcomeColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, outcomeColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = outcomeColor
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (stopsInTime) {
                val marginM = obstacleDist - stoppingDistance
                Text(
                    text = "STOPS SAFELY",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = outcomeColor
                )
                Text(
                    text = "${marginM.roundToInt()}m safety buffer",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = "CRASHES!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = outcomeColor
                )
                Text(
                    text = "Hits @ ${impactSpeedKmh.roundToInt()} km/h!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = outcomeColor
                )
            }
        }
    }
}

@Composable
private fun ComparisonRow(label: String, valA: String, valB: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1.2f)
            )
            Text(
                text = valA,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valB,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    }
}
