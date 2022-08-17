package com.dsmagic.kibira


//import com.dsmagic.kibira.roomDatabase.AppDatabase
//import com.dsmagic.kibira.roomDatabase.BasePoint
import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.Location.distanceBetween
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.dsmagic.kibira.data.LocationDependant.LocationDependantFunctions
import com.dsmagic.kibira.roomDatabase.AppDatabase
import com.dsmagic.kibira.roomDatabase.DbFunctions
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.getProjects

import com.dsmagic.kibira.roomDatabase.Entities.Basepoints

import com.dsmagic.kibira.services.*
import com.dsmagic.kibira.services.BasePoints.projectID
import com.dsmagic.kibira.utils.GeneralHelper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.PointData
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import kotlinx.android.synthetic.main.navheader.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import kotlin.math.sqrt


open class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

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
    val handler2 = Handler(Looper.getMainLooper())

    var linesIndex = MutableS2ShapeIndex() // S2 index of lines...
    var Darkmode = false
    var pointsIndex = S2PointIndex<S2LatLng>()


    var asyncExecutor: ExecutorService = Executors.newCachedThreadPool()

    var currentLocation = mutableListOf<LatLng>()
    var clearFragment = false

    var closestPointRadius = ArrayList<Any>()
    var tempPlantingRadius = 0.0f
    var tempProximityRadius = 0.0f


    private lateinit var fromRTKFeed: LatLng

    // Declaring sensorManager
    // and acceleration constants
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var switchedLines = false
    private var plantingMode = false

    // private var markers: Marker? = null
    private var fabFlag = true
    private var zoomMode = false
    private var ViewProjects = false


    var userID: String? = null

    var extras: Bundle? = null
    var projectList = ArrayList<String>()
    var projectIDList = mutableListOf<Int>()
    var projectSizeList = mutableListOf<Int>()
    var projectMeshSizeList = mutableListOf<Int>()

    var DirectionToHead: Boolean = false
    lateinit var debugXloc: LatLng

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var sharedPreferences: SharedPreferences


    //-------------compass----------//
    lateinit var fabCampus: FloatingActionButton
    lateinit var directionImage: ImageView
    lateinit var directionText: TextView
    lateinit var directionAheadText: TextView
    var BearingPhoneIsFacing: Float = 0.0f

    lateinit var drawerlayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var fab_reset: FloatingActionButton
    lateinit var fab_map: FloatingActionButton
    lateinit var progressBar: ProgressBar
    lateinit var spinner: Spinner
    lateinit var buttonConnect: Button

    lateinit var btnCloseDrawer :ImageButton

    //---------------Time---------//
    lateinit var initialTime: SimpleDateFormat
    lateinit var initialTimeValue: String


    lateinit var pace: TextView
    lateinit var linesMarked: TextView
    lateinit var totalPoints: TextView
    val bluetoothList = ArrayList<String>()
    var delta = 1.0
    var projectLoaded = false

    companion object {
        var listOfMarkedPoints = mutableListOf<LatLng>()
        var listofmarkedcircles = mutableListOf<Circle>()

        var unmarkedCirclesList = mutableListOf<Circle>()

        var listOfPlantingLines = mutableListOf<Polyline>()

       var polyLines = ArrayList<Polyline?>()
       var meshDone = false
       lateinit var card: CardView
       lateinit var directionCardLayout: CardView
       lateinit var appdb: AppDatabase
       lateinit var lineInS2Format: S2PointIndex<S2LatLng>
       lateinit var mapFragment: SupportMapFragment
   }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.appToolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fabCampus = findViewById(R.id.fab_compass)
        drawerlayout = findViewById(R.id.drawerlayout)
        navView = findViewById(R.id.navView)
        fab_map = findViewById(R.id.fab_map)
        fab_reset = findViewById(R.id.fab_reset)
        progressBar = findViewById(R.id.progressBar)
        buttonConnect = findViewById(R.id.buttonConnect)
        spinner = findViewById(R.id.spinner)
        card = findViewById<CardView>(R.id.cardView2)
        pace = findViewById(R.id.paceValue)
        linesMarked = findViewById(R.id.linesMarkedValue)
        totalPoints = findViewById(R.id.totalPointsValue)
        directionImage = findViewById(R.id.directionImageValue)
        directionText = findViewById(R.id.directionText)
        directionCardLayout = findViewById(R.id.directionsLayout)

        toggle = ActionBarDrawerToggle(this, drawerlayout, R.string.open, R.string.close)


        drawerlayout.addDrawerListener(toggle)
        toggle.syncState()

        appdb = AppDatabase.dbInstance(this)
        navView.setNavigationItemSelectedListener(this)
        val headerView: View = navView.getHeaderView(0)
        val btnCloseDrawer = headerView.findViewById<View>(R.id.btnCloseDrawer) as ImageButton
        btnCloseDrawer.setOnClickListener {
            drawerlayout.closeDrawer(Gravity.LEFT)
        }

        fabCampus.setOnClickListener {
            // rotate the map accordingly
            val newAngel = GeneralHelper.sanitizeMagnetometerBearing(lastRotateDegree)
            if (map != null) {

                GeneralHelper.changeMapPosition(map, newAngel)
            }
            BearingPhoneIsFacing = newAngel
        }

        fab_reset.setOnClickListener {
            undoDrawingLines()
        }

        extras = intent.extras

        //retrieve userid and token from intent, then store it in a shared preferences file for access
        //throughout the app

        sharedPreferences = this.getSharedPreferences(
            CreateProjectDialog.sharedPrefFile,
            Context.MODE_PRIVATE
        )!!

        if (extras != null) {
            userID = extras!!.getString("userID")
            val APIToken = extras!!.getString("token")

            val editor = sharedPreferences.edit()
            editor.putString("userid_key", userID)
            editor.putString("apiToken_key", APIToken)
            editor.apply()
            editor.commit()

        } else {
           var s = null
        }
        // Getting the Sensor Manager instance

        //if (savedInstanceState == null) {
           // getProjects(userID!!.toInt())
            //getProjects()
            createDialog(projectList)
             mapFragment =
                 (supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?)!!
            mapFragment.getMapAsync(callback)
        //}

        //register bluetooth broadcaster for scanning devices
        var intent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, intent)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager!!.registerListener(
            listener,
            magneticSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager!!.registerListener(listener, accSensor, SensorManager.SENSOR_DELAY_NORMAL)
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH



        NmeaReader.listener.setLocationChangedTrigger(object : LocationChanged {
            override fun onLocationChanged(loc: Location) {

                if (map == null)
                    return // Not yet...
                fromRTKFeed = LatLng(loc.latitude, loc.longitude)

                if (marker == null) {
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(fromRTKFeed, zoomLevel))
                    firstPoint = loc as LongLat // Grab it

                }
                // Get the displacement from the last position.
                val moved = NmeaReader.significantChange(lastLoc, loc)
                lastLoc = loc // Grab last location
                if (moved) { // If it has changed, move the thing...
                    marker?.remove()
                    directionMarker?.remove()
                    polyline1?.remove()
                    // markers?.remove()

                    marker = map?.addCircle(
                        CircleOptions().center(fromRTKFeed).fillColor(Color.GREEN).radius(0.5)
                            .strokeWidth(1.0f)

                    )
                    directionMarker?.remove()
                    directionMarker = map?.addMarker(
                        MarkerOptions().position(fromRTKFeed)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.man_icon))

                    )

                    plotFunc()
                    distanceToPoint(fromRTKFeed)
                    // We need to calculate distance of where we are to point to be marked
                    if (closestPointRadius.size > 0) {
                        val pointOfInterest = closestPointRadius[0] as LatLng
                        val acceptedPlantingRadius = tempPlantingRadius
                        val closeToPoint = tempProximityRadius

                        val distanceAway =
                            GeneralHelper.findDistanceBtnTwoPoints(
                                fromRTKFeed,
                                pointOfInterest
                            )
                        //animate point as person is closer to point
//                        if(distanceAway < acceptedPlantingRadius){
////blinkEffectForPoint("Cyan",plantingRadius)
//                        }
                        // divide by 2 means we want to mark when user is closer to the point
                        if ((distanceAway < acceptedPlantingRadius) && (pointOfInterest !in listOfMarkedPoints)) {
                            if (distanceAway < delta) {
                                // mark point
                                markPoint(pointOfInterest)
                            }

                        }
                    }
                }
            }
        })

        //scantBlueTooth()
        fab_map.setOnClickListener {
            try {
                val activePlantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
                if (!fabFlag) {

                    fab_map.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_baseline_map_24
                        )
                    )
                    map?.mapType = GoogleMap.MAP_TYPE_NORMAL

                    for (item in polyLines) {
                        item!!.isVisible = false
                        item.isClickable = false

                    }
                    activePlantingLine.isVisible = true

                    fabFlag = true

                } else if (fabFlag) {
                    fab_map.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.walk_mode
                        )
                    )
                    map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    for (c in polyLines) {
                        c?.isVisible = true
                        c?.isClickable = true
                    }
                    activePlantingLine.isVisible = true
                    fabFlag = false

                }


            } catch (e: Exception) {

            }
        }

    }

    val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        googleMap.setLocationSource(NmeaReader.listener)
        googleMap.setOnMapClickListener(onMapClick)
        googleMap.setOnPolylineClickListener(onPolyClick)
        //googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
        val isl = LatLng(-.366044, 32.441599)
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.addMarker(MarkerOptions().position(isl).title("Marker in N Residence"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(isl, 21.0f))

        val fab = findViewById<FloatingActionButton>(R.id.fab_map)
        fab.hide()

    }

    fun blinkEffectForPoint(color: String, T: Circle) {
        val n = color
        val CirclePoint = T
        var animationColor: Int = 0

        lateinit var displayTextView: TextView
        when (n) {
            "Green" -> {
                animationColor = Color.GREEN
            }
            "Yellow" -> {
                animationColor = Color.YELLOW
            }
            "Red" -> {
                animationColor = Color.RED

            }
            "Cyan" -> {
                animationColor = Color.CYAN

            }
        }

        animForPoint = ObjectAnimator.ofInt(
            CirclePoint,
            "fillColor", animationColor, Color.WHITE, animationColor, animationColor
        )
        animForPoint.duration = 1500
        animForPoint.setEvaluator(ArgbEvaluator())
        animForPoint.repeatMode = ValueAnimator.RESTART
        animForPoint.repeatCount = 1
        animForPoint.start()
    }

    private fun CheckConnectivity(): Boolean {
        val connectivity =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConnection = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConnection = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConnection!!.isConnected || mobileConnection!!.isConnected
    }


    private fun getPoints(id: Int) {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog.sharedPrefFile,
            Context.MODE_PRIVATE
        )!!

        if (listOfMarkedPoints.isNotEmpty()) {
            listOfMarkedPoints.clear()
        }
        val userIDString: String? = sharedPreferences.getString("userid_key", "0")!!

        val userID = userIDString!!.toInt()

        val retrofitGetPointsObject = AppModule.retrofitInstance()
        val modal = RequestPoints(id, userID)

        GlobalScope.launch(Dispatchers.IO) {

            val retrofitData = retrofitGetPointsObject.retrievePoints(modal)
            if (retrofitData.isSuccessful) {
                if (retrofitData.body()!!.message == "Success") {

                    val result = retrofitData.body()!!.results
                    if (result.isNotEmpty()) {
                        for (r in result) {
                            val point = LatLng(r.Lat.toDouble(), r.Long.toDouble())

                            listOfMarkedPoints.add(point)
                        }

                    } else {
                        listOfMarkedPoints.clear()
                    }

                    Log.d("points", "${Thread().name}")
                }
            } else {
                alertfail("Could not retrieve points at this time!")
            }
        }
    }

    private var pressedTime: Long = 0
    override fun onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            exitAlert("Quitting app! \n\nAre you sure?")
            //Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        pressedTime = System.currentTimeMillis()
    }


