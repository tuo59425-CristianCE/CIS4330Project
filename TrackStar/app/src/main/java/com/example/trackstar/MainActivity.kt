package com.example.trackstar

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
import androidx.compose.ui.unit.sp
import com.example.trackstar.ui.theme.TrackStarMobileAppTheme
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set initial volume to 10%
        adjustVolumeBasedOnSpeed(this, 0f)

        setContent {
            TrackStarMobileAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var speed by remember { mutableStateOf(0.0f) }
    var volumeProgress by remember { mutableStateOf(0f) }
    var isTracking by remember { mutableStateOf(false) }

    // Sensor setup
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    var sensorListener: SensorEventListener? by remember { mutableStateOf(null) }

    // Start Tracking Function
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

                    val acceleration = sqrt(
                        event.values[0] * event.values[0] +
                                event.values[1] * event.values[1] +
                                event.values[2] * event.values[2]
                    ) - 9.81f

                    val newSpeed = lastVelocity + acceleration * timeDiff
                    val decayFactor = 0.95f
                    if (acceleration < 0.1f) {
                        lastVelocity *= decayFactor
                    } else {
                        lastVelocity = newSpeed
                    }

                    lastVelocity = lastVelocity.coerceAtLeast(0f)
                    speed = lastVelocity
                    volumeProgress = adjustVolumeBasedOnSpeed(context, speed)
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

    // Stop Tracking Function
    fun stopTracking() {
        sensorListener?.let {
            sensorManager.unregisterListener(it)
            sensorListener = null
            isTracking = false
            speed = 0f
            volumeProgress = adjustVolumeBasedOnSpeed(context, 0f)
            Toast.makeText(context, "Tracking Stopped", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome to TrackStar",
            style = MaterialTheme.typography.headlineLarge
        )

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

        Text(
            text = "Volume will increase as speed increases.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Volume Level",
            style = MaterialTheme.typography.labelLarge
        )

        LinearProgressIndicator(
            progress = volumeProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Volume: ${(volumeProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

/** Adjust volume based on speed and return volume progress (0.0 to 1.0) */
fun adjustVolumeBasedOnSpeed(context: Context, speed: Float): Float {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    val volume = when {
        speed > 5 -> maxVolume
        speed > 3 -> (maxVolume * 0.7).toInt()
        speed > 2 -> (maxVolume * 0.5).toInt()
        speed > 0.5 -> (maxVolume * 0.2).toInt()
        else -> (maxVolume * 0.0).toInt()
    }

    // This will show the volume slider
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI)

    return volume.toFloat() / maxVolume
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TrackStarMobileAppTheme {
        MainScreen()
    }
}
