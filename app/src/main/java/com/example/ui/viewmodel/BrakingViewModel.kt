package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PredictionEntity
import com.example.data.model.BrakeCondition
import com.example.data.model.BrakingPrediction
import com.example.data.model.RoadSlope
import com.example.data.model.RoadSurface
import com.example.data.model.SpeedUnit
import com.example.data.model.TireCondition
import com.example.data.model.VehicleLoad
import com.example.data.model.WeatherCondition
import com.example.data.repository.PredictionRepository
import com.example.ml.BrakingPredictorEngine
import com.example.ml.CurvePoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.round

data class ScenarioPreset(
    val name: String,
    val icon: String,
    val description: String,
    val speedKmh: Double,
    val roadSurface: RoadSurface,
    val weather: WeatherCondition,
    val tireCondition: TireCondition,
    val vehicleLoad: VehicleLoad,
    val roadSlope: RoadSlope,
    val brakeCondition: BrakeCondition,
    val reactionTime: Double = 1.5
)

class BrakingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PredictionRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PredictionRepository(db.predictionDao())
    }

    val historyList: StateFlow<List<PredictionEntity>> = repository.allPredictions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Current Parameters
    private val _speedKmh = MutableStateFlow(60.0)
    val speedKmh: StateFlow<Double> = _speedKmh.asStateFlow()

    private val _speedUnit = MutableStateFlow(SpeedUnit.KMH)
    val speedUnit: StateFlow<SpeedUnit> = _speedUnit.asStateFlow()

    private val _roadSurface = MutableStateFlow(RoadSurface.DRY)
    val roadSurface: StateFlow<RoadSurface> = _roadSurface.asStateFlow()

    private val _weather = MutableStateFlow(WeatherCondition.CLEAR)
    val weather: StateFlow<WeatherCondition> = _weather.asStateFlow()

    private val _tireCondition = MutableStateFlow(TireCondition.GOOD)
    val tireCondition: StateFlow<TireCondition> = _tireCondition.asStateFlow()

    private val _vehicleLoad = MutableStateFlow(VehicleLoad.MEDIUM)
    val vehicleLoad: StateFlow<VehicleLoad> = _vehicleLoad.asStateFlow()

    private val _roadSlope = MutableStateFlow(RoadSlope.FLAT)
    val roadSlope: StateFlow<RoadSlope> = _roadSlope.asStateFlow()

    private val _brakeCondition = MutableStateFlow(BrakeCondition.GOOD)
    val brakeCondition: StateFlow<BrakeCondition> = _brakeCondition.asStateFlow()

    private val _reactionTimeSec = MutableStateFlow(1.5)
    val reactionTimeSec: StateFlow<Double> = _reactionTimeSec.asStateFlow()

    // Calculated Prediction
    private val _currentPrediction = MutableStateFlow(calculateCurrentPrediction())
    val currentPrediction: StateFlow<BrakingPrediction> = _currentPrediction.asStateFlow()

    // Curves for Visualizer Graph
    private val _curvePoints = MutableStateFlow<List<CurvePoint>>(emptyList())
    val curvePoints: StateFlow<List<CurvePoint>> = _curvePoints.asStateFlow()

    // Multi-surface comparison curves for Visualizer
    private val _multiSurfaceCurves = MutableStateFlow<Map<RoadSurface, List<CurvePoint>>>(emptyMap())
    val multiSurfaceCurves: StateFlow<Map<RoadSurface, List<CurvePoint>>> = _multiSurfaceCurves.asStateFlow()

    // Comparison Mode: Scenario A & Scenario B
    private val _compareA = MutableStateFlow(
        BrakingPredictorEngine.predict(
            speedKmh = 60.0,
            roadSurface = RoadSurface.DRY,
            weather = WeatherCondition.CLEAR,
            tireCondition = TireCondition.GOOD,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD,
            reactionTimeSeconds = 1.5
        )
    )
    val compareA: StateFlow<BrakingPrediction> = _compareA.asStateFlow()

    private val _compareB = MutableStateFlow(
        BrakingPredictorEngine.predict(
            speedKmh = 60.0,
            roadSurface = RoadSurface.WET,
            weather = WeatherCondition.RAIN,
            tireCondition = TireCondition.WORN,
            vehicleLoad = VehicleLoad.HEAVY,
            roadSlope = RoadSlope.DOWNHILL,
            brakeCondition = BrakeCondition.GOOD,
            reactionTimeSeconds = 1.5
        )
    )
    val compareB: StateFlow<BrakingPrediction> = _compareB.asStateFlow()

    private val _obstacleDistanceM = MutableStateFlow(40.0)
    val obstacleDistanceM: StateFlow<Double> = _obstacleDistanceM.asStateFlow()

    // Presets
    val presets = listOf(
        ScenarioPreset(
            name = "City Commute",
            icon = "🏙️",
            description = "Typical urban driving, dry asphalt, clear weather",
            speedKmh = 50.0,
            roadSurface = RoadSurface.DRY,
            weather = WeatherCondition.CLEAR,
            tireCondition = TireCondition.GOOD,
            vehicleLoad = VehicleLoad.LIGHT,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD,
            reactionTime = 1.5
        ),
        ScenarioPreset(
            name = "Highway Rain Storm",
            icon = "🌧️",
            description = "High speed on wet asphalt with reduced tyre tread",
            speedKmh = 100.0,
            roadSurface = RoadSurface.WET,
            weather = WeatherCondition.RAIN,
            tireCondition = TireCondition.WORN,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD,
            reactionTime = 1.6
        ),
        ScenarioPreset(
            name = "Black Ice Warning",
            icon = "🧊",
            description = "Severe freezing hazard, near-zero traction",
            speedKmh = 60.0,
            roadSurface = RoadSurface.ICE,
            weather = WeatherCondition.FOG,
            tireCondition = TireCondition.GOOD,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD,
            reactionTime = 1.8
        ),
        ScenarioPreset(
            name = "Mountain Descent (Loaded)",
            icon = "⛰️",
            description = "Steep downhill with full cargo load and faded brakes",
            speedKmh = 80.0,
            roadSurface = RoadSurface.WET,
            weather = WeatherCondition.RAIN,
            tireCondition = TireCondition.WORN,
            vehicleLoad = VehicleLoad.HEAVY,
            roadSlope = RoadSlope.DOWNHILL,
            brakeCondition = BrakeCondition.WORN,
            reactionTime = 1.5
        ),
        ScenarioPreset(
            name = "Snowy Slush",
            icon = "❄️",
            description = "Packed snow with reduced visibility and cold tires",
            speedKmh = 65.0,
            roadSurface = RoadSurface.SNOW,
            weather = WeatherCondition.FOG,
            tireCondition = TireCondition.WORN,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD,
            reactionTime = 1.7
        )
    )

    init {
        recalculate()
    }

    private fun calculateCurrentPrediction(): BrakingPrediction {
        return BrakingPredictorEngine.predict(
            speedKmh = _speedKmh.value,
            roadSurface = _roadSurface.value,
            weather = _weather.value,
            tireCondition = _tireCondition.value,
            vehicleLoad = _vehicleLoad.value,
            roadSlope = _roadSlope.value,
            brakeCondition = _brakeCondition.value,
            reactionTimeSeconds = _reactionTimeSec.value
        )
    }

    private fun recalculate() {
        _currentPrediction.value = calculateCurrentPrediction()
        updateCurves()
    }

    private fun updateCurves() {
        _curvePoints.value = BrakingPredictorEngine.generateCurvePoints(
            roadSurface = _roadSurface.value,
            weather = _weather.value,
            tireCondition = _tireCondition.value,
            vehicleLoad = _vehicleLoad.value,
            roadSlope = _roadSlope.value,
            brakeCondition = _brakeCondition.value,
            reactionTimeSeconds = _reactionTimeSec.value
        )

        // Generate curves across all 4 surfaces holding other params constant
        val surfaceMap = mutableMapOf<RoadSurface, List<CurvePoint>>()
        RoadSurface.entries.forEach { surface ->
            surfaceMap[surface] = BrakingPredictorEngine.generateCurvePoints(
                roadSurface = surface,
                weather = _weather.value,
                tireCondition = _tireCondition.value,
                vehicleLoad = _vehicleLoad.value,
                roadSlope = _roadSlope.value,
                brakeCondition = _brakeCondition.value,
                reactionTimeSeconds = _reactionTimeSec.value
            )
        }
        _multiSurfaceCurves.value = surfaceMap
    }

    // Setters
    fun setSpeedKmh(speed: Double) {
        _speedKmh.value = round(speed * 10.0) / 10.0
        recalculate()
    }

    fun toggleSpeedUnit() {
        _speedUnit.value = if (_speedUnit.value == SpeedUnit.KMH) SpeedUnit.MPH else SpeedUnit.KMH
    }

    fun setRoadSurface(surface: RoadSurface) {
        _roadSurface.value = surface
        recalculate()
    }

    fun setWeather(weather: WeatherCondition) {
        _weather.value = weather
        recalculate()
    }

    fun setTireCondition(tire: TireCondition) {
        _tireCondition.value = tire
        recalculate()
    }

    fun setVehicleLoad(load: VehicleLoad) {
        _vehicleLoad.value = load
        recalculate()
    }

    fun setRoadSlope(slope: RoadSlope) {
        _roadSlope.value = slope
        recalculate()
    }

    fun setBrakeCondition(condition: BrakeCondition) {
        _brakeCondition.value = condition
        recalculate()
    }

    fun setReactionTime(seconds: Double) {
        _reactionTimeSec.value = round(seconds * 100.0) / 100.0
        recalculate()
    }

    fun applyPreset(preset: ScenarioPreset) {
        _speedKmh.value = preset.speedKmh
        _roadSurface.value = preset.roadSurface
        _weather.value = preset.weather
        _tireCondition.value = preset.tireCondition
        _vehicleLoad.value = preset.vehicleLoad
        _roadSlope.value = preset.roadSlope
        _brakeCondition.value = preset.brakeCondition
        _reactionTimeSec.value = preset.reactionTime
        recalculate()
    }

    fun setObstacleDistance(distM: Double) {
        _obstacleDistanceM.value = max(5.0, round(distM * 10.0) / 10.0)
    }

    fun updateCompareScenarioA(update: (BrakingPrediction) -> BrakingPrediction) {
        _compareA.value = update(_compareA.value)
    }

    fun updateCompareScenarioB(update: (BrakingPrediction) -> BrakingPrediction) {
        _compareB.value = update(_compareB.value)
    }

    fun copyCurrentToScenarioA() {
        _compareA.value = _currentPrediction.value
    }

    fun copyCurrentToScenarioB() {
        _compareB.value = _currentPrediction.value
    }

    // Database Actions
    fun saveCurrentPrediction(title: String? = null) {
        viewModelScope.launch {
            val pred = _currentPrediction.value
            val label = title?.ifBlank { null }
                ?: "${pred.speedKmh.toInt()} km/h on ${pred.roadSurface.displayName}"
            
            val entity = PredictionEntity(
                title = label,
                speedKmh = pred.speedKmh,
                roadSurface = pred.roadSurface.name,
                weather = pred.weather.name,
                tireCondition = pred.tireCondition.name,
                vehicleLoad = pred.vehicleLoad.name,
                roadSlope = pred.roadSlope.name,
                brakeCondition = pred.brakeCondition.name,
                reactionTimeSeconds = pred.reactionTimeSeconds,
                brakingDistanceM = pred.brakingDistanceM,
                reactionDistanceM = pred.reactionDistanceM,
                totalStoppingDistanceM = pred.totalStoppingDistanceM,
                stoppingTimeSeconds = pred.stoppingTimeSeconds,
                decelerationMs2 = pred.decelerationMs2,
                safetyLevel = pred.safetyLevel.name
            )
            repository.savePrediction(entity)
        }
    }

    fun loadPredictionEntity(entity: PredictionEntity) {
        _speedKmh.value = entity.speedKmh
        _roadSurface.value = runCatching { RoadSurface.valueOf(entity.roadSurface) }.getOrDefault(RoadSurface.DRY)
        _weather.value = runCatching { WeatherCondition.valueOf(entity.weather) }.getOrDefault(WeatherCondition.CLEAR)
        _tireCondition.value = runCatching { TireCondition.valueOf(entity.tireCondition) }.getOrDefault(TireCondition.GOOD)
        _vehicleLoad.value = runCatching { VehicleLoad.valueOf(entity.vehicleLoad) }.getOrDefault(VehicleLoad.MEDIUM)
        _roadSlope.value = runCatching { RoadSlope.valueOf(entity.roadSlope) }.getOrDefault(RoadSlope.FLAT)
        _brakeCondition.value = runCatching { BrakeCondition.valueOf(entity.brakeCondition) }.getOrDefault(BrakeCondition.GOOD)
        _reactionTimeSec.value = entity.reactionTimeSeconds
        recalculate()
    }

    fun deletePrediction(entity: PredictionEntity) {
        viewModelScope.launch {
            repository.deletePrediction(entity)
        }
    }

    fun toggleBookmark(id: Long) {
        viewModelScope.launch {
            repository.toggleBookmark(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
