package com.dsmagic.kibira


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.Location.distanceBetween
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import kotlinx.android.synthetic.main.activity_main.*
import org.checkerframework.checker.units.qual.degrees
import org.checkerframework.checker.units.qual.radians
import java.lang.Math.atan2
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.atan
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener,
    GoogleMap.InfoWindowAdapter, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {
    var deviceList = ArrayList<BluetoothDevice>()
    var device: BluetoothDevice? = null
    private var map: GoogleMap? = null
    private var marker: Circle? = null
    private var directionMarker: Marker? = null
    var tempListMarker = mutableListOf<Marker>()
    var lastLoc: Location? = null
    var zoomLevel = 21.0f
    var firstPoint: LongLat? = null
    var secondPoint: LongLat? = null
    val handler = Handler(Looper.getMainLooper())
    var meshDone = false
    var linesIndex = MutableS2ShapeIndex() // S2 index of lines...

    var pointsIndex = S2PointIndex<S2LatLng>()
    var polyLines = ArrayList<Polyline?>()

    //var asyncExecutor = Executors.newSingleThreadExecutor()
    var asyncExecutor = Executors.newCachedThreadPool()

    //var basePoints = mutableListOf<LongLat>()
    var currentLocation = mutableListOf<LatLng>()

    // Declaring sensorManager
    // and acceleration constants
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var switchedLines = false
    private var walkingMode = false
    private var markers: Marker? = null
    private var fabFlag = true
    private var zoomMode = false

    //lateinit var fab: FloatingActionButton
    private var ready = false    //flag fro direction marker
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //binding = ActivityMainBinding.inflate(layoutInflater)
        val title = findViewById<TextView>(R.id.title)
        val size = findViewById<TextView>(R.id.size)
        val dista = findViewById<TextView>(R.id.distance)


        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.appToolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Getting the Sensor Manager instance

        if (savedInstanceState != null) {

        } else {
            createDialog()
            val mapFragment =
                supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)


//        Objects.requireNonNull(sensorManager)
//            ?.registerListener(sensorListener,
//                sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_NORMAL)
//
//
//       val y = Objects.requireNonNull(sensorManager)?.registerListener(sensorListener,
//                sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
//                SensorManager.SENSOR_DELAY_NORMAL)

        sensorManager!!.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager!!.registerListener(listener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        if (magneticSensor != null) {

        } else {
            var campus = false

            Toast.makeText(applicationContext,
                "No Geomagnetic sensor, so some features have been disabled",
                Toast.LENGTH_LONG).show()
            //compass.isVisible = false
            Log.d("Supports", "doest not supports")
        }

        NmeaReader.listener.setLocationChangedTrigger(object : LocationChanged {
            override fun onLocationChanged(loc: Location) {

                if (map == null)
                    return // Not yet...
                val xloc = LatLng(loc.latitude, loc.longitude)

                if (marker == null) {
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(xloc, zoomLevel))
                    firstPoint = loc as LongLat // Grab it.
                    // basePoints.add(firstPoint!!)

                }
                // Get the displacement from the last position.
                val moved = NmeaReader.significantChange(lastLoc, loc)
                lastLoc = loc // Grab last location
                if (moved) { // If it has changed, move the thing...
                    marker?.remove()
                    directionMarker?.remove()

                    marker = map?.addCircle(
                        CircleOptions().center(xloc).fillColor(Color.GREEN).radius(0.5)
                            .strokeWidth(1.0f)

                    )
                    if (ready) {
                        directionMarker?.remove()
//                        directionMarker = map?.addMarker(MarkerOptions().position(xloc)
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.blackarrow1))
//
//                            .rotation(diff!!.toFloat())
//
//                        )
                    }
                    if (currentLocation.isNotEmpty()) {
                        currentLocation.clear()
                    }
                    marker?.let {
                        currentLocation.add(it.center)
                    }
                    val location = currentLocation[currentLocation.lastIndex]
                    distanceToPoint(location)

                    val runnableCode = object : Runnable {
                        override fun run() {
                            // headingDirection(location)
                            handler.postDelayed(this, 5000)
                        }
                    }
                    handler.postDelayed(runnableCode, 5000)

                }
                if (currentLocation.distinct().size == 1) {
                    plotFunc()
                }


//pulsing effect when line is switched
                if (switchedLines) {
                    val lineOfInterest = listOfPlantingLines[listOfPlantingLines.lastIndex]
                    val tempPoint = mutableListOf<LatLng>()
                    val r = lineOfInterest.tag as List<*>

                    if (currentLocation.isNotEmpty()) {

                        val cl = currentLocation[currentLocation.lastIndex]
                        val closeloc =
                            cl.let { S2Helper.findClosestPointOnLine(pointsIndex, it) } as S2LatLng?

                        val pt = closeloc?.let { LatLng(it.latDegrees(), closeloc.lngDegrees()) }
                        tempPoint.add(pt!!)
                        if (pt !in r) {
                            tempPoint.remove(pt)
                        }
                        if (pt in r) {
                            val LocationOfPointOfInterestOnPolyline =
                                Location(LocationManager.GPS_PROVIDER)

                            LocationOfPointOfInterestOnPolyline.latitude =
                                pt.latitude
                            LocationOfPointOfInterestOnPolyline.longitude =
                                pt.longitude

                            val locationOfRoverLatLng = Location(LocationManager.GPS_PROVIDER)

                            locationOfRoverLatLng.latitude = cl.latitude
                            locationOfRoverLatLng.longitude = cl.longitude
                            var distance = 0.0F
                            distance =
                                LocationOfPointOfInterestOnPolyline.distanceTo(locationOfRoverLatLng)

                            val d = 3
                            val half = d / 2
                            val halfhalf = half / 2
                            if (distance < half && distance > halfhalf) {
                                lineOfInterest.color = Color.YELLOW
                            } else {
                                if (distance < halfhalf || halfhalf == 0) {
                                    lineOfInterest.color = Color.GREEN
                                    tempPoint.clear()
                                    switchedLines = false
                                    handler.removeMessages(0)
                                    lineOfInterest.width = 3f
                                }
                            }


                        }
                    }

                }

                //controlling visibility given the zoom level on the map
                if (zoomMode) {
                    val zmlevel = map?.cameraPosition?.zoom
                    val maxZmLevel = map?.maxZoomLevel
                    val b = map?.cameraPosition?.bearing
                    Log.d("bearing", "$b")

                    var pt: LatLng? = null

                    if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
                        return
                    }
                    //get current line person is walking on, lat/lng and find closest point on line as they are moving
                    val currentPlantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
                    val roverPoint = currentLocation[currentLocation.lastIndex]
                    val l = currentPlantingLine.tag as List<*>
                    var tcpt = mutableListOf<LatLng>()
                    var plantingCircle: Circle? = null
                    var templistCircle = mutableListOf<Circle>()

                    val closestPoint =
                        S2Helper.findClosestPointOnLine(pointsIndex, roverPoint) as S2LatLng?

                    if (closestPoint != null) {
                        pt = LatLng(closestPoint.latDegrees(), closestPoint.lngDegrees())
                    }

                    if (zmlevel != null) {
                        if (zmlevel == maxZmLevel) {
                            //remove line and circles once at max zoom level
                            currentPlantingLine.isVisible = false
                            if (unmarkedCirclesList.isNotEmpty()) {
                                for (c in unmarkedCirclesList) {
                                    c.isVisible = false
                                }
                            }
                            if (pt in listOfMarkedPoints) {
                                return
                            }
                            if (pt !in l) {
                                return
                            } else {
                                tcpt.add(pt!!)
                                //if (pt in l) {
                                if (templistCircle.isNotEmpty()) {
                                    for (c in templistCircle) {
                                        if (c.center == pt) {
                                            return    //do nothing if a circle is already drawn at that point
                                        }
                                        if (c.center !in tcpt) {
                                            //removes the circle as one walks away from that point
                                            c.remove()

                                        }
                                    }

                                }
                                //}
                                // else {
                                plantingCircle = map?.addCircle(
                                    CircleOptions().center(pt).fillColor(Color.RED).radius(0.5)
                                        .strokeWidth(1.0f)
                                )
                                // }

                            }
                            if (tcpt.isNotEmpty()) {
                                tcpt.clear()
                            }

                            if (plantingCircle != null) {
                                if (templistCircle.isNotEmpty()) {
                                    for (c in templistCircle) {
                                        c.remove()
                                    }
                                    templistCircle.clear()
                                    templistCircle.add(plantingCircle)
                                } else {
                                    templistCircle.add((plantingCircle))
                                }
                            }

                        } else {
                            showLine()
                        }
                    }
                }
            }
        })

        scantBlueTooth()
        fab_map.setOnClickListener {
            try {
                if (!fabFlag) {

                    fab_map.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),
                        R.drawable.ic_baseline_map_24))
                    map?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    map?.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json))
                    for (item in polyLines) {
                        item!!.isVisible = false

                    }

                    val activePlantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
                    activePlantingLine.isVisible = true

                    var target = map?.cameraPosition?.target
                    var bearing = map?.cameraPosition?.bearing
                    val cameraPosition = CameraPosition.Builder()
                        .target(target!!)
                        .zoom(19f)
                        .bearing(bearing!!)
                        .tilt(45f)
                        .build()
                    map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    fabFlag = true

                } else if (fabFlag) {
                    fab_map.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),
                        R.drawable.walk_mode))
                    map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    for (c in polyLines) {
                        c?.isVisible = true
                    }
                    fabFlag = false

                }


            } catch (e: Exception) {

            }
        }


    }

    private fun showLine() {
        var currentPlantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
        currentPlantingLine.isVisible = true
        if (unmarkedCirclesList.isNotEmpty()) {
            for (c in unmarkedCirclesList) {
                c.isVisible = true
            }
        }
    }

