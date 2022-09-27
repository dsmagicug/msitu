package com.dsmagic.kibira

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
import android.hardware.usb.UsbManager
import android.location.Location
import android.location.Location.distanceBetween
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
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
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.beepingSoundForMarkingPosition
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.isBeeping
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.isUserlocationOnPath
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.keepUserInStraightLine
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.pulseUserLocationCircle
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.reasonForBeeping
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.statisticsWindow
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.stopBeep
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.vibration
import com.dsmagic.kibira.roomDatabase.AppDatabase
import com.dsmagic.kibira.roomDatabase.DbFunctions
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.ProjectID
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.retrieveMarkedpoints
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.saveProject
import com.dsmagic.kibira.roomDatabase.Entities.Basepoints
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.dsmagic.kibira.roomDatabase.sharing.ImportProject
import com.dsmagic.kibira.roomDatabase.sharing.ImportProject.Companion.REQUEST_CODE
import com.dsmagic.kibira.ui.login.LoginActivity
import com.dsmagic.kibira.usb.USBSerialReader
import com.dsmagic.kibira.utils.Alerts
import com.dsmagic.kibira.utils.Alerts.Companion.undoAlertWarning
import com.dsmagic.kibira.utils.Alerts.Companion.warningAlert
import com.dsmagic.kibira.utils.Conversions
import com.dsmagic.kibira.utils.GeneralHelper
import com.dsmagic.kibira.utils.ScaleLargeProjects
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.maps.android.ktx.utils.contains
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.PointData
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt


