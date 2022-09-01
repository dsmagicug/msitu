package com.dsmagic.kibira

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import gov.nasa.worldwind.geom.LatLon
import gov.nasa.worldwind.geom.coords.UTMCoord
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// var METRES_FROM_FEET = 3.28084   //one meter = 3.28084 ft
// var GAP_SIZE_FEET = 12
// var MAX_MESH_SIZE = 100.0 // In metres
// var GAP_SIZE_METRES = GAP_SIZE_FEET / METRES_FROM_FEET
//var Geoggapsize: Double? = 0.0 //in meters
//var Geogmesh_size:Double? = 0.0  //in meters

var MAX_MESH_SIZE:Double = 0.0 // In metres
var GAP_SIZE_METRES :Double = 0.0
var Geoggapsize: Double? = 0.0 //in meters
var Geogmesh_size:Double? = 0.0  //in meters


const val SINE_60 = 0.86602540378 // Sine of 60 degrees...

/**
 * The mesh direction to use. That is, facing the direction point from where we stand, are we going left or right in our planting?
 */
enum class MeshDirection {LEFT, RIGHT}

// Represents a point where a tree is planted. Units are metres.
open class Point(internal var x: Double, internal var y: Double) {

    var zone = 0 // UTM zone
    var hemisphere = "N"

    // Rotate the point X degrees around the origin..
    open fun rotate(cosTheta: Double, sinTheta: Double) {
        val xnew = x * cosTheta - y * sinTheta
        val yNew = x * sinTheta + y * cosTheta
        x = xnew
        y = yNew
    }


    /**
     * Create UTM coordinates
     * See https://github.com/rolfrander/geo/blob/master/src/main/java/org/pvv/rolfn/geo/UTM.java
     */
    constructor(loc: LongLat) : this(0.0, 0.0) {
        // Convert using Nasa libs
        val ll = LatLon.fromDegrees(loc.getLatitude(), loc.getLongitude())
        val utm = UTMCoord.fromLatLon(ll.latitude, ll.longitude)
        hemisphere = utm.hemisphere
        x = utm.easting
        y = utm.northing
        zone = utm.zone
    }
}

// Represents a planting line...
open class PlantingLine(private val points: ArrayList<Point>) {
    constructor(
        startPointX: Double,
        startPointY: Double,
        gap: Double,
        maxLineLength: Double
    ) : this(ArrayList()) {
        // Make the points.
        var current = startPointX
        var lim = maxLineLength
        while (lim > 0) {
            val p = Point(current, startPointY)
            points.add(p)
            current += gap
            lim -= gap
        }
    }

    constructor(startPoint: Point, xDelta: Double, yDelta: Double) : this(ArrayList()) {
        var current = startPoint
        var lim = MAX_MESH_SIZE

        points.add(current) // First point goes in.
        while (lim > 0 ) {
            current = Point(current.x+xDelta,current.y+yDelta) // .move(xDelta,yDelta)
            points.add(current)
            lim -= GAP_SIZE_METRES
        }
    }

    fun rotate(theta: Pair<Double, Double>): PlantingLine {
        for (p in points) {
            p.rotate(theta.first, theta.second)
        }
        return this
    }


    fun fromUTM(centre: Point): List<LongLat> {
        return points.map {
            // We need to shift the basis back to UTM from our point-centred axes
            LongLat(centre.zone, centre.hemisphere, it.x /*+ centre.x*/, it.y /*+ centre.y*/)
        }
    }
}

var LOCATION_PROVIDER = "Rtk"

class LongLat(var long: Double, var lat: Double) : Location(LOCATION_PROVIDER) {

    companion object {
        var lastHorizontalAccuracy = 1.0 // Current accuracy
        var lastVerticalAccuracy = 1.0
        var lastAltitudeAccuracy = 1.0
    }

    enum class FixType {
        NoFixData,
        Autonomous,
        Estimated,
        DGPS,
        PPS,
        RTKFix,
        RTKFloat,
        ManualInput,
        Simulated
    }

    var fixType = FixType.NoFixData
    var timeStamp = Date()
    var numSatellites = 0
    var hdop = 0.0
    var aboveSeaLevel = 0.0
    var speed: Double? = null
    var accuracy: Double? = null // Default to 1m
    var latitude = ""
    var longitude = ""

    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("HHmmss.SSZ")

    @SuppressLint("SimpleDateFormat")
    val dateFormatLong = SimpleDateFormat("DDMMyy-HHmmss.SSZ")

