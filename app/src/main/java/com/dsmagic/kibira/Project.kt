//package com.dsmagic.kibira
//
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.content.Intent
//import android.graphics.Color
//import android.location.Location
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.view.View
//import android.widget.AdapterView
//import android.widget.ArrayAdapter
//import android.widget.Button
//import android.widget.Spinner
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.*
//import dilivia.s2.S2LatLng
//import dilivia.s2.index.point.S2PointIndex
//import dilivia.s2.index.shape.MutableS2ShapeIndex
//import kotlinx.android.synthetic.main.activity_main.*
//import java.util.concurrent.Executors
//
//class Project : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {
//    var deviceList = ArrayList<BluetoothDevice>()
//    var device: BluetoothDevice? = null
//    private var map: GoogleMap? = null
//    private var marker: Circle? = null
//    var lastLoc: Location? = null
//    var zoomLevel = 30.0f
//    var firstPoint: LongLat? = null
//    var secondPoint: LongLat? = null
//    val handler = Handler(Looper.getMainLooper())
//    var meshDone = false
//    var linesIndex = MutableS2ShapeIndex() // S2 index of lines...
//
//    var pointsIndex = S2PointIndex<S2LatLng>()
//    var polyLines = ArrayList<Polyline?>()
//    var asyncExecutor = Executors.newSingleThreadExecutor()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        //   binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_main)
//
//        val mapFragment =
//            supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
//        mapFragment?.getMapAsync(callback)
//
//        // Set callback
//        NmeaReader.listener.setLocationChangedTrigger(object : LocationChanged {
//            override fun onLocationChanged(loc: Location) {
//                if (map == null)
//                    return // Not yet...
//                val xloc = LatLng(loc.latitude, loc.longitude)
//
//                if (marker == null) {
//                    Log.d("Location", "First Location $loc!")
//                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(xloc, zoomLevel))
//                    firstPoint = loc as LongLat // Grab it.
//                }
//                // Get the displacement from the last position.
//                val moved = NmeaReader.significantChange(lastLoc, loc)
//                lastLoc = loc // Grab last location
//                if (moved) { // If it has changed, move the thing...
//                    // Log.d("Location","Location ${loc.latitude}, ${loc.longitude}")
//                    marker?.remove()
//                    marker = map?.addCircle(
//                        CircleOptions().center(xloc).fillColor(Color.YELLOW).radius(1.0)
//                            .strokeWidth(1.0f)
//                    )
//                }
//            }
//        })
//        scantBlueTooth()
//    }
//
//
//    private fun scantBlueTooth() {
//        val bluetoothAdaptor = BluetoothAdapter.getDefaultAdapter() ?: return
//
//        if (!bluetoothAdaptor.isEnabled) {
//            var enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivity(enableBT)
//            return
//        }
//        if (this.let {
//                androidx.core.app.ActivityCompat.checkSelfPermission(
//                    it,
//                    android.Manifest.permission.BLUETOOTH_CONNECT
//                )
//            } != android.content.pm.PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            // return
//        }
//
//        val l = ArrayList<String>()
//        for (d in
//        bluetoothAdaptor.bondedDevices) {
//            l.add(d.name)
//            deviceList.add(d)
//        }
//
//        val items = l.toArray()
//        val adaptor =
//            this.let {
//                ArrayAdapter(
//                    it,
//                    android.R.layout.simple_spinner_dropdown_item,
//                    items
//                )
//            }
//
//
//        spinner.adapter = adaptor
//        spinner.onItemSelectedListener = this
//        spinner.visibility = Spinner.VISIBLE
//        Log.d("bt", "Bluetooth scan complete")
//        buttonConnect.setOnClickListener(this)
//    }
//
//    override fun onItemSelected(
//        var1: AdapterView<*>?,
//        view: View?,
//        i: Int,
//        l: Long
//    ) {
//        device = deviceList.get(i)
//
//        // Show connect button.
//        buttonConnect.visibility = Button.VISIBLE // Shown
//    }
//
//    override fun onNothingSelected(p0: AdapterView<*>?) {
//        buttonConnect.visibility = Button.INVISIBLE
//        // Do nothing...
//    }
//
//    override fun onClick(v: View?) {
//        if (device == null)
//            return
//        if (NmeaReader.readingStarted) {
//            NmeaReader.stop()
//        } else
//            openBlueTooth()
//    }
//
//    private fun openBlueTooth() {
//        device?.let {
//            // Load the map
//
//            //showMap()
//            this.let { it1 ->
//                NmeaReader.start(it1, it)
//                Log.d("bt", "Bluetooth read started")
//            }
//
//        }
//    }
//
//
//    private val drawLine: (List<LongLat>) -> Unit = { it ->
//        val ml = it.map {
//            LatLng(
//                it.getLatitude(),
//                it.getLongitude()
//            )
//        } // Convert to LatLng as expected by polyline
//        val poly = PolylineOptions().addAll(ml)
//            .color(Color.RED)
//            .jointType(JointType.ROUND)
//            .width(3f)
//            .geodesic(true)
//            .startCap(RoundCap())
//            .endCap(SquareCap())
//
//        handler.post {
//            val p = map?.addPolyline(poly) // Add it and set the tag to the line...
//            // Add it to the index
//            val idx = polyLines.size
//            S2Helper.addS2Polyline2Index(idx, linesIndex, S2Helper.makeS2PolyLine(ml, pointsIndex))
//            // Add it to the list as well.
//            polyLines.add(p)
//
//            p?.tag = ml // Keep the latlng
//            p?.isClickable = true
//        }
//    }
//
//    private val onMapClick = GoogleMap.OnMapClickListener { loc ->
//        val pt = LongLat(loc.longitude, loc.latitude)
//        if (firstPoint == null) { // Special case, no BT
//            firstPoint = pt
//            handler.post {
//                marker = map?.addCircle(
//                    CircleOptions().center(loc).fillColor(Color.YELLOW).radius(1.0)
//                        .strokeWidth(1.0f)
//                )
//                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPoint!!.getLatitude(),firstPoint!!.getLongitude()), 20.0f))
//            }
//            return@OnMapClickListener
//        }
//        secondPoint = pt
//        if (firstPoint == null || secondPoint == null || meshDone)
//            return@OnMapClickListener
//
//        map?.addCircle(
//            CircleOptions().center(loc).fillColor(Color.YELLOW).radius(1.0).strokeWidth(1.0f)
//        )
//        asyncExecutor.execute {
//            val c = Point(firstPoint!!)
//            val p = Point(secondPoint!!)
//            val lines = Geometry.generateMesh(c, p)
//            Geometry.generateLongLat(c, lines, drawLine)
//            meshDone = true
//            handler.post { // Centre it...
//                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPoint!!.getLatitude(),firstPoint!!.getLongitude()), 20.0f))
//            }
//        }
//    }
//    private val onPolyClick = GoogleMap.OnPolylineClickListener {
//        val l = it.tag as List<*>
//        var lastp: LatLng? = null
//        for (loc in l) {
//            var xloc = loc as LatLng
//            // Draw the points...
//            map?.addCircle(
//                CircleOptions().center(loc).fillColor(Color.RED).radius(0.5)
//                    .strokeWidth(1.0f)
//            )
//            if (lastp != null) {
//                val res = floatArrayOf(0f)
//                Location.distanceBetween(
//                    lastp.latitude,
//                    lastp.longitude,
//                    xloc.latitude,
//                    xloc.longitude,
//                    res
//                )
//
//                Log.d("distance", "Distance from last point: ${res[0]}")
//            }
//            lastp = xloc
//        }
//        it.tag = null
//        Log.d("polylines", "Added points to line...")
//    }
//    private val onLongMapPress = GoogleMap.OnMapLongClickListener {
//        if (polyLines.size == 0)
//            return@OnMapLongClickListener // Not yet...
//
//        map?.addMarker(MarkerOptions().title("Landing").position(it))
//
//        //  val loc =  S2Helper.makeS2PointFromLngLat( it) // Get the point
//        val p = S2Helper.findClosestLine(linesIndex, it, polyLines)
//        Log.d("closest", "Closest line found, will look for closest point!")
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
//            Log.d("closest", "Closest point drawn")
//        }
//    }
//
//    private val callback = OnMapReadyCallback { googleMap ->
//        map = googleMap
//        googleMap.setLocationSource(NmeaReader.listener)
//        googleMap.setOnMapClickListener(onMapClick)
//        googleMap.setOnPolylineClickListener(onPolyClick)
//
//        googleMap.setOnMapLongClickListener(onLongMapPress)
//
//
//        val isl = LatLng(-.366044, 32.441599) // LatLng(0.0,32.44) //
//        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
//        googleMap.addMarker(MarkerOptions().position(isl).title("Marker in N Residence"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(isl, 20.0f))
//
//    }
//}
//
//
//
////package com.dsmagic.kibira
////
//////
//////import com.google.android.gms.maps.model.LatLng
//////import com.google.android.gms.maps.model.Polyline
//////
//////class Project(
//////    var name: String? = null,
//////    var gapSize: Int? = null,
//////    var meshSize:Double? = null,
//////    var markedPoints: MutableList<LatLng>? = null ,
//////    val basePoints:MutableList<LatLng>? = null,
//////    var plantedLines:MutableList<Polyline>? = null
//////)
////
////
////import android.content.Context
////import android.hardware.Sensor
////import android.hardware.SensorEvent
////import android.hardware.SensorEventListener
////import android.hardware.SensorManager
////import androidx.appcompat.app.AppCompatActivity
////import android.os.Bundle
////import android.widget.Toast
////import java.util.*
////import kotlin.math.sqrt
////
////class Project : AppCompatActivity() {
////
////    // Declaring sensorManager
////    // and acceleration constants
////    private var sensorManager: SensorManager? = null
////    private var acceleration = 0f
////    private var currentAcceleration = 0f
////    private var lastAcceleration = 0f
////
//////    val plantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
//////    val l = plantingLine.tag as List<*>
//////    var distance = 0.0F
//////    //Get where you are and get corresponding Loaction
//////    val myCurrentLocation =  marker?.center
//////    val currentLocation = Location(LocationManager.GPS_PROVIDER)
//////    currentLocation.latitude = myCurrentLocation!!.latitude
//////    currentLocation.longitude = myCurrentLocation.longitude
//////
//////    val unMarkedPoints = mutableListOf<LatLng>()
//////    val locationOfPoint = Location(LocationManager.GPS_PROVIDER)
//////    l.forEach { loc ->
//////        unMarkedPoints.add(loc as LatLng)
//////        locationOfPoint.latitude = loc.latitude
//////        locationOfPoint.longitude = loc.longitude
//////        distance = currentLocation.distanceTo(locationOfPoint)
//////    }
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_main)
////
////        // Getting the Sensor Manager instance
////        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
////
////        Objects.requireNonNull(sensorManager)!!
////            .registerListener(sensorListener, sensorManager!!
////                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
////
////        acceleration = 10f
////        currentAcceleration = SensorManager.GRAVITY_EARTH
////        lastAcceleration = SensorManager.GRAVITY_EARTH
////    }
////
////    private val sensorListener: SensorEventListener = object : SensorEventListener {
////        override fun onSensorChanged(event: SensorEvent) {
////
////            // Fetching x,y,z values
////            val x = event.values[0]
////            val y = event.values[1]
////            val z = event.values[2]
////            lastAcceleration = currentAcceleration
////
////            // Getting current accelerations
////            // with the help of fetched x,y,z values
////            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
////            val delta: Float = currentAcceleration - lastAcceleration
////            acceleration = acceleration * 0.9f + delta
////
////            // Display a Toast message if
////            // acceleration value is over 12
////            if (acceleration > 12) {
////                Toast.makeText(applicationContext, "Shake event detected", Toast.LENGTH_SHORT).show()
////            }
////        }
////        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
////    }
////
////    override fun onResume() {
////        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
////            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
////        )
////        super.onResume()
////    }
////
////    override fun onPause() {
////        sensorManager!!.unregisterListener(sensorListener)
////        super.onPause()
////    }
////}
//////Log.d("plot","$roverLatLng" + "$radius")
////
//////                var distance = 0.0F
//////Get where you are and get corresponding Loaction
//////                val myCurrentLocation = marker?.center
//////                val currentLocation = Location(LocationManager.GPS_PROVIDER)
//////
//////                currentLocation.latitude = myCurrentLocation!!.latitude
//////                currentLocation.longitude = myCurrentLocation.longitude
//////
//////                val unMarkedPoints = mutableListOf<LatLng>()
//////                l.forEach { loc ->
//////                    unMarkedPoints.add(loc as LatLng)
//////
////////                    map?.addCircle(
////////                        CircleOptions().center(loc).fillColor(Color.RED)
////////                            .radius(0.5)
////////                            .strokeWidth(1.0f)
////////                    )
//////                }
//////                val locationOfPoint = Location(LocationManager.GPS_PROVIDER)
//////
//////                l.forEach { loc ->
//////                    loc as LatLng
//////                    if (myCurrentLocation == loc) {
//////                        Log.d("yes", "equal")
//////                    }
//////                    locationOfPoint.latitude = loc.latitude
//////                    locationOfPoint.longitude = loc.longitude
//////
//////                    distance = locationOfPoint.distanceTo(currentLocation)
//////                    if (distance < 295) {
//////                        val indexs = unMarkedPoints.indexOf(loc)
//////                        Toast.makeText(applicationContext, "Point found" +
//////                                "$indexs", Toast.LENGTH_SHORT).show()
//////                        map?.addCircle(
//////                            CircleOptions().center(loc).fillColor(Color.YELLOW)
//////                                .radius(0.5)
//////                                .strokeWidth(1.0f)
//////                        )
//////                        return
//////
//////                    } else {
//////                        Toast.makeText(applicationContext, "outside" +
//////                                "", Toast.LENGTH_SHORT).show()
//////                    }
//////
////////                    Log.d("distance","${radius(4)}")
////////
////////
////////
////////                            return
//////
//////
//////                }
////
////
//////                val plantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
//////                val l = plantingLine.tag as List<*>
//////
//////                //val listOfCords = plantingLine.tag
//////                val unMarkedPoints = mutableListOf<LatLng>()
//////                l.forEach { loc ->
//////                    unMarkedPoints.add(loc as LatLng)
//////                }
//////                if (listOfMarkedPoints.isEmpty()) {
//////                    Toast.makeText(applicationContext,
//////                        "Select a starting Point first by clicking a circle on line " ,
//////                        Toast.LENGTH_SHORT).show()
//////                }
//////                else
//////                {
//////                    val startingPoint =
//////                        listOfMarkedPoints[listOfMarkedPoints.lastIndex]   //point clicked on the polyline
//////
//////                    unMarkedPoints.forEach { uloc ->
//////                        if (uloc == startingPoint) {
//////                             index = unMarkedPoints.indexOf(uloc)
//////
////////                            currentLatLng =
////////                                unMarkedPoints[nextIndex]  // Exactly where we are on the polyline
//////                        }
//////                    }
//////                    loopIndex = index + 1
//////                    currentLatLng = unMarkedPoints[loopIndex]
//////                  var myCurrentLocation =  marker?.center
//////                    Log.d("lcurrent","$myCurrentLocation")
//////
//////                    val temp = Location(LocationManager.GPS_PROVIDER)
//////                    temp.latitude = currentLatLng.latitude
//////                    temp.longitude = currentLatLng.longitude
//////
//////                    Log.d("points", "$l")
//////                    // var index = l[++i]
//////                    var distance = 0.0F
//////                    currentLocation.forEach { loc ->
//////                        val current = Location(LocationManager.GPS_PROVIDER)
//////                        current.latitude = loc.latitude
//////                        current.longitude = loc.longitude
//////                        distance = temp.distanceTo(current)
//////                    }
//////                    if (distance > radius(4)) {
//////
//////                        Toast.makeText(applicationContext, "Outside planting zone" +
//////                                "", Toast.LENGTH_SHORT).show()
//////
//////                    } else {
//////
//////                        Toast.makeText(applicationContext, "Point Marked " +
//////                                "", Toast.LENGTH_SHORT).show()
//////                        val circlePoint = map?.addCircle(
//////                            CircleOptions().center(currentLatLng).fillColor(Color.YELLOW)
//////                                .radius(0.5)
//////                                .strokeWidth(1.0f)
//////                        )
//////
//////                        if (circlePoint != null) {
//////
//////                            listOfMarkedPoints.add(circlePoint.center)
//////                            plantingTolerance(circlePoint, unMarkedPoints)
//////
//////
//////                            for (greenCircle in listOfMarkedCircles) {
//////                                if (greenCircle.center == circlePoint.center) {
//////                                    greenCircle.remove()
//////                                }
//////                            }
//////                            Toast.makeText(applicationContext, "Point Marked " +
//////                                    "", Toast.LENGTH_SHORT).show()
//////
//////                        }
//////
//////                    }
//////                }