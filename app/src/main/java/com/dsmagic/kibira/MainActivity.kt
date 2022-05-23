package com.dsmagic.kibira

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {
    var deviceList = ArrayList<BluetoothDevice>()
    var device: BluetoothDevice? = null
    private var map: GoogleMap? = null
    private var marker: Circle? = null

    var lastLoc: Location? = null
    var zoomLevel = 30.0f
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //   binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.appToolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Getting the Sensor Manager instance
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH


        if (savedInstanceState != null) {

        } else {
            createDialog()
            val mapFragment =
                supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
        }

        // Set callback
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
                    // Log.d("Location","Location ${loc.latitude}, ${loc.longitude}")
                    marker?.remove()
                    marker = map?.addCircle(
                        CircleOptions().center(xloc).fillColor(Color.BLUE).radius(0.5)
                            .strokeWidth(1.0f)
                    )
                    if (currentLocation.isNotEmpty()) {
                        currentLocation.clear()
                    }
                    marker?.let { currentLocation.add(it.center) }
                    if (currentLocation.distinct().size == 1) {
                        plotFunc()
                    }

                }
            }
        })

        scantBlueTooth()
    }
    val l = ArrayList<String>()
   private fun discover(){

      // val pBar = findViewById<ProgressBar>(R.id.progressBar)
       val bluetoothAdaptor = BluetoothAdapter.getDefaultAdapter() ?: return
       if (ActivityCompat.checkSelfPermission(
               this,
               Manifest.permission.BLUETOOTH_SCAN
           ) != PackageManager.PERMISSION_GRANTED
       ) {
           // TODO: Consider calling
           //    ActivityCompat#requestPermissions
           // here to request the missing permissions, and then overriding
           //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
           //                                          int[] grantResults)
           // to handle the case where the user grants the permission. See the documentation
           // for ActivityCompat#requestPermissions for more details.
           //return
       }
       if(bluetoothAdaptor.isDiscovering){
            bluetoothAdaptor.cancelDiscovery()

            bluetoothAdaptor.startDiscovery()
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
           Toast.makeText(this,"Scanning for devices",Toast.LENGTH_LONG).show()
        }
        else{
            bluetoothAdaptor.startDiscovery()
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
            Toast.makeText(this,"Scanning for Available devices ",Toast.LENGTH_LONG).show()
            }
    }
//    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val bluetoothAdaptor = BluetoothAdapter.getDefaultAdapter() ?: return
//            val pBar = findViewById<ProgressBar>(R.id.progressBar)
            val action: String? = intent.action
            when (action) {
               BluetoothDevice.ACTION_FOUND -> {
//                    // Discovery has found a device. Get the BluetoothDevice
//                    // object and its info from the Intent.
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                   if (ActivityCompat.checkSelfPermission(applicationContext,Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                       // TODO: Consider calling
                       //    ActivityCompat#requestPermissions
                       // here to request the missing permissions, and then overriding
                       //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                       //                                          int[] grantResults)
                       // to handle the case where the user grants the permission. See the documentation
                       // for ActivityCompat#requestPermissions for more details.
                       //return
                   }

                      val name = device.name

                       l.add(name)
                       deviceList.add(device)
                       Toast.makeText(applicationContext,"Found a device to connect to ${device.name}",Toast.LENGTH_LONG).show()
                   bluetoothAdaptor.cancelDiscovery()


//
                   }
//                        device.name
//
//                  if(device.name in l){
//                      return
//                  }else{
//                      l.add(device.name)
//                      deviceList.add(device)
//                      pBar.isVisible = false
//                      Toast.makeText(applicationContext,"Found a device",Toast.LENGTH_LONG).show()
//                  }
//
//                    //bluetoothAdaptor.cancelDiscovery()
//                    val deviceHardwareAddress = device.address // MAC address
//
//                }
            }
        }
   }

    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
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

//        val l = ArrayList<String>()
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

        } else if (item.itemId == R.id.scan) {
       discover()

            return true
        } else {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

//    private val sharedPrefFile = "kibirasharedfile"
//    override fun onPostResume() {
//
//        super.onPostResume()
//
//       // var displayProjectName = findViewById<TextView>(R.id.display_project_name)
//        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
//        val saved_project_name: String? = sharedPreferences.getString("name_key", "defaultValue")
//        val saved_gap_size: Int = sharedPreferences.getInt("gap_size", 0)
//
//        // displayProjectName?.text = saved_project_name
//
//        Log.d("valuesMain", "saved data $saved_project_name")
//
//        //overridePendingTransition(0, 0)
//
//        val db = DBHelper(this, null)
//        if (saved_project_name != null) {
//            with(db) {
//                addProject(saved_project_name, saved_gap_size)
//            }
//            //Toast.makeText(this, "Project $saved_project_name created", Toast.LENGTH_LONG).show()
////            val refresh = Intent(this,MainActivity::class.java)
////            finish()
////            startActivity(refresh)
//        } else {
//            Toast.makeText(this, "Project not created", Toast.LENGTH_LONG).show()
//        }
//
//    }

//
//    fun listProjects() {
//        try {
//
//            //val obj: JSONObject = JSONObject(str)
//           // val names: JSONObject = obj.getJSONObject("projects")
//
//            val name = names.getString("name")
//
//            // set employee name and salary in TextView's
//            display_project_name.setText("Name: $name");
//
//
//        } catch (e: JSONException) {
//            throw RuntimeException(e)
//        }
//    }

    private fun toggleWidgets() {

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
            .jointType(JointType.ROUND)
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
                    firstPoint!!.getLongitude()), 60.0f))
