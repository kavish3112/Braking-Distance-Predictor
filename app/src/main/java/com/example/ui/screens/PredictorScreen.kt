package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeedUnit
import com.example.ui.components.ConditionSelectorChips
import com.example.ui.components.MetricCardsGrid
import com.example.ui.components.PhysicsModelDialog
import com.example.ui.components.StoppingDistanceTrackView
import com.example.ui.viewmodel.BrakingViewModel
import kotlin.math.roundToInt

@Composable
fun PredictorScreen(
    viewModel: BrakingViewModel,
    onNavigateToCompare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speedKmh by viewModel.speedKmh.collectAsState()
    val speedUnit by viewModel.speedUnit.collectAsState()
    val roadSurface by viewModel.roadSurface.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val tireCondition by viewModel.tireCondition.collectAsState()
    val vehicleLoad by viewModel.vehicleLoad.collectAsState()
    val roadSlope by viewModel.roadSlope.collectAsState()
    val brakeCondition by viewModel.brakeCondition.collectAsState()
    val reactionTimeSec by viewModel.reactionTimeSec.collectAsState()
    val currentPrediction by viewModel.currentPrediction.collectAsState()

    var showPhysicsDialog by remember { mutableStateOf(false) }
    var showAdvancedParams by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    if (showPhysicsDialog) {
        PhysicsModelDialog(onDismiss = { showPhysicsDialog = false })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Hero Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vehicle Braking Predictor",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Physics & ML stopping distance analysis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showPhysicsDialog = true },
                    modifier = Modifier.testTag("button_open_physics_lab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Physics Lab",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Live Animated Stopping Track Visualization
        item {
            StoppingDistanceTrackView(prediction = currentPrediction)
        }

        // Speed Input Card with Slider and Quick Steppers
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("speed_control_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Vehicle Speed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Speed Unit Toggle
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleSpeedUnit() }
                                .testTag("speed_unit_toggle"),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Unit: ${speedUnit.symbol.uppercase()}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Large Speed Display & +/- Stepper buttons
                    val displaySpeed = if (speedUnit == SpeedUnit.KMH) {
                        speedKmh.toInt()
                    } else {
                        (speedKmh / speedUnit.kmhMultiplier).roundToInt()
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.setSpeedKmh((speedKmh - 5.0).coerceAtLeast(15.0)) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .testTag("speed_decrement_button")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease speed")
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$displaySpeed",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = speedUnit.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.setSpeedKmh((speedKmh + 5.0).coerceAtMost(160.0)) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .testTag("speed_increment_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase speed")
                        }
                    }

                    // Interactive Slider (20 to 140 km/h)
                    Slider(
                        value = speedKmh.toFloat(),
                        onValueChange = { viewModel.setSpeedKmh(it.toDouble()) },
                        valueRange = 20f..140f,
                        steps = 23,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("speed_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Quick Speed Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(30, 50, 80, 100, 120).forEach { presetSpeed ->
                            Text(
                                text = "$presetSpeed",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setSpeedKmh(presetSpeed.toDouble()) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (speedKmh.toInt() == presetSpeed) FontWeight.Bold else FontWeight.Normal,
                                color = if (speedKmh.toInt() == presetSpeed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Environmental & Vehicle Conditions Selector Chips
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("conditions_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ConditionSelectorChips(
                        roadSurface = roadSurface,
                        onRoadSurfaceChange = { viewModel.setRoadSurface(it) },
                        weather = weather,
                        onWeatherChange = { viewModel.setWeather(it) },
                        tireCondition = tireCondition,
                        onTireConditionChange = { viewModel.setTireCondition(it) },
                        vehicleLoad = vehicleLoad,
                        onVehicleLoadChange = { viewModel.setVehicleLoad(it) },
                        roadSlope = roadSlope,
                        onRoadSlopeChange = { viewModel.setRoadSlope(it) },
                        brakeCondition = brakeCondition,
                        onBrakeConditionChange = { viewModel.setBrakeCondition(it) }
                    )
                }
            }
        }

        // Driver Reaction Time Customizer Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvancedParams = !showAdvancedParams },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Driver Reaction Time: ${reactionTimeSec}s",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (showAdvancedParams) "Hide" else "Adjust",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(visible = showAdvancedParams) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = "Standard reaction time is 1.5s (0.75s perception + 0.75s foot transfer). Inclement weather adds reaction delay.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = reactionTimeSec.toFloat(),
                                onValueChange = { viewModel.setReactionTime(it.toDouble()) },
                                valueRange = 0.6f..2.5f,
                                modifier = Modifier.testTag("reaction_time_slider")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Alert (0.8s)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { viewModel.setReactionTime(0.8) }
                                )
                                Text(
                                    text = "Average (1.5s)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { viewModel.setReactionTime(1.5) }
                                )
                                Text(
                                    text = "Impaired/Tired (2.2s)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.clickable { viewModel.setReactionTime(2.2) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Secondary Kinematics Telemetry Grid
        item {
            MetricCardsGrid(prediction = currentPrediction)
        }

        // Factor Breakdown Card
        item {
            if (currentPrediction.factorContributions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Distance Increase Factors",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        currentPrediction.factorContributions.forEach { factor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = factor.factorName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = factor.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = if (factor.extraMeters >= 0) "+${String.format("%.1f", factor.extraMeters)}m" else "${String.format("%.1f", factor.extraMeters)}m",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (factor.extraMeters >= 0) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Save Run, Compare Scenario
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        viewModel.saveCurrentPrediction()
                        saveMessage = "Saved to History!"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_save_prediction"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = saveMessage ?: "Save Result")
                }

                Button(
                    onClick = {
                        viewModel.copyCurrentToScenarioA()
                        onNavigateToCompare()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_compare_scenario"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Compare")
                }
            }
        }
    }
}
