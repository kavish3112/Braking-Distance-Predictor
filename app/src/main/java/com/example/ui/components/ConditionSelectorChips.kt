package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrakeCondition
import com.example.data.model.RoadSlope
import com.example.data.model.RoadSurface
import com.example.data.model.TireCondition
import com.example.data.model.VehicleLoad
import com.example.data.model.WeatherCondition

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConditionSelectorChips(
    roadSurface: RoadSurface,
    onRoadSurfaceChange: (RoadSurface) -> Unit,
    weather: WeatherCondition,
    onWeatherChange: (WeatherCondition) -> Unit,
    tireCondition: TireCondition,
    onTireConditionChange: (TireCondition) -> Unit,
    vehicleLoad: VehicleLoad,
    onVehicleLoadChange: (VehicleLoad) -> Unit,
    roadSlope: RoadSlope,
    onRoadSlopeChange: (RoadSlope) -> Unit,
    brakeCondition: BrakeCondition,
    onBrakeConditionChange: (BrakeCondition) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Road Surface (Dry, Wet, Snow, Ice)
        SectionHeader(title = "Road Surface", subtitle = "Affects road adhesion coefficient (µ)")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RoadSurface.entries.forEach { surface ->
                SelectableChip(
                    text = surface.iconLabel,
                    isSelected = surface == roadSurface,
                    accentColor = when (surface) {
                        RoadSurface.DRY -> Color(0xFF10B981)
                        RoadSurface.WET -> Color(0xFF3B82F6)
                        RoadSurface.SNOW -> Color(0xFF38BDF8)
                        RoadSurface.ICE -> Color(0xFFEF4444)
                    },
                    testTag = "chip_road_${surface.name.lowercase()}",
                    onClick = { onRoadSurfaceChange(surface) }
                )
            }
        }

        // 2. Weather Condition (Clear, Rain, Fog)
        SectionHeader(title = "Weather", subtitle = "Affects driver reaction time and visibility")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeatherCondition.entries.forEach { cond ->
                SelectableChip(
                    text = cond.iconLabel,
                    isSelected = cond == weather,
                    accentColor = Color(0xFFF59E0B),
                    testTag = "chip_weather_${cond.name.lowercase()}",
                    onClick = { onWeatherChange(cond) }
                )
            }
        }

        // 3. Tire Condition (Good, Worn)
        SectionHeader(title = "Tire Condition", subtitle = "Tread depth & water expulsion capability")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TireCondition.entries.forEach { tire ->
                SelectableChip(
                    text = tire.iconLabel,
                    isSelected = tire == tireCondition,
                    accentColor = if (tire == TireCondition.GOOD) Color(0xFF10B981) else Color(0xFFF97316),
                    modifier = Modifier.weight(1f),
                    testTag = "chip_tire_${tire.name.lowercase()}",
                    onClick = { onTireConditionChange(tire) }
                )
            }
        }

        // 4. Vehicle Load (Light, Medium, Heavy)
        SectionHeader(title = "Vehicle Load / Weight", subtitle = "Gross vehicle mass and forward inertia")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VehicleLoad.entries.forEach { load ->
                SelectableChip(
                    text = load.iconLabel,
                    isSelected = load == vehicleLoad,
                    accentColor = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f),
                    testTag = "chip_load_${load.name.lowercase()}",
                    onClick = { onVehicleLoadChange(load) }
                )
            }
        }

        // 5. Road Slope (Uphill, Flat, Downhill)
        SectionHeader(title = "Road Slope / Grade", subtitle = "Gravitational assist or drag on deceleration")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RoadSlope.entries.forEach { slope ->
                SelectableChip(
                    text = slope.iconLabel,
                    isSelected = slope == roadSlope,
                    accentColor = when (slope) {
                        RoadSlope.UPHILL -> Color(0xFF10B981)
                        RoadSlope.FLAT -> Color(0xFF38BDF8)
                        RoadSlope.DOWNHILL -> Color(0xFFEF4444)
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "chip_slope_${slope.name.lowercase()}",
                    onClick = { onRoadSlopeChange(slope) }
                )
            }
        }

        // 6. Brake Condition
        SectionHeader(title = "Brake System", subtitle = "Thermal brake fade & pad wear")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BrakeCondition.entries.forEach { brake ->
                SelectableChip(
                    text = brake.iconLabel,
                    isSelected = brake == brakeCondition,
                    accentColor = if (brake == BrakeCondition.GOOD) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    testTag = "chip_brake_${brake.name.lowercase()}",
                    onClick = { onBrakeConditionChange(brake) }
                )
            }
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        label = "chipBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        label = "chipBorder"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
