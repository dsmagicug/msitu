package com.dsmagic.kibira


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
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.dsmagic.kibira.services.*
import com.dsmagic.kibira.utils.GeneralHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dilivia.s2.S2LatLng
import dilivia.s2.index.point.PointData
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt
import kotlin.properties.Delegates


open class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    View.OnClickListener {
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
    var meshDone = false
    var linesIndex = MutableS2ShapeIndex() // S2 index of lines...

    var pointsIndex = S2PointIndex<S2LatLng>()
    var polyLines = ArrayList<Polyline?>()

    var asyncExecutor: ExecutorService = Executors.newCachedThreadPool()

    var currentLocation = mutableListOf<LatLng>()
    var clearFragment = false

    var closestPointRadius = ArrayList<Any>()
    var tempPlantingRadius by Delegates.notNull<Float>()

    lateinit var lineInS2FormatforSwitch: S2PointIndex<S2LatLng>
    lateinit var lineInS2Format: S2PointIndex<S2LatLng>
    private lateinit var fromRTKFeed:LatLng

    // Declaring sensorManager
    // and acceleration constants
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var switchedLines = false
    private var plantingMode = false
    private var markers: Marker? = null
    private var fabFlag = true
    private var zoomMode = false
    private var ViewProjects = false

    var listOfMarkedPoints = mutableListOf<LatLng>()
    var listofmarkedcircles = mutableListOf<Circle>()

    var unmarkedCirclesList = mutableListOf<Circle>()

    var listOfPlantingLines = mutableListOf<Polyline>()

    var userID: String? = null

    var extras: Bundle? = null
    var projectList = ArrayList<String>()
    var projectIDList = mutableListOf<Int>()
    var projectSizeList = mutableListOf<Int>()
    var projectMeshSizeList = mutableListOf<Int>()

    lateinit var debugXloc: LatLng
    lateinit var txt: TextView
    lateinit var colortext: EditText
    lateinit var card: androidx.cardview.widget.CardView
    lateinit var left: TextView
    lateinit var right: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.appToolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val displayProjectName: TextView? = findViewById(R.id.display_project_name)
        txt = findViewById<TextView>(R.id.myTextView);

        card = findViewById<CardView>(R.id.cardView2)
        left = findViewById(R.id.strayingLeft)

        card.setOnClickListener {
            stopBlink()
        }
        //dropdown menu for project settings
        val arrayForProjectDetails = resources.getStringArray(R.array.ProjectDetails)
        val arrayAdapterForProjectDetails =
            ArrayAdapter(this, R.layout.projectdetails, arrayForProjectDetails)

        var x = displayProjectName as AutoCompleteTextView
        x.setAdapter(arrayAdapterForProjectDetails)
        extras = getIntent().extras

        //retrieve userid and token from intent, then store it in a shared preferences file for access
        //throughout the app

        if (extras != null) {
            userID = extras!!.getString("userID")
            val APIToken = extras!!.getString("token")

            val sharedPreferences: SharedPreferences = this.getSharedPreferences(
                CreateProjectDialog().sharedPrefFile,
                Context.MODE_PRIVATE
            )!!
            val editor = sharedPreferences.edit()
            editor.putString("userid_key", userID)
            editor.putString("apiToken_key", APIToken)
            editor.apply()
            editor.commit()

        }
        // Getting the Sensor Manager instance

        if (savedInstanceState != null) {

        } else {
            getProjects()
            createDialog(projectList)
            val mapFragment =
                supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager!!.registerListener(
            listener,
            magneticSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        );
        sensorManager!!.registerListener(listener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
                    markers?.remove()

                    marker = map?.addCircle(
                        CircleOptions().center(fromRTKFeed).fillColor(Color.GREEN).radius(0.5)
                            .strokeWidth(1.0f)

                    )
                    directionMarker?.remove()
                    directionMarker = map?.addMarker(
                        MarkerOptions().position(fromRTKFeed)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.smallman))

                    )


                    plotFunc()
                    distanceToPoint(fromRTKFeed)
                    // We need to calculate distance of where we are to point to be marked
                    if (closestPointRadius.size > 0) {
                        val pointOfInterest = closestPointRadius[0] as LatLng
                        val acceptedPlantingRadius = tempPlantingRadius
                        val distanceAway =
                            GeneralHelper.findDistanceBtnTwoPoints(fromRTKFeed, pointOfInterest) /// 2  // divide by 2 means we want to mark when user is closer to the point
                        if (distanceAway < acceptedPlantingRadius) {
                            // mark point
                            markPoint(pointOfInterest)
                        }
                    }