//
//    var bDegrees: Double? = null
//    var bring: Int? = null

//    private fun headingDirection(location: LatLng) {
//        var currentLat = (location.latitude) / Math.PI * 180
//        var currentLong = (location.longitude) / Math.PI * 180
//
//        var prevLat = (0.000000) / Math.PI * 180
//        var prevLng = (0.000000) / Math.PI * 180
//        var lastRotateDegree = 0.0f
////        if (currentLong < 0) {
////            currentLong * -1
////        }
//
//        //diff between the longitudes
//        var deltaLong = (currentLong - prevLng)
//
//        var y = Math.sin(deltaLong) * Math.cos(currentLat)
//        var x =
//            Math.cos(prevLat) * Math.sin(currentLat) - Math.sin(prevLat) * Math.cos(currentLat) * Math.cos(
//                deltaLong)
//        var b = atan2(y, x)
//        bDegrees = (b * 180 / Math.PI + 360) % 360
//
//        prevLat = currentLat
//        prevLng = currentLong
//        Log.d("bearing", " plot me: $bDegrees ")
//
//        ready = true
//    }

    var bearing: Double? = null
    var diff: Float? = null
    var threshold: Float = 5.0f
//    private val listener: SensorEventListener = object : SensorEventListener {
//        var accelerometerValues = FloatArray(3)
//        var magneticValues = FloatArray(3)
//        private var lastRotateDegree = 0f
//        override fun onSensorChanged(event: SensorEvent) {
//            // Determine whether it is an acceleration sensor or a geomagnetic sensor
//            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//                // Pay attention to call the clone() method when assigning
//                accelerometerValues = event.values.clone()
//
//            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
//                magneticValues = event.values.clone()
//                //Toast.makeText(applicationContext, "Changed magnet", Toast.LENGTH_LONG).show()
//            }
//            val R = FloatArray(9)
//            val values = FloatArray(3)
//            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues)
//            SensorManager.getOrientation(R, values)
//            bearing = -Math.toDegrees(values[0].toDouble())
//            val rotateDegree = (-Math.toDegrees(values[0].toDouble())).toFloat()
//            diff = rotateDegree - lastRotateDegree
//            val bearingAngle = Math.abs(diff!!)
//            if (bearingAngle > 1) {
//
//                val animation = RotateAnimation(lastRotateDegree,
//                    rotateDegree,
//                    Animation.RELATIVE_TO_SELF,
//                    0.5f,
//                    Animation.RELATIVE_TO_SELF,
//                    0.5f)
//                animation.fillAfter = true
//                compass.startAnimation(animation)
//                lastRotateDegree = rotateDegree
//               // Log.d("rotation", "$diff and $lastRotateDegree")
//                //wait till lines are drawn then check closeness to line
//                if (zoomMode) {
//                    if (bearingAngle > threshold) {
//                        showLine()
//                        Toast.makeText(this@MainActivity, "Straying from line, zoom in to see line", Toast.LENGTH_LONG)
//                            .show()
//                        showLine()
//                    }
//                }
//
//
//            }
//        }
//
//        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
//    }

    //private var View row  = getLayoutInflater().inflate(R.layout.custom_info_window, null)
    //private val contents: View = layoutInflater.inflate(R.layout.custom_info_contents, null)
    private var myContentsView: View? = null

    fun MyInfoWindowAdapter() {


    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }

    override fun getInfoContents(p0: Marker): View? {
        myContentsView = layoutInflater.inflate(R.layout.custom_info_window, null)

        return myContentsView
    }

    private fun distanceToPoint(loc: LatLng) {
        if (walkingMode && listOfMarkedPoints.isNotEmpty()) {
            var line = listOfPlantingLines[listOfPlantingLines.lastIndex]
            var l = line.tag as List<*>
            var size = listOfMarkedPoints.size
            var pt: LatLng? = null
            var distance = 0.0f

            val runnableCode = object : Runnable {
                override fun run() {
                    val closeloc =
                        loc.let { S2Helper.findClosestPointOnLine(pointsIndex, it) } as S2LatLng?

                    pt = closeloc?.let { LatLng(it.latDegrees(), closeloc.lngDegrees()) }!!

                    if (pt !in l || pt in listOfMarkedPoints) {
                        return
                    }

                    val locationOfNextPoint = Location(LocationManager.GPS_PROVIDER)

                    locationOfNextPoint.latitude = pt!!.latitude
                    locationOfNextPoint.longitude = pt!!.longitude

                    val locationOfRoverLatLng = Location(LocationManager.FUSED_PROVIDER)

                    locationOfRoverLatLng.latitude = loc.latitude
                    locationOfRoverLatLng.longitude = loc.longitude

                    distance = locationOfNextPoint.distanceTo(locationOfRoverLatLng)

                    if (tempListMarker.isNotEmpty()) {
                        for (m in tempListMarker) {
                            if (m.position == pt) {
                                return    //do nothing if a marker is already drawn at that point
                            }
                        }

                    }

                    markers = map?.addMarker(MarkerOptions().position(pt!!)
                        .title("Marked Points: $size")
                        .snippet("distance to next point:" + "<" + "$distance")


                    )

                    tempListMarker.add(markers!!)
                    markers?.showInfoWindow()
                    handler.postDelayed(this, 2000)
                }
            }
            markers?.hideInfoWindow()
            handler.postDelayed(runnableCode, 2000)
        }

        markers?.showInfoWindow()
    }

    private fun scantBlueTooth() {
        val bluetoothAdaptor = BluetoothAdapter.getDefaultAdapter() ?: return

        if (!bluetoothAdaptor.isEnabled) {
            var enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBT)
            return
        }
        if (this.let {
                androidx.core.app.ActivityCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                )
            } != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // return
        }

        val l = ArrayList<String>()
        for (d in
        bluetoothAdaptor.bondedDevices) {
            l.add(d.name)
            deviceList.add(d)
        }

        val items = l.toArray()
        val adaptor =
            this.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_spinner_dropdown_item,
                    items
                )
            }


        spinner.adapter = adaptor
        spinner.onItemSelectedListener = this
        spinner.visibility = Spinner.VISIBLE
        Log.d("bt", "Bluetooth scan complete")
        buttonConnect.setOnClickListener(this)
    }


    // Display the menu layout
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appmenu, menu)
        return true
    }

    // display the create dialog if no projects available.
    private fun createDialog(): Boolean {
        val onAppOpen = firstActivity()
        onAppOpen.show(supportFragmentManager, "createDialog")
        return true
    }

    //Handling the options in the menu layout
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_create) {

            val createNewProject = CreateProjectDialog()
            createNewProject.show(supportFragmentManager, "create")

            return true
        } else if (item.itemId == R.id.action_view_projects) {
            val createNewProject = CreateProjectDialog()
            createNewProject.show(supportFragmentManager, "view_projects")

//        listProjects()
            return true
        } else if (item.itemId == R.id.bluetooth_spinner) {
            toggleWidgets()
            return true

        } else if (item.itemId == R.id.bluetooth_spinner) {

            showmap()

            return true
        } else {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun showmap() {


        map?.mapType = GoogleMap.MAP_TYPE_SATELLITE

        for (c in polyLines) {
            c?.isVisible = true
        }

    }

    private fun toggleWidgets() {
        //TODO REMOVE AND USE TOGGLE BUTTONS INSTEAD

        if (spinner.visibility == Spinner.INVISIBLE && buttonConnect.visibility == Button.INVISIBLE) {
            spinner.visibility = Spinner.VISIBLE
            buttonConnect.visibility = Button.VISIBLE
            scantBlueTooth()
        } else {
            spinner.visibility = Spinner.INVISIBLE
            buttonConnect.visibility = Button.INVISIBLE
        }

    }

    override fun onItemSelected(
        var1: AdapterView<*>?,
        view: View?,
        i: Int,
        l: Long,
    ) {
        device = deviceList[i]

        // Show connect button.
        buttonConnect.visibility = Button.VISIBLE // Shown
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

        buttonConnect.visibility = Button.INVISIBLE
        // Do nothing...
    }


    override fun onClick(v: View?) {

        if (device == null)
            return
        if (NmeaReader.readingStarted) {
            NmeaReader.stop()
        } else
            openBlueTooth()
    }

    private fun hideButton() {
        buttonConnect.visibility = Button.INVISIBLE
    }

    private fun openBlueTooth() {
        //progressBar.isVisible = true
        device?.let {
            // Load the map

            //showMap()
            this.let { it1 ->
                NmeaReader.start(it1, it)
            }

        }
        hideButton()
    }

    private val drawLine: (List<LongLat>) -> Unit = { it ->
        val ml = it.map {
            LatLng(
                it.getLatitude(),
                it.getLongitude()
            )
        } // Convert to LatLng as expected by polyline

        val poly = PolylineOptions().addAll(ml)
            .color(Color.GRAY)
            .jointType(JointType.BEVEL)
            .width(3f)
            .geodesic(true)
            .startCap(RoundCap())
            .endCap(SquareCap())
        handler.post {
            val p = map?.addPolyline(poly) // Add it and set the tag to the line...
            // Add it to the index
            val idx = polyLines.size
            S2Helper.addS2Polyline2Index(idx, linesIndex, S2Helper.makeS2PolyLine(ml, pointsIndex))
            // Add it to the list as well.
            polyLines.add(p)

            p?.tag = ml // Keep the latlng
            p?.isClickable = true

        }

    }

    private val onMapClick = GoogleMap.OnMapClickListener { loc ->
        val pBar = findViewById<ProgressBar>(R.id.progressBar)
        val pt = LongLat(loc.longitude, loc.latitude)
        if (firstPoint == null) { // Special case, no BT
            firstPoint = pt
            handler.post {
                marker = map?.addCircle(
                    CircleOptions().center(loc).fillColor(Color.YELLOW).radius(1.0)
                        .strokeWidth(1.0f)
                )
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPoint!!.getLatitude(),
                    firstPoint!!.getLongitude()), 21.0f))

            }
            return@OnMapClickListener
        }
        secondPoint = pt
        if (firstPoint == null || secondPoint == null || meshDone)
            return@OnMapClickListener

        var l = map?.addCircle(
            CircleOptions().center(loc).fillColor(Color.YELLOW).radius(0.5).strokeWidth(1.0f)
        )
        runOnUiThread {
            Toast.makeText(applicationContext,
                "Drawing grid! This won't take long..",
                Toast.LENGTH_LONG)
                .show()

            pBar.isVisible = true
        }
        //basePoints.add(secondPoint!!)

        asyncExecutor.execute {

            val c = Point(firstPoint!!)
            val p = Point(secondPoint!!)
            val lines = Geometry.generateMesh(c, p)
            Geometry.generateLongLat(c, lines, drawLine)
            meshDone = true

            runOnUiThread {
                pBar.isVisible = false
            }
            handler.post { // Centre it...
//
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPoint!!.getLatitude(),
                    firstPoint!!.getLongitude()), 21.0f))


            }

        }

        l?.remove()
        // marker?.remove()
    }

    var listOfMarkedPoints = mutableListOf<LatLng>()

    var unmarkedCirclesList = mutableListOf<Circle>()

    var listOfPlantingLines = mutableListOf<Polyline>()

    private val onPolyClick = GoogleMap.OnPolylineClickListener {
        it.isClickable = false

        if (listOfPlantingLines.isEmpty()) {
            listOfPlantingLines.add(it)
            it.color = Color.GREEN

            Toast.makeText(applicationContext, "Planting line selected...", Toast.LENGTH_LONG)
                .show()

        } else {

            val recentLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
            recentLine.color = Color.GRAY
            recentLine.isClickable = true
            recentLine.width =
                3f   //return to default width after pulse effect and stop the handler
            listOfPlantingLines.clear()
            listOfPlantingLines.add(it)
            it.color = Color.GREEN

            Toast.makeText(applicationContext, "Switching Lines..", Toast.LENGTH_LONG)
                .show()
            val runnableCode = object : Runnable {
                override fun run() {
                    var w = it.width;
                    w += 0.5f;
                    if (w > 13.0) {
                        w = 1.0f;
                    }
                    it.width = w;
                    handler.postDelayed(this, 50)
                }
            }

            handler.postDelayed(runnableCode, 50)   //enqueue the impulsing effect function

            //remove the planting radius circle if it exists
            if (templist.isNotEmpty()) {
                templist[templist.lastIndex].remove()
                templist.clear()
            }

            //remove any markers if they exist
            if (tempListMarker.isNotEmpty()) {
                tempListMarker[tempListMarker.lastIndex].remove()
                tempListMarker.clear()
            }
            switchedLines = true
        }

        //Give the process of drawing points on line a thread --- makes the process faster
        handler.post {
            for (item in polyLines) {
                item!!.isVisible = false
            }
            it.isVisible = true
            val l = it.tag as MutableList<*>

            plotLine(l)
            fab_map.show()
            zoomMode = true
        }

        handler.post {
            //changing map type lags, so handle it ina thread as well
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                map?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));

            } catch (e: java.lang.Exception) {

            }
            var target = map?.cameraPosition?.target
            var bearing = map?.cameraPosition?.bearing
            val cameraPosition = CameraPosition.Builder()
                .target(target!!)
                .zoom(21f)
                .bearing(bearing!!)
                .tilt(45f)
                .build()
            map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

    }

    private fun plotLine(line: MutableList<*>) {
        if (unmarkedCirclesList.isNotEmpty()) {
            unmarkedCirclesList.clear()

        }
        var lastp: LatLng? = null
        handler.post {
            for (loc in line) {
                val xloc = loc as LatLng
                // Draw the points...
                if (loc !in listOfMarkedPoints) {

                    val unmarkedCircles = map?.addCircle(
                        CircleOptions().center(xloc).fillColor(Color.RED).radius(0.5)
                            .strokeWidth(1.0f)
                        //if set to zero, no outline is drawn
                    )

                    unmarkedCirclesList.add(unmarkedCircles!!)

                    if (lastp != null) {
                        val res = floatArrayOf(0f)
                        distanceBetween(
                            lastp!!.latitude,
                            lastp!!.longitude,
                            xloc.latitude,
                            xloc.longitude,
                            res
                        )

                        Log.d("distance", "Distance from last point: ${res[0]}")

                        lastp = xloc

                    }

                } else {
                    map?.addCircle(
                        CircleOptions().center(xloc).fillColor(Color.YELLOW).radius(0.5)
                            .strokeWidth(1.0f)
                        //if set to zero, no outline is drawn
                    )
                }

            }

        }


    }

    var listOfPlantingRadius: Circle? = null
    val templist = mutableListOf<Circle>()
    val tempClosestPoint = mutableListOf<LatLng>()

    private fun plotFunc() {

        var plantingRadius: Circle? = null

        val roverPoint = currentLocation[currentLocation.lastIndex]

        if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
            return
        }
        val lineOfInterest = listOfPlantingLines[listOfPlantingLines.lastIndex]

        val l = lineOfInterest.tag as List<*>
        val xloc = S2Helper.findClosestPointOnLine(pointsIndex, roverPoint) as S2LatLng?

        if (xloc != null) {
            val pt = LatLng(xloc.latDegrees(), xloc.lngDegrees())

            if (pt !in l) {
                return
            }
            if (pt in listOfMarkedPoints) {
                return
            }
            tempClosestPoint.add(pt)
            if (pt in l) {
                if (templist.isNotEmpty()) {
                    for (c in templist) {
                        if (c.center == pt) {
                            return    //do nothing if a circle is already drawn at that point
                        }
                        if (c.center !in tempClosestPoint || c.center == markers?.position) {
                            //removes the planting radius circle as one walks away from that point
                            c.remove()
                            markers?.remove()

                        }
                    }

                }
            }
            // else {
            plantingRadius = map?.addCircle(
                CircleOptions().center(pt).fillColor(Color.GREEN).radius(radius(4))
                    .strokeWidth(1.0f)
                    .fillColor(0x22228B22)
                    .strokeColor(Color.GREEN)
                    .strokeWidth(1.0f)
            )
            // }

        }
        if (tempClosestPoint.isNotEmpty()) {
            tempClosestPoint.clear()
        }

        if (plantingRadius != null) {
            if (templist.isNotEmpty()) {
                for (c in templist) {
                    c.remove()
                }
                templist.clear()
                templist.add(plantingRadius)
            } else {
                templist.add((plantingRadius))
            }


            listOfPlantingRadius = templist[templist.lastIndex]

//            val temp = templist.distinct()
//            if (temp.size == 1) {
//                listOfPlantingRadius = temp[templist.lastIndex]
//            }

        }

    }


    // Marking off points on a planting line
    private val listener: SensorEventListener = object : SensorEventListener {
        var accelerometerValues = FloatArray(3)
        var magneticValues = FloatArray(3)
        private var lastRotateDegree = 0f

        override fun onSensorChanged(event: SensorEvent) {

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Pay attention to call the clone() method when assigning
                accelerometerValues = event.values.clone()

            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticValues = event.values.clone()
                //Toast.makeText(applicationContext, "Changed magnet", Toast.LENGTH_LONG).show()
            }
            val R = FloatArray(9)
            val values = FloatArray(3)
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues)
            SensorManager.getOrientation(R, values)
            bearing = -Math.toDegrees(values[0].toDouble())
            val rotateDegree = (-Math.toDegrees(values[0].toDouble())).toFloat()
            diff = rotateDegree - lastRotateDegree
            val bearingAngle = Math.abs(diff!!)

            if (bearingAngle > 1) {

                val animation = RotateAnimation(lastRotateDegree,
                    rotateDegree,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f)
                animation.fillAfter = true
                compass.startAnimation(animation)
                lastRotateDegree = rotateDegree
               // Log.d("rotation", "$diff and $lastRotateDegree")
                //wait till lines are drawn then check closeness to line
                if (zoomMode) {
                    if (bearingAngle > threshold) {
                        showLine()
                        Toast.makeText(this@MainActivity, "Straying from line, zoom in to see line", Toast.LENGTH_LONG)
                            .show()
                        showLine()
                    }
                }


            }

            // Fetching x,y,z values
            val x = accelerometerValues[0]
            val y = accelerometerValues[1]
            val z = accelerometerValues[2]

            lastAcceleration = currentAcceleration

            // Getting current accelerations
            // with the help of fetched x,y,z values
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            //   private val listener: SensorEventListener = object : SensorEventListener {
//        var accelerometerValues = FloatArray(3)
//        var magneticValues = FloatArray(3)
//        private var lastRotateDegree = 0f

