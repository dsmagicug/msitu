package com.dsmagic.kibira

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context

import android.content.Intent
import android.graphics.Color
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import kotlinx.android.synthetic.main.activity_main.*

import org.json.JSONException
import org.json.JSONObject

import java.net.URL
import java.util.concurrent.Executors
import kotlin.reflect.typeOf


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {
    var deviceList = ArrayList<BluetoothDevice>()
    var device: BluetoothDevice? = null
    private var map: GoogleMap? = null
    private var marker: Circle? = null
    private var tolerance:Circle? = null
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

    var str ="[\n" +
            "  [{\"lat\":8.4,\"lng\":43.9},{\"lat\":8,\"lng\":80}],\n" +
            "  {\"Base points\":{\"first\":[{\"lat\":8,\"lng\":9}],\n" +
            "    \"second\":[{\"lat\":9,\"lng\":10}]}\n" +
            "  },\n" +
            "  {\"name\":\"project one\"},\n" +
            "  {\"gap size\":4},\n" +
            "  {\"mesh\":600.0}\n" +
            "\n" +
            "\n" +
            "]"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //   binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.appToolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)


        if(savedInstanceState!=null){

        }else {
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

fun showFragment(){
  finish()

    setContentView(R.layout.activity_main)

    setSupportActionBar(findViewById(R.id.appToolbar))
    supportActionBar?.setDisplayShowTitleEnabled(false)
    val mapFragment =
        supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
    mapFragment?.getMapAsync(callback)
//    finish()
//    startActivity(intent)
    Log.d("clear","cleraed")
}
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appmenu, menu)

        return true
    }
fun createDialog():Boolean{
    var onAppOpen = firstActivity()
    onAppOpen.show(supportFragmentManager, "pick")
    return true

}
    fun newProject(): Boolean {

        var onNewProject =  firstActivity() as DialogFragment

        onNewProject.show(supportFragmentManager, "project")
        return true
    }
    fun showSnackBar(view: View) {
        val snackBar = Snackbar.make(view, "Please Choose a project", Snackbar.LENGTH_INDEFINITE)
            .setAction("hidden") {
//                on click btn
            }
//            .setActionTextColor(ContextCompat.getColor(this, R.color.green))
//        val snackBarView = snackBar.view
//        val txt =
//            snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
//        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
//        txt.setTextColor(ContextCompat.getColor(this, R.color.white))

        snackBar.show()
        Log.d("snack","snakbar")
    }