                    /* if (currentLocation.isNotEmpty()) {
                         currentLocation.clear()
                     }
                     marker?.let {
                         currentLocation.add(it.center)
                     }
                     val location = currentLocation[currentLocation.lastIndex]*/


                }
                /*if (currentLocation.distinct().size == 1) {
                    plotFunc()
                }*/

                //markPoint()
                //controlling visibility given the zoom level on the map
                /*if (zoomMode) {
                    val zmlevel = map?.cameraPosition?.zoom
                    val maxZmLevel = map?.maxZoomLevel

                    if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
                        return
                    }
                    //get current line person is walking on, lat/lng and find closest point on line as they are moving
                    val currentPlantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]
                    val roverPoint = currentLocation[currentLocation.lastIndex]
                    val l = currentPlantingLine.tag as MutableList<*>

                    if (zmlevel != null) {
                        if (zmlevel == maxZmLevel) {
                            currentPlantingLine.isVisible = false

                            for (c in unmarkedCirclesList) {
                                c.isVisible = false
                            }

                            var plantingRadius: Circle? = null

                            val locx = S2Helper.findClosestPointOnLine(
                                pointsIndex,
                                roverPoint
                            ) as S2LatLng?

                            if (locx != null) {
                                val pt = LatLng(locx.latDegrees(), locx.lngDegrees())

                                if (pt !in l) {
                                    return
                                }
                                if (pt in listOfMarkedPoints) {
                                    return
                                }
                                tempClosestPoint.add(pt)
                                unmarkedCirclesList[20].isVisible = true

                                //handle visibility and lifecycle of the radius circle
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
                                plantingRadius = map?.addCircle(
                                    CircleOptions().center(pt).fillColor(Color.GREEN)
                                        .radius(radius(Geoggapsize!!))
                                        .strokeWidth(1.0f)
                                        .fillColor(0x22228B22)
                                        .strokeColor(Color.GREEN)
                                        .strokeWidth(1.0f)
                                )
                                for (c in unmarkedCirclesList) {
                                    if (c.center == pt) {
                                        c.isVisible = true
                                    }
                                }
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
                                plantingRadiusCircle = templist[templist.lastIndex]

                                for (c in unmarkedCirclesList) {
                                    if (c.center == plantingRadius.center) {
                                        c.isVisible = true
                                    }
                                }

                            }
                            plantingMode = true
                            markPoint()
                        } else {
                            showLine(currentPlantingLine)
                            //plotLine(l)
                        }
                    }
                } else {
                    if (currentLocation.distinct().size == 1) {
                        plotFunc()
                    }
                }*/
            }
        })

        scantBlueTooth()
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
//                    map?.setMapStyle(
//                        MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json)
//                    )
                    for (item in polyLines) {
                        item!!.isVisible = false

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
                    }
                    activePlantingLine.isVisible = true
                    fabFlag = false

                }


            } catch (e: Exception) {

            }
        }

    }

    private fun getPoints(id: Int) {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog().sharedPrefFile,
            Context.MODE_PRIVATE
        )!!

        if (listOfMarkedPoints.isNotEmpty()) {
            listOfMarkedPoints.clear()
        }
        val userIDString: String? = sharedPreferences.getString("userid_key", "0")!!

        val userID = userIDString!!.toInt()

        val retrofitGetPointsObject = AppModule.retrofitInstance()
        val modal = RequestPoints(id, userID)

        val retrofitData = retrofitGetPointsObject.retrievePoints(modal)
        retrofitData.enqueue(object : Callback<ResponsePoints?> {
            override fun onResponse(
                call: Call<ResponsePoints?>,
                response: Response<ResponsePoints?>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.message == "Success") {
                        Log.d("results", "$response")
                        val result = response.body()!!.results
                        if (result.isNotEmpty()) {
                            for (r in result) {
                                val point = LatLng(r.Lat.toDouble(), r.Long.toDouble())

                                listOfMarkedPoints.add(point)
                            }

                        } else {
                            listOfMarkedPoints.clear()
                        }

                        Log.d("points", "$listOfMarkedPoints")
                    }
                } else {
                    alertfail("Could not retrieve points at this time!")
                }
            }

            override fun onFailure(call: Call<ResponsePoints?>, t: Throwable) {
                Log.d("points", "Can't retrieve points at this time")
            }
        })

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

    fun getProjects(): ArrayList<String> {

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog().sharedPrefFile,
            Context.MODE_PRIVATE
        )!!
        val apiToken: String? = sharedPreferences.getString("apiToken_key", "defaultValue")

        val retrofitDataObject = ServiceInterceptor("Bearer", apiToken!!).httpClient(apiToken)
        val retrofitData = retrofitDataObject.getProjectsList(apiToken)
        retrofitData.enqueue(object : Callback<List<ResponseProjectsDataClass>?> {
            override fun onResponse(
                call: Call<List<ResponseProjectsDataClass>?>,
                response: Response<List<ResponseProjectsDataClass>?>
            ) {
                val responseBody = response.body()

                if (responseBody != null) {
                    //Crude: clear the lists to avoid duplicates
                    // or! Make name unique, and check its existance before adding i to list to avoid duplicates.
                    projectList.clear()
                    projectIDList.clear()
                    projectMeshSizeList.clear()
                    projectSizeList.clear()
                    for (data in responseBody) {
                        projectList.add(data.name)
                        projectIDList.add(data.id)
                        projectMeshSizeList.add(data.mesh_size)
                        projectSizeList.add(data.gap_size)
                    }
                    if (ViewProjects) {
                        displayProjects(

                        )
                    }

                    Log.d("Projects", "$projectList")

                } else {
                    alertfail("No response got from server")
                }
            }

            override fun onFailure(call: Call<List<ResponseProjectsDataClass>?>, t: Throwable) {
                Log.d("error", "${t.message}")
            }
        })
        return projectList
    }

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


    private fun loadProject(ProjectID: Int, Meshsize: Int, Gapsize: Int) {
        val displayProjectName: TextView? = findViewById(R.id.display_project_name)

        Toast.makeText(
            applicationContext, "Loading project, This may take some few minutes time." +
                    "", Toast.LENGTH_LONG
        ).show()

        val retrofitGetPointsObject = AppModule.retrofitInstance()

        val modal = RequestBasePointsDataClass(ProjectID)

        val retrofitData = retrofitGetPointsObject.retrieveBasePoints(modal)

        retrofitData.enqueue(object : Callback<RetrieveBasePointsDataClass?> {
            override fun onResponse(
                call: Call<RetrieveBasePointsDataClass?>,
                response: Response<RetrieveBasePointsDataClass?>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        val results = response.body()!!.points
                        if (results.isNotEmpty()) {
                            displayProjectName?.text = selectedProject
                            Log.d("results", "$results")
                            val l = results[0]
                            val y = results[1]
                            val firstPoint = LongLat(l.Long.toDouble(), l.Lat.toDouble())
                            val secondPoint = LongLat(y.Long.toDouble(), y.Lat.toDouble())

                            Geoggapsize = Gapsize
                            Geogmesh_size = Meshsize.toDouble()
                            clearFragment = true
//
                            plotMesh(firstPoint, secondPoint)


                        } else {

                            warningAlert(
                                "\nProject is empty!! Might be best to delete it.",
                                ProjectID
                            )
                        }


                    }
                } else {

                    Log.d("results", "Failed")

                }
            }


            override fun onFailure(call: Call<RetrieveBasePointsDataClass?>, t: Throwable) {
                alertfail("Could not load the project. It could be empty")
            }
        })


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

    private fun deleteProjectFunc(ID: Int) {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog().sharedPrefFile,
            Context.MODE_PRIVATE
        )!!

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
//        val line = L.tag as MutableList<*>
        for (c in unmarkedCirclesList) {
            c.isVisible = true
        }

        for (p in listOfMarkedPoints) {
            map?.addCircle(
                CircleOptions().center(p).fillColor(Color.YELLOW).radius(0.5)
                    .strokeWidth(1.0f)
            )
        }