//        override fun onSensorChanged(event: SensorEvent) {
//            // Determine whether it is an acceleration sensor or a geomagnetic sensor
//            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//                // Pay attention to call the clone() method when assigning
//                accelerometerValues = event.values.clone()
//
//            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
//                magneticValues = event.values.clone()
//                //Toast.makeText(applicationContext, "Changed magnet", Toast.LENGTH_LONG).show()
//            }
//            val R = FloatArray(9)
//            val values = FloatArray(3)
//            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues)
//            SensorManager.getOrientation(R, values)
//            bearing = -Math.toDegrees(values[0].toDouble())
//            val rotateDegree = (-Math.toDegrees(values[0].toDouble())).toFloat()
//            diff = rotateDegree - lastRotateDegree
//            val bearingAngle = Math.abs(diff!!)
//            if (bearingAngle > 1) {
//
//                val animation = RotateAnimation(lastRotateDegree,
//                    rotateDegree,
//                    Animation.RELATIVE_TO_SELF,
//                    0.5f,
//                    Animation.RELATIVE_TO_SELF,
//                    0.5f)
//                animation.fillAfter = true
//                compass.startAnimation(animation)
//                lastRotateDegree = rotateDegree
//               // Log.d("rotation", "$diff and $lastRotateDegree")
//                //wait till lines are drawn then check closeness to line
//                if (zoomMode) {
//                    if (bearingAngle > threshold) {
//                        showLine()
//                        Toast.makeText(this@MainActivity, "Straying from line, zoom in to see line", Toast.LENGTH_LONG)
//                            .show()
//                        showLine()
//                    }
//                }
//
//
//            }
//        }
//
//        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
//    }

            if (acceleration > 10) {
                val plantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]

                val l = plantingLine.tag as MutableList<*>
                var pointOfInterestOnPolyline: LatLng? = null

                l.forEach { loc ->
                    loc as LatLng
                    if (loc == listOfPlantingRadius?.center)
                        pointOfInterestOnPolyline = loc

                }
                if (pointOfInterestOnPolyline == null) {

                    Toast.makeText(applicationContext, "Can't find point to mark" +
                            "", Toast.LENGTH_SHORT).show()
                }

                if (currentLocation.isNotEmpty()) {
                    val cl = currentLocation[currentLocation.lastIndex]

                    val LocationOfPointOfInterestOnPolyline =
                        Location(LocationManager.GPS_PROVIDER)

                    LocationOfPointOfInterestOnPolyline.latitude =
                        pointOfInterestOnPolyline!!.latitude
                    LocationOfPointOfInterestOnPolyline.longitude =
                        pointOfInterestOnPolyline!!.longitude

                    val locationOfRoverLatLng = Location(LocationManager.GPS_PROVIDER)

                    locationOfRoverLatLng.latitude = cl.latitude
                    locationOfRoverLatLng.longitude = cl.longitude
                    var distance = 0.0F
                    distance =
                        LocationOfPointOfInterestOnPolyline.distanceTo(locationOfRoverLatLng)

                    if (distance > listOfPlantingRadius?.radius!!) {

                        Toast.makeText(applicationContext, "Outside planting zone" +
                                "", Toast.LENGTH_SHORT).show()

                    } else {

                        val markedCirclePoint = map?.addCircle(
                            CircleOptions().center(listOfPlantingRadius?.center!!)
                                .fillColor(Color.YELLOW)
                                .radius(0.5)
                                .strokeWidth(1.0f)
                        )
                        if (markedCirclePoint!!.center in listOfMarkedPoints) {

                            return
                        }

                        listOfMarkedPoints.add(markedCirclePoint.center)

                        var color = markedCirclePoint.fillColor
                        if (color != Color.YELLOW) {
                            Toast.makeText(this@MainActivity,
                                "Was not yellow",
                                Toast.LENGTH_LONG).show()
                            map?.addCircle(
                                CircleOptions().center(markedCirclePoint.center)
                                    .fillColor(Color.YELLOW)
                                    .radius(0.5)
                                    .strokeWidth(1.0f)
                            )
                        }

                        if (markedCirclePoint.center == listOfPlantingRadius!!.center) {

                            listOfPlantingRadius?.remove()
                            if (markers != null) {
                                markers!!.remove()

                            }
                            if (tempListMarker.isNotEmpty()) {
                                tempListMarker.clear()
                            }
                            Toast.makeText(applicationContext, "Point Marked " +
                                    "", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(applicationContext, "ERROR " +
                                    "", Toast.LENGTH_SHORT).show()
                        }
                        if (templist.isNotEmpty()) {
                            templist.clear()
                        }
                        if (tempClosestPoint.isNotEmpty()) {
                            tempClosestPoint.clear()
                        }

                        walkingMode = true

                    }
                }

            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        sensorManager?.registerListener(listener, sensorManager!!.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(listener)
        super.onPause()
    }


    fun radius(size: Int): Double {
        val sizeInCentimeters = size * 100
        return ((0.1 * sizeInCentimeters) / 100) + 1.0
    }

//    private val onLongMapPress = GoogleMap.OnMapLongClickListener {
//        if (polyLines.size == 0)
//            return@OnMapLongClickListener // Not yet...
//
//        map?.addMarker(MarkerOptions().title("Landing").position(it)
//
//        )
//
//
//        val loc = S2Helper.makeS2PointFromLngLat(it) // Get the point
//        val p = S2Helper.findClosestLine(linesIndex, it, polyLines)
//        Log.d("closest", "Closest l" +
//                "ine found, will look for closest point!")
//        if (p != null) {
//            (p as Polyline).color = Color.CYAN // change its colour..
//        }
//
//        val xloc = S2Helper.findClosestPointOnLine(pointsIndex, it) as S2LatLng?
//        if (xloc != null) {
//            val pt = LatLng(xloc.latDegrees(), xloc.lngDegrees())
//            // Draw the point on the line...
//            map?.addCircle(
//                CircleOptions().center(pt).fillColor(Color.CYAN).radius(0.5)
//                    .strokeWidth(1.0f)
//
//            )
//
//            Log.d("closest", "Closest point drawn")
//        }
//    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        googleMap.setLocationSource(NmeaReader.listener)
        googleMap.setOnMapClickListener(onMapClick)

        googleMap.setOnPolylineClickListener(onPolyClick)
        //googleMap.setOnCircleClickListener(onClickingPoint)
        // googleMap.setOnMapLongClickListener(onLongMapPress)
        googleMap.isMyLocationEnabled = true
        val isl = LatLng(-.366044, 32.441599) // LatLng(0.0,32.44) //
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.addMarker(MarkerOptions().position(isl).title("Marker in N Residence"))
//
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(isl, 21.0f))

        val fab = findViewById<FloatingActionButton>(R.id.fab_map)
        fab.hide()

    }


    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }
}