    /**
     * From UTM.
     * See: https://github.com/rolfrander/geo/blob/master/src/main/java/org/pvv/rolfn/geo/WGS84.java
     */
    constructor(zone: Int, hemisphere: String, easting: Double, northing: Double) : this(0.0, 0.0) {
        val ll = UTMCoord.locationFromUTMCoord(zone, hemisphere, easting, northing)
        lat = ll.latitude.degrees
        long = ll.longitude.degrees
    }

    override fun toString(): String {
        return "${this.longitude}, ${this.latitude} (${this.long}, ${this.lat}), altitude: ${this.altitude}, hdop: ${this.hdop}, fix: ${this.fixType}"
    }


    // Parse NMEA sentence, set NoFixData if there was no fix data...
    constructor (sentence: String) : this(0.0, 0.0) {
        // Split it.
        val l = sentence.split(",")
        if (l.isEmpty() || l[0].isEmpty())
            return
        val sType = l[0].substring(1)

        if (sType.endsWith("rmc", true)) {
            initRMC(l)
        } else if (sType.endsWith("gga", true)) {
            initGGA(l)
        } else if (sType.endsWith("gst", true)) {
            lastVerticalAccuracy = l[6].toDouble()
            lastHorizontalAccuracy = l[7].toDouble()
            //   lastAltitudeAccuracy = l[7].toDouble() -- ignore, since has *checksum -- we don't need it.

            fixType = FixType.NoFixData
        } else
            fixType = FixType.NoFixData

        accuracy =
            (lastHorizontalAccuracy + lastVerticalAccuracy) / 2 // Grab last accuracy, average regardless
    }

    private fun parseDegrees(v: String, indicator: String): Double {
        // s = the value, indicator = N,S,E,W
        // s has the format XXYY.blah
        // https://stackoverflow.com/questions/36254363/how-to-convert-latitude-and-longitude-of-nmea-format-data-to-decimal

        var offset = v.indexOf('.') - 2
        if (offset < 0)
            offset = 0
        val d = if (offset == 0) 0 else v.substring(0, offset).toLong()
        val m = v.substring(offset).toDouble() / 60.0
        val sign = when (indicator) {
            "N" -> 1
            "E" -> 1
            else -> -1
        }
        return sign * (d + m)
    }

    private fun initGGA(l: List<String>) {
        // See http://lefebure.com/articles/nmea-gga/
        dateFormat.parse(l[1] + "+0000").also { timeStamp = it }
        lat = parseDegrees(l[2], l[3])
        latitude = l[2] + l[3]
        long = parseDegrees(l[4], l[5])
        longitude = l[4] + l[5]
        val x = l[6].toInt()
        fixType = when (x) {
            1 -> FixType.Autonomous
            2 -> FixType.DGPS
            3 -> FixType.PPS
            4 -> FixType.RTKFix
            5 -> FixType.RTKFloat
            6 -> FixType.Estimated
            7 -> FixType.ManualInput
            8 -> FixType.Simulated
            else -> FixType.NoFixData
        }
        numSatellites = l[7].toInt()
        hdop = l[8].toDouble()
        aboveSeaLevel = l[9].toDouble()

    }

    private fun initRMC(l: List<String>) {
        // See https://orolia.com/manuals/VSP/Content/NC_and_SS/Com/Topics/APPENDIX/NMEA_RMCmess.htm
        val dd = l[9] + "-" + l[1] + "+0000"
        timeStamp = dateFormatLong.parse(dd)

        lat = parseDegrees(l[3], l[4])
        latitude = l[3] + l[4]
        long = parseDegrees(l[5], l[6])
        longitude = l[5] + l[6]
        speed = l[7].toDouble() * 1.8 * 1000 // Knots to km/hr
        if (l[2].equals("V")) // Void as per spec
            fixType = FixType.NoFixData
        else
            fixType = when (l[12]) {
                "A" -> FixType.Autonomous
                "D" -> FixType.DGPS
                "E" -> FixType.Estimated
                "F" -> FixType.RTKFloat
                "M" -> FixType.ManualInput
                "R" -> FixType.RTKFix
                "S" -> FixType.Simulated
                else -> FixType.NoFixData
            }
    }



    override fun getAltitude(): Double {
        return aboveSeaLevel
    }

    override fun getAccuracy(): Float {
        return if (accuracy == null)
            if (fixType == FixType.RTKFix) 0.01f else 0.3f // XX rough guess
        else
            accuracy!!.toFloat() // Else return what we got...
    }

    override fun getLatitude(): Double {
        return lat
    }

    override fun getLongitude(): Double {
        return long
    }

    override fun getTime(): Long {
        return timeStamp.time
    }

