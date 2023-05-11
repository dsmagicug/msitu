package com.dsmagic.kibira.activities

/*
 *  This file is part of Msitu.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Msitu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Msitu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Msitu. If not, see <http://www.gnu.org/licenses/>
 */

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
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
import android.location.LocationManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
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
import com.dsmagic.kibira.R
import com.dsmagic.kibira.activities.CreateProjectDialog.jsonArray
import com.dsmagic.kibira.bluetooth.BluetoothFunctions
import com.dsmagic.kibira.dataReadings.*
import com.dsmagic.kibira.notifications.NotifyUserSignals
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.isUserlocationOnPath
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.keepUserInStraightLine
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.pulseEffectOnUserLocationCircle
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.vibrate
import com.dsmagic.kibira.roomDatabase.AppDatabase
import com.dsmagic.kibira.roomDatabase.DbFunctions
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.ProjectID
import com.dsmagic.kibira.roomDatabase.DbFunctions.Companion.retrieveMarkedPoints
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
import com.dsmagic.kibira.utils.GeneralHelper
import com.dsmagic.kibira.utils.ScaleLargeProjects
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.maps.android.ktx.utils.contains
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.PointData
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.*
import kotlin.math.abs
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private var marker: Circle? = null
    private var tempListMarker = mutableListOf<Marker>()
    private var lastLoc: Location? = null
    private var zoomLevel = 21.0f
    private var firstPoint: LongLat? = null
    private var secondPoint: LongLat? = null
    private val handler = Handler(Looper.getMainLooper())
    private var linesIndex = MutableS2ShapeIndex() // S2 index of lines...
    private var pointsIndex = S2PointIndex<S2LatLng>()
    private var asyncExecutor: ExecutorService = Executors.newCachedThreadPool()
    private var closestPointAndRadiusArray = ArrayList<Any>()
    private lateinit var fromRTKFeed: LatLng

    /*Declaring sensorManager and acceleration constants*/
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var plantingMode = false
    private var fabFlag = true
    private var zoomMode = false
    lateinit var createNewProject: CreateProjectDialog
    lateinit var areaDialog: AreaDialog
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
    lateinit var fab_area: FloatingActionButton
    lateinit var fab_moreLines: FloatingActionButton

    lateinit var longValue: TextView
    lateinit var latValue: TextView
    var delta = 0.146 // small enough to increase accuracy but big enough that it is achievable.
    var projectLoaded = false
    lateinit var positionText: TextView
    lateinit var positionLayout: LinearLayout
    lateinit var displayedDistance: TextView
    lateinit var displayedDistanceUnits: TextView
    lateinit var displayedPoints: TextView
    lateinit var fixType: TextView
    lateinit var fixTypeValue: TextView

    private val workerPool: ExecutorService = Executors.newFixedThreadPool(3)

    val usbSupport = USBSerialReader()
    lateinit var progressBar: ProgressBar
    lateinit var alertDialog: AlertDialog
    var showMe = true
    lateinit var whereIamMarker: Marker
    var locationString = ""
    lateinit var spinner: Spinner
    lateinit var buttonConnect: Button

    companion object {

        var device: BluetoothDevice? = null

        lateinit var projectLines: MutableList<PlantingLine>
        var lastFixType = ""
        var projectList = ArrayList<String>()
        var projectIDList = mutableListOf<Int>()
        var projectSizeList = mutableListOf<Double>()
        var projectMeshSizeList = mutableListOf<Double>()
        var meshTypeList = mutableListOf<String>()
        var gapUnitsList = mutableListOf<String>()

        lateinit var initialTimeValue: String
        val bluetoothList = ArrayList<String>()
        var map: GoogleMap? = null
        var toleranceCircleRadius = 0.0f
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
        lateinit var fab_center: FloatingActionButton
        lateinit var lineInS2Format: S2PointIndex<S2LatLng>
        var plantingToleranceCircle: Circle? = null
        var onLoad = false

        lateinit var projectStartPoint: Point
        var MeshType = " "
        var gapUnits = " "
        var meshUnits = " "
        var position = "None"
        var created = false
        var polyVertex = false

        var latVertexList = mutableListOf<Double>()
        var longVertexList = mutableListOf<Double>()
        val VerticePoints  = mutableListOf<LatLng>()

        /* Use a safer alternative to a cachedThreadPool (To prevent OutOfMemoryException) while handling processes
         that are short lived*/
        val corePoolSize = 4
        val maximumPoolSize = corePoolSize * 4
        val keepAliveTime = 100L
        val workQueue = SynchronousQueue<Runnable>()
        val MapWorkerPool: ExecutorService = ThreadPoolExecutor(
            corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue
        )

        var onCreation = false

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
        latValue = findViewById(R.id.latValue)
        longValue = findViewById(R.id.longValue)
        directionImage = findViewById(R.id.directionImageValue)
        positionText = findViewById(R.id.plantText)

        directionText = findViewById(R.id.directionText)
        pointCardview = findViewById(R.id.positionCardView)
        positionLayout = findViewById(R.id.plant)
        directionCardLayout = findViewById(R.id.directionsLayout)
        displayedDistance = findViewById<TextView>(R.id.distance)
        displayedDistanceUnits = findViewById(R.id.distanceUnits)
        displayedPoints = findViewById(R.id.numberOfPoints)
        fixType = findViewById(R.id.fixType)
        fixTypeValue = findViewById(R.id.fixTypeValue)
        toggle = ActionBarDrawerToggle(this, drawerlayout, R.string.open, R.string.close)

        drawerlayout.addDrawerListener(toggle)
        fab_center = findViewById(R.id.fab_center)
        fab_area = findViewById(R.id.fab_area)
        toggle.syncState()

        appdb = AppDatabase.dbInstance(this)
        navView.setNavigationItemSelectedListener(this)
        val headerView: View = navView.getHeaderView(0)
        val btnCloseDrawer = headerView.findViewById<View>(R.id.btnCloseDrawer) as ImageButton
        btnCloseDrawer.setOnClickListener {
            drawerlayout.closeDrawer(Gravity.LEFT)
        }

        //Change the map orientation to match the direction that the user is facing, once they have created a project.
        if (created) {
            val newAngel = GeneralHelper.sanitizeMagnetometerBearing(lastRotateDegree)
            if (map != null) {
                GeneralHelper.changeMapPosition(map, newAngel)
            }
            BearingPhoneIsFacing = newAngel
            created = false
        }

        fabCampus.setOnClickListener {
            /*
            * rotate the map accordingly
            * */
            val newAngel = GeneralHelper.sanitizeMagnetometerBearing(lastRotateDegree)
            if (map != null) {
                GeneralHelper.changeMapPosition(map, newAngel)
            }
            BearingPhoneIsFacing = newAngel
        }

        fab_reset.setOnClickListener {

            undoDrawingLines()
        }

        /*
        * show the current real time position of the user
        * */
        fab_center.setOnClickListener {
            if (showMe) {
                val loc = LatLng(lastLoc!!.latitude, lastLoc!!.longitude)
                val title = "Where I am"
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 21f))

                whereIamMarker = map?.addMarker(
                    MarkerOptions().position(loc).title(title).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                    )
                )!!
            } else {
                whereIamMarker.remove()
            }
            showMe = !showMe
        }

        /* draw more planting lines on the map */
        fab_moreLines.setOnClickListener {
            val drawPoints = ScaleLargeProjects.updateProjectLines(this)
            Geometry.generateLongLat(projectStartPoint, drawPoints, drawLine)
        }

        extras = intent.extras
        sharedPreferences = this.getSharedPreferences(
            CreateProjectDialog.sharedPrefFile, Context.MODE_PRIVATE
        )!!

        if (extras != null) {
            userID = extras!!.getString("userID")
            val APIToken = extras!!.getString("token")

            val editor = sharedPreferences.edit()
            editor.putString("userid_key", userID)
            editor.putString("apiToken_key", APIToken)
            editor.apply()

        } else {

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            workerPool.submit {
                retrieveProjectsFromBackend(userID!!.toInt())
            }
        }

        onLoad = true
        createDialog("onLoad")
        if (savedInstanceState == null) {

            val mapFragment =
                (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?)!!
            mapFragment.getMapAsync(callback)
        }
        onLoad = false

        usbSupport.manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter()
        filter.addAction(USBSerialReader.ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        registerReceiver(usbSupport.usbReceiver, filter)

        val intent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(BluetoothFunctions.receiver, intent)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager!!.registerListener(
            listener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager!!.registerListener(listener, accSensor, SensorManager.SENSOR_DELAY_NORMAL)
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH



        NmeaReader.listener.setLocationChangedTrigger(object : LocationChanged {
            override fun onLocationChanged(loc: Location, fix: LongLat.FixType) {

                if (map == null) return // Not yet...
                fromRTKFeed = LatLng(loc.latitude, loc.longitude)

                if (marker == null) {
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(fromRTKFeed, zoomLevel))
                    firstPoint = loc as LongLat // Grab it

                }
                latValue.text = loc.latitude.toString()
                longValue.text = loc.longitude.toString()
                // Get the displacement from the last position.
                val moved = NmeaReader.significantChange(lastLoc, loc)
                lastLoc = loc // Grab last location
                fab_center.isVisible = true
                if (moved) { // If it has changed, move the marker as well...
                    marker?.remove()
                    polyline1?.remove()

                    marker = map?.addCircle(
                        CircleOptions().center(fromRTKFeed).radius(circleRadius).strokeWidth(1.0f)
                            .strokeColor(Color.GREEN).fillColor(Color.BLUE)
                    )

                    marker?.let {
                        pulseEffectOnUserLocationCircle(it)
                    }
                    fixType.isVisible = true
                    fixTypeValue.isVisible = true
                    fixTypeValue.setTextColor(Color.WHITE)

                    val cFix = fix.toString()
                    if (cFix != lastFixType) {
                        fixTypeValue.text = cFix
                    }
                    lastFixType = cFix
                    if(onCreation){
                        Log.d("Null","$onCreation")
                        var firstPointC: LongLat? = null
                        var secondPointC:LongLat? = null
                        for (i in 0 until jsonArray.length()) {
                            val json = jsonArray.getJSONObject(i)
                            val lat = json.getDouble("lat")
                            val lng = json.getDouble("lng")
                            firstPointC = LongLat(lng, lat)
                            secondPointC = LongLat(lng, lat)

                        }
                        firstPointC.let { secondPointC.let { it1 ->
                            if (it != null && it1 != null) {
                                plotMesh(it, it1,0)
                            } else {
                                Log.d("Null","Null")
                            }
                        } }
                    }
                    else {
                        Log.d("Null","$onCreation")

                    }

                    if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
                        return

                    }

                    else {
                        val lineOfInterest = listOfPlantingLines[listOfPlantingLines.lastIndex]
                        val listOfPointsOnLineOfInterest = lineOfInterest.tag as MutableList<LatLng>
                        getClosestPointOnLineRelativeToUserLocation(
                            fromRTKFeed,
                            listOfPointsOnLineOfInterest
                        )
                        distanceToTheNextPointOfInterest(fromRTKFeed)
                        approachingPoint()
                    }

                }
            }

        })


        fab_map.setOnClickListener {
            try {

                MapWorkerPool.submit {
                    handler.post {
                        fabMapAction()
                    }

                }


            } catch (e: Exception) {
                Log.d("TAG", "$e")
            }
        }

        fab_area.setOnClickListener {
            try {
                val u = 70
                AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setIcon(R.drawable.caution)
                    .setMessage("Add point to list of Vertices")
                    .setPositiveButton(
                        "Add ",
                        DialogInterface.OnClickListener { _, _ ->
                            if (lastLoc != null) {
                                val lat = lastLoc?.latitude
                                val long = lastLoc?.longitude
                                if (lat != null && long != null) {
                                    latVertexList.add(lat)
                                    longVertexList.add(long)
                                    VerticePoints.add(LatLng(lat,long))
                                    Toast.makeText(this, "Point added", Toast.LENGTH_SHORT).show()
                                }

                            } else {

                                Toast.makeText(this, "No point to use", Toast.LENGTH_SHORT).show()
                            }

                            polyVertex = true
                        })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { _, _ ->
                            polyVertex = false
                        })
                    .show()


            } catch (e: Exception) {
                Log.d("TAG", "$e")
            }
        }

        val clipBoardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipBoardManager.addPrimaryClipChangedListener {
            val items = clipBoardManager.primaryClip?.itemCount

            val clipBoardBasepoint1 = clipBoardManager.primaryClip?.getItemAt(0)

            Log.d("first Point", clipBoardBasepoint1.toString())
            Log.d("Number", items.toString())

        }


    }

    fun fabMapAction() {
        val activePlantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]

        if (!fabFlag) {

            fab_map.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext, R.drawable.ic_baseline_map_24
                )
            )
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            runOnUiThread {
                polyLines.forEach {
                    it?.isVisible = false
                    it?.isClickable = false
                }

            }
            card.isVisible = false
            directionCardLayout.isVisible = false
            activePlantingLine.isVisible = true
            removeMarkedCirclesFromUI(listofmarkedcircles)
            val fix: TextView = findViewById(R.id.fixTypeValue)
            fix.setTextColor(Color.WHITE)

            fabFlag = true

        } else if (fabFlag) {
            val fix: TextView = findViewById(R.id.fixTypeValue)
            fix.setTextColor(Color.BLACK)

            fab_map.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext, R.drawable.walk_mode
                )
            )
