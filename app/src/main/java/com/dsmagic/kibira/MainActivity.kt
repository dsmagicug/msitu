package com.dsmagic.kibira

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.dsmagic.kibira.DBHelper.Companion.NAME_COl
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_projects.*
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text
import java.net.URL
import java.util.concurrent.Executors



class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener
{
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
    var asyncExecutor = Executors.newSingleThreadExecutor()

    var str =
        " {\n    \"_id\": \"1\",\n    \"name\": \"Project 1\",\n   \"grid\": [\n     {\n       \"size\": 200\n     },\n     {\n       \"gap_size\": 4\n     }\n   ],\n    \"marked points\": [\n      {\n        \"latitude\": -76.646831,\n        \"longitude\": -47.676246\n      },\n      {\n        \"latitude\": -86.646831,\n        \"longitude\": -57.676246\n      },\n      {\n        \"latitude\": -96.646831,\n        \"longitude\": -67.676246\n      }\n    ]\n  }"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //   binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.appToolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)


        val mapFragment =
            supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

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
                }
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appmenu, menu)

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

        } else if(item.itemId == R.id.reload){
            finish()
            startActivity(intent)
            Log.d("reload","reloaded")
            return true
        }
        else {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
        return true
    }

    val sharedPrefFile = "kibirasharedfile"


    override fun onPostResume() {

        super.onPostResume()
        var url:URL = URL("http://uinames.com/api/")

        var displayProjectName = findViewById<TextView>(R.id.display_project_name)
        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val saved_project_name: String? = sharedPreferences.getString("name_key", "defaultValue")
        var saved_gap_size: Int? = sharedPreferences.getInt("gap_size", 0)

        displayProjectName?.text = saved_project_name

        Log.d("valuesMain","saved data $saved_project_name")

        //overridePendingTransition(0, 0)

        val db = DBHelper(this,null)
        if (saved_project_name != null && saved_gap_size != null ) {
            with(db) {
                addProject(
                    saved_project_name,
                    saved_gap_size
                )
            }
            //Toast.makeText(this, "Project $saved_project_name created", Toast.LENGTH_LONG).show()

        }else{
            Toast.makeText(this, "Project not created", Toast.LENGTH_LONG).show()
        }

    }


    fun listProjects() {


        try {

            val obj:JSONObject = JSONObject(str)
            val names:JSONObject = obj.getJSONObject("projects")

            val name = names.getString("name")

            // set employee name and salary in TextView's
            display_project_name.setText("Name: $name");


        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    private fun toggleWidgets(){
        val btSpinner = findViewById<Spinner>(R.id.spinner)
        val btn  = findViewById<Button>(R.id.buttonConnect)

        if(btSpinner.visibility == Spinner.INVISIBLE && btn.visibility == Button.INVISIBLE){
            btSpinner.visibility = Spinner.VISIBLE
            btn.visibility = Button.VISIBLE
            scantBlueTooth()
        }
        else{
            btSpinner.visibility = Spinner.INVISIBLE
            btn.visibility = Button.INVISIBLE
        }

    }
    private fun scantBlueTooth() {
        val btSpinner = findViewById<Spinner>(R.id.spinner)
        val btn  = findViewById<Button>(R.id.buttonConnect)

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
        l: Long
    ) {
        device = deviceList.get(i)

        // Show connect button.
        val btn  = findViewById<Button>(R.id.buttonConnect)
        btn.visibility = Button.VISIBLE // Shown
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        val btn  = findViewById<Button>(R.id.buttonConnect)
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

    private fun hideButton(){
        val btn  = findViewById<Button>(R.id.buttonConnect)
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
            .color(Color.RED)
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
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPoint!!.getLatitude(),firstPoint!!.getLongitude()), 20.0f))
            }
            return@OnMapClickListener
        }
        secondPoint = pt
        if (firstPoint == null || secondPoint == null || meshDone)
            return@OnMapClickListener

        map?.addCircle(
            CircleOptions().center(loc).fillColor(Color.YELLOW).radius(1.0).strokeWidth(1.0f)
        )
        asyncExecutor.execute {
            val c = Point(firstPoint!!)
            val p = Point(secondPoint!!)
            val lines = Geometry.generateMesh(c, p)
            Geometry.generateLongLat(c, lines, drawLine)
            meshDone = true
            handler.post { // Centre it...
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPoint!!.getLatitude(),firstPoint!!.getLongitude()), 20.0f))
            }

        }

    }

    var ls = mutableListOf<LatLng>()
    private val onPolyClick = GoogleMap.OnPolylineClickListener {
        it.color = Color.MAGENTA
        val l = it.tag as List<*>
        var lastp: LatLng? = null

for(loc in l ){
    ls.add(loc as LatLng)
}
        var mut = ls.subList(1, 40) as List<*>

        for (loc in l) {
            var xloc = loc as LatLng
            // Draw the points...
            if(loc !in mut){
                map?.addCircle(
                    CircleOptions().center(loc).fillColor(Color.RED).radius(1.0)
                        .clickable(true)
                        .strokeWidth(1.0f)  //if set to zero, no outline is drawn
                )
            }else {
                map?.addCircle(
                    CircleOptions().center(loc).fillColor(Color.YELLOW).radius(1.0)
                        .clickable(true)
                        .strokeWidth(1.0f)  //if set to zero, no outline is drawn
                )
            }


            this.onMarkingPoint
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
            //lastp = xloc

            Log.d("ls","$ls")
        }

        clearUnMarkedLines(it)
    }
    private fun clearUnMarkedLines(polyline: Polyline) {
        var mut = ls.subList(1, 5)
        val l = polyline.tag as List<*>
        for (loc in mut) {
            var xloc = loc as LatLng

            // Draw the points...
            map?.addCircle(
                CircleOptions().center(loc).fillColor(Color.YELLOW).radius(0.5)
                    .clickable(true)
                    .strokeWidth(1.0f)  //if set to zero, no outline is drawn
            )
            Log.d("lsunmarked", "$mut")

        }
    }
    private val onMarkingPoint = GoogleMap.OnCircleClickListener {
        Log.d("clicked","circle clicked")


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

        googleMap.setOnMapLongClickListener(onLongMapPress)
        googleMap.setOnCircleClickListener(onMarkingPoint)

        val isl = LatLng(-.366044, 32.441599) // LatLng(0.0,32.44) //
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.addMarker(MarkerOptions().position(isl).title("Marker in N Residence"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(isl, 20.0f))

    }


}