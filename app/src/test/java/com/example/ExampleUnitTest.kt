package com.example

import com.example.data.model.BrakeCondition
import com.example.data.model.RoadSlope
import com.example.data.model.RoadSurface
import com.example.data.model.TireCondition
import com.example.data.model.VehicleLoad
import com.example.data.model.WeatherCondition
import com.example.ml.BrakingPredictorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun baselinePrediction_matchesPhysicsModel() {
        // At 60 km/h on Dry surface with Good tires, Medium load, Flat road
        val prediction = BrakingPredictorEngine.predict(
            speedKmh = 60.0,
            roadSurface = RoadSurface.DRY,
            weather = WeatherCondition.CLEAR,
            tireCondition = TireCondition.GOOD,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD,
            reactionTimeSeconds = 1.5
        )

        // d_braking ~ 20.58m (+/- 1m)
        assertEquals(20.59, prediction.brakingDistanceM, 1.0)
        // d_reaction = (60 / 3.6) * 1.5 = 25.0m
        assertEquals(25.0, prediction.reactionDistanceM, 0.5)
        // d_total = 25.0 + 20.59 = 45.59m
        assertEquals(45.59, prediction.totalStoppingDistanceM, 1.0)
    }

    @Test
    fun wetAndIceSurfaces_increaseBrakingDistanceSubstantially() {
        val dryPred = BrakingPredictorEngine.predict(
            speedKmh = 80.0,
            roadSurface = RoadSurface.DRY,
            weather = WeatherCondition.CLEAR,
            tireCondition = TireCondition.GOOD,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD
        )

        val wetPred = BrakingPredictorEngine.predict(
            speedKmh = 80.0,
            roadSurface = RoadSurface.WET,
            weather = WeatherCondition.CLEAR,
            tireCondition = TireCondition.GOOD,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD
        )

        val icePred = BrakingPredictorEngine.predict(
            speedKmh = 80.0,
            roadSurface = RoadSurface.ICE,
            weather = WeatherCondition.CLEAR,
            tireCondition = TireCondition.GOOD,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD
        )

        assertTrue("Wet distance should exceed Dry", wetPred.brakingDistanceM > dryPred.brakingDistanceM * 1.3)
        assertTrue("Ice distance should exceed Dry by over 3.5x", icePred.brakingDistanceM > dryPred.brakingDistanceM * 3.5)
    }

    @Test
    fun wornTiresAndDownhill_increaseStoppingDistance() {
        val goodTireFlat = BrakingPredictorEngine.predict(
            speedKmh = 70.0,
            roadSurface = RoadSurface.DRY,
            weather = WeatherCondition.CLEAR,
            tireCondition = TireCondition.GOOD,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD
        )

        val wornTireDownhill = BrakingPredictorEngine.predict(
            speedKmh = 70.0,
            roadSurface = RoadSurface.DRY,
            weather = WeatherCondition.CLEAR,
            tireCondition = TireCondition.WORN,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.DOWNHILL,
            brakeCondition = BrakeCondition.GOOD
        )

        assertTrue(wornTireDownhill.brakingDistanceM > goodTireFlat.brakingDistanceM)
    }

    @Test
    fun collisionSpeed_calculatedCorrectly() {
        val pred = BrakingPredictorEngine.predict(
            speedKmh = 60.0,
            roadSurface = RoadSurface.DRY,
            weather = WeatherCondition.CLEAR,
            tireCondition = TireCondition.GOOD,
            vehicleLoad = VehicleLoad.MEDIUM,
            roadSlope = RoadSlope.FLAT,
            brakeCondition = BrakeCondition.GOOD,
            reactionTimeSeconds = 1.5
        )

        // If obstacle is farther than total stopping distance (~46m), collision speed must be 0
        val safeSpeed = BrakingPredictorEngine.calculateCollisionSpeedKmh(60.0, pred, 55.0)
        assertEquals(0.0, safeSpeed, 0.01)

        // If obstacle is at 20m (less than reaction distance 25m), collision speed must equal initial 60 km/h
        val immediateCrashSpeed = BrakingPredictorEngine.calculateCollisionSpeedKmh(60.0, pred, 20.0)
        assertEquals(60.0, immediateCrashSpeed, 0.01)

        // If obstacle is midway during braking (e.g. 35m), collision speed must be between 0 and 60 km/h
        val partialBrakeSpeed = BrakingPredictorEngine.calculateCollisionSpeedKmh(60.0, pred, 35.0)
        assertTrue(partialBrakeSpeed in 1.0..59.0)
    }
}
