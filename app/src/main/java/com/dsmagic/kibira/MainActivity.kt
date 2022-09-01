package com.dsmagic.kibira


//import com.dsmagic.kibira.roomDatabase.AppDatabase
//import com.dsmagic.kibira.roomDatabase.BasePoint

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
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
import com.dsmagic.kibira.notifications.NotifyUserSignals
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.circle
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.keepUserInStraightLine
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.statisticsWindow
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.vibration
import com.dsmagic.kibira.roomDatabase.AppDatabase
import com.dsmagic.kibira.roomDatabase.DbFunctions
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.ProjectID
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.deleteSavedPoints
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.retrieveMarkedpoints
import com.dsmagic.kibira.roomDatabase.Entities.Basepoints
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.dsmagic.kibira.services.AppModule
import com.dsmagic.kibira.services.savePointsDataClass
import com.dsmagic.kibira.ui.login.LoginActivity
import com.dsmagic.kibira.utils.Alerts
import com.dsmagic.kibira.utils.Alerts.Companion.alertfail
import com.dsmagic.kibira.utils.Alerts.Companion.undoAlertWarning
import com.dsmagic.kibira.utils.Alerts.Companion.warningAlert
import com.dsmagic.kibira.utils.Conversions
import com.dsmagic.kibira.utils.GeneralHelper
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {


    var device: BluetoothDevice? = null

    private var marker: Circle? = null
    private var directionMarker: Marker? = null
    var tempListMarker = mutableListOf<Marker>()
    var lastLoc: Location? = null
    var zoomLevel = 21.0f
    var firstPoint: LongLat? = null
    var secondPoint: LongLat? = null
    val handler = Handler(Looper.getMainLooper())
    var linesIndex = MutableS2ShapeIndex() // S2 index of lines...
    var Darkmode = false
    var pointsIndex = S2PointIndex<S2LatLng>()
    var asyncExecutor: ExecutorService = Executors.newCachedThreadPool()
    var closestPointRadius = ArrayList<Any>()


    private lateinit var fromRTKFeed: LatLng

    /*Declaring sensorManager and acceleration constants*/
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private var plantingMode = false

    // private var markers: Marker? = null
    private var fabFlag = true
    private var zoomMode = false
    private var ViewProjects = false
    var userID: String? = null
    var extras: Bundle? = null

    var DirectionToHead: Boolean = false
    lateinit var debugXloc: LatLng
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var sharedPreferences: SharedPreferences

    //-------------compass----------//
    lateinit var fabCampus: FloatingActionButton
    lateinit var directionImage: ImageView

    lateinit var directionText: TextView
    var BearingPhoneIsFacing: Float = 0.0f

    lateinit var drawerlayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var fab_reset: FloatingActionButton
    lateinit var fab_map: FloatingActionButton
    lateinit var fab_moreLines: FloatingActionButton
    lateinit var progressBar: ProgressBar
    lateinit var spinner: Spinner
    lateinit var buttonConnect: Button

    lateinit var pace: TextView
    lateinit var linesMarked: TextView
    lateinit var totalPoints: TextView
    lateinit var projectLines: MutableList<PlantingLine>
    var delta = 0.1
    var projectLoaded = false


    companion object {
        lateinit var context: Context
        lateinit var initialTime: SimpleDateFormat
        lateinit var initialTimeValue: String
        val bluetoothList = ArrayList<String>()
        var map: GoogleMap? = null
        var tempPlantingRadius = 0.0f
        var toleranceRadius = 0.0f
        var listOfMarkedPoints = mutableListOf<LatLng>()
        var listofmarkedcircles = mutableListOf<Circle>()
        lateinit var positionText: TextView
        lateinit var positionImage: ImageView
        lateinit var pointCardview: CardView
        lateinit var positionLayout: LinearLayout
        var unmarkedCirclesList = mutableListOf<Circle>()

        var listOfPlantingLines = mutableListOf<Polyline>()
        var deviceList = ArrayList<BluetoothDevice>()

        var polyLines = ArrayList<Polyline?>()
        var meshDone = false
        lateinit var card: CardView
        lateinit var directionCardLayout: CardView
        lateinit var appdb: AppDatabase
        lateinit var lineInS2Format: S2PointIndex<S2LatLng>
        lateinit var mapFragment: SupportMapFragment
        var plantingRadius: Circle? = null
        var onLoad = false
        lateinit var thisActivity: Activity
        lateinit var displayedDistance: TextView
        lateinit var displayedDistanceUnits: TextView
        lateinit var displayedPoints: TextView
        lateinit var projectStartPoint: Point
        var MeshType = " "
        var gapUnits = " "
        var meshUnits = " "
        var position = "None"
//        var projectIDList = mutableListOf<Int>()
//        var projectSizeList = mutableListOf<Double>()
//        var projectMeshSizeList = mutableListOf<Double>()

    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.appToolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        context = this
        thisActivity = this
        fabCampus = findViewById(R.id.fab_compass)
        fab_moreLines = findViewById(R.id.fab_moreLines)
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
        positionImage = findViewById(R.id.plantValue)
        positionText = findViewById<TextView>(R.id.plantText)
        directionText = findViewById(R.id.directionText)
        pointCardview = findViewById(R.id.positionCardView)
        positionLayout = findViewById(R.id.plant)
        directionCardLayout = findViewById(R.id.directionsLayout)
        displayedDistance = findViewById(R.id.distance)
        displayedDistanceUnits = findViewById(R.id.distanceUnits)
        displayedPoints = findViewById(R.id.numberOfPoints)

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
        fab_moreLines.setOnClickListener {
            val drawPoints = updateProjectLines()
            Geometry.generateLongLat(projectStartPoint, drawPoints, drawLine)
            if (listOfMarkedPoints.isNotEmpty()) {
                for (point in listOfMarkedPoints) {
                    val c = map?.addCircle(
                        CircleOptions().center(point)
                            .fillColor(Color.YELLOW)
                            .radius(0.5)
                            .strokeWidth(1.0f)
                    )
                    listofmarkedcircles.add(c!!)
                }
            }
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

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }
        // Getting the Sensor Manager instance
        getProjects(userID!!.toInt())
        onLoad = true
        createDialog("onLoad")
        if (savedInstanceState == null) {
            val mapFragment =
                (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?)!!
            mapFragment.getMapAsync(callback)
        }
        onLoad = false
        //register bluetooth broadcaster for scanning devices
        val intent = IntentFilter(BluetoothDevice.ACTION_FOUND)
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
                    // directionMarker?.remove()
                    polyline1?.remove()
                    // markers?.remove()

                    marker = map?.addCircle(
                        CircleOptions().center(fromRTKFeed).radius(0.3)
                            .strokeWidth(1.0f)
                            .strokeColor(Color.GREEN)

                    )
//                    directionMarker?.remove()
//                    directionMarker = map?.addMarker(
//                        MarkerOptions().position(fromRTKFeed)
//                           // .icon(BitmapDescriptorFactory.fromResource(R.drawable.man_icon))
//
//                    )

                    plotFunc()
                    distanceToPoint(fromRTKFeed)

                    // We need to calculate distance of where we are to point to be marked
                    if (closestPointRadius.size > 0) {
                        toleranceRadius = radius(GAP_SIZE_METRES).toFloat()
                        val pointOfInterest = closestPointRadius[0] as LatLng
//                        val acceptedPlantingRadius = tempPlantingRadius
                        val distanceAway =
                            GeneralHelper.findDistanceBtnTwoPoints(
                                fromRTKFeed,
                                pointOfInterest
                            )

                        if ((distanceAway  < toleranceRadius)) {
                            blink(position)
                            NotifyUserSignals.flashPosition("Orange", positionText)

                            if (distanceAway > delta && distanceAway  < toleranceRadius) {

                            if(listOfMarkedPoints.isNotEmpty() && pointOfInterest == listOfMarkedPoints[listOfMarkedPoints.lastIndex]){
                                when {
                                    distanceAway < GAP_SIZE_METRES -> {
                                        NotifyUserSignals.flashPosition("Red", positionText)
                                    }
                                    distanceAway > GAP_SIZE_METRES -> {
                                        NotifyUserSignals.flashPosition("Yellow", positionText)
                                    }
                                }
                            }

//                                when {
//                                    distanceAway < GAP_SIZE_METRES -> {
//                                        NotifyUserSignals.flashPosition("Red", positionText)
//                                    }
//                                    distanceAway > GAP_SIZE_METRES -> {
//                                        NotifyUserSignals.flashPosition("Yellow", positionText)
//                                    }
//                                }
                            // startBeep()
                                // NotifyUserSignals.flashPosition("Red", positionText)
                            }

                            if (distanceAway < delta) {
                                NotifyUserSignals.flashPosition("Green", positionText)
                                markPoint(pointOfInterest)
                            }


                        }
                        if (distanceAway > toleranceRadius) {
                            NotifyUserSignals.flashPosition("Stop", positionText)
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
                    removeMarkedPoints(listofmarkedcircles)

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
                    showMarkedPoints(listOfMarkedPoints)
                    fabFlag = false

                }


            } catch (e: Exception) {

            }
        }

    }

    fun showMarkedPoints(List: MutableList<LatLng>) {
        if (List.isNotEmpty()) {
            for (latlang in List) {
                val c = map?.addCircle(
                    CircleOptions().center(latlang)
                        .fillColor(Color.YELLOW)
                        .radius(0.3)
                        .strokeWidth(1.0f)
                )
                listofmarkedcircles.add(c!!)
            }

        } else {
            Log.d("empty", "empty")
        }
    }

    fun removeMarkedPoints(List: MutableList<Circle>) {
        if (List.isNotEmpty()) {
            for (circle in List) {
                circle.remove()
            }

        } else {
            Log.d("empty", "empty")
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


    private fun CheckConnectivity(): Boolean {
        val connectivity =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConnection = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConnection = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConnection!!.isConnected || mobileConnection!!.isConnected
    }


    fun getPoints(id: Int) {


//        val retrofitGetPointsObject = AppModule.retrofitInstance()
//        val modal = RequestPoints(id, userID)
//
//        GlobalScope.launch(Dispatchers.IO) {
//
//            val retrofitData = retrofitGetPointsObject.retrievePoints(modal)
//            if (retrofitData.isSuccessful) {
//                if (retrofitData.body()!!.message == "Success") {
//
//                    val result = retrofitData.body()!!.results
//                    if (result.isNotEmpty()) {
//                        for (r in result) {
//                            val point = LatLng(r.Lat.toDouble(), r.Long.toDouble())
//
//                            listOfMarkedPoints.add(point)
//                        }
//
//                    } else {
//                        listOfMarkedPoints.clear()
//                    }
//
//                    Log.d("points", "${Thread().name}")
//                }
//            } else {
//                alertfail("Could not retrieve points at this time!")
//            }
//        }
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


    var selectedProject: String = " "


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


    fun displayProjects() {
        onLoad = false
        var l: Array<String>
        var checkedItemIndex = -1

        val larray = com.dsmagic.kibira.projectList.toTypedArray()
        if (larray.size > 5 || larray.size == 5) {
            l = larray.sliceArray(0..4)
        } else {
            l = larray
        }

        AlertDialog.Builder(context)
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
                            val id = com.dsmagic.kibira.projectIDList[index]

                            Alerts.DeleteAlert(
                                "\nProject '$selectedProject' $id  will be deleted permanently.\n\nAre you sure?",
                                id
                            )
                        }

                    }


                })
            .setNeutralButton("More..",
                DialogInterface.OnClickListener { dialog, id ->

                    AlertDialog.Builder(context)
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
                                        val id = com.dsmagic.kibira.projectIDList[index]

                                        Alerts.DeleteAlert(
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
                                            val id = com.dsmagic.kibira.projectIDList[index]
                                            val gap_size = com.dsmagic.kibira.projectSizeList[index]
                                            val mesh_size =
                                                com.dsmagic.kibira.projectMeshSizeList[index]
                                            val mesh_type = meshTypeList[index]
                                            val gapUnits = gapUnitsList[index]
                                            loadProject(
                                                id,
                                                mesh_size,
                                                gap_size,
                                                mesh_type,
                                                gapUnits
                                            )
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
                                val id = com.dsmagic.kibira.projectIDList[index]
                                val gap_size = com.dsmagic.kibira.projectSizeList[index]
                                val mesh_size = com.dsmagic.kibira.projectMeshSizeList[index]
                                val mesh_type = meshTypeList[index]
                                val gapUnits = gapUnitsList[index]
                                loadProject(id, mesh_size, gap_size, mesh_type, gapUnits)
                            }

                        }


                    }

                })

            .show()


    }

    fun getProjects(UID: Int): MutableList<String> {

        GlobalScope.launch(Dispatchers.IO) {
            try {

                val listOfProjects = appdb.kibiraDao().getAllProjects(UID)

                listOfProjects as MutableList<Project>
                if (projectList.isNotEmpty()) {
                    projectList.clear()
                    projectIDList.clear()
                    projectMeshSizeList.clear()
                    projectSizeList.clear()
                    meshTypeList.clear()
                    gapUnitsList.clear()
                } else {
                    var t = 90
                }

                for (project in listOfProjects) {
                    com.dsmagic.kibira.projectList.add(project.name)
                    com.dsmagic.kibira.projectIDList.add(project.id!!)
                    com.dsmagic.kibira.projectMeshSizeList.add(project.lineLength)
                    com.dsmagic.kibira.projectSizeList.add(project.gapsize)
                    meshTypeList.add(project.MeshType)
                    gapUnitsList.add(project.gapsizeunits)
                }

                runOnUiThread {
                    displayProjects()
                }


            } catch (e: NullPointerException) {
                Log.d("Projects", "Empty Project")

            }

        }

        return projectList
    }

    fun loadProject(
        PID: Int,
        Meshsize: Double,
        Gapsize: Double,
        meshType: String,
        gapUnits: String
    ) {
        var displayProjectName: TextView? = findViewById(R.id.display_project_name)

        Toast.makeText(
            context, "Loading project, This may take some few minutes time." +
                    "", Toast.LENGTH_LONG
        ).show()
        freshFragment()

        listOfMarkedPoints = retrieveMarkedpoints(PID)

        var ListOfBasePoints: MutableList<Basepoints>
        GlobalScope.launch(Dispatchers.IO) {
            ListOfBasePoints =
                appdb.kibiraDao().getBasepointsForProject(PID) as MutableList<Basepoints>

            if (ListOfBasePoints.size == 0) {
                runOnUiThread {
                    warningAlert("Project Empty", PID)
                }
            } else {
                val l = ListOfBasePoints[0]
                val y = ListOfBasePoints[1]
                val firstPoint = LongLat(l.lng, l.lat)
                val secondPoint = LongLat(y.lng, y.lat)

                runOnUiThread {
                    displayProjectName!!.text = selectedProject
                    GAP_SIZE_METRES = Gapsize
                    MAX_MESH_SIZE = Meshsize
                    plotMesh(firstPoint, secondPoint, PID, meshType, listOfMarkedPoints, gapUnits)

                }

            }


        }

    }