//
//    fun getProjects(): ArrayList<String> {
//
//        val apiToken: String? = sharedPreferences.getString("apiToken_key", "defaultValue")
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val retrofitDataObject = ServiceInterceptor("Bearer", apiToken!!).httpClient(apiToken)
//            val retrofitData = retrofitDataObject.getProjectsList(apiToken)
//
//            if (retrofitData.isSuccessful) {
//                //Crude: clear the lists to avoid duplicates
//                // or! Make name unique, and check its existance before adding i to list to avoid duplicates.
//                projectList.clear()
//                projectIDList.clear()
//                projectMeshSizeList.clear()
//                projectSizeList.clear()
//
//                for (data in retrofitData.body()!!) {
//                    projectList.add(data.name)
//                    projectIDList.add(data.id)
//                    projectMeshSizeList.add(data.mesh_size)
//                    projectSizeList.add(data.gap_size)
//                }
//                if (ViewProjects) {
//                    displayProjects(
//
//                    )
//                }
//
//                Log.d("Projects", "$projectList")
//
//            } else {
////                runOnUiThread {
////                    alertfail("No response got from server")
////                }
//
//            }
//
//        }
//
//        return projectList
//    }

    var selectedProject: String = " "

    fun displayProjects() {
        var l: Array<String>
        var checkedItemIndex = -1


        val larray = projectList.toTypedArray()
        if (larray.size > 5 || larray.size == 5) {
            l = larray.sliceArray(0..4)
        } else {
            l = larray
        }
        runOnUiThread {

            AlertDialog.Builder(this)
                .setTitle("Projects")
                .setSingleChoiceItems(l, checkedItemIndex,
                    DialogInterface.OnClickListener { dialog, which ->
                        checkedItemIndex = which
                        selectedProject = larray[which]
                    })
                .setNegativeButton("Delete",
                    DialogInterface.OnClickListener { dialog, id ->
                        for (j in larray) {
                            if (j == selectedProject) {
                                val index = larray.indexOf(j)
                                val id = projectIDList[index]

                                DeleteAlert(
                                    "\nProject '$selectedProject' $id  will be deleted permanently.\n\nAre you sure?",
                                    id
                                )
                            }

                        }


                    })
                .setNeutralButton("More..",
                    DialogInterface.OnClickListener { dialog, id ->

                        AlertDialog.Builder(this)
                            .setTitle("All Projects")
                            // .setMessage(s)
                            .setSingleChoiceItems(larray, checkedItemIndex,
                                DialogInterface.OnClickListener { dialog, which ->
                                    checkedItemIndex = which
                                    selectedProject = larray[which]
                                })
                            .setNegativeButton("Delete",
                                DialogInterface.OnClickListener { dialog, id ->
                                    for (j in larray) {
                                        if (j == selectedProject) {
                                            val index = larray.indexOf(j)
                                            val id = projectIDList[index]

                                            DeleteAlert(
                                                "\nProject '$selectedProject' $id will be deleted permanently.\n\nAre you sure?",
                                                id
                                            )
                                        }

                                    }


                                })
                            .setPositiveButton("Open",

                                DialogInterface.OnClickListener { dialog, id ->

                                    if (selectedProject == "") {

                                    } else {
                                        for (j in larray) {
                                            if (j == selectedProject) {
                                                val index = larray.indexOf(j)
                                                val id = projectIDList[index]
                                                var gap_size = projectSizeList[index]
                                                var mesh_size = projectMeshSizeList[index]
                                                getPoints(id)
                                                loadProject(id, mesh_size, gap_size)
                                            }

                                        }


                                    }

                                })

                            .show()

                    })
                .setPositiveButton("Open",

                    DialogInterface.OnClickListener { dialog, id ->

                        if (selectedProject == "") {

                        } else {
                            for (j in l) {
                                if (j == selectedProject) {
                                    val index = l.indexOf(j)
                                    val id = projectIDList[index]
                                    var gap_size = projectSizeList[index]
                                    var mesh_size = projectMeshSizeList[index]
                                    getPoints(id)
                                    loadProject(id, mesh_size, gap_size)
                                }

                            }


                        }

                    })

                .show()
        }
    }


    private fun loadProject(ProjectID: Int, Meshsize: Int, Gapsize: Int) {
        val displayProjectName: TextView? = findViewById(R.id.display_project_name)

        Toast.makeText(
            applicationContext, "Loading project, This may take some few minutes time." +
                    "", Toast.LENGTH_LONG
        ).show()


        GlobalScope.launch(Dispatchers.IO) {
            val retrofitGetPointsObject = AppModule.retrofitInstance()

            val modal = RequestBasePointsDataClass(ProjectID)

            val retrofitData = retrofitGetPointsObject.retrieveBasePoints(modal)
            if (retrofitData.isSuccessful) {
                if (retrofitData.body() != null) {
                    val results = retrofitData.body()!!.points
                    if (results.isNotEmpty()) {
                        runOnUiThread {
                            displayProjectName?.text = selectedProject
                        }

                        Log.d("results", "$results")
                        val l = results[0]
                        val y = results[1]
                        val firstPoint = LongLat(l.Long.toDouble(), l.Lat.toDouble())
                        val secondPoint = LongLat(y.Long.toDouble(), y.Lat.toDouble())

                        Geoggapsize = Gapsize
                        Geogmesh_size = Meshsize.toDouble()
                        clearFragment = true

                        runOnUiThread {
                            plotMesh(firstPoint, secondPoint)
                        }
//

                    } else {
                        runOnUiThread {
                            warningAlert(
                                "\nProject is empty!! Might be best to delete it.",
                                ProjectID
                            )
                        }

                    }
                }
            } else {

                Log.d("results", "Failed")

            }

        }
    }


    fun alertfail(S: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setIcon(R.drawable.cross)
            .setMessage(S)
            .show()
    }

    fun warningAlert(S: String, I: Int) {
        AlertDialog.Builder(this)
            .setTitle("Warning")
            .setIcon(R.drawable.caution)
            .setMessage(S)
            .setPositiveButton(
                "Delete",
                DialogInterface.OnClickListener { dialog, id ->
                    deleteProjectFunc(I)
                })
            .setNegativeButton("Just leave it alone",
                DialogInterface.OnClickListener { dialog, id ->
                    progressBar.isVisible = false
                })
            .show()
    }

    fun exitAlert(S: String) {
        AlertDialog.Builder(this)
            .setTitle("Warning")
            .setIcon(R.drawable.caution)
            .setMessage(S)
            .setPositiveButton(
                "Exit",

                DialogInterface.OnClickListener { dialog, id ->
                    // SuccessAlert("Exiting App")
                    finish()

                })
            .setNegativeButton("Stay",

                DialogInterface.OnClickListener { dialog, id ->

                })

            .show()
    }

    fun SuccessAlert(S: String) {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setIcon(R.drawable.tick)
            .setMessage(S)
            .show()
    }

    fun DeleteAlert(S: String, I: Int) {
        AlertDialog.Builder(this)
            .setTitle("Caution")
            .setIcon(R.drawable.caution)
            .setMessage(S)
            .setPositiveButton(
                "Delete",

                DialogInterface.OnClickListener { dialog, id ->

                    deleteProjectFunc(I)

                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                })

            .show()
    }

    fun deleteProjectFunc(ID: Int) {
//        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
//            CreateProjectDialog().sharedPrefFile,
//            Context.MODE_PRIVATE
//        )!!

        val ProjectIDString: String? = sharedPreferences.getString("productID_key", "0")

        val ProjectID = ProjectIDString!!.toInt()

        if (ProjectID == 0) {
            Toast.makeText(
                applicationContext, "Could not load project" +
                        "", Toast.LENGTH_SHORT
            ).show()
        }

        val retrofitDeleteProjectInstance = AppModule.retrofitInstance()

        val modal = deleteProjectDataClass(ID)
        val retrofitData = retrofitDeleteProjectInstance.deleteProject(modal)

        retrofitData.enqueue(object : Callback<deleteProjectResponse?> {
            override fun onResponse(
                call: Call<deleteProjectResponse?>,
                response: Response<deleteProjectResponse?>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.message == "success") {
                        Toast.makeText(applicationContext, "Project deleted", Toast.LENGTH_LONG)
                            .show()

                    } else {
                        alertfail("Could not delete project :(")
                    }
                } else {
                    alertfail("Error!! We all have bad days!! :( $response")
                }
            }

            override fun onFailure(call: Call<deleteProjectResponse?>, t: Throwable) {
                alertfail("Error ${t.message}")
            }
        })

    }

    fun showLine(L: Polyline) {
        L.isVisible = true

        for (c in unmarkedCirclesList) {
            c.isVisible = true
        }

        for (p in listOfMarkedPoints) {
            map?.addCircle(
                CircleOptions().center(p).fillColor(Color.YELLOW).radius(0.5)
                    .strokeWidth(1.0f)
            )
        }

    }

    var polyline1: Polyline? = null
    var refPoint: LatLng? = null
    var distance = 0.0f
    var latLng: LatLng? = null


    private fun distanceToPoint(loc: LatLng) {
        if (plantingMode && listOfMarkedPoints.isNotEmpty()) {
            fab_reset.isVisible = false
            when {
                listOfMarkedPoints.size == 1 -> {
                    initialTime = SimpleDateFormat(" HH:mm:ss ")
                    initialTimeValue = initialTime.format(Date())
                }
            }
            card.isVisible = true
            directionCardLayout.isVisible = true
            var displayedDistance = findViewById<TextView>(R.id.distance)
            var displayedPoints = findViewById<TextView>(R.id.numberOfPoints)
            displayedDistance.text = ""
            displayedPoints.text = " "

            var position = "None"

            try {
                val locationOfNextPoint = Location(LocationManager.GPS_PROVIDER)
                val locationOfRoverLatLng = Location(LocationManager.GPS_PROVIDER)
                val locationOfCurrentPoint = Location(LocationManager.GPS_PROVIDER)

                val line = listOfPlantingLines[listOfPlantingLines.lastIndex]
                val l = line.tag as MutableList<*>
                val size = listOfMarkedPoints.size
                refPoint = listOfMarkedPoints[listOfMarkedPoints.lastIndex]
                var index = 0

                if (tempListMarker.isNotEmpty()) {
                    var last = tempListMarker[tempListMarker.lastIndex]
                    last.remove()
                    tempListMarker.clear()
                }
                for (point in l) {
                    if (point == refPoint) {
                        index = l.indexOf(point)
                    }
                }

                if (lastRotateDegree in (-180.0..90.0)) {

                    distance = 0f
                    var nextIndex = index + 1
                    latLng = l[nextIndex] as LatLng

                    if (latLng in listOfMarkedPoints) {
                        nextIndex = index - 1
                        latLng = l[nextIndex] as LatLng
                    }

                    locationOfNextPoint.latitude = latLng!!.latitude
                    locationOfNextPoint.longitude = latLng!!.longitude

                    locationOfRoverLatLng.latitude = loc.latitude
                    locationOfRoverLatLng.longitude = loc.longitude

                    distance = locationOfRoverLatLng.distanceTo(locationOfNextPoint)

                } else {
                    distance = 0f
                    var nextIndex = index - 1

                    latLng = l[nextIndex] as LatLng

                    if (latLng in listOfMarkedPoints) {
                        nextIndex = index + 1
                        latLng = l[nextIndex] as LatLng
                    }


                    locationOfNextPoint.latitude = latLng!!.latitude
                    locationOfNextPoint.longitude = latLng!!.longitude

                    locationOfRoverLatLng.latitude = loc.latitude
                    locationOfRoverLatLng.longitude = loc.longitude

                    locationOfCurrentPoint.latitude = refPoint!!.latitude
                    locationOfCurrentPoint.longitude = refPoint!!.longitude


                    distance = locationOfRoverLatLng.distanceTo(locationOfNextPoint)

                    distance
                    if (tempListMarker.isNotEmpty()) {
                        for (m in tempListMarker) {
                            if (m.position == latLng) {
                                return    //do nothing if a marker is already drawn at that point
                            }
                        }
                    }

                }


                position = LocationDependantFunctions().facingDirection(
                    BearingPhoneIsFacing,
                    lastRotateDegree
                )
                DirectionToHead = true
                //}
                val decimalFormat = DecimalFormat("##.##")
                decimalFormat.roundingMode = RoundingMode.DOWN


                val dist = decimalFormat.format(distance)
                val d = dist.toString()
                pace()
                displayedDistance.text = d
                displayedPoints.text = size.toString()
                totalPoints.text = l.size.toString()
                blinkEffectOfMarkedPoints("Green", displayedPoints)

                if (distance > (Geoggapsize!! * 0.02)) {

                    polyline1 = map?.addPolyline(
                        PolylineOptions()
                            .color(Color.BLACK)
                            .jointType(JointType.DEFAULT)
                            .width(3.5f)
                            .geodesic(true)
                            .startCap(RoundCap())
                            .add(
                                LatLng(loc.latitude, loc.longitude),   //roverpoint
                                LatLng(latLng!!.latitude, latLng!!.longitude), //nextPoint
                            )
                    )
                    polyline1!!.endCap = CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.blackarrow1), 10f
                    )

                    blink("Red", position)
                    showLine(line)

                } else {
                    polyline1?.remove()
                    stopBlink()
                }

                // markers!!.showInfoWindow()

                // tempListMarker.add(markers!!)

            } catch (e: Exception) {
                Log.d("ERROR", "frOM SWITCHING ${e.message}")
            }


        }


    }

    lateinit var anim: ObjectAnimator
    lateinit var animForDirectionText: ObjectAnimator
    lateinit var animForPoint: ObjectAnimator
    lateinit var animation: RotateAnimation

    fun blink(color: String, p: String) {
        val textViewToBlink = p
        var animationColor: Int = 0

        when (textViewToBlink) {
            "Left" -> {
                directionImage.setImageResource(R.drawable.leftarrow)
                directionImage.isVisible = true
                directionText.text = "Turn Right"
                directionText.isVisible = true
            }
            "Right" -> {
                directionImage.setImageResource(R.drawable.rightarrow)
                directionText.text = "Turn Left"
                directionImage.isVisible = true
                directionText.isVisible = true
            }
            "Stop" -> {
                directionText.isVisible = false
                directionImage.isVisible = false

            }

        }


        /*animForDirectionText = ObjectAnimator.ofInt(
            directionCardLayout,
            "cardBackgroundColor",R.color.teal, R.color.teal, R.color.teal_700, R.color.teal
        )
        animForDirectionText.duration = 1500
        animForDirectionText.setEvaluator(ArgbEvaluator())
        animForDirectionText.start()*/

    }

    fun blinkEffectOfMarkedPoints(color: String, T: TextView) {
        val textViewToBlink = T
        var animationColor: Int = 0
        lateinit var displayTextView: TextView
        when (color) {
            "Green" -> {
                animationColor = R.color.teal_700
            }
            "Yellow" -> {
                animationColor = Color.YELLOW
            }
            "Red" -> {
                animationColor = Color.RED

            }
            "Cyan" -> {
                animationColor = Color.CYAN

            }
        }

        anim = ObjectAnimator.ofInt(
            textViewToBlink,
            "backgroundColor", animationColor, Color.WHITE, animationColor, animationColor
        )
        anim.duration = 1500
        anim.setEvaluator(ArgbEvaluator())
        anim.repeatMode = ValueAnimator.RESTART
        anim.repeatCount = 1
        anim.start()
    }

    fun stopBlink() {

        if (anim.isRunning) {
            anim.end()
        }

    }

    fun checkLocation() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if (androidx.core.app.ActivityCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
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

        for (d in
        bluetoothAdaptor.bondedDevices) {
            bluetoothList.add(d.name)
            deviceList.add(d)
        }

        val items = bluetoothList.toArray()
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
        //btsearching.isVisible = false
    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.BLUETOOTH_CONNECT
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

                    //if (!bluetoothList.contains(device!!.name)) {

                    bluetoothList.add(device!!.name)
                    deviceList.add(device)
                    var v = device.name
                    //}

                }

            }
            scantBlueTooth()
        }
    }

    override fun onDestroy() {
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
        NmeaReader.listener.deactivate()
        sensorManager!!.unregisterListener(listener)
        super.onDestroy()


    }


    // Display the menu layout
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appmenu1, menu)
        return true
    }

    // display the create dialog if no projects available.
    private fun createDialog(list: ArrayList<String>): Boolean {
        val openingDialog = firstActivity()
        openingDialog.show(supportFragmentManager, "openingDialog")
        return true
    }

    //Handling the options in the menu layout
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            // drawerlayout.openDrawer(GravityCompat.START)
            return true
        }


        if (item.itemId == R.id.bluetooth_spinner) {
            discover()
            toggleWidgets()


            return true

        }

        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item)

    }

    private fun discover() {

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
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

        //checkLocation()
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
            bluetoothAdapter.startDiscovery()
        } else {
            bluetoothAdapter.startDiscovery()

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

            checkLocation()

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
        progressBar.isVisible = false
        buttonConnect.visibility = Button.INVISIBLE
    }

    private fun openBlueTooth() {
        progressBar.isVisible = true
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
            .color(Color.BLUE)
            .jointType(JointType.DEFAULT)
            .width(3f)
            .geodesic(true)
            .startCap(RoundCap())
            .endCap(SquareCap())
        handler.post {
            val p = map?.addPolyline(poly) // Add it and set the tag to the line...
            // Add it to the index
            val idx = polyLines.size
            S2Helper.addS2Polyline2Index(
                idx,
                linesIndex,
                S2Helper.makeS2PolyLine(ml, pointsIndex)
            )
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
                map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            firstPoint!!.getLatitude(),
                            firstPoint!!.getLongitude()
                        ), 21.0f
                    )
                )

            }

            return@OnMapClickListener
        }
        secondPoint = pt

        if (firstPoint == null || secondPoint == null)
            return@OnMapClickListener

        if (meshDone) {
            return@OnMapClickListener
        }

        var l = map?.addCircle(
            CircleOptions().center(loc).fillColor(Color.YELLOW).radius(0.5).strokeWidth(1.0f)
        )
        Log.d("BasePoints", "$loc")
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                "Drawing grid! This won't take long..",
                Toast.LENGTH_LONG
            )
                .show()

        }

        saveBasepoints(firstPoint!!)
        saveBasepoints(secondPoint!!)

        //saveBasepoints(firstPoint!!)