//    fun crea(name:String){
//        val newvalues = "[\n" +
//                "  [{\"lat\":8.4,\"lng\":43.9},{\"lat\":8,\"lng\":80}],\n" +
//                "  {\"Base points\":{\"first\":[{\"lat\":8,\"lng\":9}],\n" +
//                "    \"second\":[{\"lat\":9,\"lng\":10}]}\n" +
//                "  },\n" +
//                "  {\"name\":\"Project one\"},\n" +
//                "  {\"gap size\":4},\n" +
//                "  {\"mesh\":600.0}\n" +
//                "\n" +
//                "\n" +
//                "]"
//        val project2 =  "[\n" +
//                "  [{\"lat\":8.4,\"lng\":43.9},{\"lat\":8,\"lng\":80}],\n" +
//                "  {\"Base points\":{\"first\":[{\"lat\":8,\"lng\":9}],\n" +
//                "    \"second\":[{\"lat\":9,\"lng\":10}]}\n" +
//                "  },\n" +
//                "  {\"name\":\"Project two\"},\n" +
//                "  {\"gap size\":4},\n" +
//                "  {\"mesh\":600.0}\n" +
//                "\n" +
//                "\n" +
//                "]"
//        if(name == ""){
//            val view = R.layout.activity_main
//            val snack = Snackbar.make(this,"This is a simple Snackbar", Snackbar.LENGTH_LONG)
//            snack.show()
//        }
//        val displayProjectName: TextView? = findViewById(R.id.display_project_name)
//        displayProjectName?.text = name
//
////        val displayProjectName: TextView? = activity?.findViewById(R.id.display_project_name)
////        displayProjectName?.text = selectedProject
////
////        val intent = Intent(this,Geometry::class.java)
////        intent.putExtra("values",project2)
////        startActivity(intent)
//    }

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
            finish()
            startActivity(intent)
            Log.d("reload", "reloaded")
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

        }


    }

    var listOfMarkedPoints = mutableListOf<LatLng>()
    var listWithNext = mutableListOf<LatLng>()
    var clickedLines =  ArrayList<Polyline>()

    private val onPolyClick = GoogleMap.OnPolylineClickListener {

        Toast.makeText(this, "Planting line selected...", Toast.LENGTH_LONG).show()
        it.isClickable = false
        it.color = Color.GREEN

        val l = it.tag as List<*>

        var lastp: LatLng? = null

        for (loc in l) {
            listOfMarkedPoints.add(loc as LatLng)
        }

        var mut = listOfMarkedPoints.subList(40, 100) as List<*>
        val newStartingPointAfterPlantingIndex = mut.last()
        val index = mut.lastIndex

        for (loc in l) {
            var xloc = loc as LatLng
            // Draw the points...
            if (loc !in mut) {

              var circle =  map?.addCircle(
                    CircleOptions().center(loc).fillColor(Color.RED).radius(0.5)
                        .clickable(true)
                        .strokeWidth(1.0f)
                    //if set to zero, no outline is drawn
                )
                circle?.isClickable
               var ta= circle?.tag
                Log.d("circle","$ta")
            } else {
                map?.addCircle(
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
        if (lastp != null) {
            tolerance(newStartingPointAfterPlantingIndex as LatLng,it)
        }
    }

fun tolerance(loc:LatLng,polyline: Polyline){

    val l = polyline.tag as List<*>

   //map?.addMarker(MarkerOptions().title("MyPosition").position(loc))
   for(pt in l){
//
       if (pt == loc){
           Log.d("point","$pt")
//            println(findIndex(l, pt))
           val index = l.indexOf(pt)
           val nextIndex = index + 1
           var nextPoint = l[nextIndex]
           listWithNext.add(nextPoint as LatLng)


           Log.d("nextPoint","$nextPoint")
           var circles =     map?.addCircle(
              CircleOptions().center(nextPoint!! as LatLng)
                  .radius( radius(4))
                  .fillColor(0x22228B22)
                  .strokeColor(Color.GREEN)
                    .strokeWidth(1.0f)
                  .clickable(true)
            )
        }
    }
    Log.d("tag","$l")

 map?.addCircle(
        CircleOptions().center(loc!!).fillColor(Color.CYAN).radius(1.0)
            .strokeWidth(1.0f)


    )


Log.d("closest","$loc")


}
    fun radius(size:Int):Double{
        val sizeInCentimeteres = size * 100
        val toleranceCircle = ((0.1 * sizeInCentimeteres)/100) +1.0
        return  toleranceCircle
    }

    fun animate() {
        object : CountDownTimer(300000, 10000) {

            override fun onTick(millisUntilFinished: Long) {
                var x = 1.0
                while(true){
                    x++
                }

                Log.d("time","seconds remaining: " + "$millisUntilFinished / 100")
            }

            override fun onFinish() {
                Log.d("finish","time")
            }
        }.start()
    }

    private val onMarkingPoint = GoogleMap.OnCircleClickListener {
       var cordinatesOfClickedCircle= it.center
        listOfMarkedPoints.add(cordinatesOfClickedCircle)

     var current =   map?.addCircle(
            CircleOptions().center(cordinatesOfClickedCircle).fillColor(Color.YELLOW).radius(0.5)

                .strokeWidth(1.0f)
        )
        if (current != null) {
            if(current.isVisible){

            }
        }
        Log.d("clicked", "$cordinatesOfClickedCircle")



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
        googleMap.setOnCircleClickListener(onMarkingPoint)

        val isl = LatLng(-.366044, 32.441599) // LatLng(0.0,32.44) //
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.addMarker(MarkerOptions().position(isl).title("Marker in N Residence"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(isl, 20.0f))

    }


}