class MainActivity  : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    var device: BluetoothDevice? = null
    private var marker: Circle? = null
    var tempListMarker = mutableListOf<Marker>()
    var lastLoc: Location? = null
    var zoomLevel = 21.0f
    var firstPoint: LongLat? = null
    var secondPoint: LongLat? = null
    val handler = Handler(Looper.getMainLooper())
    var linesIndex = MutableS2ShapeIndex() // S2 index of lines...
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
    private var fabFlag = true
    private var zoomMode = false
    var userID: String? = null
    var extras: Bundle? = null
    lateinit var debugXloc: LatLng
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var sharedPreferences: SharedPreferences
    lateinit var fabCampus: FloatingActionButton
    lateinit var directionImage: ImageView
    lateinit var directionText: TextView
    var BearingPhoneIsFacing: Float = 0.0f
    lateinit var drawerlayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var fab_reset: FloatingActionButton
    lateinit var fab_map: FloatingActionButton
    lateinit var fab_moreLines: FloatingActionButton
    lateinit var spinner: Spinner
    lateinit var buttonConnect: Button
    lateinit var pace: TextView

    lateinit var linesMarked: TextView
    lateinit var totalPoints: TextView
    var delta = 0.146  //6 inches
    var projectLoaded = false
    lateinit var positionText: TextView
    lateinit var positionLayout: LinearLayout
    lateinit var displayedDistance: TextView
    lateinit var displayedDistanceUnits: TextView
    lateinit var displayedPoints: TextView

    val usbSupport = USBSerialReader()
    lateinit var progressBar: ProgressBar

    companion object {
        lateinit var projectLines: MutableList<PlantingLine>
        lateinit var fixType: TextView
        lateinit var fixTypeValue:TextView
         var  lastFixType = ""
        var projectList = ArrayList<String>()
        var projectIDList = mutableListOf<Int>()
        var projectSizeList = mutableListOf<Double>()
        var projectMeshSizeList = mutableListOf<Double>()
        var meshTypeList = mutableListOf<String>()
        var gapUnitsList = mutableListOf<String>()
        lateinit var initialTime: SimpleDateFormat
        lateinit var initialTimeValue: String
        val bluetoothList = ArrayList<String>()
        var map: GoogleMap? = null
        var tempPlantingRadius = 0.0f
        var toleranceRadius = 0.0f
        var circleRadius = 0.4
        var listOfMarkedPoints = mutableListOf<LatLng>()
        var listofmarkedcircles = mutableListOf<Circle>()
        lateinit var pointCardview: CardView
        var unmarkedCirclesList = mutableListOf<Circle>()
        var listOfPlantingLines = mutableListOf<Polyline>()
        var deviceList = ArrayList<BluetoothDevice>()
        var polyLines = ArrayList<Polyline?>()
        var meshDone = false
        lateinit var card: CardView
        lateinit var directionCardLayout: CardView
        lateinit var appdb: AppDatabase
        lateinit var lineInS2Format: S2PointIndex<S2LatLng>
        var plantingRadius: Circle? = null
        var onLoad = false

        lateinit var projectStartPoint: Point
        var MeshType = " "
        var gapUnits = " "
        var meshUnits = " "
        var position = "None"
        var created = false

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.appToolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        positionText = findViewById(R.id.plantText)

        directionText = findViewById(R.id.directionText)
        pointCardview = findViewById(R.id.positionCardView)
        positionLayout = findViewById(R.id.plant)
        directionCardLayout = findViewById(R.id.directionsLayout)
        displayedDistance = findViewById<TextView>(R.id.distance)
        displayedDistanceUnits = findViewById(R.id.distanceUnits)
        displayedPoints = findViewById(R.id.numberOfPoints)
        fixType =  findViewById(R.id.fixType)
        fixTypeValue =  findViewById(R.id.fixTypeValue)
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
        if(created){
            val newAngel = GeneralHelper.sanitizeMagnetometerBearing(lastRotateDegree)
            if (map != null) {
                GeneralHelper.changeMapPosition(map, newAngel)
            }
            BearingPhoneIsFacing = newAngel
            created = false
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
            val drawPoints = ScaleLargeProjects.updateProjectLines(this)
            Geometry.generateLongLat(projectStartPoint, drawPoints, drawLine)
            if (listOfMarkedPoints.isNotEmpty()) {
                for (point in listOfMarkedPoints) {
                    val c = map?.addCircle(
                        CircleOptions().center(point)
                            .fillColor(Color.YELLOW)
                            .radius(circleRadius)
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

        }
        else {

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            retrieveProjectsFromBackend(userID!!.toInt())
        }

        onLoad = true
        createDialog("onLoad")
        if (savedInstanceState == null) {
            val mapFragment =
                (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?)!!
            mapFragment.getMapAsync(callback)
        }
        onLoad = false
        //register bluetooth broadcaster for scanning devices
        usbSupport.manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter()
        filter.addAction(USBSerialReader.ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        registerReceiver(usbSupport.usbReceiver,filter)

        val intent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, intent)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magneticSensor == null) {
            Toast.makeText(this, "No magnetic sensor", Toast.LENGTH_SHORT).show()
        }

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
            override fun onLocationChanged(loc: Location, fix: LongLat.FixType) {

                if (map == null)
                    return // Not yet...
                fromRTKFeed = LatLng(loc.latitude, loc.longitude)
                //fixValue = fix.toString()

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
                        CircleOptions().center(fromRTKFeed).radius(circleRadius)
                            .strokeWidth(1.0f)
                            .strokeColor(Color.GREEN)
                            .fillColor(Color.BLUE)

                    )
                    marker?.let {
                        pulseUserLocationCircle(it)
                    }
                    fixType.isVisible=true
                    fixTypeValue.isVisible=true

                    val cFix = fix.toString()
                    if (cFix != lastFixType) {
                        fixTypeValue.text = cFix
                    }
                    lastFixType = cFix
                    plotFunc()
                    distanceToPoint(fromRTKFeed)
                    approachingPoint()

                }
            }

        })

        //displayBluetoothDevices()
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
                    card.isVisible = false
                    directionCardLayout.isVisible = false
                    activePlantingLine.isVisible = true
                    removeMarkedCirclesFromUI(listofmarkedcircles)

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
                    displayMarkedPointsOnUI(listOfMarkedPoints)
                    fabFlag = false
                }

            } catch (e: Exception) {

            }
        }

    }

    private fun approachingPoint() {

        // We need to calculate distance of where we are to point to be marked
        if (closestPointRadius.size > 0) {
            toleranceRadius = tempPlantingRadius
            val pointOfInterest = closestPointRadius[0] as LatLng
            val distanceAway =
                GeneralHelper.findDistanceBtnTwoPoints(
                    fromRTKFeed,
                    pointOfInterest
                )

            if ((distanceAway < toleranceRadius)) {
                if (pointOfInterest in listOfMarkedPoints) {
                    if (distanceAway < delta) {
                        NotifyUserSignals.flashPosition("Green", positionText, this)
                        vibration(this)
                        if (isBeeping) {
                            stopBeep(this)
                        }
                    } else {
                        NotifyUserSignals.flashPosition("Orange", positionText, this)

                        if (isBeeping) {
                            stopBeep(this)
                        }
                    }
                } else {

                    NotifyUserSignals.flashPosition("Orange", positionText, this)

                    if (distanceAway < delta) {
                        NotifyUserSignals.flashPosition("Green", positionText, this)

//                        if (isBeeping && reasonForBeeping != "At Point") {
                           stopBeep(this)
//                            beepingSoundForMarkingPosition("At Point", this)
//                            isBeeping = true
//                            reasonForBeeping = "At Point"
//                        }
                            markPoint(pointOfInterest)

                    } else {
                        if (!isBeeping && reasonForBeeping != "Slow Down") {
                            beepingSoundForMarkingPosition("Slow Down", this)
                            isBeeping = true
                            reasonForBeeping = "Slow Down"
                        }
                    }
                }
            }

            if (distanceAway > toleranceRadius) {
                stopBeep(this)
                NotifyUserSignals.flashPosition("Stop", positionText, this)
                stopBeep(this)
            }

        }
    }

    fun displayMarkedPointsOnUI(List: MutableList<LatLng>) {
        removeMarkedCirclesFromUI(listofmarkedcircles)
        if (List.isNotEmpty()) {
            for (latlang in List) {
                val c = map?.addCircle(
                    CircleOptions().center(latlang)
                        .fillColor(Color.YELLOW)
                        .radius(circleRadius)
                        .strokeWidth(1.0f)
                )
                listofmarkedcircles.add(c!!)
            }

        } else {
            Log.d("empty", "empty")
        }
    }

    private fun removeMarkedCirclesFromUI(List: MutableList<Circle>) {
        if (List.isNotEmpty()) {
            for (circle in List) {
                circle.remove()
            }
        }
    }

    val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        googleMap.setLocationSource(NmeaReader.listener)
        googleMap.setOnMapClickListener(onMapClick)
        googleMap.setOnPolylineClickListener(onPolyClick)
        val isl = LatLng(-.366044, 32.441599)
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.addMarker(MarkerOptions().position(isl).title("Marker in N Residence"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(isl, 21.0f))

        val fab = findViewById<FloatingActionButton>(R.id.fab_map)
        fab.hide()

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
        val l: Array<String>
        var checkedItemIndex = -1

        val projectListAsArray = projectList.toTypedArray()
        l = if (projectListAsArray.size > 5 || projectListAsArray.size == 5) {
            projectListAsArray.sliceArray(0..4)
        } else {
            projectListAsArray
        }
        AlertDialog.Builder(this)
            .setTitle("Projects")
            .setSingleChoiceItems(l, checkedItemIndex,
                DialogInterface.OnClickListener { dialog, which ->
                    checkedItemIndex = which
                    selectedProject = projectListAsArray[which]
                })
            /*.setNegativeButton("Delete",
                DialogInterface.OnClickListener { _, _ ->
                    for (j in projectListAsArray) {
                        if (j == selectedProject) {
                            val index = projectListAsArray.indexOf(j)
                            val id = projectIDList[index]
                            Alerts.DeleteAlert(
                                "\nProject '$selectedProject' $id  will be deleted permanently.\n\nAre you sure?",
                                id, this
                            )
                        }

                    }

                })*/
            .setNegativeButton("Export",
                DialogInterface.OnClickListener { _, _ ->
                    for (j in projectListAsArray) {
                        if (j == selectedProject) {
                            val index = projectListAsArray.indexOf(j)
                            val id = projectIDList[index]
                            Alerts.confirmAlert(
                                "\nConfirm Export Project '$selectedProject'",
                                id, this
                            )
                        }

                    }

                })
            .setNeutralButton("More..",
                DialogInterface.OnClickListener { _, _ ->
                    displayMoreProjects(projectListAsArray)
                })
            .setPositiveButton("Open",
                DialogInterface.OnClickListener { _, _ ->

                    if (selectedProject == "") {
                        return@OnClickListener
                    } else {
                        for (j in l) {
                            if (j == selectedProject) {
                                val index = l.indexOf(j)
                                val id = projectIDList[index]
                                val gap_size = projectSizeList[index]
                                val mesh_size = projectMeshSizeList[index]
                                val mesh_type = meshTypeList[index]
                                val gapUnits = gapUnitsList[index]
                                loadProject(id, mesh_size, gap_size, mesh_type, gapUnits)
                            }

                        }


                    }

                })

            .show()
    }

    fun displayMoreProjects(list: Array<String>) {
        var checkedItemIndex = -1
        AlertDialog.Builder(this)
            .setTitle("All Projects")
            // .setMessage(s)
            .setSingleChoiceItems(list, checkedItemIndex,
                DialogInterface.OnClickListener { _, which ->
                    checkedItemIndex = which
                    selectedProject = list[which]
                })
            .setNegativeButton("Delete",
                DialogInterface.OnClickListener { _, _ ->
                    for (j in list) {
                        if (j == selectedProject) {
                            val index = list.indexOf(j)
                            val id = projectIDList[index]

                            Alerts.DeleteAlert(
                                "\nProject '$selectedProject' will be deleted permanently.\n\nAre you sure?",
                                id, this
                            )
                        }

                    }


                })
            .setPositiveButton("Open",

                DialogInterface.OnClickListener { _, _ ->

                    if (selectedProject == "") {
                        return@OnClickListener
                    } else {
                        for (j in list) {
                            if (j == selectedProject) {
                                val index = list.indexOf(j)
                                val id = projectIDList[index]
                                val gap_size = projectSizeList[index]
                                val mesh_size = projectMeshSizeList[index]
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
    }

    private fun retrieveProjectsFromBackend(UID: Int): MutableList<String> {

        GlobalScope.launch(Dispatchers.IO) {

                val listOfProjects = appdb.kibiraDao().getAllProjects(UID)

                if(listOfProjects.isEmpty()){
                    runOnUiThread{
                        Toast.makeText(applicationContext,"No projects Created yet",Toast.LENGTH_LONG).show()
                    }
                }
                else {

                    listOfProjects as MutableList<Project>
                    if (projectList.isNotEmpty()) {
                        projectList.clear()
                        projectIDList.clear()
                        projectMeshSizeList.clear()
                        projectSizeList.clear()
                        meshTypeList.clear()
                        gapUnitsList.clear()
                    }
                    for (project in listOfProjects) {
                        projectList.add(project.name)
                        projectIDList.add(project.id!!)
                        projectMeshSizeList.add(project.lineLength)
                        projectSizeList.add(project.gapsize)
                        meshTypeList.add(project.MeshType)
                        gapUnitsList.add(project.gapsizeunits)
                    }

                    runOnUiThread {
                        displayProjects()
                    }
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
        val displayProjectName: TextView? = findViewById(R.id.display_project_name)

        Toast.makeText(
            this, "Loading project, This may take some few minutes time." +
                    "", Toast.LENGTH_LONG
        ).show()
        cleanUpExistingFragment()

        listOfMarkedPoints = retrieveMarkedpoints(PID)

        var ListOfBasePoints: MutableList<Basepoints>
        GlobalScope.launch(Dispatchers.IO) {
            ListOfBasePoints =
                appdb.kibiraDao().getBasepointsForProject(PID) as MutableList<Basepoints>

            if (ListOfBasePoints.size == 0) {
                runOnUiThread {
                    warningAlert("Project Empty", PID, applicationContext)
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

    var polyline1: Polyline? = null
    var refPoint: LatLng? = null
    var distance = 0.0f
    var latLng: LatLng? = null


    private fun distanceToPoint(loc: LatLng) {
        val locationOfNextPoint = Location(LocationManager.GPS_PROVIDER)
        val locationOfRoverLatLng = Location(LocationManager.GPS_PROVIDER)
        val locationOfCurrentPoint = Location(LocationManager.GPS_PROVIDER)

        if (listOfPlantingLines.isEmpty()) {
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
                l.apply {
                    for (point in this) {
                        if (point == refPoint) {
                            index = indexOf(point)
                        }
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

                    locationOfNextPoint.apply {
                        latitude = latLng!!.latitude
                        longitude = latLng!!.longitude
                    }
                    locationOfRoverLatLng.apply {
                        latitude = loc.latitude
                        longitude = loc.longitude
                        distance = distanceTo(locationOfNextPoint)
                    }

                } else {
                    distance = 0f
                    var nextIndex = index - 1

                    latLng = l[nextIndex] as LatLng

                    if (latLng in listOfMarkedPoints) {

                        nextIndex = index + 1
                        latLng = l[nextIndex] as LatLng
                    }

                    locationOfNextPoint.apply {
                        latitude = latLng!!.latitude
                        longitude = latLng!!.longitude
                    }
                    locationOfRoverLatLng.apply {
                        latitude = loc.latitude
                        longitude = loc.longitude
                        distance = distanceTo(locationOfNextPoint)
                    }
                    locationOfCurrentPoint.apply {
                        latitude = refPoint.let {
                            latitude
                        }
                        longitude = refPoint.let {
                            longitude
                        }
                    }

                }

                position = keepUserInStraightLine(
                    locationOfCurrentPoint,
                    locationOfNextPoint,
                    locationOfRoverLatLng
                )
                //check that user is walking in straight line!
                val q = line.contains(loc)
                val p = isUserlocationOnPath(loc, l as MutableList<LatLng>)
                if (p || q) {
                    blink("On track")
                } else {
                    blink(position)
                }
                //Toast.makeText(context,"$p && $t",Toast.LENGTH_SHORT).show()

                val distanceInUnitsRespectiveToProject =
                    Conversions.ftToMeters(distance.toString(), gapUnits)
                statisticsWindow(
                    this,
                    size,
                    totalPoints,
                    l,
                    distanceInUnitsRespectiveToProject.toFloat()
                )

                //when straying from line
                if (distance > (GAP_SIZE_METRES * 0.5)) {
                    actionWhenPersonIsStraying(loc)

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

                if (positionOfPoint == 0) {
                    val nextPoint = positionOfPoint + 1
                    nextPointLatLng = l[nextPoint] as LatLng?
                }
                if (positionOfPoint == lastIndex) {
                    val nextPoint = positionOfPoint - 1
                    nextPointLatLng = l[nextPoint] as LatLng?
                }
                if (positionOfPoint != lastIndex && positionOfPoint != 0) {
                    if (positionOfPoint < 0) {
                        return
                    }
                    val nextPoint = positionOfPoint - 1
                    nextPointLatLng = l[nextPoint] as LatLng?
                }

                locationOfNextPoint.apply {
                    latitude = nextPointLatLng!!.latitude
                    longitude = nextPointLatLng.longitude
                }
                locationOfRoverLatLng.apply {
                    latitude = loc.latitude
                    longitude = loc.longitude
                    distance = distanceTo(locationOfNextPoint)
                }
                locationOfCurrentPoint.apply {
                    latitude = pointOfInterest.let {
                        latitude
                    }
                    longitude = pointOfInterest.let {
                        longitude
                    }
                }

                position = keepUserInStraightLine(
                    locationOfCurrentPoint,
                    locationOfNextPoint,
                    locationOfRoverLatLng
                )
                val p = isUserlocationOnPath(loc, l as MutableList<LatLng>)
                //Toast.makeText(context,"$p",Toast.LENGTH_SHORT).show()
                if (p) {
                    blink("On track")
                } else {
                    blink(position)
                }

            }
        }
    }

    fun actionWhenPersonIsStraying(loc: LatLng) {
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
    }

    lateinit var anim: ObjectAnimator

    fun blink(p: String) {
        val textViewToBlink = p

        when (textViewToBlink) {
            "Left" -> {
                directionImage.setImageResource(R.drawable.rightarrow)
                directionText.text = "Turn Left"
                directionImage.isVisible = true
                directionText.isVisible = true

//                if (player.isPlaying) {
//                    player.stop()
//                }
//                 beepingSoundForDirectionIndicator("Left")
            }
            "Right" -> {
                directionImage.setImageResource(R.drawable.leftarrow)
                directionImage.isVisible = true
                directionText.text = "Turn Right"
                directionText.isVisible = true
                //  beepingSoundForDirectionIndicator("Right")
            }
            "Stop" -> {
                directionText.isVisible = false
                directionImage.isVisible = false
                directionCardLayout.isVisible = false
            }
            "On track" -> {
                directionImage.setImageResource(R.drawable.tick)
                directionImage.isVisible = true
                directionText.text = "On Track"
                directionText.isVisible = true
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

    private fun stopBlink() {
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
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
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
                    }

                }

            }
            displayBluetoothDevices()
        }
    }

    fun discoverBluetoothDevices(){

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBT)
            return
        }
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

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
            bluetoothAdapter.startDiscovery()
        } else {
            bluetoothAdapter.startDiscovery()

        }
    }

    fun checkLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            }
        }
    }

    fun displayBluetoothDevices() {
        val bluetoothAdaptor = BluetoothAdapter.getDefaultAdapter() ?: return

        if (!bluetoothAdaptor.isEnabled) {
            val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBT)
            return
        }

        if (this.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            } != PackageManager.PERMISSION_GRANTED
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

        val items = bluetoothList.toArray()
        val adaptor =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                items
            )

        spinner.adapter = adaptor
        spinner.onItemSelectedListener = this
        spinner.visibility = Spinner.VISIBLE
        buttonConnect.setOnClickListener(this)

    }

    override fun onDestroy() {
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
        NmeaReader.listener.deactivate()
        fixType.isVisible=false
        fixTypeValue.isVisible =false
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
        val createNewProject = CreateProjectDialog
        createNewProject.show(supportFragmentManager, UserTag)
        return true
    }

    //Handling the options in the menu layout
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        if (item.itemId == R.id.bluetooth_spinner) {
            discoverBluetoothDevices()
            toggleWidgets()

            return true

        }

        return super.onOptionsItemSelected(item)
    }

    private fun toggleWidgets() {
        //TODO REMOVE AND USE TOGGLE BUTTONS INSTEAD

        if (spinner.visibility == Spinner.INVISIBLE && buttonConnect.visibility == Button.INVISIBLE) {
            spinner.visibility = Spinner.VISIBLE
            buttonConnect.visibility = Button.VISIBLE

            checkLocation()
            //displayBluetoothDevices()

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
                    CircleOptions().center(loc).fillColor(Color.YELLOW).radius(circleRadius)
                        .strokeWidth(1.0f)
                )
                val tapPt =  LatLng(
                    firstPoint!!.getLatitude(),
                    firstPoint!!.getLongitude()
                )
                val title  = "Tapped Point: ${tapPt.latitude} :  ${tapPt.longitude}"
                val title2  = "Current  Location: ${loc.latitude} :  ${loc.longitude}"
                map?.addMarker(MarkerOptions().position(tapPt).title(title).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

                map?.addMarker(MarkerOptions().position(loc).title(title2).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

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

        if (firstPoint == null || secondPoint == null || meshDone)
            return@OnMapClickListener

        val l = map?.addCircle(
            CircleOptions().center(loc).fillColor(Color.YELLOW).radius(circleRadius)
                .strokeWidth(1.0f)
        )
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                "Drawing grid! This won't take long..",
                Toast.LENGTH_LONG
            )
                .show()

        }

        saveBasepoints(firstPoint!!)
        saveBasepoints(secondPoint!!,false)

        plotMesh(firstPoint!!, secondPoint!!, 0, "", mutableListOf<LatLng>(), "")

        l?.remove()
        // marker?.remove()
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
                        MeshDirection.RIGHT
                    ) as MutableList<PlantingLine>

                    val drawPoints = ScaleLargeProjects.updateProjectLines(this)
                    Geometry.generateLongLat(c, drawPoints, drawLine)
                }
                "Square Grid" -> {
                    projectLines = Geometry.generateSquareMesh(
                        c,
                        p,
                        MeshDirection.RIGHT
                    ) as MutableList<PlantingLine>
                    val drawPoints = ScaleLargeProjects.updateProjectLines(this)
                    Geometry.generateLongLat(c, drawPoints, drawLine)
                }
            }
            meshDone = true

            handler.post { // Centre it...

                val firstpt =  LatLng(
                    cp.getLatitude(),
                    cp.getLongitude()
                )
                val secondPt =  LatLng(
                    pp.getLatitude(),
                    pp.getLongitude()
                )
                val title  = "First Point: ${firstpt.latitude} :  ${firstpt.longitude}"
                val title2  = "Second Point: ${secondPt.latitude} :  ${secondPt.longitude}"
                map?.addMarker(MarkerOptions().position(firstpt).title(title).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                map?.addMarker(MarkerOptions().position(secondPt).title(title2).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
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

        displayMarkedPointsOnUI(list)

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
            val end = System.currentTimeMillis()
            LocationDependantFunctions().pace(textViewPace, end, this)
            LocationDependantFunctions().markedLines(
                recentLine,
                listOfMarkedPoints,
                textViewMarkedLines
            )
        }

        //Get the process to be handled by the main thread through a handler--- makes the process faster
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
                        CircleOptions().center(xloc).fillColor(Color.RED).radius(circleRadius)
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
                    val c = map?.addCircle(
                        CircleOptions().center(xloc).fillColor(Color.YELLOW).radius(circleRadius)
                            .strokeWidth(1.0f)
                        //if set to zero, no outline is drawn
                    )
                    listofmarkedcircles.add(c!!)
                }

            }

        }

    }

    private fun undoDrawingLines() {

        undoAlertWarning(ProjectID.toInt(), this)
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

    var markedCirclePoint: Circle? = null
    var thread: Thread? = null
    fun markPoint(pointOfInterestOnPolyline: LatLng) {

        if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
            return
        }

        if (pointOfInterestOnPolyline in listOfMarkedPoints) {
            return
        }
        thread = Thread {
            thread!!.priority = Thread.MAX_PRIORITY
            val point = S2LatLng.fromDegrees(
                pointOfInterestOnPolyline.latitude,
                pointOfInterestOnPolyline.longitude
            )
            val pointData = PointData(point.toPoint(), point)
            lineInS2Format.remove(pointData)

            //hand over UI Updates to the UI Thread
            handler.post {
                if (listOfMarkedPoints.add(pointOfInterestOnPolyline)) {
                    plantingRadiusCircle?.remove()   //remove planting radius, marker and clear list

                    if (tempListMarker.isNotEmpty()) {
                        tempListMarker.clear()
                    }

                }

                if (templist.isNotEmpty()) {
                    templist.clear()
                }
                if (tempClosestPoint.isNotEmpty()) {
                    tempClosestPoint.clear()
                }
                mark(pointOfInterestOnPolyline)
                vibration(this)
                blinkEffectOfMarkedPoints("Cyan", displayedPoints)
                Toast.makeText(
                    this, "Point Marked", Toast.LENGTH_SHORT
                ).show()

            }

        }
        thread!!.start()

        DbFunctions.savePoints(pointOfInterestOnPolyline, ProjectID.toInt())

    }

    fun mark(pt: LatLng) {
        markedCirclePoint = map?.addCircle(
            CircleOptions().center(pt)
                .fillColor(Color.YELLOW)
                .radius(circleRadius)
                .strokeWidth(1.0f)
        )

        listofmarkedcircles.add(markedCirclePoint!!)
    }

    var bearing: Double? = null
    var diff: Float? = null
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

                current_measured_bearing = (magneticValues[0] * 180 / Math.PI).toFloat()
                if (current_measured_bearing < 0) current_measured_bearing += 360f

                val r = FloatArray(9)

                val values = FloatArray(3)
                SensorManager.getRotationMatrix(r, null, accelerometerValues, magneticValues)
                SensorManager.getOrientation(r, values)
                bearing = -Math.toDegrees(values[0].toDouble())
                val rotateDegree = (-Math.toDegrees(values[0].toDouble())).toFloat()
                diff = rotateDegree - lastRotateDegree
                val bearingAngle = abs(diff!!)

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
        Log.d("Level", "Level resumed")
        //provider = OrientationProvider.getInstance()

    }

    override fun onPause() {
        sensorManager!!.unregisterListener(listener)
        super.onPause()
        usbSupport.disconnect()
    }

    fun saveBasepoints(loc: LongLat, isStart:Boolean=true) {
        val lat = loc.getLatitude()
        val lng = loc.getLongitude()
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog.sharedPrefFile,
            Context.MODE_PRIVATE
        )!!

        val displayProjectName: TextView = findViewById(R.id.display_project_name)

        DbFunctions.ProjectID
        val editor = sharedPreferences.edit()
        editor.putString("productID_key", "$ProjectID")
        if (ProjectID == 0L) {
            Toast.makeText(this, "Create a project First!!", Toast.LENGTH_SHORT).show()
            return
        }

        val basePoints = Basepoints(null, lat, lng, ProjectID.toInt())

        GlobalScope.launch(Dispatchers.IO) {
            val d = appdb.kibiraDao().insertBasepoints(basePoints)

        }

    }

    fun radius(size: Double): Double {
        val sizeInCentimeters = size * 100
        return ((0.1 * sizeInCentimeters) / 100) + 1.0
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

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
                } else{
                    var y = 90
                }
               // Log.d("User","$userID")
               retrieveProjectsFromBackend(userID!!.toInt())
                return true
            }
            R.id.action_import_project->{
                // open file uploader
                openFilePicker()
            }
        }
        return true
    }

    //
    fun cleanUpExistingFragment() {

        meshDone = false
        plantingRadius?.remove()
        fabFlag = true
        card.isVisible = false
        pointCardview.isVisible = false
        directionCardLayout.isVisible = false

        for (item in polyLines) {
            item!!.remove()
        }
        removeMarkedCirclesFromUI(listofmarkedcircles)
        polyline1?.remove()

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

    fun  openFilePicker(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type="*/*"
        startActivityForResult(intent, REQUEST_CODE)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == REQUEST_CODE
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri ->
                val importedProject = ImportProject.getImportedProjectObject(uri,contentResolver)
                ///importedProject.coordinates is a list of JsonObjects.
                // save this to room db
                if (importedProject != null && userID != null){
                    val name = importedProject.name.replace("\\s".toRegex(), "").replace("\"","")
                    val gapSize = importedProject.gapsize
                    val lineLength=importedProject.lineLength
                    val meshType = importedProject.meshType.replace("\"","")
                    val gapSizeUnits = importedProject.gapsizeunits.replace("\\s".toRegex(), "").replace("\"","")
                    val lineLengthUnits = importedProject.lineLengthUnits.replace("\\s".toRegex(), "").replace("\"","")
                    val basePoints = importedProject.basePoints
                     val epoch = Calendar.getInstance().time
                   val project= Project(
                        id=null,
                        name= "$name-$epoch",
                        gapsize = gapSize,
                        lineLength = lineLength,
                        MeshType = meshType,
                        gapsizeunits=gapSizeUnits,
                        lineLengthUnits=lineLengthUnits,
                        userID=userID!!.toInt())
                    GlobalScope.launch(Dispatchers.IO) {
                        val projectUid = appdb.kibiraDao().insertProject(project)
                        for (coord in importedProject.coordinates!!){
                            val coordinate = coord as com.google.gson.JsonObject

                            val lat = coordinate.get("lat").asDouble
                            val lng = coordinate.get("lng").asDouble
                            val point = Coordinates(id=null, lat, lng, projectUid.toInt())
                            appdb.kibiraDao().insertCoordinates(point)
                        }
                        for(point  in basePoints){
                            val pointJson = point as com.google.gson.JsonObject
                            val lat =pointJson.get("lat").asDouble
                            val lng = pointJson.get("lng").asDouble
                            val pt = Basepoints(id=null, lat=lat,lng=lng, projectID=projectUid.toInt())
                            appdb.kibiraDao().insertBasepoints(pt);
                        }
                    }
                }



            }
        }
    }
}






