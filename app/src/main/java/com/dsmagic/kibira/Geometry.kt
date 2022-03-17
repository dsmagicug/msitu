package com.dsmagic.kibira

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import dilivia.s2.S2LatLng
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

val EARTH_RADIUS = 6367 * 1000 // In metres
val MAX_MESH_SIZE = 100.0 // In metres
val GAP_SIZE = 3.6 // In metres (or 12ft)

// Represents a point where a tree is planted. Units are metres.
class Point(internal var x: Double, internal var y: Double) {

    open fun toGPSCoords(centre: LongLat): LongLat {
        return LongLat(
            centre.long + ((x * 180) / (Math.PI * EARTH_RADIUS * cos(centre.lat))),
            centre.lat + (y * 180 / (Math.PI * EARTH_RADIUS))
        )
    }

    // Rotate the point X degrees around the origin..
    open fun rotate(cosTheta: Double, sinTheta: Double) {
        val xnew = x * cosTheta - y * sinTheta
        val yNew = x * sinTheta + y * cosTheta
        x = xnew
        y = yNew
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

    fun rotate(theta: Pair<Double, Double>): PlantingLine {
        for (p in points) {
            p.rotate(theta.first, theta.second)
        }
        return this
    }

    open fun toGPS(centre: LongLat): ArrayList<LongLat> {
        val l = ArrayList<LongLat>()
        for (p in points) {
            l.add(p.toGPSCoords(centre))
        }
        return l
    }
}

var LOCATION_PROVIDER = "Rtk"

class LongLat(var long: Double, var lat: Double) : Location(LOCATION_PROVIDER) {

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

    var latitude = ""
    var longitude = ""

    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("HHmmss.SSZ")

    @SuppressLint("SimpleDateFormat")
    val dateFormatLong = SimpleDateFormat("DDMMyy-HHmmss.SSZ")


    override fun toString(): String {
        return "${this.longitude}, ${this.latitude} (${this.long}, ${this.lat}), altitude: ${this.altitude}, hdop: ${this.hdop}, fix: ${this.fixType}"
    }

    // Project the point onto a plane..
    // See https://www.themathdoctors.org/distances-on-earth-3-planar-approximation/
    open fun projectToPlane(centre: LongLat): Point {
        val longdiff = long - centre.long
        val latdiff = lat - centre.lat
        return Point(
            longdiff * cos(centre.lat / Math.PI) * Math.PI * EARTH_RADIUS / 180.0,
            latdiff * Math.PI * EARTH_RADIUS / 180.0
        )
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
        } else
            fixType = FixType.NoFixData
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

    fun toS2(): S2LatLng {
        return S2LatLng((lat / Math.PI).toFloat(), (long / Math.PI).toFloat())
    }

    override fun getAltitude(): Double {
        return aboveSeaLevel
    }

    override fun getAccuracy(): Float {
        return if (fixType == FixType.RTKFix) 0.01f else 0.3f // XX rough guess
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
        return true
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
            0.0.toFloat()
        else
            speed!!.toFloat()
    }
}

class Geometry {
    companion object {
        private fun rotationMatrix(theta: Double): Pair<Double, Double> {
            // Takes an angle in radians, computes the rotation values cosTheta and signTheta
            val x = cos(theta)
            val y = sin(theta)

            Log.d("rotate", "sin($theta) = $y, cos($theta)= $x")
            return Pair(x, y)
        }

        // Get the angle made with the horizontal by the vector from the origin to a point.
        private fun theta(p: Point): Double {
            val x =  atan2(p.y, p.x)
            val y = x/Math.PI * 180
            Log.d("theta","Angle between horizontal and point is $x (or $y°)")
            return x
        }

        fun generateMesh(centre: LongLat, directionPoint: LongLat): ArrayList<PlantingLine> {
            val theta = theta(directionPoint.projectToPlane(centre))
            val mat = rotationMatrix(theta)
            val l = ArrayList<PlantingLine>()
            // X starts at the left. We draw the centre line first, then generate the ones below and above in order until we are done...
            val startX = -MAX_MESH_SIZE / 2.0
            var currentY = GAP_SIZE

            // Put in centre/base line
            l.add(PlantingLine(startX, 0.0, GAP_SIZE, MAX_MESH_SIZE).rotate(mat))

            while (currentY < MAX_MESH_SIZE / 2.0) {
                // The + one, then the - one
                l.add(PlantingLine(startX, currentY, GAP_SIZE, MAX_MESH_SIZE).rotate(mat))
                l.add(PlantingLine(startX, -currentY, GAP_SIZE, MAX_MESH_SIZE).rotate(mat))
                currentY += GAP_SIZE
            }

            return l
        }

        fun generateLongLat(
            centre: LongLat,
            a: ArrayList<PlantingLine>
        ): ArrayList<ArrayList<LongLat>> {
            val al = ArrayList<ArrayList<LongLat>>()
            for (l in a) {
                al.add(l.toGPS(centre))
            }

            return al
        }
    }
}