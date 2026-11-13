package com.example.myapplication.sdk

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object AMapSphericalUtils {
    private const val EARTH_RADIUS_KM = 6371.0

    fun calculateDistanceKm(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Double {
        val dLat = Math.toRadians(endLat - startLat)
        val dLng = Math.toRadians(endLng - startLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(startLat)) * cos(Math.toRadians(endLat)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    fun estimateSafeDeliveryMinutes(distanceKm: Double, averageSpeedKmH: Double = 20.0): Int {
        val baseMinutes = (distanceKm / averageSpeedKmH * 60).toInt()
        val bufferMinutes = 10
        return baseMinutes + bufferMinutes
    }
}

class LocationAndPaymentSdk {
    fun getSdkStatus(): String {
        return "AMapSphericalUtils & Payment Gateway SDK ready"
    }
}
