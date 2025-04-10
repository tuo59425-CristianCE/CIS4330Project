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
import com.example.trackstar.ui.theme.TrackStarMobileAppTheme // Added import
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TrackStarMobileAppTheme { // Use the custom theme
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var speed by remember { mutableStateOf(0.0f) } // Speed state
    var volumeProgress by remember { mutableStateOf(0f) } // Volume progress state

    LaunchedEffect(Unit) {
        startAccelerometerTracking(context) { newSpeed ->
            speed = newSpeed
            volumeProgress = adjustVolumeBasedOnSpeed(context, speed) // Update volume progress
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome to TrackStar",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Speed: ${String.format("%.2f", speed)} m/s",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            Toast.makeText(context, "Tracking Started", Toast.LENGTH_SHORT).show()
        }) {
            Text("Start Tracking")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            Toast.makeText(context, "Tracking Stopped", Toast.LENGTH_SHORT).show()
        }) {
            Text("Stop Tracking")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Volume will increase as speed increases.")

        // Volume Progress Bar
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Volume Level", fontSize = 16.sp)
        LinearProgressIndicator(
            progress = volumeProgress,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
    }
}


/** Start tracking speed using accelerometer */
fun startAccelerometerTracking(context: Context, onSpeedUpdate: (Float) -> Unit) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val sensorListener = object : SensorEventListener {
        var lastUpdateTime = System.currentTimeMillis()
        var lastVelocity = 0f
        var lastAcceleration = 0f // Track previous acceleration to apply decay

        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            val currentTime = System.currentTimeMillis()
            val timeDiff = (currentTime - lastUpdateTime) / 1000.0f // Time in seconds
            lastUpdateTime = currentTime

            // Calculate acceleration magnitude (vector sum)
            val acceleration = sqrt(event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]) - 9.81f // Subtract gravity

            // Calculate speed based on acceleration
            val newSpeed = lastVelocity + acceleration * timeDiff // v = u + at

            // Apply a decay to the speed when there's little or no acceleration
            val decayFactor = 0.95f // This factor controls how quickly the speed decays
            if (acceleration < 0.1f) { // If the acceleration is small, reduce the speed slowly
                lastVelocity *= decayFactor // Gradually reduce the speed
            } else {
                lastVelocity = newSpeed // Update speed based on acceleration
            }

            // Ensure speed doesn't go negative
            lastVelocity = lastVelocity.coerceAtLeast(0f)

            onSpeedUpdate(lastVelocity) // Update the speed state
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
}


/** Adjust volume based on speed and return the volume percentage */
fun adjustVolumeBasedOnSpeed(context: Context, speed: Float): Float {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    // Speed thresholds (adjustable)
    val volume = when {
        speed > 5 -> maxVolume // Full volume if speed > 5 m/s
        speed > 2 -> (maxVolume * 0.7).toInt() // 70% volume if speed > 2 m/s
        else -> (maxVolume * 0.4).toInt() // 40% volume otherwise
    }

    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)

    // Return the volume progress as a fraction of the max volume
    return volume.toFloat() / maxVolume
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TrackStarMobileAppTheme { // Use the custom theme
        MainScreen()
    }
}