//                runOnUiThread {
//                    pBar.isVisible = true
//                }
            }
            return@OnMapClickListener
        }
        secondPoint = pt
        if (firstPoint == null || secondPoint == null || meshDone)
            return@OnMapClickListener

        var l = map?.addCircle(
            CircleOptions().center(loc).fillColor(Color.YELLOW).radius(0.5).strokeWidth(1.0f)
        )
        //basePoints.add(secondPoint!!)

        asyncExecutor.execute {

            val c = Point(firstPoint!!)
            val p = Point(secondPoint!!)
            val lines = Geometry.generateMesh(c, p)
            Geometry.generateLongLat(c, lines, drawLine)
            meshDone = true

            runOnUiThread {
                pBar.isVisible = true
            }
            handler.post { // Centre it...
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPoint!!.getLatitude(),
                    firstPoint!!.getLongitude()), 60.0f))

            }

        }

        l?.remove()
        // marker?.remove()
    }

    var listOfMarkedPoints = mutableListOf<LatLng>()

    var unmarkedCirclesList = mutableListOf<Circle>()

    var listOfPlantingLines = mutableListOf<Polyline>()

    private val onPolyClick = GoogleMap.OnPolylineClickListener {
        //val pBar = findViewById<ProgressBar>(R.id.progressBar)
        it.isClickable = false

        if (listOfPlantingLines.isEmpty()) {
            listOfPlantingLines.add(it)

            it.color = Color.GREEN
            Toast.makeText(applicationContext, "Planting line selected...", Toast.LENGTH_LONG)
                .show()

        } else {

            val recentLineIndex = listOfPlantingLines.lastIndex
            val recentLine = listOfPlantingLines[recentLineIndex]
            recentLine.color = Color.GRAY
            recentLine.isClickable = true
            recentLine.tag = null
            listOfPlantingLines.add(it)
            Toast.makeText(this, "Switching PLanting line...", Toast.LENGTH_LONG).show()
            it.color = Color.GREEN

//            if (tempClosestPoint.isNotEmpty() || templist.isNotEmpty() || currentLocation.isNotEmpty()) {
//                tempClosestPoint.clear()
//                templist.clear()
//                currentLocation.clear()
//            }

        }
        val l = it.tag as List<*>
        var lastp: LatLng? = null

        for (loc in l) {
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
                        lastp.latitude,
                        lastp.longitude,
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

        var listOfPlantingRadius: Circle? = null
        val templist = mutableListOf<Circle>()
        val tempClosestPoint = mutableListOf<LatLng>()

        private fun plotFunc() {
            if (tempClosestPoint.isNotEmpty()) {
                tempClosestPoint.clear()
            }

            var plantingRadius: Circle? = null

            val roverPoint = currentLocation[currentLocation.lastIndex]

            if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
                return
            }
            val lineOfInterest = listOfPlantingLines[listOfPlantingLines.lastIndex]

            val l = lineOfInterest.tag as List<*>

            val xloc =
                roverPoint.let { S2Helper.findClosestPointOnLine(pointsIndex, it) } as S2LatLng?

            val pt = xloc?.let { LatLng(it.latDegrees(), xloc.lngDegrees()) }

            // if (tempClosestPoint.size <= 1) {
            tempClosestPoint.add(pt!!)
            Log.d("closest", "$tempClosestPoint")
            //}
            if (pt !in l) {
                return
            }
            if (tempClosestPoint.distinct().size == 1 && pt in l) {

                if (pt in listOfMarkedPoints) {
                    return
                }
                if (templist.isNotEmpty()) {
                    for (c in templist) {
                        if (c.center == pt) {
                            return
                        }
                    }
                } else {
                    plantingRadius = map?.addCircle(
                        CircleOptions().center(pt!!).fillColor(Color.GREEN).radius(radius(4))
                            .strokeWidth(1.0f)
                            .fillColor(0x22228B22)
                            .strokeColor(Color.GREEN)
                            .strokeWidth(1.0f)
                    )
                }


            }
            if (plantingRadius != null) {

                templist.add(plantingRadius)

                val temp = templist.distinct()
                if (templist.distinct().size == 1) {
                    listOfPlantingRadius = temp[temp.lastIndex]
                }

            }

        }


        // Marking off points on a planting line
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

                if (acceleration > 23) {
                    val plantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
                    val l = plantingLine.tag as List<*>

                    var pointOfInterestOnPolyline: LatLng? = null

                    val unMarkedPoints = mutableListOf<LatLng>()
                    l.forEach { loc ->
                        unMarkedPoints += loc as LatLng
                    }

                    l.forEach { loc ->
                        loc as LatLng
                        if (loc == listOfPlantingRadius?.center)

                            pointOfInterestOnPolyline = loc

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
                                CircleOptions().center(pointOfInterestOnPolyline!!)
                                    .fillColor(Color.YELLOW)
                                    .radius(0.5)
                                    .strokeWidth(1.0f)
                            )
                            Toast.makeText(applicationContext, "Point Marked " +
                                    "", Toast.LENGTH_SHORT).show()

                            tempClosestPoint.clear()
                            templist.clear()
                            currentLocation.clear()

                            if(markedCirclePoint!!.center in listOfMarkedPoints){
                                return

                            }
                            listOfMarkedPoints.add(markedCirclePoint.center)
                            if (markedCirclePoint.center == listOfPlantingRadius!!.center)
                                listOfPlantingRadius?.remove()
                        }


//
//                    val isWithin =
//                        S2Helper.isPointWithInPlantingRadius(pointsIndex, pointOfInterestOnPolyline!!, cl)
//
//                    if (isWithin) {

//                        val markedCirclePoint = map?.addCircle(
//                            CircleOptions().center(pointOfInterestOnPolyline!!)
//                                .fillColor(Color.YELLOW)
//                                .radius(0.5)
//                                .strokeWidth(1.0f)
//                        )
//                        Toast.makeText(applicationContext, "Point Marked " +
//                                "", Toast.LENGTH_SHORT).show()
//
//                        listOfMarkedPoints.add(markedCirclePoint!!.center)

//                       var loc = markedCirclePoint.center
//                        var point = S2Helper.makeS2PointFromLngLat(loc)
//                        var S2LatLngPoint = S2LatLng.fromDegrees(loc.latitude, loc.longitude)
//                  var remove =  pointsIndex.remove(point,S2LatLngPoint)
//                        if(remove){
//                            Log.d("remove", "modified$pointsIndex")
//                        }else{
//                            Log.d("remove"," not modified")
//                        }

//                    }
//                    else {
//                        Toast.makeText(applicationContext, "Point out of bounds" +
//                                "", Toast.LENGTH_SHORT).show()
//                    }

                        // }
                    }
                    return
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        override fun onResume() {
            sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
            )
            super.onResume()
        }

        override fun onPause() {
            sensorManager!!.unregisterListener(sensorListener)
            super.onPause()
        }


        fun radius(size: Int): Double {
            val sizeInCentimeters = size * 100
            return ((0.1 * sizeInCentimeters) / 100) + 1.0
        }

        private val onLongMapPress = GoogleMap.OnMapLongClickListener {
            if (polyLines.size == 0)
                return@OnMapLongClickListener // Not yet...

            map?.addMarker(MarkerOptions().title("Landing").position(it))

            val loc = S2Helper.makeS2PointFromLngLat(it) // Get the point
            val p = S2Helper.findClosestLine(linesIndex, it, polyLines)
            Log.d("closest", "Closest l" +
                    "ine found, will look for closest point!")
            if (p != null) {
                (p as Polyline).color = Color.CYAN // change its colour..
            }

            val xloc = S2Helper.findClosestPointOnLine(pointsIndex, it) as S2LatLng?
            if (xloc != null) {
                val pt = LatLng(xloc.latDegrees(), xloc.lngDegrees())
                // Draw the point on the line...
                map?.addCircle(
                    CircleOptions().center(pt).fillColor(Color.CYAN).radius(0.5)
                        .strokeWidth(1.0f)

                )

                Log.d("closest", "Closest point drawn")
            }
        }

        private val callback = OnMapReadyCallback { googleMap ->
            map = googleMap
            googleMap.setLocationSource(NmeaReader.listener)
            googleMap.setOnMapClickListener(onMapClick)

            googleMap.setOnPolylineClickListener(onPolyClick)
            //googleMap.setOnCircleClickListener(onClickingPoint)
            googleMap.setOnMapLongClickListener(onLongMapPress)


            val isl = LatLng(-.366044, 32.441599) // LatLng(0.0,32.44) //
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            googleMap.addMarker(MarkerOptions().position(isl).title("Marker in N Residence"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(isl, 60.0f))

        }


    }