//            for (loc in line) {
//                val xloc = loc as LatLng
//                // Draw the points...
//                if (loc !in listOfMarkedPoints) {
//                    map?.addCircle(
//                        CircleOptions().center(xloc).fillColor(Color.RED).radius(0.5)
//                            .strokeWidth(1.0f)
//                        //if set to zero, no outline is drawn
//                    )
//
//                } else {
//                    map?.addCircle(
//                        CircleOptions().center(xloc).fillColor(Color.YELLOW).radius(0.5)
//                            .strokeWidth(1.0f)
//                        //if set to zero, no outline is drawn
//                    )
//                }
//
//            }
//
//        }
    }

    var polyline1: Polyline? = null
    var refPoint: LatLng? = null
    var distance = 0.0f
    var latLng: LatLng? = null


    private fun distanceToPoint(loc: LatLng) {
        if (plantingMode && listOfMarkedPoints.isNotEmpty()) {
            card.isVisible = true
            var displayedDistance = findViewById<TextView>(R.id.distance)
            var displayedPoints = findViewById<TextView>(R.id.numberOfPoints)
            displayedDistance.text = ""
            displayedPoints.text = " "

            try {
                val locationOfNextPoint = Location(LocationManager.GPS_PROVIDER)
                val locationOfRoverLatLng = Location(LocationManager.GPS_PROVIDER)
                var line = listOfPlantingLines[listOfPlantingLines.lastIndex]
                var l = line.tag as MutableList<*>
                var size = listOfMarkedPoints.size
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

                    distance = locationOfRoverLatLng.distanceTo(locationOfNextPoint)
                    if (tempListMarker.isNotEmpty()) {
                        for (m in tempListMarker) {
                            if (m.position == latLng) {
                                return    //do nothing if a marker is already drawn at that point
                            }
                        }
                    }

                }
                displayedDistance.text = distance.toString()
                displayedPoints.text = size.toString()

                markers = map?.addMarker(
                    MarkerOptions().position(latLng!!)
                        .title("Point Stats!")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                        .snippet("Marked Points : $size" + "\nDistance : $distance (m)")
                )

                polyline1!!.endCap = CustomCap(
                    BitmapDescriptorFactory.fromResource(R.drawable.blackarrow1), 10f
                )
                if (distance > (Geoggapsize!! + 1)) {
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
                    blink("Red")
                    showLine(line)

                    //  showLine()
                } else {
                    //polyline1?.remove()
                    stopBlink()
                }

                markers!!.showInfoWindow()
                for (x in unmarkedCirclesList) {
                    if (x.center == latLng) {
                        x.isVisible = true
                    } else {
                        return
                        //Toast.makeText(this, "point not drawn", Toast.LENGTH_SHORT).show()
                    }
                }
                tempListMarker.add(markers!!)
            } catch (e: Exception) {
                Log.d("ERROR", "frOM SWITCHING ${e.message}")
            }


        }


    }

    lateinit var anim: ObjectAnimator
    fun blink(c: String) {
        val n = c
        var animationColor: Int = 0
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
        anim = ObjectAnimator.ofInt(
            left,
            "backgroundColor", animationColor, Color.WHITE, animationColor, Color.GRAY
        )
        anim.duration = 1500;
        anim.setEvaluator(ArgbEvaluator())
        anim.repeatMode = ValueAnimator.RESTART;
        anim.repeatCount = Animation.INFINITE;
        anim.start()

//        anim = ObjectAnimator.ofInt(right,
//            "backgroundColor",animationColor,Color.WHITE,animationColor,Color.GRAY)
//        anim.duration = 1500;
//        anim.setEvaluator(ArgbEvaluator())
//        anim.repeatMode = ValueAnimator.RESTART;
//        anim.repeatCount = Animation.INFINITE;
//        anim.start()

    }

    fun stopBlink() {

        if (anim.isRunning) {
            anim.end()
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
                        return
                    }

                    device?.name
                    Log.d("Devices", "${device?.name}")
                    val deviceHardwareAddress = device?.address // MAC address
                }
            }
        }
    }

    override fun onDestroy() {
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
        sensorManager!!.unregisterListener(listener)
        super.onDestroy()

    }


    // Display the menu layout
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appmenu, menu)
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
        return if (item.itemId == R.id.action_create) {

            val createNewProject = CreateProjectDialog()
            createNewProject.show(supportFragmentManager, "create")

            return true
        } else if (item.itemId == R.id.action_view_projects) {

            ViewProjects = true
            getProjects()
            return true
        } else if (item.itemId == R.id.bluetooth_spinner) {
            toggleWidgets()
            return true

        } else if (item.itemId == R.id.map_view) {
            discover()

            return true
        } else {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
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
            return
        }

        bluetoothAdapter.startDiscovery()
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
        plotMesh(firstPoint!!, secondPoint!!)


        l?.remove()
        // marker?.remove()
    }

    fun plotMesh(cp: LongLat, pp: LongLat) {
        if (clearFragment) {
            val mapFragment =
                supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
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

        progressBar.isVisible = true

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
                // unmarkedCirclesList.add(c!!)
            }

        } else {
            Log.d("empty", "empty")
        }


        progressBar.isVisible = false


    }

    fun freshFragment(v: Boolean) {
        if (v) {
            val mapFragment =
                supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)

            for (item in polyLines) {
                item!!.remove()
            }
        }
    }


    private val onPolyClick = GoogleMap.OnPolylineClickListener {

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
            recentLine.color = Color.BLUE
            recentLine.isClickable = true
            recentLine.width =
                3f   //return to default width after pulse effect and stop the handler
            listOfPlantingLines.clear()
            listOfPlantingLines.add(it)
            it.color = Color.GREEN

            Toast.makeText(applicationContext, "Switching Lines..", Toast.LENGTH_LONG)
                .show()

//            val runnableCode = object : Runnable {
//                override fun run() {
//                    var w = it.width;
//                    w += 0.5f;
//                    if (w > 13.0) {
//                        w = 1.0f;
//                    }
//                    it.width = w;
//                    handler2.postDelayed(this, 50)
//                }
//            }
//
//            handler2.postDelayed(runnableCode, 50)   //enqueue the impulsing effect function

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

            //make the lines and circles on other polylines disaapear, once we have line of interest
            for (item in polyLines) {
                item!!.isVisible = false
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
            var target = map?.cameraPosition?.target
            var bearing = map?.cameraPosition?.bearing
            val cameraPosition = CameraPosition.Builder()
                .target(target!!)
                .zoom(20f)
                .bearing(bearing!!)
                .tilt(45f)
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
        listOfMarkedPoints
        Log.d("Points", " plot line $listOfMarkedPoints")
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

    var plantingRadiusCircle: Circle? = null
    val templist = mutableListOf<Circle>()
    val tempClosestPoint = mutableListOf<LatLng>()

    private fun plotFunc() {

        closestPointRadius.clear()
        if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
            return
        }
        Log.d("LoperPlot", "${Thread().name}")
        var plantingRadius: Circle? = null

        val roverPoint = fromRTKFeed

        val lineOfInterest = listOfPlantingLines[listOfPlantingLines.lastIndex]

        val l = lineOfInterest.tag as MutableList<*>
        val l2 = lineOfInterest.tag as Collection<LatLng>

        lineInS2Format = GeneralHelper.convertLineToS2(l2);

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
                CircleOptions().center(pt).fillColor(Color.GREEN).radius(radius(Geoggapsize!!))
                    .strokeWidth(1.0f)
                    .fillColor(0x22228B22)
                    .strokeColor(Color.GREEN)
                    .strokeWidth(1.0f)
            )
            // }


        }
        if (switchedLines) {
            val runnableCode = object : Runnable {
                override fun run() {
                    var w = lineOfInterest.width;
                    w += 0.5f;
                    if (w > 13.0) {
                        w = 1.0f;
                    }
                    lineOfInterest.width = w;
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

        }

    }

    fun markPoint(pointOfInterestOnPolyline: LatLng) {

        if (polyLines.size == 0 || listOfPlantingLines.size == 0 || unmarkedCirclesList.size == 0) {
            return
        }
        if (plantingMode) {

            //val lineOfInterest = listOfPlantingLines[listOfPlantingLines.lastIndex]

            //val l = lineOfInterest.tag as MutableList<*>

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

            listOfMarkedPoints.add(markedCirclePoint.center)
            listofmarkedcircles.add(markedCirclePoint)
           // savePoints(markedCirclePoint)

            /*if (listofmarkedcircles.isNotEmpty()) {
                val last = listofmarkedcircles[listofmarkedcircles.lastIndex]
                val color = last.fillColor
                if (color != Color.YELLOW) {
                    Toast.makeText(
                        applicationContext,
                        "Was not yellow",
                        Toast.LENGTH_LONG
                    ).show()

                    map?.addCircle(
                        CircleOptions().center(markedCirclePoint.center)
                            .fillColor(Color.YELLOW)
                            .radius(0.5)
                            .strokeWidth(1.0f)
                    )
                }
            }*/

            if (markedCirclePoint.center == plantingRadiusCircle?.center) {

                plantingRadiusCircle?.remove()   //remove planting radius, marker and clear list

                if (markers != null) {
                    markers!!.remove()

                }

                if (tempListMarker.isNotEmpty()) {
                    tempListMarker.clear()
                }


            } else {
                Toast.makeText(
                    applicationContext, "ERROR " +
                            "", Toast.LENGTH_SHORT
                ).show()
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

//                val animation = RotateAnimation(
//                    lastRotateDegree,
//                    rotateDegree,
//                    Animation.RELATIVE_TO_SELF,
//                    0.5f,
//                    Animation.RELATIVE_TO_SELF,
//                    0.5f
//                )
//                animation.fillAfter = true
                //compass.startAnimation(animation)
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

                try {
                    val plantingLine = listOfPlantingLines[listOfPlantingLines.lastIndex]

                    val l = plantingLine.tag as MutableList<*>
                    var pointOfInterestOnPolyline: LatLng? = null

//                    var indexOfXloc = l.indexOf(debugXloc)
//
//
//
//                    var lastMarkedIndex = listOfMarkedPoints.lastIndex
//                    var lastMarkedPoint = listOfMarkedPoints[lastMarkedIndex]
//                    var indexOfInterest = l.indexOf(lastMarkedPoint)
//                    var nextPoint = l[indexOfInterest+1] as LatLng
//                    var previousPoint = l[indexOfInterest-1] as LatLng
//
//                    var ourGuy:LatLng? = null
//
//                    //var pointOfInterest
//                    if(nextPoint in listOfMarkedPoints){
//                        ourGuy = previousPoint
//                    }else{
//                        ourGuy = nextPoint
//                    }
//                    var a = debugXloc
//                    var indexOfOurGuy = l.indexOf(ourGuy)
//                    if (debugXloc != ourGuy){
//                        Log.d("SKIP", "GATCH!!!")
//
//                        var b = debugXloc
//
//                    }


                    l.forEach { loc ->
                        loc as LatLng
                        if (loc == plantingRadiusCircle?.center)
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

                        if (distance > plantingRadiusCircle?.radius!!) {

                            Toast.makeText(
                                applicationContext, "Outside planting zone" +
                                        "", Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            val markedCirclePoint = map?.addCircle(
                                CircleOptions().center(plantingRadiusCircle?.center!!)
                                    .fillColor(Color.YELLOW)
                                    .radius(0.5)
                                    .strokeWidth(1.0f)
                            )

                            // point is already marked, so ignore it
                            if (markedCirclePoint!!.center in listOfMarkedPoints) {
                                return
                            }

                            listOfMarkedPoints.add(markedCirclePoint.center)
                            listofmarkedcircles.add(markedCirclePoint)
                           // savePoints(markedCirclePoint)

                            if (listofmarkedcircles.isNotEmpty()) {
                                val last = listofmarkedcircles[listofmarkedcircles.lastIndex]
                                val color = last.fillColor
                                if (color != Color.YELLOW) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Was not yellow",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    map?.addCircle(
                                        CircleOptions().center(markedCirclePoint.center)
                                            .fillColor(Color.YELLOW)
                                            .radius(0.5)
                                            .strokeWidth(1.0f)
                                    )
                                }
                            }

                            if (markedCirclePoint.center == plantingRadiusCircle?.center) {

                                plantingRadiusCircle?.remove()   //remove planting radius, marker and clear list

                                if (markers != null) {
                                    markers!!.remove()

                                }

                                if (tempListMarker.isNotEmpty()) {
                                    tempListMarker.clear()
                                }


                            } else {
                                Toast.makeText(
                                    applicationContext, "ERROR " +
                                            "", Toast.LENGTH_SHORT
                                ).show()
                            }
                            if (templist.isNotEmpty()) {
                                templist.clear()
                            }
                            if (tempClosestPoint.isNotEmpty()) {
                                tempClosestPoint.clear()
                            }

                            plantingMode = true
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext, "Can't find point to mark {$e.message}" +
                                "", Toast.LENGTH_SHORT
                    ).show()
                }


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

    private fun saveBasepoints(loc: LongLat) {
        val lat = loc.getLatitude()
        val lng = loc.getLongitude()

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog().sharedPrefFile,
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

        val modal = SaveBasePointsClass(lat, lng, ProjectID)
        val retrofitDataObject = AppModule.retrofitInstance()

        val retrofitData = retrofitDataObject.storeBasePoints(modal)
        retrofitData.enqueue(object : Callback<SaveBasePointsResponse?> {
            override fun onResponse(
                call: Call<SaveBasePointsResponse?>,
                response: Response<SaveBasePointsResponse?>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.message == "success") {
//
//                            Toast.makeText(
//                                applicationContext, "Bpoints Saved!! " +
//                                        "", Toast.LENGTH_SHORT
//                            ).show()
                        } else {
                            val m = response.body()!!.meta
                            alertfail(m)
                        }
                    } else {
                        alertfail("BODY IS NULL")
                    }
                } else {
                    alertfail("An error has occured")
                }
            }

            override fun onFailure(call: Call<SaveBasePointsResponse?>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

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
    fun savePoints(circle: Circle) {
        val latlng = circle.center
        val lat = latlng.latitude
        val lng = latlng.longitude

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            CreateProjectDialog().sharedPrefFile,
            Context.MODE_PRIVATE
        )!!


        val userIDString: String? = sharedPreferences.getString("userid_key", "0")!!
        val ProjectIDString: String? = sharedPreferences.getString("productID_key", "0")

        val userID = userIDString!!.toInt()
        val ProjectID = ProjectIDString!!.toInt()

        val modal = savePointsDataClass(lat, lng, ProjectID, userID)
        val retrofitDataObject = AppModule.retrofitInstance()

        val retrofitData = retrofitDataObject.storePoints(modal)

        retrofitData.enqueue(object : Callback<savePointsResponse?> {
            override fun onResponse(
                call: Call<savePointsResponse?>,
                response: Response<savePointsResponse?>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.message == "success") {

                            Toast.makeText(
                                applicationContext, "Point Marked " +
                                        "", Toast.LENGTH_SHORT
                            ).show()
                            Log.d("Loper", Thread().name + "savedb")
                            // convert to S2 and remove it from queryset
                            var set = lineInS2Format
                            val point = S2LatLng.fromDegrees(lat, lng)
                            val pointData = PointData(point.toPoint(), point)
                            lineInS2Format.remove(pointData)
                            var s = lineInS2Format

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

            override fun onFailure(call: Call<savePointsResponse?>, t: Throwable) {
                alertfail("Point not saved! ${t.message}")
            }
        })


    }

    private fun radius(size: Int): Double {
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

    val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        googleMap.setLocationSource(NmeaReader.listener)
        googleMap.setOnMapClickListener(onMapClick)
        googleMap.setOnPolylineClickListener(onPolyClick)
        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
        //googleMap.setOnCircleClickListener(onClickingPoint)
        // googleMap.setOnMapLongClickListener(onLongMapPress)

        val isl = LatLng(-.366044, 32.441599) // LatLng(0.0,32.44) //
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.addMarker(MarkerOptions().position(isl).title("Marker in N Residence"))
//
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(isl, 21.0f))

        val fab = findViewById<FloatingActionButton>(R.id.fab_map)
        fab.hide()
        card.isVisible = false

    }

    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        private val window: View = layoutInflater.inflate(R.layout.custom_info_contents, null)

        override fun getInfoWindow(marker: Marker): View? {
            render(markers!!, window)
            return window
        }

        override fun getInfoContents(marker: Marker): View? {
            render(markers!!, window)
            return window
        }

        private fun render(marker: Marker, view: View) {

            // Set the title and snippet for the custom info window

            val titleUi = view.findViewById<TextView>(R.id.title)
            if (marker.title != "") {
                titleUi.text = marker.title
            }

            val snippet: String? = marker.snippet
            val snippetUi = view.findViewById<TextView>(R.id.snippet)

            if (marker.snippet != "") {
                snippetUi.text = marker.snippet
            }

            snippetUi.text = snippet

        }
    }

}