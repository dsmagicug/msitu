package com.dsmagic.kibira

//
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.Polyline
//
//class Project(
//    var name: String? = null,
//    var gapSize: Int? = null,
//    var meshSize:Double? = null,
//    var markedPoints: MutableList<LatLng>? = null ,
//    val basePoints:MutableList<LatLng>? = null,
//    var plantedLines:MutableList<Polyline>? = null
//)


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import java.util.*
import kotlin.math.sqrt

class Project : AppCompatActivity() {

    // Declaring sensorManager
    // and acceleration constants
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

//    val plantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
//    val l = plantingLine.tag as List<*>
//    var distance = 0.0F
//    //Get where you are and get corresponding Loaction
//    val myCurrentLocation =  marker?.center
//    val currentLocation = Location(LocationManager.GPS_PROVIDER)
//    currentLocation.latitude = myCurrentLocation!!.latitude
//    currentLocation.longitude = myCurrentLocation.longitude
//
//    val unMarkedPoints = mutableListOf<LatLng>()
//    val locationOfPoint = Location(LocationManager.GPS_PROVIDER)
//    l.forEach { loc ->
//        unMarkedPoints.add(loc as LatLng)
//        locationOfPoint.latitude = loc.latitude
//        locationOfPoint.longitude = loc.longitude
//        distance = currentLocation.distanceTo(locationOfPoint)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Getting the Sensor Manager instance
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            // Fetching x,y,z values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            // Getting current accelerations
            // with the help of fetched x,y,z values
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            // Display a Toast message if
            // acceleration value is over 12
            if (acceleration > 12) {
                Toast.makeText(applicationContext, "Shake event detected", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }
}
