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
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import kotlinx.android.synthetic.main.activity_main.*

import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter

import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {
    var deviceList = ArrayList<BluetoothDevice>()
    var device: BluetoothDevice? = null
    private var map: GoogleMap? = null
    private var marker: Circle? = null
    private var tolerance: Circle? = null
    var lastLoc: Location? = null
    var zoomLevel = 30.0f
    var firstPoint: LongLat? = null
    var secondPoint: LongLat? = null
    val handler = Handler(Looper.getMainLooper())
    var meshDone = false
    var linesIndex = MutableS2ShapeIndex() // S2 index of lines...

    var pointsIndex = S2PointIndex<S2LatLng>()
    var polyLines = ArrayList<Polyline?>()
    var asyncExecutor = Executors.newSingleThreadExecutor()

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
                    Log.d("Location", "First Location $loc!")
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(xloc, zoomLevel))
                    firstPoint = loc as LongLat // Grab it.
                }
                // Get the displacement from the last position.
                val moved = NmeaReader.significantChange(lastLoc, loc)
                lastLoc = loc // Grab last location
                if (moved) { // If it has changed, move the thing...
                    // Log.d("Location","Location ${loc.latitude}, ${loc.longitude}")
                    marker?.remove()
                    marker = map?.addCircle(
                        CircleOptions().center(xloc).fillColor(Color.YELLOW).radius(1.0)
                            .strokeWidth(1.0f)


                    )
                    marker as LatLng

                }
            }
        })

    }
    val str ="{\"employee\":{\"name\":\"Abhishek Saini\",\"salary\":65000}}"

    fun showFragment() {
        finish()

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.appToolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val mapFragment =
            supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
//    finish()
//    startActivity(intent)
        Log.d("clear", "cleraed")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appmenu, menu)

        return true
    }

    fun createDialog(): Boolean {
        var onAppOpen = firstActivity()
        onAppOpen.show(supportFragmentManager, "pick")
        return true

    }

    fun newProject(): Boolean {

        var onNewProject = firstActivity() as DialogFragment

        onNewProject.show(supportFragmentManager, "project")
        return true
    }

    //Handling the options in the app action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_create) {
            var createNewProject = CreateProjectDialog()
            createNewProject.show(supportFragmentManager, "create")
            return true
        } else if (item.itemId == R.id.action_view_projects) {
            var createNewProject = CreateProjectDialog()
            createNewProject.show(supportFragmentManager, "view_projects")
//        listProjects()
            return true
        } else if (item.itemId == R.id.bluetooth_spinner) {
            toggleWidgets()
            return true

        } else if (item.itemId == R.id.reload) {
//            finish()
//            startActivity(intent)
//            Log.d("reload", "reloaded")

            return true
        } else {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
        return true
    }

    val sharedPrefFile = "kibirasharedfile"


    override fun onPostResume() {

        super.onPostResume()
        var url: URL = URL("http://uinames.com/api/")

        var displayProjectName = findViewById<TextView>(R.id.display_project_name)
        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val saved_project_name: String? = sharedPreferences.getString("name_key", "defaultValue")
        var saved_gap_size: Int? = sharedPreferences.getInt("gap_size", 0)

        // displayProjectName?.text = saved_project_name

        Log.d("valuesMain", "saved data $saved_project_name")

        //overridePendingTransition(0, 0)

        val db = DBHelper(this, null)
        if (saved_project_name != null && saved_gap_size != null) {
            with(db) {
                addProject(
                    saved_project_name,
                    saved_gap_size
                )
            }
            //Toast.makeText(this, "Project $saved_project_name created", Toast.LENGTH_LONG).show()
//            val refresh = Intent(this,MainActivity::class.java)
//            finish()
//            startActivity(refresh)
        } else {
            Toast.makeText(this, "Project not created", Toast.LENGTH_LONG).show()
        }

    }


    fun listProjects() {
        try {

            val obj: JSONObject = JSONObject(str)
            val names: JSONObject = obj.getJSONObject("projects")

            val name = names.getString("name")

            // set employee name and salary in TextView's
            display_project_name.setText("Name: $name");


        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    private fun toggleWidgets() {
        val btSpinner = findViewById<Spinner>(R.id.spinner)
        val btn = findViewById<Button>(R.id.buttonConnect)

        if (btSpinner.visibility == Spinner.INVISIBLE && btn.visibility == Button.INVISIBLE) {
            btSpinner.visibility = Spinner.VISIBLE
            btn.visibility = Button.VISIBLE
            scantBlueTooth()
        } else {
            btSpinner.visibility = Spinner.INVISIBLE
            btn.visibility = Button.INVISIBLE
        }

    }

    private fun scantBlueTooth() {
        val btSpinner = findViewById<Spinner>(R.id.spinner)
        val btn = findViewById<Button>(R.id.buttonConnect)

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


        btSpinner.adapter = adaptor
        btSpinner.onItemSelectedListener = this
        btSpinner.visibility = Spinner.VISIBLE
        Log.d("bt", "Bluetooth scan complete")

        btn.setOnClickListener(this)
    }

    override fun onItemSelected(
        var1: AdapterView<*>?,
        view: View?,
        i: Int,
        l: Long,
    ) {
        device = deviceList.get(i)

        // Show connect button.
        val btn = findViewById<Button>(R.id.buttonConnect)
        btn.visibility = Button.VISIBLE // Shown
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        val btn = findViewById<Button>(R.id.buttonConnect)
        btn.visibility = Button.INVISIBLE
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
        val btn = findViewById<Button>(R.id.buttonConnect)
        btn.visibility = Button.INVISIBLE
    }

    private fun openBlueTooth() {
        device?.let {
            // Load the map

            //showMap()
            this.let { it1 ->
                NmeaReader.start(it1, it)
                Log.d("bt", "Bluetooth read started")
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
        val pt = LongLat(loc.longitude, loc.latitude)
        if (firstPoint == null) { // Special case, no BT
            firstPoint = pt
            handler.post {
                marker = map?.addCircle(
                    CircleOptions().center(loc).fillColor(Color.YELLOW).radius(1.0)
                        .strokeWidth(1.0f)
                )
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPoint!!.getLatitude(),
                    firstPoint!!.getLongitude()), 30.0f))
            }
            return@OnMapClickListener
        }
        secondPoint = pt
        if (firstPoint == null || secondPoint == null || meshDone)
            return@OnMapClickListener

        map?.addCircle(
            CircleOptions().center(loc).fillColor(Color.YELLOW).radius(1.0).strokeWidth(1.0f)
        )
        map?.addCircle(CircleOptions().center(loc).fillColor(Color.RED)
            .strokeWidth(1.0f)
        )
        asyncExecutor.execute {
            val c = Point(firstPoint!!)
            val p = Point(secondPoint!!)
            val lines = Geometry.generateMesh(c, p)
            Geometry.generateLongLat(c, lines, drawLine)
            meshDone = true

            handler.post { // Centre it...
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPoint!!.getLatitude(),
                    firstPoint!!.getLongitude()), 30.0f))
            }

            //Toast.makeText(this, "Grid done..", Toast.LENGTH_LONG).show()
        }

    }

    var listOfMarkedPoints = mutableListOf<LatLng>()
    var listOfMarkedPointsDemo = mutableListOf<LatLng>()
    var listWithNext = mutableListOf<LatLng>()
    var unmarkedCirclesList = mutableListOf<Circle>()
    var listOfMarkedCircles = mutableListOf<Circle>()
    var listOfPlantingLines = mutableListOf<Polyline>()


    private val onPolyClick = GoogleMap.OnPolylineClickListener {
        it.isClickable = false
        if(listOfPlantingLines.isEmpty()){
            listOfPlantingLines.add(it)
            Toast.makeText(this, "Planting line selected...", Toast.LENGTH_LONG).show()
            it.color = Color.GREEN


        }else{
            Toast.makeText(this, "Switching planting line...", Toast.LENGTH_LONG).show()
          var recentLineIndex = listOfPlantingLines.lastIndex
            var recentLine = listOfPlantingLines[recentLineIndex]
            recentLine.color = Color.GRAY
            recentLine.isClickable = true
            listOfPlantingLines.add(it)
            it.color = Color.GREEN
        }
        val l = it.tag as List<*>

        var lastp: LatLng? = null

        for (loc in l) {
            listOfMarkedPointsDemo.add(loc as LatLng)
        }
        var mut = listOfMarkedPoints as List<*>
        var mut2 = listOfMarkedPointsDemo.subList(40, 100) as List<*>

        val index = mut.lastIndex

        for (loc in l) {
            var xloc = loc as LatLng
            // Draw the points...
            if (loc !in mut2) {

                var unmarkedCircles = map?.addCircle(
                    CircleOptions().center(loc).fillColor(Color.RED).radius(0.5)
                        .clickable(true)
                        .strokeWidth(1.0f)
                    //if set to zero, no outline is drawn
                )
                unmarkedCircles?.isClickable
                unmarkedCirclesList.add(unmarkedCircles!!)
            } else {

                var markedCircles = map?.addCircle(
                    CircleOptions().center(loc).fillColor(Color.YELLOW).radius(0.5)

                        .strokeWidth(1.0f)  //if set to zero, no outline is drawn
                )
            }

            if (lastp != null) {
                val res = floatArrayOf(0f)
                Location.distanceBetween(
                    lastp.latitude,
                    lastp.longitude,
                    xloc.latitude,
                    xloc.longitude,
                    res
                )

                Log.d("distance", "Distance from last point: ${res[0]}")
            }
            lastp = xloc


            Log.d("ls", "$listOfMarkedPoints")
        }
        if (lastp != null && mut.isNotEmpty()) {
            val newStartingPointAfterPlantingIndex = mut.last()
            tolerance(newStartingPointAfterPlantingIndex as LatLng, it)
        }
    }

    var i = 10

    val sensorListener: SensorEventListener = object : SensorEventListener {
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
                var plantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
                val l = plantingLine.tag as List<*>



                Log.d("points","$l")
                var index = l[++i]
                    for(loc in l){
                        if(loc == index){
                Log.d("shake","$loc")
                      var circlePoint =  map?.addCircle(
                            CircleOptions().center(loc as LatLng).fillColor(Color.CYAN).radius(0.5)
                                .clickable(true)
                                .strokeWidth(1.0f)
                            //if set to zero, no outline is drawn
                        )

                            if (circlePoint != null) {
                                plantingTolerance(circlePoint, unmarkedCirclesList)
                                listOfMarkedCircles.add((circlePoint))
                            }
                    }


                }

                markPoints()
                Toast.makeText(applicationContext, "Point Marked $index", Toast.LENGTH_SHORT).show()


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
    fun tolerance(loc: LatLng, polyline: Polyline) {

        val l = polyline.tag as List<*>

        //map?.addMarker(MarkerOptions().title("MyPosition").position(loc))
        //
        for (pt in l) if (pt == loc) {
            Log.d("point", "$pt")
//            println(findIndex(l, pt))
            val index = l.indexOf(pt)
            val nextIndex = index + 1
            var nextPoint = l[nextIndex] as LatLng
            // listWithNext.add(nextPoint as LatLng)


            Log.d("nextPoint", "$nextPoint")
            map?.addCircle(
                CircleOptions().center(nextPoint)
                    .radius(radius(4))
                    .fillColor(0x22228B22)
                    .strokeColor(Color.GREEN)
                    .strokeWidth(1.0f)
                    .clickable(true)
            )
        }
        Log.d("tag", "$l")

        map?.addCircle(
            CircleOptions().center(loc).fillColor(Color.CYAN).radius(0.7)
                .strokeWidth(1.0f)


        )


        Log.d("closest", "$loc")


    }

    fun radius(size: Int): Double {
        val sizeInCentimeteres = size * 100
        val toleranceCircle = ((0.1 * sizeInCentimeteres) / 100) + 1.0
        return toleranceCircle
    }

    fun markPoint(){


    }
    fun markPoints()=GoogleMap.OnCircleClickListener{

        //val onMarkingPoint = GoogleMap.OnCircleClickListener {
            it.isClickable = false

            var cordinatesOfClickedCircle = it.center

            listOfMarkedPoints.add(cordinatesOfClickedCircle)
            // Log.d("circleMarkedPoints","$listOfMarkedPoints")
            map?.addCircle(
                CircleOptions().center(cordinatesOfClickedCircle).fillColor(Color.YELLOW).radius(0.5)

                    .strokeWidth(1.0f)
            )
            for (greenCircle in listOfMarkedCircles) {
                if (greenCircle.center == cordinatesOfClickedCircle) {
                    greenCircle.remove()
                }
            }
            plantingTolerance(it, unmarkedCirclesList)

        }


    var listTolerance = mutableListOf<Circle>()
    fun plantingTolerance(circleCords: Circle, ummarkedcircles: MutableList<Circle>) {

        var listOfUnmarkedCircles = ummarkedcircles
        var currentCircleId = circleCords.id
Log.d("shake","I reach tolerance")
        for (aCircle in listOfUnmarkedCircles) {
            if (currentCircleId == aCircle.id) {
                val index = listOfUnmarkedCircles.indexOf(aCircle)
                val nextIndex = index + 1
                var nextPoint = listOfUnmarkedCircles[nextIndex]
                var nextPointLatLng = nextPoint.center
                //listWithNext.add(nextPoint as LatLng)

                var circles = map?.addCircle(
                    CircleOptions().center(nextPointLatLng)
                        .radius(radius(4))
                        .fillColor(0x22228B22)
                        .strokeColor(Color.GREEN)
                        .strokeWidth(1.0f)

                )
                circles?.isClickable

                circles?.let { listOfMarkedCircles.add(it) }
            }

        }

    }

    private val onLongMapPress = GoogleMap.OnMapLongClickListener {
        if (polyLines.size == 0)
            return@OnMapLongClickListener // Not yet...

        map?.addMarker(MarkerOptions().title("Landing").position(it))

        //  val loc =  S2Helper.makeS2PointFromLngLat( it) // Get the point
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
//        googleMap.setOnCircleClickListener(onClickingPoint)
        googleMap.setOnMapLongClickListener(onLongMapPress)
        googleMap.setOnCircleClickListener(markPoints())

        val isl = LatLng(-.366044, 32.441599) // LatLng(0.0,32.44) //
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.addMarker(MarkerOptions().position(isl).title("Marker in N Residence"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(isl, 20.0f))

    }


}