//            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            runOnUiThread {
                polyLines.forEach {
                    it?.isVisible = true
                    it?.isClickable = true
                }

                activePlantingLine.isVisible = true
            }

            displayMarkedPointsOnUI(listOfMarkedPoints)
            fabFlag = false
        }
    }

    private fun approachingPoint() {

        if (closestPointAndRadiusArray.size > 0) {
            val pointOfInterest = closestPointAndRadiusArray[0] as LatLng
            val distanceAway = GeneralHelper.findDistanceBtnTwoPoints(
                fromRTKFeed, pointOfInterest
            )

            if ((distanceAway < toleranceCircleRadius)) {
                if (pointOfInterest in listOfMarkedPoints) {
                    if (distanceAway < delta) {
                        NotifyUserSignals.flashSignal("Green", positionText, this)
                        vibrate(this)

                    } else {
                        NotifyUserSignals.flashSignal("Orange", positionText, this)

                    }
                } else {

                    NotifyUserSignals.flashSignal("Orange", positionText, this)

                    if (distanceAway < delta) {
                        NotifyUserSignals.flashSignal("Green", positionText, this)

                        workerPool.submit {
                            markPoint(pointOfInterest)
                        }
                    }
                }
            }

            if (distanceAway > toleranceCircleRadius) {
                NotifyUserSignals.flashSignal("Stop", positionText, this)
            }

        }
    }

    fun displayMarkedPointsOnUI(List: MutableList<LatLng>) {
        removeMarkedCirclesFromUI(listofmarkedcircles)
        MapWorkerPool.submit {
            runOnUiThread {
                if (List.isNotEmpty()) {
                    List.forEach {
                        val c = map?.addCircle(
                            CircleOptions().center(it).fillColor(Color.YELLOW).radius(circleRadius)
                                .strokeWidth(1.0f)
                        )
                        listofmarkedcircles.add(c!!)
                    }

                }
            }

        }

    }

    private fun removeMarkedCirclesFromUI(List: MutableList<Circle>) {
        if (List.isNotEmpty()) {
            List.forEach {
                it.remove()
            }
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
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
            Alerts.exitAlert("Quitting app! \n\nAre you sure?", this)
        }
        pressedTime = System.currentTimeMillis()
    }

    var selectedProject: String = " "

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
        alertDialog = AlertDialog.Builder(this).setTitle("Projects").setSingleChoiceItems(
            l,
            checkedItemIndex,
            DialogInterface.OnClickListener { dialog, which ->
                checkedItemIndex = which
                selectedProject = projectListAsArray[which]
            })
            .setNegativeButton("Export", DialogInterface.OnClickListener { _, _ ->
                //request storage permission
                requestPermission()
                for (j in projectListAsArray) {
                    if (j == selectedProject) {
                        val index = projectListAsArray.indexOf(j)
                        val id = projectIDList[index]
                        Alerts.confirmAlert(
                            "\nConfirm Export Project '$selectedProject'", id, this
                        )
                    }

                }

            }).setNeutralButton("More..", DialogInterface.OnClickListener { _, _ ->
                displayMoreProjects(projectListAsArray)
            }).setPositiveButton("Open", DialogInterface.OnClickListener { _, _ ->

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
        alertDialog = AlertDialog.Builder(this).setTitle("All Projects")
            .setSingleChoiceItems(
                list,
                checkedItemIndex,
                DialogInterface.OnClickListener { _, which ->
                    checkedItemIndex = which
                    selectedProject = list[which]
                }).setNegativeButton("Delete", DialogInterface.OnClickListener { _, _ ->
                for (j in list) {
                    if (j == selectedProject) {
                        val index = list.indexOf(j)
                        val id = projectIDList[index]

                        Alerts.DeleteAlert(
                            "\nProject '$selectedProject' will be deleted permanently.\n\nAre you sure?",
                            id,
                            this
                        )
                    }

                }


            }).setPositiveButton("Open",

                DialogInterface.OnClickListener { _, _ ->

                    if (selectedProject == "") {
                        return@OnClickListener
                    } else {
                        for (j in list) {
                            if (j == selectedProject) {
                                val index = list.indexOf(j)
                                val id = projectIDList[index]
                                val projectGapSize = projectSizeList[index]
                                val projectMeshSize = projectMeshSizeList[index]
                                val projectMeshType = meshTypeList[index]
                                val gapUnits = gapUnitsList[index]
                                loadProject(
                                    id, projectMeshSize, projectGapSize, projectMeshType, gapUnits
                                )
                            }
                        }
                    }

                })

            .show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun retrieveProjectsFromBackend(UID: Int): MutableList<String> {

        GlobalScope.launch(Dispatchers.IO) {

            val listOfProjects = appdb.kibiraDao().getAllProjects(UID)

            if (listOfProjects.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "No projects Created yet", Toast.LENGTH_LONG)
                        .show()
                }
            } else {

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

    /*
    * Android versions below 11 require for explicit permission for device storage
    * */
    private fun requestPermission() {
        var r = 10
        if (SDK_INT >= Build.VERSION_CODES.R) {
//            try {
//                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
//                intent.addCategory("android.intent.category.DEFAULT")
//                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
//                this.startActivityForResult(intent, 2296)
//            } catch (e: java.lang.Exception) {
//                val intent = Intent()
//                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
//
//                startActivityForResult(intent, 2296)
//            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                2
            )

            if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_EXTERNAL_STORAGE),
                    2
                )
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun loadProject( PID: Int, Meshsize: Double, Gapsize: Double, meshType: String, gapUnits: String) {
        val displayProjectName: TextView? = findViewById(R.id.display_project_name)

        Toast.makeText(
            this, "Loading project, This may take some few minutes time." + "", Toast.LENGTH_LONG
        ).show()
        cleanUpExistingFragment()

        listOfMarkedPoints = retrieveMarkedPoints(PID)

        var listOfBasePoints: MutableList<Basepoints>
        GlobalScope.launch(Dispatchers.IO) {
            listOfBasePoints =
                appdb.kibiraDao().getBasepointsForProject(PID) as MutableList<Basepoints>

            if (listOfBasePoints.size == 0) {
                runOnUiThread {
                    warningAlert("Project has no base points", PID, this@MainActivity)
                }
            } else {
                val l = listOfBasePoints[0]
                val y = listOfBasePoints[1]
                val firstPoint = LongLat(l.lng, l.lat)
                val secondPoint = LongLat(y.lng, y.lat)

                runOnUiThread {
                    displayProjectName!!.text = selectedProject
                    GAP_SIZE_METRES = Gapsize
                    MAX_MESH_SIZE = Meshsize

                    plotMesh(firstPoint, secondPoint, PID, meshType, gapUnits, listOfMarkedPoints)

                }
            }
        }

    }

    var polyline1: Polyline? = null
    var refPoint: LatLng? = null
    var distance = 0.0f
    var latLng: LatLng? = null


    /*
    * function to calculate the distance between the next point to be marked and the user location(rover location)
    * The next point to be marked can be either in front or behind the user, depending on the planting direction
    * being followed at that time. hence the degree stuff.
    * */
    private fun distanceToTheNextPointOfInterest(loc: LatLng) {
        val locationOfNextPoint = Location(LocationManager.GPS_PROVIDER)
        val locationOfRoverLatLng = Location(LocationManager.GPS_PROVIDER)
        val locationOfCurrentPoint = Location(LocationManager.GPS_PROVIDER)

        if (listOfPlantingLines.isEmpty()) {
            return
        }
        val line = listOfPlantingLines[listOfPlantingLines.lastIndex]
        val lineAsList = line.tag as MutableList<LatLng>
        val l = lineAsList.asSequence()

        if (plantingMode && listOfMarkedPoints.isNotEmpty()) {
            fab_reset.isVisible = false

            card.isVisible = true
            directionCardLayout.isVisible = true

            refPoint = listOfMarkedPoints[listOfMarkedPoints.lastIndex]
            var index = 0

            try {

                l.forEachIndexed { LatLngIndex, i ->
                    if (i == refPoint)
                        index = LatLngIndex
                }

                // when the point is behind the user
                if (lastRotateDegree in (-180.0..90.0)) {

                    distance = 0f
                    var nextIndex = index + 1
                    latLng = lineAsList[nextIndex]

                    if (latLng in listOfMarkedPoints) {
                        nextIndex = index - 1

                        latLng = lineAsList[nextIndex]
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

                }
                //when point is in front of the user
                else {
                    distance = 0f
                    var nextIndex = index - 1

                    latLng = lineAsList[nextIndex]

                    if (latLng in listOfMarkedPoints) {

                        nextIndex = index + 1
                        latLng = lineAsList[nextIndex]
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
                    locationOfCurrentPoint, locationOfNextPoint, locationOfRoverLatLng
                )
                //check that user is walking in straight line!
                val q = line.contains(loc)
                val p = isUserlocationOnPath(loc, lineAsList)
                if (p || q) {
                    blink("On track")
                } else {
                    blink(position)
                }

//                displayedDistance.text = distance.toString()
                displayedPoints.text = listOfMarkedPoints.size.toString()


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
            if (closestPointAndRadiusArray.size > 0) {
                directionCardLayout.isVisible = true
                val pointOfInterest = closestPointAndRadiusArray[0] as LatLng
                val positionOfPoint = l.indexOf(pointOfInterest)

                val lastIndex = lineAsList.lastIndex
                var nextPointLatLng: LatLng? = null

                if (positionOfPoint == 0) {
                    val nextPoint = positionOfPoint + 1
                    nextPointLatLng = lineAsList[nextPoint]
                }
                if (positionOfPoint == lastIndex) {
                    val nextPoint = positionOfPoint - 1
                    nextPointLatLng = lineAsList[nextPoint]
                }
                if (positionOfPoint != lastIndex && positionOfPoint != 0) {
                    if (positionOfPoint < 0) {
                        return
                    }
                    val nextPoint = positionOfPoint - 1
                    nextPointLatLng = lineAsList[nextPoint]
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
                    locationOfCurrentPoint, locationOfNextPoint, locationOfRoverLatLng
                )
                val p = isUserlocationOnPath(loc, lineAsList)
                if (p) {
                    blink("On track")
                } else {
                    blink(position)
                }

            }
        }
    }

    private fun actionWhenPersonIsStraying(loc: LatLng) {
        polyline1 = map?.addPolyline(
            PolylineOptions().color(Color.BLACK).jointType(JointType.DEFAULT).width(3.5f)
                .geodesic(true).startCap(RoundCap()).add(
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

        when (p) {
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
            "On track" -> {
                directionImage.setImageResource(R.drawable.tick)
                directionImage.isVisible = true
                directionText.text = "On Track"
                directionText.isVisible = true
            }
        }

    }

    fun blinkEffectOfMarkedPoints(color: String, T: TextView) {
        var animationColor = 0
        when (color) {
            "GreenHex" -> {
                animationColor = Color.rgb(0, 206, 209)
            }
            "Orange" -> {
                animationColor = Color.rgb(255, 215, 0)
            }
            "Red" -> {
                animationColor = Color.RED

            }
            "Green" -> {
                animationColor = Color.GREEN

            }
        }
        handler.post {
            anim = ObjectAnimator.ofInt(
                T,
                "backgroundColor",
                animationColor,
                Color.WHITE,
                animationColor,
                animationColor
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
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        isLocationTurnedOn()
                        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT)
                            .show()

                    }

                } else {
                    //Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            2 -> {

                if (ActivityCompat.checkSelfPermission(
                        this, WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT)
                        .show()
                } else {

                    Toast.makeText(
                        this,
                        "Allow permission for storage access in order to export projects!",
                        Toast.LENGTH_SHORT
                    )
                        .show()


                }
            }
            2296 -> {
                if (ActivityCompat.checkSelfPermission(
                        this, WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT)
                        .show()
                } else {

                    Toast.makeText(
                        this,
                        "Allow permission for storage access in order to export projects!",
                        Toast.LENGTH_SHORT
                    )
                        .show()


                }
            }

        }

    }

    var REQUEST_CHECK_SETTINGS = 199

    //check whether Location is enabled in settings, and if not, request for it from user
    fun isLocationTurnedOn() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { _ ->
            // All location settings are satisfied. The client can initialize
//            Toast.makeText(this, "GPS Turned on!", Toast.LENGTH_SHORT)
//                .show()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this@MainActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun checkLocationPermissions(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                activity.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            }
        } else {
            isLocationTurnedOn()
        }

    }

    override fun onDestroy() {
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(BluetoothFunctions.receiver)
        NmeaReader.listener.deactivate()
        createNewProject.dismiss()
        sensorManager!!.unregisterListener(listener)
        super.onDestroy()
    }

    override fun onStop() {
        createNewProject.dismiss()
        super.onStop()
    }

    // Display the menu layout
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appmenu1, menu)
        return true
    }

    // display the create dialog if no projects available.
    private fun createDialog(UserTag: String): Boolean {
        createNewProject = CreateProjectDialog
        createNewProject.show(supportFragmentManager, UserTag)
        return true
    }

    //Handling the options in the menu layout
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        if (item.itemId == R.id.bluetooth_spinner) {

            BluetoothFunctions.discoverBluetoothDevices(this)
            checkLocationPermissions(this)
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


        } else {
            spinner.visibility = Spinner.INVISIBLE
            buttonConnect.visibility = Button.INVISIBLE
        }

    }

     val drawLine: (List<LongLat>) -> Unit = { it ->

        val ml = it.map {
            LatLng(
                it.getLatitude(), it.getLongitude()
            )
        } // Convert to LatLng as expected by polyline

        val poly =
            PolylineOptions().addAll(ml).color(Color.BLUE).jointType(JointType.DEFAULT).width(3f)
                .geodesic(true).startCap(RoundCap()).endCap(SquareCap())
        handler.post {
            val p = map?.addPolyline(poly) // Add it and set the tag to the line...
            // Add it to the index
            val idx = polyLines.size
            S2Helper.addS2Polyline2Index(
                idx, linesIndex, S2Helper.makeS2PolyLine(ml, pointsIndex)
            )
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
                    CircleOptions().center(loc).fillColor(Color.YELLOW).radius(circleRadius)
                        .strokeWidth(1.0f)
                )
                val tapPt = LatLng(
                    firstPoint!!.getLatitude(), firstPoint!!.getLongitude()
                )
                val title = "Tapped Point: ${tapPt.latitude} :  ${tapPt.longitude}"
                val title2 = "Current  Location: ${loc.latitude} :  ${loc.longitude}"
                map?.addMarker(
                    MarkerOptions().position(tapPt).title(title).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                )

                map?.addMarker(
                    MarkerOptions().position(loc).title(title2).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                )

                map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            firstPoint!!.getLatitude(), firstPoint!!.getLongitude()
                        ), 21.0f
                    )
                )

            }

            return@OnMapClickListener
        }
        secondPoint = pt

        if (firstPoint == null || secondPoint == null || meshDone) return@OnMapClickListener

        val l = map?.addCircle(
            CircleOptions().center(loc).fillColor(Color.YELLOW).radius(circleRadius)
                .strokeWidth(1.0f)
        )
        runOnUiThread {
            Toast.makeText(
                applicationContext, "Drawing grid! This won't take long..", Toast.LENGTH_LONG
            ).show()

        }

        saveBasepoints(firstPoint!!)
        saveBasepoints(secondPoint!!, false)

        plotMesh(firstPoint!!, secondPoint!!, 0)

        l?.remove()
    }

    @Suppress("UNCHECKED_CAST")
     fun plotMesh(
        firstBasePoint: LongLat,
        secondBasePoint: LongLat,
        id: Int,
        vararg Misc: Any
    ) {
        var listOfSavedMarkedPoints = mutableListOf<LatLng>()

        // i.e calling this func with parameters from the db, hence id will be id of the project
        if (id != 0) {
            ProjectID = id.toLong()
            MeshType = Misc[0].toString()
            gapUnits = Misc[1].toString()
            listOfSavedMarkedPoints = Misc[2] as MutableList<LatLng>


        }
        progressBar.isVisible = true

        asyncExecutor.execute {

            val c = Point(firstBasePoint)
            val p = Point(secondBasePoint)
            projectStartPoint = c

            when (MeshType) {
                "Triangular Grid" -> {
                    projectLines = Geometry.generateTriangleMesh(
                        c, p, MeshDirection.RIGHT
                    ) as MutableList<PlantingLine>

                    val drawPoints = ScaleLargeProjects.updateProjectLines(this)
                    Geometry.generateLongLat(c, drawPoints, drawLine)
                }
                "Square Grid" -> {
                    projectLines = Geometry.generateSquareMesh(
                        c, p, MeshDirection.RIGHT
                    ) as MutableList<PlantingLine>
                    val drawPoints = ScaleLargeProjects.updateProjectLines(this)
                    Geometry.generateLongLat(c, drawPoints, drawLine)
                }
            }
            meshDone = true

            handler.post {

                val firstpt = LatLng(
                    firstBasePoint.getLatitude(), firstBasePoint.getLongitude()
                )
                val secondPt = LatLng(
                    secondBasePoint.getLatitude(), secondBasePoint.getLongitude()
                )
                val title = "First Point: ${firstpt.latitude} :  ${firstpt.longitude}"
                val title2 = "Second Point: ${secondPt.latitude} :  ${secondPt.longitude}"
                map?.addMarker(
                    MarkerOptions().position(firstpt).title(title).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                )
                map?.addMarker(
                    MarkerOptions().position(secondPt).title(title2).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                )
                map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            firstBasePoint.getLatitude(), firstBasePoint.getLongitude()
                        ), 21.0f
                    )
                )

            }
        }

        displayMarkedPointsOnUI(listOfSavedMarkedPoints)
        progressBar.isVisible = false
        projectLoaded = true
        fab_reset.show()
        onCreation = false
    }

    private val onPolyClick = GoogleMap.OnPolylineClickListener { polyline ->
        // rotate the map accordingly
        val newAngel = GeneralHelper.sanitizeMagnetometerBearing(lastRotateDegree)
        if (map != null) {
            GeneralHelper.changeMapPosition(map, newAngel)
            BearingPhoneIsFacing = newAngel
        }
        polyline.isClickable = false
        if (listOfPlantingLines.isEmpty()) {
            listOfPlantingLines.add(polyline)
            polyline.color = Color.GREEN

            Toast.makeText(applicationContext, "Planting line selected...", Toast.LENGTH_SHORT)
                .show()
        } else {

            val recentLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
            recentLine.color = Color.CYAN
            recentLine.isClickable = true
            listOfPlantingLines.clear()
            listOfPlantingLines.add(polyline)
            polyline.color = Color.GREEN
            Toast.makeText(applicationContext, "Switching Lines..", Toast.LENGTH_SHORT).show()

            //remove the planting radius circle if it exists
            if (listOfToleranceCircles.isNotEmpty()) {
                listOfToleranceCircles[listOfToleranceCircles.lastIndex].remove()
                listOfToleranceCircles.clear()
            }

        }

        //Handle this processing in a different thread, since they are short lived tasks
        // use a cached thread pool.
        MapWorkerPool.submit {
            handler.post {
                //make the lines and circles on other poly-lines disappear, once we have line of interest,

                polyLines.forEach {
                    it!!.isVisible = false
                    it.isClickable = false
                }

                if (unmarkedCirclesList.isNotEmpty()) {
                    unmarkedCirclesList.forEach {
                        it.remove()
//                    it.isVisible = true
                    }

                    unmarkedCirclesList.clear()
                }

                if (listofmarkedcircles.isNotEmpty()) {
                    listofmarkedcircles.forEach {
                        it.remove()
//                    it.isVisible = true
                    }
                }
                polyline.isClickable = true
                polyline.isVisible = true
                val l = polyline.tag as MutableList<*>
                plotLine(l)
                fab_map.show()
            }

            handler.post {
                map?.mapType = GoogleMap.MAP_TYPE_NORMAL
                val target = map?.cameraPosition?.target
                val bearing = map?.cameraPosition?.bearing
                val cameraPosition =
                    CameraPosition.Builder().target(target!!).zoom(21f).bearing(bearing!!).build()
                map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }

        }
        zoomMode = true
    }

    private fun plotLine(line: MutableList<*>) {
        //clear it so that the points to loop through are not many

        if (unmarkedCirclesList.isNotEmpty()) {
            unmarkedCirclesList.clear()

        }
//Processing includes UI updates, and time it takes is unpredictable, so use a fixed thread pool
// to handle it.
        workerPool.submit {
            handler.post {
                //A sequence scales better than a list, with large items.
                val lineAsSequence = line.asSequence()
                val markedPointsSequence = lineAsSequence.filter {
                    it in listOfMarkedPoints
                }

                if (markedPointsSequence.count() != 0) {
                    for (pt in markedPointsSequence) {
                        val c = map?.addCircle(
                            CircleOptions().center(pt as LatLng).fillColor(Color.YELLOW)
                                .radius(circleRadius)
                                .strokeWidth(1.0f)
                            //if set to zero, no outline is drawn
                        )
                        listofmarkedcircles.add(c!!)
                    }

                    val unMarkedPointsSequence = lineAsSequence.minus(markedPointsSequence)

                    for (pt in unMarkedPointsSequence) {
                        val unmarkedCircles = map?.addCircle(
                            CircleOptions().center(pt as LatLng).fillColor(Color.RED)
                                .radius(circleRadius)
                                .strokeWidth(1.0f)
                        )
                        unmarkedCirclesList.add(unmarkedCircles!!)
                    }

                } else {
                    line.forEach {
                        val unmarkedCircles = map?.addCircle(
                            CircleOptions().center(it as LatLng).fillColor(Color.RED)
                                .radius(circleRadius)
                                .strokeWidth(1.0f)
                        )
                        unmarkedCirclesList.add(unmarkedCircles!!)
                    }

                }
            }
        }

    }

    private fun undoDrawingLines() {

        undoAlertWarning(ProjectID.toInt(), this)
    }

    var plantingRadiusCircle: Circle? = null
    private val listOfToleranceCircles = mutableListOf<Circle>()
    val tempClosestPoint =
        mutableSetOf<LatLng>() //set allow only unique elements which is what we want


    private fun getClosestPointOnLineRelativeToUserLocation(
        roverPoint: LatLng,
        listOfPointsOnlineOfInterest: MutableList<LatLng>
    ) {

        closestPointAndRadiusArray.clear()
        fab_reset.hide()

        /* Convert polyline of interest into an S2 Line, so that we can use S2 Geometry, to get the closest point
         to the user-- its much faster
        */
        lineInS2Format = GeneralHelper.convertLineToS2(listOfPointsOnlineOfInterest)
        val xloc = S2Helper.findClosestPointOnLine(lineInS2Format, roverPoint) as S2LatLng?

        if (xloc != null) {
            val pt = LatLng(xloc.latDegrees(), xloc.lngDegrees())
            closestPointAndRadiusArray.add(pt)

            if (pt in listOfMarkedPoints) {
                return
            }
            /*
            * Add the found point to a set, it is used to track the points as the user moves along the line
            *  */
            tempClosestPoint.add(pt)

            /* Focus only on the points on the line of interest*/
            if (pt !in listOfPointsOnlineOfInterest) {
                return
            } else {
                /*
                * listOfToleranceCircles holds the light green circle that shows planting tolerance
                * */
                if (listOfToleranceCircles.isNotEmpty()) {   //we have found a point on the line
                    for (c in listOfToleranceCircles) {
                        if (c.center == pt) {
                            return    //do nothing if a circle is already drawn at that point
                        }
                        if (c.center !in tempClosestPoint) {
                            //remove the planting tolerance circle as one walks away from that point
                            c.remove()

                        }
                    }

                }
            }

            plantingToleranceCircle = map?.addCircle(
                CircleOptions().center(pt).fillColor(Color.GREEN).radius(radius(GAP_SIZE_METRES))
                    .strokeWidth(1.0f).fillColor(0x22228B22).strokeColor(Color.GREEN)
            )!!

        }

        if (tempClosestPoint.isNotEmpty()) {
            tempClosestPoint.clear()
        }

        if (plantingToleranceCircle != null) {
            closestPointAndRadiusArray.add(plantingToleranceCircle!!.radius.toFloat())

            if (listOfToleranceCircles.isNotEmpty()) {
                listOfToleranceCircles.forEach {
                    it.remove()
                }
                listOfToleranceCircles.clear()
                listOfToleranceCircles.add((plantingToleranceCircle!!))

            } else {
                listOfToleranceCircles.add((plantingToleranceCircle!!))
            }
            /*
            * The function that calculates the closest point, returns multiple points depending on the position the user is
            * in. But we are always interested in the most recent tolerance circle drawn
            * as that is the best reflection of where the user is, hence it is our circle of interest
            *  */
            plantingRadiusCircle = listOfToleranceCircles[listOfToleranceCircles.lastIndex]
            plantingMode = true
            toleranceCircleRadius = plantingToleranceCircle!!.radius.toFloat()

        }
    }

    var markedCirclePoint: Circle? = null
    fun markPoint(pointOfInterestOnPolyline: LatLng) {

        if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
            return
        }

        if (pointOfInterestOnPolyline in listOfMarkedPoints) {
            return
        }
        val point = S2LatLng.fromDegrees(
            pointOfInterestOnPolyline.latitude, pointOfInterestOnPolyline.longitude
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
            if (listOfToleranceCircles.isNotEmpty()) {
                listOfToleranceCircles.clear()
            }
            if (tempClosestPoint.isNotEmpty()) {
                tempClosestPoint.clear()
            }

            mark(pointOfInterestOnPolyline)
            vibrate(this)

            blinkEffectOfMarkedPoints("Green", displayedPoints)
            Toast.makeText(
                this, "Point Marked", Toast.LENGTH_SHORT
            ).show()
        }
        DbFunctions.savePoints(pointOfInterestOnPolyline, ProjectID.toInt())
    }

    fun mark(pt: LatLng) {
        markedCirclePoint = map?.addCircle(
            CircleOptions().center(pt).fillColor(Color.YELLOW).radius(circleRadius)
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
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(listener)
        super.onPause()
        usbSupport.disconnect()
        // kill the dialog
        createNewProject.dismiss()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveBasepoints(loc: LongLat, isStart: Boolean = true) {
        val lat = loc.getLatitude()
        val lng = loc.getLongitude()
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog.sharedPrefFile, Context.MODE_PRIVATE
        )!!

        ProjectID
        val editor = sharedPreferences.edit()
        editor.putString("productID_key", "$ProjectID")
        editor.apply()
        if (ProjectID == 0L) {
            Toast.makeText(this, "Create a project First!!", Toast.LENGTH_SHORT).show()
            return
        }

        val basePoints = Basepoints(null, lat, lng, ProjectID.toInt())

        GlobalScope.launch(Dispatchers.IO) {
            appdb.kibiraDao().insertBasepoints(basePoints)
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
                }

                workerPool.submit {
                    retrieveProjectsFromBackend(userID!!.toInt())
                }

                return true
            }
            R.id.action_import_project -> {
                // open file uploader
                openFilePicker()
            }
            R.id.action_clipboard_copy -> {
                if (lastLoc != null) {
                    val lat = lastLoc?.latitude
                    val long = lastLoc?.longitude
                    locationString += "{\"lat\":$lat, \"lng\":$long},"
                    copyToClipBoard(locationString, "Current position")
                } else {

                    Toast.makeText(this, "No location to copy", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.action_area -> {
                val area = AreaDialog
                area.show(supportFragmentManager, "AREA")
                return true
            }
        }
        return true
    }

    fun cleanUpExistingFragment() {

        meshDone = false
        plantingToleranceCircle?.remove()
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

    fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_CODE)

    }

    fun copyToClipBoard(str: String?, label: String?) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(label, str)
        clipboardManager.setPrimaryClip(clipData)
//        Toast.makeText(this, "Base coordinates to clipboard!", Toast.LENGTH_SHORT).show()
    }


    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri ->
                val importedProject = ImportProject.getImportedProjectObject(uri, contentResolver)
                ///importedProject.coordinates is a list of JsonObjects.
                // save this to room db
                if (importedProject != null && userID != null) {
                    val name = importedProject.name.replace("\\s".toRegex(), "").replace("\"", "")
                    val gapSize = importedProject.gapsize
                    val lineLength = importedProject.lineLength
                    val meshType = importedProject.meshType.replace("\"", "")
                    val gapSizeUnits =
                        importedProject.gapsizeunits.replace("\\s".toRegex(), "").replace("\"", "")
                    val lineLengthUnits =
                        importedProject.lineLengthUnits.replace("\\s".toRegex(), "")
                            .replace("\"", "")
                    val basePoints = importedProject.basePoints
                    val epoch = Calendar.getInstance().time
                    val project = Project(
                        id = null,
                        name = name,
                        gapsize = gapSize,
                        lineLength = lineLength,
                        MeshType = meshType,
                        gapsizeunits = gapSizeUnits,
                        lineLengthUnits = lineLengthUnits,
                        userID = userID!!.toInt()
                    )
                    GlobalScope.launch(Dispatchers.IO) {
                        val projectUid = appdb.kibiraDao().insertProject(project)
                        for (coord in importedProject.coordinates!!) {
                            val coordinate = coord as com.google.gson.JsonObject

                            val lat = coordinate.get("lat").asDouble
                            val lng = coordinate.get("lng").asDouble
                            val point = Coordinates(id = null, lat, lng, projectUid.toInt())
                            appdb.kibiraDao().insertCoordinates(point)
                        }
                        for (point in basePoints) {
                            val pointJson = point as com.google.gson.JsonObject
                            val lat = pointJson.get("lat").asDouble
                            val lng = pointJson.get("lng").asDouble
                            val pt = Basepoints(
                                id = null, lat = lat, lng = lng, projectID = projectUid.toInt()
                            )
                            appdb.kibiraDao().insertBasepoints(pt)
                        }
                    }
                }


            }
        }
    }
}