    override fun hasAccuracy(): Boolean {
        return lastHorizontalAccuracy > 0.0
    }

    override fun hasAltitude(): Boolean {
        return true
    }

    override fun hasBearing(): Boolean {
        return false
    }

    override fun hasSpeed(): Boolean {
        return speed != null
    }

    override fun hasSpeedAccuracy(): Boolean {
        return false
    }

    override fun getSpeed(): Float {
        return if (speed == null)
            0.0f
        else
            speed!!.toFloat()
    }
}

class Geometry {
    companion object {

        // Get the angle made with the horizontal by the vector from the origin to a point.
        // Returns angle in polar form
        private fun theta(p1: Point, p2: Point): Double {
            val p = Point(p2.x - p1.x, p2.y - p1.y)
            val x = atan2(p.y, p.x)
            val y = x / Math.PI * 180
            Log.d("theta", "Angle between horizontal and point is $x (or $y°)")
            return x
        }

        /**
         * Return a mesh, starting at startPoint, forward in the direction given. Between each two lines is GAP_SIZE metres,
         * between each point is GAP_SIZE metres.
         */
        fun generateSquareMesh(startPoint: Point, directionPoint: Point, plantingDirection: MeshDirection): List<PlantingLine> {
            val theta = theta(startPoint, directionPoint)

            val l = ArrayList<PlantingLine>()
            val xDelta = GAP_SIZE_METRES * cos(theta)
            val yDelta = GAP_SIZE_METRES * sin(theta)

            // The next line starting point is always a 90 degree turn from the last starting point.
            val linesDirection = if  (plantingDirection == MeshDirection.RIGHT)  1  else -1
            val gamma =  theta + linesDirection * (Math.PI/2)  // Better methinks...
            val lineDeltaX =  GAP_SIZE_METRES * cos(gamma)
            val lineDeltaY =  GAP_SIZE_METRES * sin(gamma)
            var lineStart = startPoint

            var lanesWidth = 0.0 // This is lanes (i.e. lines) times gap size
            while (lanesWidth < MAX_MESH_SIZE) {
                l.add(PlantingLine(lineStart, xDelta,yDelta))
                lineStart = Point(lineStart.x + lineDeltaX, lineStart.y + lineDeltaY) // .move(lineDeltaX,lineDeltaY) // Move the start point to the next line
                lanesWidth += GAP_SIZE_METRES
            }
            return l
        }

        fun generateTriangleMesh(startPoint: Point, directionPoint: Point, plantingDirection: MeshDirection): List<PlantingLine> {
            val theta = theta(startPoint, directionPoint)

            val l = ArrayList<PlantingLine>()
            val xDelta = GAP_SIZE_METRES * cos(theta)
            val yDelta = GAP_SIZE_METRES * sin(theta)

            // The next line starting point is always a 90 degree turn from the last starting point.
            val linesDirection = if  (plantingDirection == MeshDirection.LEFT)  1  else -1
            val gamma =  theta + linesDirection * (Math.PI/2) // If we are painting the lines going left, then we must add 90 to current angle to get direction. Otherwise subtract 90.
            val lineDeltaX =  SINE_60 * GAP_SIZE_METRES * cos(gamma)
            val lineDeltaY =  SINE_60 * GAP_SIZE_METRES * sin(gamma)
            var lineStart = startPoint
            var xSkip = 1
            var lanesWidth = 0.0 // This is lanes (i.e. lines) times gap size
            while (lanesWidth < MAX_MESH_SIZE) {
                l.add(PlantingLine(lineStart, xDelta,yDelta))

                val nextLineOffsetX  = xSkip * (xDelta/2)
                val nexLineOffsetY = xSkip * (yDelta/2)

                lineStart = Point(lineStart.x + lineDeltaX + nextLineOffsetX, lineStart.y + lineDeltaY + nexLineOffsetY) // .move(lineDeltaX,lineDeltaY) // Move the start point to the next line
                lanesWidth += GAP_SIZE_METRES * SINE_60

                xSkip *= -1 // (xSkip + 1) % 2 // skip forward by half the displacement, or back by the same for each line...
            }
            return l
        }


        fun generateLongLat(
            c: Point,
            a: List<PlantingLine>,
            printLine: (List<LongLat>) -> Unit
        ): List<List<LongLat>> {
            val al = ArrayList<List<LongLat>>()
            for (l in a) {

                val xl = l.fromUTM(c)
                al.add(xl) // Use UTM centre...

                printLine(xl) // Cause it to be printed
            }

            return al
        }
    }
}