/*
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

        }*/

//    fun deleteProjectFunc(ID: Int) {
////        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
////            CreateProjectDialog().sharedPrefFile,
////            Context.MODE_PRIVATE
////        )!!
//
//        val ProjectIDString: String? = sharedPreferences.getString("productID_key", "0")
//
//        val ProjectID = ProjectIDString!!.toInt()
//
//        if (ProjectID == 0) {
//            Toast.makeText(
//                applicationContext, "Could not load project" +
//                        "", Toast.LENGTH_SHORT
//            ).show()
//        }
//
//        val retrofitDeleteProjectInstance = AppModule.retrofitInstance()
//
//        val modal = deleteProjectDataClass(ID)
//        val retrofitData = retrofitDeleteProjectInstance.deleteProject(modal)
//
//        retrofitData.enqueue(object : Callback<deleteProjectResponse?> {
//            override fun onResponse(
//                call: Call<deleteProjectResponse?>,
//                response: Response<deleteProjectResponse?>
//            ) {
//                if (response.isSuccessful) {
//                    if (response.body()!!.message == "success") {
//                        Toast.makeText(applicationContext, "Project deleted", Toast.LENGTH_LONG)
//                            .show()
//
//                    } else {
//                        alertfail("Could not delete project :(")
//                    }
//                } else {
//                    alertfail("Error!! We all have bad days!! :( $response")
//                }
//            }
//
//            override fun onFailure(call: Call<deleteProjectResponse?>, t: Throwable) {
//                alertfail("Error ${t.message}")
//            }
//        })
//
//    }

    var polyline1: Polyline? = null
    var refPoint: LatLng? = null
    var distance = 0.0f
    var latLng: LatLng? = null


    private fun distanceToPoint(loc: LatLng) {
        val locationOfNextPoint = Location(LocationManager.GPS_PROVIDER)
        val locationOfRoverLatLng = Location(LocationManager.GPS_PROVIDER)
        val locationOfCurrentPoint = Location(LocationManager.GPS_PROVIDER)

        if(listOfPlantingLines.isEmpty()){
            return
        }
        val line = listOfPlantingLines[listOfPlantingLines.lastIndex]
        val l = line.tag as MutableList<*>

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

            displayedDistance.text = ""
            displayedPoints.text = " "

            val size = listOfMarkedPoints.size
            refPoint = listOfMarkedPoints[listOfMarkedPoints.lastIndex]
            var index = 0
            try {
                if (tempListMarker.isNotEmpty()) {
                    val last = tempListMarker[tempListMarker.lastIndex]
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

                    if (tempListMarker.isNotEmpty()) {
                        for (m in tempListMarker) {
                            if (m.position == latLng) {
                                return    //do nothing if a marker is already drawn at that point
                            }
                        }
                    }

                }

//                position = LocationDependantFunctions().facingDirection(
//                    BearingPhoneIsFacing,
//                    lastRotateDegree
//                )
                position =  keepUserInStraightLine(locationOfCurrentPoint,locationOfNextPoint,locationOfRoverLatLng)

                DirectionToHead = true
                val displayDistanceInUnitsRespectiveToProject =
                    Conversions.ftToMeters(distance.toString(), gapUnits)
                statisticsWindow(
                    size, totalPoints, l, displayDistanceInUnitsRespectiveToProject.toFloat()
                )

                //when straying from line
                if (distance > (GAP_SIZE_METRES * 0.5)) {
                    vibration()
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

                    blink(position)

                } else {
                    polyline1?.remove()
                    stopBlink()
                }

            } catch (e: Exception) {
                Log.d("ERROR", "frOM SWITCHING ${e.message}")
            }


        } else {
            //ensure we have a point first
            if (closestPointRadius.size > 0) {
                directionCardLayout.isVisible = true
                val pointOfInterest = closestPointRadius[0] as LatLng
                val positionOfPoint = l.indexOf(pointOfInterest)

                val lastIndex = l.lastIndex
                var nextPointLatLng: LatLng? = null

                val lastPointLatLng= l [lastIndex] as LatLng
                if (positionOfPoint == 0) {
                    val nextPoint = positionOfPoint + 1
                    nextPointLatLng = l[nextPoint] as LatLng?
                }
                if(positionOfPoint == lastIndex){
                    val nextPoint = positionOfPoint - 1
                    nextPointLatLng = l[nextPoint] as LatLng?
                }
                if(positionOfPoint != lastIndex && positionOfPoint != 0){
                    val nextPoint = positionOfPoint - 1
                    nextPointLatLng = l[nextPoint] as LatLng?
                }

                locationOfNextPoint.latitude = nextPointLatLng!!.latitude
                locationOfNextPoint.longitude = nextPointLatLng.longitude

                locationOfRoverLatLng.latitude = loc.latitude
                locationOfRoverLatLng.longitude = loc.longitude

                locationOfCurrentPoint.latitude = pointOfInterest.latitude
                locationOfCurrentPoint.longitude = pointOfInterest.longitude

              position =  keepUserInStraightLine(locationOfCurrentPoint,locationOfNextPoint,locationOfRoverLatLng)
                blink(position)

            }
        }

    }

    lateinit var anim: ObjectAnimator
    lateinit var animForPoint: ObjectAnimator

    fun blink(p: String) {
        val textViewToBlink = p

        when (textViewToBlink) {
            "Left" -> {
                directionImage.setImageResource(R.drawable.rightarrow)
                directionText.text = "Turn Left"
                directionImage.isVisible = true
                directionText.isVisible = true


            }
            "Right" -> {
                directionImage.setImageResource(R.drawable.leftarrow)
                directionImage.isVisible = true
                directionText.text = "Turn Right"
                directionText.isVisible = true
            }
            "Stop" -> {
                directionText.isVisible = false
                directionImage.isVisible = false
                directionCardLayout.isVisible = false

            }

        }


    }

    fun blinkEffectOfMarkedPoints(color: String, T: TextView) {
        val textViewToBlink = T
        var animationColor: Int = 0
        when (color) {
            "Green" -> {
                animationColor = Color.rgb(0, 206, 209)
            }
            "Orange" -> {
                animationColor = Color.rgb(255, 215, 0)
            }
            "Red" -> {
                animationColor = Color.RED

            }
            "Cyan" -> {
                animationColor = Color.GREEN

            }
        }
        handler.post {
            anim = ObjectAnimator.ofInt(
                textViewToBlink,
                "backgroundColor", animationColor, Color.WHITE, animationColor, animationColor
            )
            anim.duration = 6000
            anim.setEvaluator(ArgbEvaluator())
            anim.repeatMode = ValueAnimator.RESTART
            anim.repeatCount = 2
            anim.start()
        }

    }

    fun stopBlink() {

        if (anim.isRunning) {
            anim.end()
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
                            context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (ActivityCompat.checkSelfPermission(
                            context,
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
                    if (!bluetoothList.contains(device!!.name)) {
                        if (device.name == null) {
                            return
                        }
                        bluetoothList.add(device.name)
                        deviceList.add(device)
                        var v = device.name
                    }

                }

            }
            scantBlueTooth()
        }
    }

    fun discover() {

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (ActivityCompat.checkSelfPermission(
                context,
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

    fun checkLocation() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    thisActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    thisActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    thisActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            }
        }
    }

    fun scantBlueTooth() {
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

//        for (d in
//        bluetoothAdaptor.bondedDevices) {
//            bluetoothList.add(d.name)
//            deviceList.add(d)
//        }

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
    private fun createDialog(UserTag: String): Boolean {
        var dialogTag = UserTag
        val createNewProject = CreateProjectDialog
        createNewProject.show(supportFragmentManager, dialogTag)
        return true
    }

    //Handling the options in the menu layout
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            // drawerlayout.openDrawer(GravityCompat.START)
            return true
        }


        if (item.itemId == R.id.bluetooth_spinner) {
//            bluetoothFunctions.discover()
            discover()
            toggleWidgets()


            return true

        }

        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item)

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

            //scantBlueTooth()

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

        var poly = PolylineOptions().addAll(ml)
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

        plotMesh(firstPoint!!, secondPoint!!, 0, "", mutableListOf<LatLng>(), "")


        l?.remove()
        // marker?.remove()
    }

    fun updateProjectLines(): MutableList<PlantingLine> {
        lateinit var drawPoints: MutableList<PlantingLine>
        val projectLinesSize = projectLines.size

        if (projectLinesSize < 10) {
            drawPoints = projectLines.subList(0, projectLines.size)
            projectLines = projectLines.subList(projectLines.size, projectLines.size)
        } else {
            drawPoints = projectLines.subList(0, 10)
            projectLines = projectLines.subList(10, projectLines.size)
        }
        fab_moreLines.isVisible = projectLinesSize != 0
        return drawPoints
    }

    fun plotMesh(
        cp: LongLat,
        pp: LongLat,
        id: Int,
        mesh: String,
        list: MutableList<LatLng>,
        gapunits: String
    ) {
        // i.e calling this func with parameters from the db
        if (id != 0) {
            MeshType = mesh
            gapUnits = gapunits
            ProjectID = id.toLong()

        }
        progressBar.isVisible = true

        asyncExecutor.execute {

            val c = Point(cp)
            val p = Point(pp)
            projectStartPoint = c

            when (MeshType) {
                "Triangular Grid" -> {
                    projectLines = Geometry.generateTriangleMesh(
                        c,
                        p,
                        MeshDirection.LEFT
                    ) as MutableList<PlantingLine>

                    val drawPoints = updateProjectLines()
                    Geometry.generateLongLat(c, drawPoints, drawLine)
                }
                "Square Grid" -> {
                    val lines = Geometry.generateSquareMesh(c, p, MeshDirection.LEFT)
                    var listOfLineWithPoints = Geometry.generateLongLat(c, lines, drawLine)
                }
            }
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

        showMarkedPoints(list)

        progressBar.isVisible = false

        projectLoaded = true
        fab_reset.show()

    }


    private val onPolyClick = GoogleMap.OnPolylineClickListener {
        // rotate the map accordingly
        val newAngel = GeneralHelper.sanitizeMagnetometerBearing(lastRotateDegree)
        if (map != null) {
            GeneralHelper.changeMapPosition(map, newAngel)
            BearingPhoneIsFacing = newAngel
        }

        it.isClickable = false

        if (listOfPlantingLines.isEmpty()) {
            listOfPlantingLines.add(it)
            it.color = Color.GREEN

            Toast.makeText(applicationContext, "Planting line selected...", Toast.LENGTH_LONG)
                .show()

        } else {

            val recentLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
            recentLine.color = Color.CYAN
            recentLine.isClickable = true
            listOfPlantingLines.clear()
            listOfPlantingLines.add(it)
            it.color = Color.GREEN
            Toast.makeText(applicationContext, "Switching Lines..", Toast.LENGTH_SHORT)
                .show()

            //remove the planting radius circle if it exists
            if (templist.isNotEmpty()) {
                templist[templist.lastIndex].remove()
                templist.clear()
            }
            val textViewPace = findViewById<TextView>(R.id.paceValue)
            val textViewMarkedLines = findViewById<TextView>(R.id.linesMarkedValue)
            LocationDependantFunctions().pace(textViewPace)
            LocationDependantFunctions().markedLines(
                recentLine,
                listOfMarkedPoints,
                textViewMarkedLines
            )
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
                        CircleOptions().center(xloc).fillColor(Color.RED).radius(0.3)
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

        undoAlertWarning(ProjectID.toInt())
    }

    var plantingRadiusCircle: Circle? = null
    val templist = mutableListOf<Circle>()
    val tempClosestPoint = mutableListOf<LatLng>()


    private fun plotFunc() {

        closestPointRadius.clear()
        if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
            return
        }
        fab_reset.hide()
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
                CircleOptions().center(pt).fillColor(Color.GREEN).radius(radius(GAP_SIZE_METRES))
                    .strokeWidth(1.0f)
                    .fillColor(0x22228B22)
                    .strokeColor(Color.GREEN)
                    .strokeWidth(1.0f)
            )!!

        }

        if (tempClosestPoint.isNotEmpty()) {
            tempClosestPoint.clear()
        }

        if (plantingRadius != null) {
            closestPointRadius.add(plantingRadius!!.radius.toFloat())

            if (templist.isNotEmpty()) {
                for (c in templist) {
                    c.remove()
                }
                templist.clear()
                templist.add(plantingRadius!!)
            } else {
                templist.add((plantingRadius!!))
            }
            plantingRadiusCircle = templist[templist.lastIndex]
            plantingMode = true
            tempPlantingRadius = plantingRadius!!.radius.toFloat()
            // tempProximityRadius = proximityCircle!!.radius.toFloat()

        }

    }

    fun markPoint(pointOfInterestOnPolyline: LatLng) {

        if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
            return
        }
        if (plantingMode) {

            val markedCirclePoint = map?.addCircle(
                CircleOptions().center(pointOfInterestOnPolyline)
                    .fillColor(Color.YELLOW)
                    .radius(0.3)
                    .strokeWidth(1.0f)
            )

            if (markedCirclePoint!!.center in listOfMarkedPoints) {
                return
            }

            listofmarkedcircles.add(markedCirclePoint)
            Log.d("ID","$ProjectID")

            DbFunctions.savePoints(pointOfInterestOnPolyline, ProjectID.toInt())

            if (listOfMarkedPoints.add(markedCirclePoint.center)) {
                plantingRadiusCircle?.remove()   //remove planting radius, marker and clear list

                if (tempListMarker.isNotEmpty()) {
                    tempListMarker.clear()
                }

                Toast.makeText(
                    this, "Point Marked", Toast.LENGTH_SHORT
                ).show()

                blinkEffectOfMarkedPoints("Cyan", displayedPoints)

                val point = S2LatLng.fromDegrees(
                    pointOfInterestOnPolyline.latitude,
                    pointOfInterestOnPolyline.longitude
                )
                val pointData = PointData(point.toPoint(), point)
                lineInS2Format.remove(pointData)

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
            handler.post {
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
                    if ((distanceAway < acceptedPlantingRadius) && (pt !in listOfMarkedPoints)) {
                        if (distanceAway < delta) {
                            markPoint(pt)
                        }

                    }
                } else {

                    //circles drawn on the map are 0.3 in radius, thus the 1.0
                    //this prevebts the immediate re narking of that point

                    if ((distanceAway < 1.0) && pt in listOfMarkedPoints) {
                        val point = S2LatLng.fromDegrees(pt.latitude, pt.longitude)
                        val pointData = PointData(point.toPoint(), point)
                        lineInS2Format.add(pointData)
                        listOfMarkedPoints.remove(pt)
                        map?.addCircle(
                            CircleOptions().center(pt)
                                .fillColor(Color.RED)
                                .radius(0.3)
                                .strokeWidth(1.0f)
                        )
                        deleteSavedPoints(pt)
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
//
//    private fun deleteBasePoints(id: Int) {
//
//        val modal = projectID(id)
//        val retrofitDataObject = AppModule.retrofitInstance()
//
//        val retrofitData = retrofitDataObject.deleteBasePoints(modal)
//        retrofitData.enqueue(object : Callback<DeleteCoordsResponse?> {
//            override fun onResponse(
//                call: Call<DeleteCoordsResponse?>,
//                response: Response<DeleteCoordsResponse?>
//            ) {
//                if (response.isSuccessful) {
//
//                }
//            }
//
//            override fun onFailure(call: Call<DeleteCoordsResponse?>, t: Throwable) {
//                TODO("Not yet implemented")
//            }
//        })
//    }

    fun saveBasepoints(loc: LongLat) {
        val lat = loc.getLatitude()
        val lng = loc.getLongitude()
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog.sharedPrefFile,
            Context.MODE_PRIVATE
        )!!

        val displayProjectName: TextView = findViewById(R.id.display_project_name)

        DbFunctions.ProjectID

//             ProjectID = DbFunctions.getProjectID(Geoggapsize!!, displayProjectName.text.toString())
        var editor = sharedPreferences.edit()
        editor.putString("productID_key", "$ProjectID")

        val basePoints = Basepoints(null, lat, lng, ProjectID.toInt())

        GlobalScope.launch(Dispatchers.IO) {
            val d = appdb.kibiraDao().insertBasepoints(basePoints)

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

    //saving points in the db
//    fun savePoints(l: MutableList<LatLng>) {
//
//        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
//            CreateProjectDialog.sharedPrefFile,
//            Context.MODE_PRIVATE
//        )!!
//
//        val userIDString: String? = sharedPreferences.getString("userid_key", "0")!!
//        val ProjectIDString: String? = sharedPreferences.getString("productID_key", "0")
//
//        val userID = userIDString!!.toInt()
//        val ProjectID = ProjectIDString!!.toInt()
//
//
//
//
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val modal = savePointsDataClass(l, ProjectID, userID)
//            runOnUiThread {
//                progressBar.isVisible = true
//                Toast.makeText(
//                    applicationContext, "Saving points " +
//                            "", Toast.LENGTH_SHORT
//                ).show()
//            }
//            val retrofitDataObject = AppModule.retrofitInstance()
//
//            val retrofitData = retrofitDataObject.storePoints(modal)
//            if (retrofitData.isSuccessful) {
//                if (retrofitData.body() != null) {
//                    if (retrofitData.body()!!.message == "success") {
//                        runOnUiThread {
//                            progressBar.isVisible = false
//                        }
//
//                        Log.d("Loper", Thread().name + "savedb")
//                        // convert to S2 and remove it from queryset
//
//
//                    } else {
//                        Toast.makeText(
//                            applicationContext, "Something Went wrong!! " +
//                                    "", Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            } else {
//                alertfail("Not saved!")
//            }
//        }
//
//
//    }

    fun radius(size: Double): Double {
        val sizeInCentimeters = size * 100
        return ((0.1 * sizeInCentimeters) / 100) + 1.0
    }

//    private fun proximityRadius(size: Int): Double {
//        val size = radius(size)
//        return (size * 2)
//    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
//            R.id.measurements -> Toast.makeText(
//                applicationContext,
//                "TO DO",
//                Toast.LENGTH_SHORT
//            ).show()
//            R.id.reset -> {
//                map?.mapType = GoogleMap.MAP_TYPE_NORMAL
//                for (line in polyLines) {
//                    line!!.remove()
//                }
//                for (l in listofmarkedcircles) {
//                    l.remove()
//                }
//                for (l in unmarkedCirclesList) {
//                    l.remove()
//                }
//                if (listOfPlantingLines.isNotEmpty()) {
//                    listOfPlantingLines.clear()
//                }
//            }
//            R.id.mode -> {
//                if (Darkmode) {
//                    map?.mapType = GoogleMap.MAP_TYPE_NORMAL
//                } else {
//                    if (!Darkmode) {
//                        val mapType = map?.mapType
//                        if (mapType != GoogleMap.MAP_TYPE_NORMAL) {
//                            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
//                        }
//                        map?.setMapStyle(
//                            MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json)
//                        )
//                        Darkmode = true
//                    } else {
//                        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
//                    }
//
//
//                }
//                return true
//            }
            R.id.logOut -> {
                finish()
                return true
            }
            R.id.action_create -> {
                onLoad = false
                val createNewProject = CreateProjectDialog
                createNewProject.show(supportFragmentManager, "create")
                return true
            }
            R.id.action_view_projects -> {
                if (projectList.isNotEmpty()) {
                    projectList.clear()
                    projectIDList.clear()
                    projectMeshSizeList.clear()
                    projectSizeList.clear()
                    meshTypeList.clear()
                    gapUnitsList.clear()
                }
                getProjects(userID!!.toInt())
                return true
            }
        }
        return true
    }

    //
    fun freshFragment() {

        meshDone = false
        plantingMode = false
        plantingRadius?.remove()
        fabFlag = true
        card.isVisible = false
        directionCardLayout.isVisible = false

        for (item in polyLines) {
            item!!.remove()
        }
        for (l in listofmarkedcircles) {
            l.remove()
        }

        for (l in unmarkedCirclesList) {
            l.remove()
        }
        if (listOfMarkedPoints.isNotEmpty()) {
            listOfMarkedPoints.clear()
        }
        if (unmarkedCirclesList.isNotEmpty()) {
            unmarkedCirclesList.clear()
        }
        if (listOfPlantingLines.isNotEmpty()) {
            listOfPlantingLines.clear()
        }
    }

}






