package com.example.lab5


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var tvXValue: TextView
    private lateinit var tvYValue: TextView
    private lateinit var tvZValue: TextView
    private lateinit var tvTotalG: TextView
    private lateinit var tvMaxG: TextView
    private lateinit var progressX: ProgressBar
    private lateinit var progressY: ProgressBar
    private lateinit var progressZ: ProgressBar
    private lateinit var progressTotal: ProgressBar

    private var maxGForce = 0f
    private val gThreshold = 2.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initSensor()
    }

    private fun initViews() {
        tvXValue = findViewById(R.id.tvXValue)
        tvYValue = findViewById(R.id.tvYValue)
        tvZValue = findViewById(R.id.tvZValue)
        tvTotalG = findViewById(R.id.tvTotalG)
        tvMaxG = findViewById(R.id.tvMaxG)
        progressX = findViewById(R.id.progressX)
        progressY = findViewById(R.id.progressY)
        progressZ = findViewById(R.id.progressZ)
        progressTotal = findViewById(R.id.progressTotal)
    }

    private fun initSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            tvTotalG.text = "Акселерометр недоступний"
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0] / 9.81f // конвертуємо в G
                val y = it.values[1] / 9.81f
                val z = it.values[2] / 9.81f

                val totalG = sqrt(x * x + y * y + z * z)

                updateUI(x, y, z, totalG)
                updateMaxGForce(totalG)
                updateProgressBars(x, y, z, totalG)
            }
        }
    }

    private fun updateUI(x: Float, y: Float, z: Float, totalG: Float) {
        tvXValue.text = "X: ${String.format("%.2f", x)} G"
        tvYValue.text = "Y: ${String.format("%.2f", y)} G"
        tvZValue.text = "Z: ${String.format("%.2f", z)} G"
        tvTotalG.text = "Загальне: ${String.format("%.2f", totalG)} G"

        val color = if (totalG > gThreshold) {
            ContextCompat.getColor(this, android.R.color.holo_red_light)
        } else {
            ContextCompat.getColor(this, android.R.color.white)
        }
        tvTotalG.setTextColor(color)
    }

    private fun updateMaxGForce(currentG: Float) {
        if (currentG > maxGForce) {
            maxGForce = currentG
            tvMaxG.text = "Максимум: ${String.format("%.2f", maxGForce)} G"
        }
    }

    private fun updateProgressBars(x: Float, y: Float, z: Float, totalG: Float) {

        val maxRange = 3f

        progressX.progress = ((x + maxRange) / (2 * maxRange) * 100).toInt().coerceIn(0, 100)
        progressY.progress = ((y + maxRange) / (2 * maxRange) * 100).toInt().coerceIn(0, 100)
        progressZ.progress = ((z + maxRange) / (2 * maxRange) * 100).toInt().coerceIn(0, 100)
        progressTotal.progress = (totalG / (maxRange * 2) * 100).toInt().coerceIn(0, 100)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}