//        saveBasepoints(secondPoint!!)
        plotMesh(firstPoint!!, secondPoint!!)


        l?.remove()
        // marker?.remove()
    }

    fun plotMesh(cp: LongLat, pp: LongLat) {
        progressBar.isVisible = true

        if (clearFragment) {
            runOnUiThread {
                val mapFragment =
                    supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
                mapFragment?.getMapAsync(callback)
            }
            for (item in polyLines) {
                item!!.remove()
            }
            for (l in listofmarkedcircles) {
                l.remove()
            }
            for (l in unmarkedCirclesList) {
                l.remove()
            }
            if (listOfPlantingLines.isNotEmpty()) {
                listOfPlantingLines.clear()
            }

        }

        asyncExecutor.execute {

            val c = Point(cp)
            val p = Point(pp)

            val lines = Geometry.generateTriangleMesh(c, p)
            Geometry.generateLongLat(c, lines, drawLine)

            meshDone = true

            handler.post { // Centre it...

                map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            cp.getLatitude(),
                            cp.getLongitude()
                        ), 21.0f
                    )
                )

            }

        }

        if (listOfMarkedPoints.isNotEmpty()) {
            for (latlang in listOfMarkedPoints) {
                val c = map?.addCircle(
                    CircleOptions().center(latlang)
                        .fillColor(Color.YELLOW)
                        .radius(0.5)
                        .strokeWidth(1.0f)
                )
                listofmarkedcircles.add(c!!)
            }

        } else {
            Log.d("empty", "empty")
        }


        progressBar.isVisible = false

        projectLoaded = true
        fab_reset.show()

    }


    private val onPolyClick = GoogleMap.OnPolylineClickListener {
        // rotate the map accordingly
        val newAngel = GeneralHelper.sanitizeMagnetometerBearing(lastRotateDegree)
        if (map != null) {

            GeneralHelper.changeMapPosition(map, newAngel)
        }

        handler2.removeMessages(0)

        it.isClickable = false

        if (listOfPlantingLines.isEmpty()) {
            listOfPlantingLines.add(it)
            it.color = Color.GREEN

            Toast.makeText(applicationContext, "Planting line selected...", Toast.LENGTH_LONG)
                .show()

        } else {
            for (l in listOfPlantingLines) {
                l.width = 3f
                l.isVisible = true
                // handler2.removeMessages(0)
            }

            val recentLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
            recentLine.color = Color.CYAN
            recentLine.isClickable = true
            recentLine.width =
                3f   //return to default width after pulse effect and stop the handler

            listOfPlantingLines.clear()
            listOfPlantingLines.add(it)
            it.color = Color.GREEN


            Toast.makeText(applicationContext, "Switching Lines..", Toast.LENGTH_LONG)
                .show()

            //remove the planting radius circle if it exists
            if (templist.isNotEmpty()) {
                templist[templist.lastIndex].remove()
                templist.clear()
            }


            switchedLines = true


        }

        //Give the process of drawing points on line a thread --- makes the process faster
        handler.post {

            //make the lines and circles on other polylines disaapear, once we have line of interest
            for (item in polyLines) {
                item!!.isVisible = false
                item.isClickable = false
            }

            if (unmarkedCirclesList.isNotEmpty()) {
                for (circle in unmarkedCirclesList) {
                    circle.isVisible = false
                }
            }
            if (listofmarkedcircles.isNotEmpty()) {
                for (circle in listofmarkedcircles) {
                    circle.isVisible = false
                }
            }
            it.isClickable = true
            it.isVisible = true
            val l = it.tag as MutableList<*>

            plotLine(l)
            fab_map.show()


        }

        handler.post {
            //changing map type lags, so handle it ina thread as well
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
//            try {
//                // Customise the styling of the base map using a JSON object defined
//                // in a raw resource file.
//                map?.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(
//                        this, R.raw.style_json));
//
//            } catch (e: java.lang.Exception) {
//
//            }
            val target = map?.cameraPosition?.target
            val bearing = map?.cameraPosition?.bearing
            val cameraPosition = CameraPosition.Builder()
                .target(target!!)
                .zoom(21f)
                .bearing(bearing!!)
                .build()
            map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
        zoomMode = true
    }

    private fun plotLine(line: MutableList<*>) {
        //clear it so that the points to loop through are not many

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

    private fun undoDrawingLines() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog.sharedPrefFile,
            Context.MODE_PRIVATE
        )!!

        val ProjectIDString: String? = sharedPreferences.getString("productID_key", "0")

        val ProjectID = ProjectIDString!!.toInt()

        if (ProjectID == 0) {
            Toast.makeText(
                applicationContext,
                "You did not create a project!! \n create one and continue",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Warning")
            .setIcon(R.drawable.caution)
            .setMessage("Un do drawn lines? This will clear all current lines!")
            .setPositiveButton(
                "Undo",

                DialogInterface.OnClickListener { dialog, id ->
                    for (l in polyLines) {
                        l!!.remove()
                    }
                    deleteBasePoints(ProjectID)
                    meshDone = false
                })


            .show()

    }

    var plantingRadiusCircle: Circle? = null
    val templist = mutableListOf<Circle>()
    val tempClosestPoint = mutableListOf<LatLng>()


    private fun plotFunc() {

        closestPointRadius.clear()
        if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
            return
        }
        var plantingRadius: Circle? = null
        var proximityCircle: Circle? = null


        val roverPoint = fromRTKFeed

        val lineOfInterest = listOfPlantingLines[listOfPlantingLines.lastIndex]

        val l = lineOfInterest.tag as MutableList<*>
        val l2 = lineOfInterest.tag as Collection<LatLng>

        lineInS2Format = GeneralHelper.convertLineToS2(l2)

        //val xloc = S2Helper.findClosestPointOnLine(pointsIndex, roverPoint) as S2LatLng?
        val xloc = S2Helper.findClosestPointOnLine(lineInS2Format, roverPoint) as S2LatLng?

        if (xloc != null) {
            val pt = LatLng(xloc.latDegrees(), xloc.lngDegrees())
            closestPointRadius.add(pt)
            debugXloc = pt
            if (pt !in l) {
                return
            }
            if (pt in listOfMarkedPoints) {
                return
            }
            tempClosestPoint.add(pt)

            //handle visibility and lifecycle of the radius circle
            if (pt in l) {
                if (templist.isNotEmpty()) {
                    for (c in templist) {
                        if (c.center == pt) {
                            return    //do nothing if a circle is already drawn at that point
                        }
                        if (c.center !in tempClosestPoint) {
                            //removes the planting radius circle as one walks away from that point
                            c.remove()
                            // markers?.remove()

                        }
                    }

                }
            }
//            proximityCircle = map?.addCircle(
//                CircleOptions().center(pt).fillColor(Color.GREEN).radius(proximityRadius(Geoggapsize!!))
//                    .strokeWidth(1.0f)
//
//                    .strokeColor(Color.CYAN)
//            )
            plantingRadius = map?.addCircle(
                CircleOptions().center(pt).fillColor(Color.GREEN).radius(radius(Geoggapsize!!))
                    .strokeWidth(1.0f)
                    .fillColor(0x22228B22)
                    .strokeColor(Color.GREEN)
                    .strokeWidth(1.0f)
            )!!

        }
        if (switchedLines) {
            val runnableCode = object : Runnable {
                override fun run() {
                    var w = lineOfInterest.width
                    w += 0.5f
                    if (w > 13.0) {
                        w = 1.0f
                    }
                    lineOfInterest.width = w
                    handler2.postDelayed(this, 50)
                }
            }

            handler2.postDelayed(runnableCode, 50)
            switchedLines = false

        }
        if (tempClosestPoint.isNotEmpty()) {
            tempClosestPoint.clear()
        }

        if (plantingRadius != null) {
            closestPointRadius.add(plantingRadius.radius.toFloat())
            handler2.removeMessages(0)
            if (templist.isNotEmpty()) {
                for (c in templist) {
                    c.remove()
                }
                templist.clear()
                templist.add(plantingRadius)
            } else {
                templist.add((plantingRadius))
            }
            plantingRadiusCircle = templist[templist.lastIndex]
            plantingMode = true
            tempPlantingRadius = plantingRadius.radius.toFloat()
            // tempProximityRadius = proximityCircle!!.radius.toFloat()

        }

    }

    fun markPoint(pointOfInterestOnPolyline: LatLng) {

        val mapper = jacksonObjectMapper()

        if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
            return
        }
        if (plantingMode) {

            Log.d("Loper", " markpoINT ${Thread().name}")

            val markedCirclePoint = map?.addCircle(
                CircleOptions().center(pointOfInterestOnPolyline)
                    .fillColor(Color.YELLOW)
                    .radius(0.5)
                    .strokeWidth(1.0f)
            )

            if (markedCirclePoint!!.center in listOfMarkedPoints) {
                return
            }

            listofmarkedcircles.add(markedCirclePoint)
            var circleCenter = markedCirclePoint.center

            val pt = mutableListOf<sampleItem>()
            pt.add(
                sampleItem(
                    circleCenter.latitude.toString(),
                    circleCenter.longitude.toString()
                )
            )
            pt

            val jsonArray = mapper.writeValueAsString(pt)
            Log.d("array", "$jsonArray")

          // DbFunctions.savePoints(pointOfInterestOnPolyline,userID!!.toInt(),)


            if (listOfMarkedPoints.add(markedCirclePoint.center)) {
                plantingRadiusCircle?.remove()   //remove planting radius, marker and clear list

                if (tempListMarker.isNotEmpty()) {
                    tempListMarker.clear()
                }

                Toast.makeText(
                    this, "Point Marked", Toast.LENGTH_SHORT
                ).show()

                var set = lineInS2Format
                val point = S2LatLng.fromDegrees(
                    pointOfInterestOnPolyline.latitude,
                    pointOfInterestOnPolyline.longitude
                )
                val pointData = PointData(point.toPoint(), point)
                lineInS2Format.remove(pointData)
                var s = lineInS2Format
            }
            if (templist.isNotEmpty()) {
                templist.clear()
            }
            if (tempClosestPoint.isNotEmpty()) {
                tempClosestPoint.clear()
            }
        }
    }

    var bearing: Double? = null
    var diff: Float? = null
    var threshold: Float = 10.0f
    private var lastRotateDegree = 0f
    private var current_measured_bearing = 0f

    // Marking off points on a planting line section
    private val listener: SensorEventListener = object : SensorEventListener {
        var accelerometerValues = FloatArray(3)
        var magneticValues = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent) {

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Pay attention to call the clone() method when assigning
                accelerometerValues = event.values.clone()

            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticValues = event.values.clone()
            }

            //I don't remember why i was doing this :( Always comment your code!!!!

            current_measured_bearing = (magneticValues.get(0) * 180 / Math.PI).toFloat()
            if (current_measured_bearing < 0) current_measured_bearing += 360f

            val R = FloatArray(9)

            val values = FloatArray(3)
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues)
            SensorManager.getOrientation(R, values)
            bearing = -Math.toDegrees(values[0].toDouble())
            val rotateDegree = (-Math.toDegrees(values[0].toDouble())).toFloat()
            diff = rotateDegree - lastRotateDegree
            val bearingAngle = Math.abs(diff!!)

            if (bearingAngle > 1) {

                lastRotateDegree = rotateDegree

            }

            //******** The shake event for marking points starts here *******//

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


            if (acceleration > 10) {

                ForcefullyMarkOrUnmarkPoint()


            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        sensorManager?.registerListener(
            listener, sensorManager!!.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            ), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(listener)
        super.onPause()
    }

    private fun ForcefullyMarkOrUnmarkPoint() {
        try {

            val roverPoint = fromRTKFeed

            val lineOfInterest = listOfPlantingLines[listOfPlantingLines.lastIndex]

            val l2 = lineOfInterest.tag as Collection<LatLng>

            val S2Lineformat = GeneralHelper.convertLineToS2(l2)

            val xloc = S2Helper.findClosestPointOnLine(S2Lineformat, roverPoint) as S2LatLng?

            if (xloc != null) {
                val pt = LatLng(xloc.latDegrees(), xloc.lngDegrees())
                val distanceAway =
                    GeneralHelper.findDistanceBtnTwoPoints(
                        fromRTKFeed,
                        pt
                    )
                if (pt !in listOfMarkedPoints) {
                    val acceptedPlantingRadius = tempPlantingRadius
                    if ((distanceAway / 2 < acceptedPlantingRadius) && (pt !in listOfMarkedPoints)) {

                        markPoint(pt)
                    }
                } else {
                    //circles drawn on the map are 0.5 in radius, thus the 1.5

                    if ((distanceAway < 1.0) && pt in listOfMarkedPoints) {
                        val point = S2LatLng.fromDegrees(pt.latitude, pt.longitude)
                        val pointData = PointData(point.toPoint(), point)
                        lineInS2Format.add(pointData)
                        listOfMarkedPoints.remove(pt)
                        map?.addCircle(
                            CircleOptions().center(pt)
                                .fillColor(Color.RED)
                                .radius(0.5)
                                .strokeWidth(1.0f)
                        )
                        Toast.makeText(
                            applicationContext,
                            "POINT UNMARKED",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            }
        } catch (e: Exception) {
            Log.d("error", "${e.message}")

        }
    }

    private fun pace() {

        when {
            listOfMarkedPoints.size % 5 == 0 -> {
                val sdf = SimpleDateFormat(" HH:mm:ss ")
                val time: String = sdf.format(Date())
                val currentTime = convertToMinutes(time)
                val startTime = convertToMinutes(initialTimeValue)
                val diff = currentTime - startTime
                val pace = listOfMarkedPoints.size / diff
                val paceValue = pace.roundToInt()
                val tx = findViewById<TextView>(R.id.paceValue)
                tx.text = paceValue.toString()

                //Toast.makeText(this,"$paceValue",Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun convertToMinutes(time: String): Double {
        val timeSplit = time.split(":")
        val hours = timeSplit[0].toDouble() * 60
        val minutes = timeSplit[1].toDouble()
        var seconds = timeSplit[2].toDouble()
        val totalMinutes = hours + minutes
        return totalMinutes
    }

    private fun deleteBasePoints(id: Int) {

        val modal = projectID(id)
        val retrofitDataObject = AppModule.retrofitInstance()

        val retrofitData = retrofitDataObject.deleteBasePoints(modal)
        retrofitData.enqueue(object : Callback<DeleteCoordsResponse?> {
            override fun onResponse(
                call: Call<DeleteCoordsResponse?>,
                response: Response<DeleteCoordsResponse?>
            ) {
                if (response.isSuccessful) {

                }
            }

            override fun onFailure(call: Call<DeleteCoordsResponse?>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }
    fun saveBasepoints(loc: LongLat) {
        val lat = loc.getLatitude()
        val lng = loc.getLongitude()
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog.sharedPrefFile,
            Context.MODE_PRIVATE
        )!!

        val ProjectIDString: String? = sharedPreferences.getString("productID_key", "0")

        val ProjectID = ProjectIDString!!.toInt()

        if (ProjectID == 0) {
            Toast.makeText(
                applicationContext,
                "You did not create a project!! \n create one and continue",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val basePoints = Basepoints(null, lat, lng, ProjectID)

        GlobalScope.launch(Dispatchers.IO) {
            val d = appdb.kibiraDao().insertBasepoints(basePoints)
            Log.d("data", "${d}")
            val s = 8
        }

    }

//    private fun saveBasepoints(loc: LongLat) {
//        val lat = loc.getLatitude()
//        val lng = loc.getLongitude()
//
//        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
//            CreateProjectDialog.sharedPrefFile,
//            Context.MODE_PRIVATE
//        )!!
//
//        val ProjectIDString: String? = sharedPreferences.getString("productID_key", "0")
//
//        val ProjectID = ProjectIDString!!.toInt()
//
//        if (ProjectID == 0) {
//            Toast.makeText(
//                applicationContext,
//                "You did not create a project!! \n create one and continue",
//                Toast.LENGTH_LONG
//            ).show()
//            return
//        }
//
//        val basePoints = Basepoints( null,lat,lng,ProjectID)
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val d = appdb.kibiraDao().insertBasepoints(basePoints)
//            Log.d("data", "$d")
//            val s = 8
//        }
//
//        val modal = SaveBasePointsClass(lat, lng, ProjectID)
//        val retrofitDataObject = AppModule.retrofitInstance()
//
//        val retrofitData = retrofitDataObject.storeBasePoints(modal)
//        retrofitData.enqueue(object : Callback<SaveBasePointsResponse?> {
//            override fun onResponse(
//                call: Call<SaveBasePointsResponse?>,
//                response: Response<SaveBasePointsResponse?>
//            ) {
//                if (response.isSuccessful) {
//                    if (response.body() != null) {
//                        if (response.body()!!.message == "success") {
////
////                            Toast.makeText(
////                                applicationContext, "Bpoints Saved!! " +
////                                        "", Toast.LENGTH_SHORT
////                            ).show()
//                        } else {
//                            val m = response.body()!!.meta
//                            alertfail(m)
//                        }
//                    } else {
//                        alertfail("BODY IS NULL")
//                    }
//                } else {
//                    meshDone = false
//                    Toast.makeText(
//                        applicationContext,
//                        "You did not create a project!! \n create one and continue",
//                        Toast.LENGTH_LONG
//                    ).show()
//                   // alertfail("You did not create a project!! \n create one and continue")
//                }
//            }
//
//            override fun onFailure(call: Call<SaveBasePointsResponse?>, t: Throwable) {
//                TODO("Not yet implemented")
//            }
//        })
//    }
    private fun alertWarning(s: String) {
        AlertDialog.Builder(this)
            .setTitle("Warning")
            .setIcon(R.drawable.caution)
            .setMessage(s)
            .setPositiveButton(
                "Exit",

                DialogInterface.OnClickListener { dialog, id ->


                })


            .show()
    }

    //saving points in the db
    fun savePoints(l: MutableList<LatLng>) {

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog.sharedPrefFile,
            Context.MODE_PRIVATE
        )!!

        val userIDString: String? = sharedPreferences.getString("userid_key", "0")!!
        val ProjectIDString: String? = sharedPreferences.getString("productID_key", "0")

        val userID = userIDString!!.toInt()
        val ProjectID = ProjectIDString!!.toInt()





        GlobalScope.launch(Dispatchers.IO) {
            val modal = savePointsDataClass(l, ProjectID, userID)
            runOnUiThread {
                progressBar.isVisible = true
                Toast.makeText(
                    applicationContext, "Saving points " +
                            "", Toast.LENGTH_SHORT
                ).show()
            }
            val retrofitDataObject = AppModule.retrofitInstance()

            val retrofitData = retrofitDataObject.storePoints(modal)
            if (retrofitData.isSuccessful) {
                if (retrofitData.body() != null) {
                    if (retrofitData.body()!!.message == "success") {
                        runOnUiThread {
                            progressBar.isVisible = false
                        }

                        Log.d("Loper", Thread().name + "savedb")
                        // convert to S2 and remove it from queryset


                    } else {
                        Toast.makeText(
                            applicationContext, "Something Went wrong!! " +
                                    "", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                alertfail("Not saved!")
            }
        }


    }

    private fun radius(size: Int): Double {
        val sizeInCentimeters = size * 100
        return ((0.1 * sizeInCentimeters) / 100) + 1.0
    }

    private fun proximityRadius(size: Int): Double {
        val size = radius(size)
        return (size * 2)
    }




    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.measurements -> Toast.makeText(
                applicationContext,
                "TO DO",
                Toast.LENGTH_SHORT
            ).show()
            R.id.reset -> {
                map?.mapType = GoogleMap.MAP_TYPE_NORMAL
                for (line in polyLines) {
                    line!!.remove()
                }
                for (l in listofmarkedcircles) {
                    l.remove()
                }
                for (l in unmarkedCirclesList) {
                    l.remove()
                }
                if (listOfPlantingLines.isNotEmpty()) {
                    listOfPlantingLines.clear()
                }
            }
            R.id.mode -> {
                if (Darkmode) {
                    map?.mapType = GoogleMap.MAP_TYPE_NORMAL
                } else {
                    if (!Darkmode) {
                        val mapType = map?.mapType
                        if (mapType != GoogleMap.MAP_TYPE_NORMAL) {
                            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
                        }
                        map?.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json)
                        )
                        Darkmode = true
                    } else {
                        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    }


                }
                return true
            }
            R.id.logOut -> {
                finish()
                return true
            }
            R.id.action_create -> {
                val createNewProject = CreateProjectDialog
                createNewProject.show(supportFragmentManager, "create")
                return true
            }
            R.id.action_view_projects -> {
                ViewProjects = true
                getProjects(userID!!.toInt())
                return true
            }
        }
        return true
    }

    //
    fun freshFragment(v: Boolean) {
        if (v) {
            val mapFragment =
                supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
        }
        for (item in polyLines) {
            item!!.remove()
        }
        for (l in listofmarkedcircles) {
            l.remove()
        }
        for (l in unmarkedCirclesList) {
            l.remove()
        }
        if (listOfPlantingLines.isNotEmpty()) {
            listOfPlantingLines.clear()
        }
    }

}






