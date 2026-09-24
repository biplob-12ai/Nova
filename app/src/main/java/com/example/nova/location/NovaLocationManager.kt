package com.example.nova.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class Waypoint(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val designation: String
)

data class DistanceVector(
    val waypoint: Waypoint,
    val distanceMeters: Float,
    val bearingDegrees: Float,
    val cardinalDirection: String
)

data class GpsTelemetry(
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val altitudeMeters: Double = 16.0,
    val accuracyMeters: Float = 4.2f,
    val speedKmh: Float = 0.0f,
    val bearingDegrees: Float = 0.0f,
    val formattedAddress: String = "Acquiring hardware GPS fix...",
    val cyberGridCoord: String = "GRID: 37N-122W-S01",
    val isLiveFix: Boolean = false
)

class NovaLocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _telemetry = MutableStateFlow(GpsTelemetry())
    val telemetry: StateFlow<GpsTelemetry> = _telemetry.asStateFlow()

    private val _distanceVectors = MutableStateFlow<List<DistanceVector>>(emptyList())
    val distanceVectors: StateFlow<List<DistanceVector>> = _distanceVectors.asStateFlow()

    private val waypoints = listOf(
        Waypoint("CYBER NEXUS ALPHA", 37.7891, -122.4014, "Primary HQ Node"),
        Waypoint("DATA HAVEN DOCK", 37.7955, -122.3937, "Maritime Relay"),
        Waypoint("QUANTUM RELAY TOWER", 37.7562, -122.4430, "Twin Peaks Antenna"),
        Waypoint("GLOBAL AIRPORT HUB", 37.6213, -122.3790, "SFO Terminal Grid")
    )

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(2000L)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        updateLocationData(location)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                context.mainLooper
            )

            // Also request last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    updateLocationData(loc)
                }
            }
        } catch (e: SecurityException) {
            // Permission not yet granted, fallback gracefully
            _telemetry.value = _telemetry.value.copy(
                formattedAddress = "GPS Permission needed for live fix",
                isLiveFix = false
            )
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    private fun updateLocationData(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val alt = location.altitude
        val acc = location.accuracy
        val speedKmh = location.speed * 3.6f
        val bearing = location.bearing

        val grid = "CYBER-LOC [${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lng)}]"

        _telemetry.value = GpsTelemetry(
            latitude = lat,
            longitude = lng,
            altitudeMeters = alt,
            accuracyMeters = acc,
            speedKmh = speedKmh,
            bearingDegrees = bearing,
            formattedAddress = "Resolving telemetry coordinates...",
            cyberGridCoord = grid,
            isLiveFix = true
        )

        computeDistanceVectors(lat, lng)
        resolveAddress(lat, lng)
    }

    private fun computeDistanceVectors(currentLat: Double, currentLng: Double) {
        val vectors = waypoints.map { wp ->
            val results = FloatArray(2)
            Location.distanceBetween(
                currentLat, currentLng,
                wp.latitude, wp.longitude,
                results
            )
            val distance = results[0]
            val bearing = computeBearing(currentLat, currentLng, wp.latitude, wp.longitude)
            val cardinal = getCardinalDirection(bearing)
            DistanceVector(wp, distance, bearing, cardinal)
        }
        _distanceVectors.value = vectors
    }

    private fun computeBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)
        return ((Math.toDegrees(theta) + 360) % 360).toFloat()
    }

    private fun getCardinalDirection(bearing: Float): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        val index = ((bearing + 22.5f) / 45f).toInt() % 8
        return directions[index]
    }

    private fun resolveAddress(lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(lat, lng, 1) { addresses ->
                            val addr = addresses.firstOrNull()
                            val line = addr?.getAddressLine(0) ?: "${addr?.locality ?: "Unknown Sector"}, ${addr?.countryName ?: ""}"
                            _telemetry.value = _telemetry.value.copy(formattedAddress = line)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        val addr = addresses?.firstOrNull()
                        val line = addr?.getAddressLine(0) ?: "${addr?.locality ?: "Unknown Sector"}, ${addr?.countryName ?: ""}"
                        _telemetry.value = _telemetry.value.copy(formattedAddress = line)
                    }
                }
            } catch (e: Exception) {
                _telemetry.value = _telemetry.value.copy(
                    formattedAddress = "Lat: ${String.format(Locale.US, "%.5f", lat)} | Lng: ${String.format(Locale.US, "%.5f", lng)}"
                )
            }
        }
    }
}
