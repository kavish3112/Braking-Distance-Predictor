package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.data.model.RoadSurface
import com.example.ml.CurvePoint
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun BrakingCurveCanvas(
    currentPoints: List<CurvePoint>,
    currentSpeedKmh: Double,
    multiSurfaceMap: Map<RoadSurface, List<CurvePoint>>? = null,
    showMultiSurfaceOverlay: Boolean = false,
    showTotalStoppingDistance: Boolean = false,
    onSpeedSelected: (Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    val textPaintColor = MaterialTheme.colorScheme.onSurfaceVariant.hashCode()
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("braking_curve_canvas")
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val paddingLeft = 46f
                    val paddingRight = 24f
                    val chartWidth = size.width - paddingLeft - paddingRight
                    val xFraction = ((offset.x - paddingLeft) / chartWidth).coerceIn(0f, 1f)
                    val speed = 20.0 + (xFraction * (140.0 - 20.0))
                    onSpeedSelected(speed)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val paddingLeft = 46f
                    val paddingRight = 24f
                    val chartWidth = size.width - paddingLeft - paddingRight
                    val xFraction = ((change.position.x - paddingLeft) / chartWidth).coerceIn(0f, 1f)
                    val speed = 20.0 + (xFraction * (140.0 - 20.0))
                    onSpeedSelected(speed)
                }
            }
    ) {
        val w = size.width
        val h = size.height

        val paddingLeft = 52f
        val paddingBottom = 48f
        val paddingTop = 28f
        val paddingRight = 24f

        val plotW = w - paddingLeft - paddingRight
        val plotH = h - paddingTop - paddingBottom

        if (plotW <= 0 || plotH <= 0 || currentPoints.isEmpty()) return@Canvas

        val minSpeed = 20f
        val maxSpeed = 140f

        // Determine Y max across all active curves
        var maxY = 150f
        if (showMultiSurfaceOverlay && multiSurfaceMap != null) {
            val icePoints = multiSurfaceMap[RoadSurface.ICE] ?: emptyList()
            val maxIce = icePoints.maxOfOrNull { if (showTotalStoppingDistance) it.totalDistanceM else it.brakingDistanceM } ?: 150f
            maxY = max(maxY, maxIce * 1.08f)
        } else {
            val maxCurrent = currentPoints.maxOfOrNull { if (showTotalStoppingDistance) it.totalDistanceM else it.brakingDistanceM } ?: 100f
            maxY = max(100f, maxCurrent * 1.15f)
        }

        // Draw horizontal grid lines & Y labels
        val yStep = when {
            maxY > 200f -> 50f
            maxY > 120f -> 25f
            else -> 20f
        }

        var yVal = 0f
        while (yVal <= maxY) {
            val yPos = paddingTop + plotH - ((yVal / maxY) * plotH)
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, yPos),
                end = Offset(w - paddingRight, yPos),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // Y label text
            drawContext.canvas.nativeCanvas.drawText(
                "${yVal.toInt()}m",
                10f,
                yPos + 8f,
                android.graphics.Paint().apply {
                    color = textPaintColor
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.LEFT
                    isAntiAlias = true
                }
            )
            yVal += yStep
        }

        // Draw vertical grid lines & X labels (Speed 20, 40, 60, 80, 100, 120, 140)
        for (speed in 20..140 step 20) {
            val xPos = paddingLeft + (((speed - minSpeed) / (maxSpeed - minSpeed)) * plotW)
            drawLine(
                color = gridColor,
                start = Offset(xPos, paddingTop),
                end = Offset(xPos, paddingTop + plotH),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // X label text
            drawContext.canvas.nativeCanvas.drawText(
                "$speed",
                xPos,
                h - 12f,
                android.graphics.Paint().apply {
                    color = textPaintColor
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }

        // Draw Axes lines
        drawLine(
            color = axisColor,
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, paddingTop + plotH),
            strokeWidth = 2f
        )
        drawLine(
            color = axisColor,
            start = Offset(paddingLeft, paddingTop + plotH),
            end = Offset(w - paddingRight, paddingTop + plotH),
            strokeWidth = 2f
        )

        // Draw Multi-Surface Comparison Overlay Curves if requested
        if (showMultiSurfaceOverlay && multiSurfaceMap != null) {
            val surfaceColors = mapOf(
                RoadSurface.DRY to Color(0xFF10B981), // Emerald
                RoadSurface.WET to Color(0xFF3B82F6), // Blue
                RoadSurface.SNOW to Color(0xFF38BDF8), // Cyan/Ice
                RoadSurface.ICE to Color(0xFFEF4444) // Red alert
            )

            RoadSurface.entries.forEach { surface ->
                val pts = multiSurfaceMap[surface] ?: return@forEach
                val color = surfaceColors[surface] ?: Color.Gray
                drawCurvePath(
                    points = pts,
                    minSpeed = minSpeed,
                    maxSpeed = maxSpeed,
                    maxY = maxY,
                    plotW = plotW,
                    plotH = plotH,
                    paddingLeft = paddingLeft,
                    paddingTop = paddingTop,
                    color = color.copy(alpha = 0.7f),
                    strokeWidth = 2.5f,
                    showTotal = showTotalStoppingDistance
                )
            }
        }

        // Draw Primary Selected Configuration Curve with Gradient Fill underneath
        val currentPath = Path()
        val fillPath = Path()

        var first = true
        var firstX = 0f
        var lastX = 0f

        currentPoints.forEach { pt ->
            val value = if (showTotalStoppingDistance) pt.totalDistanceM else pt.brakingDistanceM
            val xPos = paddingLeft + (((pt.speedKmh - minSpeed) / (maxSpeed - minSpeed)) * plotW)
            val yPos = paddingTop + plotH - ((value / maxY) * plotH)

            if (first) {
                currentPath.moveTo(xPos, yPos)
                fillPath.moveTo(xPos, paddingTop + plotH)
                fillPath.lineTo(xPos, yPos)
                firstX = xPos
                first = false
            } else {
                currentPath.lineTo(xPos, yPos)
                fillPath.lineTo(xPos, yPos)
            }
            lastX = xPos
        }

        fillPath.lineTo(lastX, paddingTop + plotH)
        fillPath.close()

        // Draw soft gradient area under the current curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(primaryColor.copy(alpha = 0.22f), Color.Transparent),
                startY = paddingTop,
                endY = paddingTop + plotH
            )
        )

        // Draw bold current curve
        drawPath(
            path = currentPath,
            color = primaryColor,
            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
        )

        // Calculate and Draw LIVE Speed Dot & Marker Line
        val currentSpeedClamped = currentSpeedKmh.toFloat().coerceIn(minSpeed, maxSpeed)
        val currentX = paddingLeft + (((currentSpeedClamped - minSpeed) / (maxSpeed - minSpeed)) * plotW)

        // Find interpolated distance at currentSpeed
        val nearestPoint = currentPoints.minByOrNull { kotlin.math.abs(it.speedKmh - currentSpeedClamped) }
        val currentDist = nearestPoint?.let {
            if (showTotalStoppingDistance) it.totalDistanceM else it.brakingDistanceM
        } ?: 0f

        val currentY = paddingTop + plotH - ((currentDist / maxY) * plotH)

        // Vertical indicator line at current speed
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(currentX, paddingTop),
            end = Offset(currentX, paddingTop + plotH),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )

        // Live Point outer pulse ring and inner solid dot
        drawCircle(
            color = primaryColor.copy(alpha = 0.35f),
            radius = 12f,
            center = Offset(currentX, currentY)
        )
        drawCircle(
            color = Color.White,
            radius = 6.5f,
            center = Offset(currentX, currentY)
        )
        drawCircle(
            color = primaryColor,
            radius = 4.5f,
            center = Offset(currentX, currentY)
        )

        // Live Tooltip badge above dot
        val tooltipText = "${currentDist.roundToInt()}m @ ${currentSpeedClamped.roundToInt()} km/h"
        val tooltipY = (currentY - 22f).coerceAtLeast(paddingTop + 16f)
        val tooltipX = currentX.coerceIn(paddingLeft + 60f, w - paddingRight - 60f)

        drawContext.canvas.nativeCanvas.drawText(
            tooltipText,
            tooltipX,
            tooltipY,
            android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
            }
        )
    }
}

private fun DrawScope.drawCurvePath(
    points: List<CurvePoint>,
    minSpeed: Float,
    maxSpeed: Float,
    maxY: Float,
    plotW: Float,
    plotH: Float,
    paddingLeft: Float,
    paddingTop: Float,
    color: Color,
    strokeWidth: Float,
    showTotal: Boolean
) {
    if (points.isEmpty()) return
    val path = Path()
    var first = true

    points.forEach { pt ->
        val value = if (showTotal) pt.totalDistanceM else pt.brakingDistanceM
        val xPos = paddingLeft + (((pt.speedKmh - minSpeed) / (maxSpeed - minSpeed)) * plotW)
        val yPos = paddingTop + plotH - ((value / maxY) * plotH)

        if (first) {
            path.moveTo(xPos, yPos)
            first = false
        } else {
            path.lineTo(xPos, yPos)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}
