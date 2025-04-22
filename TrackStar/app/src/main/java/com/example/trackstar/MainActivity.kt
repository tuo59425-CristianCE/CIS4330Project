package com.example.trackstar

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.trackstar.ui.theme.TrackStarMobileAppTheme
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TrackStarMobileAppTheme {
                MainScreen()
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var speed by remember { mutableFloatStateOf(0.0f) }
    var volumeProgress by remember { mutableFloatStateOf(0f) }
    var isTracking by remember { mutableStateOf(false) }
    var minVolumeLevel by remember { mutableFloatStateOf(0f) } // 0% to 100%
    var maxVolumeLevel by remember { mutableFloatStateOf(1f) } // 0% to 100%

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    var sensorListener: SensorEventListener? by remember { mutableStateOf(null) }

    fun startTracking() {
        if (sensorListener == null) {
            val listener = object : SensorEventListener {
                var lastUpdateTime = System.currentTimeMillis()
                var lastVelocity = 0f

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return

                    val currentTime = System.currentTimeMillis()
                    val timeDiff = (currentTime - lastUpdateTime) / 1000.0f
                    lastUpdateTime = currentTime

                    val rawAccel = sqrt(
                        event.values[0] * event.values[0] +
                                event.values[1] * event.values[1] +
                                event.values[2] * event.values[2]
                    )

                    val acceleration = (rawAccel - 9.81f).coerceIn(-5f, 5f) // clamp wild spikes


                    val newSpeed = lastVelocity + acceleration * timeDiff
                    val decayFactor = 0.95f
                    lastVelocity = if (acceleration < 0.1f) lastVelocity * decayFactor else newSpeed
                    lastVelocity = lastVelocity.coerceAtLeast(0f)

                    speed = lastVelocity
                    volumeProgress =
                        adjustVolumeBasedOnSpeed(context, speed, minVolumeLevel, maxVolumeLevel)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorListener = listener
            isTracking = true
            Toast.makeText(context, "Tracking Started", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopTracking() {
        sensorListener?.let {
            sensorManager.unregisterListener(it)
            sensorListener = null
            isTracking = false
            speed = 0f
            volumeProgress = adjustVolumeBasedOnSpeed(context, 0f, minVolumeLevel, maxVolumeLevel)
            Toast.makeText(context, "Tracking Stopped", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Welcome to TrackStar", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Speed: ${String.format("%.2f", speed)} m/s",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { startTracking() },
            enabled = !isTracking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Tracking")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { stopTracking() },
            enabled = isTracking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Stop Tracking")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Volume will increase as speed increases.")

        Spacer(modifier = Modifier.height(16.dp))
        Text("Volume Level", style = MaterialTheme.typography.labelLarge)
        LinearProgressIndicator(
            progress = { volumeProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Volume: ${(volumeProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Min Volume: ${(minVolumeLevel * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge
        )
        Slider(
            value = minVolumeLevel,
            onValueChange = { minVolumeLevel = it.coerceAtMost(maxVolumeLevel) },
            valueRange = 0f..1f
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Max Volume: ${(maxVolumeLevel * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge
        )
        Slider(
            value = maxVolumeLevel,
            onValueChange = { maxVolumeLevel = it.coerceAtLeast(minVolumeLevel) },
            valueRange = 0f..1f
        )
    }
}

private var currentVolume = -1

fun adjustVolumeBasedOnSpeed(
    context: Context,
    speed: Float,
    minVolumePercent: Float,
    maxVolumePercent: Float
): Float {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxSystemVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    val clampedSpeed = speed.coerceIn(0f, 6f)
    val speedRatio = clampedSpeed / 6f

    val minVolume = (minVolumePercent * maxSystemVolume).toInt()
    val maxVolume = (maxVolumePercent * maxSystemVolume).toInt()

    val targetVolume = (minVolume + (speedRatio * (maxVolume - minVolume))).toInt()

    if (currentVolume == -1) {
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    if (currentVolume < targetVolume) currentVolume += 1
    else if (currentVolume > targetVolume) currentVolume -= 1

    audioManager.setStreamVolume(
        AudioManager.STREAM_MUSIC,
        currentVolume,
        AudioManager.FLAG_SHOW_UI
    )

    return currentVolume.toFloat() / maxSystemVolume
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TrackStarMobileAppTheme {
        MainScreen()
    }
}
