package com.example.nova.safety

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class ShockEvent(
    val timestamp: Long,
    val magnitudeG: Float,
    val severity: String, // "MILD", "MODERATE", "CRITICAL"
    val x: Float,
    val y: Float,
    val z: Float
)

data class BatteryTelemetry(
    val levelPercent: Int = 85,
    val isCharging: Boolean = false,
    val temperatureCelsius: Float = 31.5f,
    val voltageMv: Int = 4120
)

class SafetyManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private val _currentGForce = MutableStateFlow(1.0f) // 1.0G is normal gravity
    val currentGForce: StateFlow<Float> = _currentGForce.asStateFlow()

    private val _maxGForce = MutableStateFlow(1.0f)
    val maxGForce: StateFlow<Float> = _maxGForce.asStateFlow()

    private val _shockHistory = MutableStateFlow<List<ShockEvent>>(emptyList())
    val shockHistory: StateFlow<List<ShockEvent>> = _shockHistory.asStateFlow()

    private val _shockThresholdG = MutableStateFlow(2.8f) // 2.8G trigger
    val shockThresholdG: StateFlow<Float> = _shockThresholdG.asStateFlow()

    private val _isSosCountdownActive = MutableStateFlow(false)
    val isSosCountdownActive: StateFlow<Boolean> = _isSosCountdownActive.asStateFlow()

    private val _sosCountdownSeconds = MutableStateFlow(10)
    val sosCountdownSeconds: StateFlow<Int> = _sosCountdownSeconds.asStateFlow()

    private val _batteryTelemetry = MutableStateFlow(BatteryTelemetry())
    val batteryTelemetry: StateFlow<BatteryTelemetry> = _batteryTelemetry.asStateFlow()

    private var sosJob: Job? = null
    private var lastShockTriggerTimestamp = 0L

    init {
        registerSensors()
        registerBatteryReceiver()
    }

    fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate total acceleration in m/s^2, normalized by earth gravity (9.81)
            val totalAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val gForce = totalAcceleration / SensorManager.GRAVITY_EARTH

            _currentGForce.value = gForce
            if (gForce > _maxGForce.value) {
                _maxGForce.value = gForce
            }

            // Check if shock threshold exceeded (cooldown 3 seconds)
            val now = System.currentTimeMillis()
            if (gForce >= _shockThresholdG.value && (now - lastShockTriggerTimestamp > 3000L)) {
                lastShockTriggerTimestamp = now
                val severity = when {
                    gForce >= 4.5f -> "CRITICAL"
                    gForce >= 3.2f -> "MODERATE"
                    else -> "MILD"
                }

                val shock = ShockEvent(now, gForce, severity, x, y, z)
                val updated = (_shockHistory.value + shock).takeLast(10)
                _shockHistory.value = updated

                // Vibrate alerting shock
                triggerHapticPulse(if (severity == "CRITICAL") 400L else 150L)

                // If critical, auto-trigger SOS safety guardian countdown
                if (severity == "CRITICAL" && !_isSosCountdownActive.value) {
                    startSosCountdown()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setThreshold(thresholdG: Float) {
        _shockThresholdG.value = thresholdG.coerceIn(1.5f, 6.0f)
    }

    fun resetPeakG() {
        _maxGForce.value = 1.0f
    }

    fun startSosCountdown() {
        if (_isSosCountdownActive.value) return
        _isSosCountdownActive.value = true
        _sosCountdownSeconds.value = 10

        sosJob?.cancel()
        sosJob = CoroutineScope(Dispatchers.Default).launch {
            for (i in 10 downTo 1) {
                _sosCountdownSeconds.value = i
                triggerHapticPulse(120L)
                delay(1000L)
            }
            _isSosCountdownActive.value = false
            dispatchEmergencySos()
        }
    }

    fun abortSosCountdown() {
        sosJob?.cancel()
        _isSosCountdownActive.value = false
        _sosCountdownSeconds.value = 10
        triggerHapticPulse(50L)
    }

    private fun triggerHapticPulse(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignore if vibration disallowed
        }
    }

    private fun dispatchEmergencySos() {
        try {
            // Open emergency dialer with 911 / 112
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:911")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback
        }
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val percent = if (level >= 0 && scale > 0) (level * 100) / scale else 85
                    val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                    val tempTenths = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300)
                    val voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000)

                    _batteryTelemetry.value = BatteryTelemetry(
                        levelPercent = percent,
                        isCharging = isCharging,
                        temperatureCelsius = tempTenths / 10.0f,
                        voltageMv = voltage
                    )
                }
            }
        }
        try {
            context.registerReceiver(receiver, filter)
        } catch (e: Exception) {
            // Fallback
        }
    }
}
