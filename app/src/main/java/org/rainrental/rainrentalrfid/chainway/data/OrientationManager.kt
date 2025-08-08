package org.rainrental.rainrentalrfid.chainway.data

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import org.rainrental.rainrentalrfid.logging.Logger


object OrientationManager: Logger {

    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)

    private var application: Application? = null

    private var sensorManager : SensorManager? = null
    private var accelerometer : Sensor? = null
    private var magnetometer : Sensor? = null

    fun setApplication(app:Application){
        application = app
        sensorManager = application?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    fun startListening() {
//        sensorManager?.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
//        sensorManager?.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    // SensorEventListener to handle sensor data
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            logi("Sensor value changed: ${event?.sensor} $event")
            when (event?.sensor?.type) {
                Sensor.TYPE_ACCELEROMETER -> accelerometerReading = event.values.clone()
                Sensor.TYPE_MAGNETIC_FIELD -> magnetometerReading = event.values.clone()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            logi("Accuracy changed: $accuracy")
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(sensorEventListener)
    }

    // Call this method to get the current device orientation
    fun getDeviceOrientation(): Orientation {
        return Orientation(0f, 0f, 0f) //bypassed

        val rotationMatrix = FloatArray(9)
        val success = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        if (success) {
            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            // Azimuth, pitch, and roll in radians; convert to degrees
            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
            logi("Orientation: $azimuth \t $pitch \t $roll")
            return Orientation(azimuth, pitch, roll)
        }
        logi("Unable to get orientation. ")
        return Orientation(0f, 0f, 0f) // Default value if orientation couldn't be calculated
    }

}