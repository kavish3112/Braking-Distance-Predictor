package com.example.data.model

enum class RoadSurface(val displayName: String, val frictionCoeff: Double, val iconLabel: String) {
    DRY("Dry Asphalt", 0.70, "☀️ Dry"),
    WET("Wet Road", 0.52, "🌧️ Wet"),
    SNOW("Snow", 0.27, "❄️ Snow"),
    ICE("Black Ice", 0.15, "🧊 Ice")
}

enum class WeatherCondition(val displayName: String, val reactionDelaySec: Double, val iconLabel: String) {
    CLEAR("Clear / Sunny", 0.0, "☀️ Clear"),
    RAIN("Rain / Mist", 0.25, "🌧️ Rain"),
    FOG("Dense Fog", 0.65, "🌫️ Fog")
}

enum class TireCondition(val displayName: String, val distanceMultiplier: Double, val iconLabel: String) {
    GOOD("Good Tread (≥4mm)", 1.0, "🛞 Good"),
    WORN("Worn / Low Tread (<2mm)", 1.186, "⚠️ Worn")
}

enum class VehicleLoad(val displayName: String, val weightEstimateKg: Int, val distanceMultiplier: Double, val iconLabel: String) {
    LIGHT("Light (Driver Only)", 1250, 0.967, "👤 Light"),
    MEDIUM("Medium (Passengers)", 1550, 1.0, "👥 Medium"),
    HEAVY("Heavy (Full Cargo)", 2100, 1.063, "📦 Heavy")
}

enum class RoadSlope(val displayName: String, val slopeAnglePercent: Double, val distanceMultiplier: Double, val iconLabel: String) {
    UPHILL("Uphill (+6% Incline)", 6.0, 0.878, "📈 Uphill"),
    FLAT("Flat Ground (0%)", 0.0, 1.0, "➖ Flat"),
    DOWNHILL("Downhill (-6% Decline)", -6.0, 1.186, "📉 Downhill")
}

enum class BrakeCondition(val displayName: String, val distanceMultiplier: Double, val iconLabel: String) {
    GOOD("Good / Responsive", 1.0, "✅ Good"),
    WORN("Worn / Faded Pads", 1.15, "⚠️ Worn")
}

enum class SpeedUnit(val symbol: String, val kmhMultiplier: Double) {
    KMH("km/h", 1.0),
    MPH("mph", 1.60934)
}

enum class SafetyLevel(val label: String, val hexColor: Long, val description: String) {
    SAFE("Low Stopping Distance", 0xFF10B981, "Normal stopping range with standard safe distance margins."),
    MODERATE("Caution Advised", 0xFFF59E0B, "Increased distance required. Leave at least 3-4 car lengths."),
    HIGH_RISK("High Hazard", 0xFFF97316, "Substantial braking distance. Drive cautiously with reduced speed."),
    CRITICAL("Extreme Risk", 0xFFEF4444, "Severe loss of traction! Vehicle requires dramatically longer distance to stop.")
}

data class FactorContribution(
    val factorName: String,
    val description: String,
    val extraMeters: Double,
    val percentageImpact: Double
)

data class BrakingPrediction(
    val speedKmh: Double,
    val roadSurface: RoadSurface,
    val weather: WeatherCondition,
    val tireCondition: TireCondition,
    val vehicleLoad: VehicleLoad,
    val roadSlope: RoadSlope,
    val brakeCondition: BrakeCondition,
    val reactionTimeSeconds: Double,
    
    // Outputs
    val brakingDistanceM: Double,
    val reactionDistanceM: Double,
    val totalStoppingDistanceM: Double,
    val stoppingTimeSeconds: Double,
    val decelerationMs2: Double,
    val decelerationG: Double,
    val effectiveFriction: Double,
    val kineticEnergyKJ: Double,
    val carLengths: Double,
    val safetyLevel: SafetyLevel,
    val baselineDryGoodDistanceM: Double,
    val extraDistancePercentOverBaseline: Double,
    val factorContributions: List<FactorContribution>
)
