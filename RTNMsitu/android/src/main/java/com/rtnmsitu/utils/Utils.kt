package com.rtnmsitu.utils

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.fasterxml.jackson.annotation.JsonFormat
import com.rtnmsitu.geometry.GAP_SIZE_METRES
import com.rtnmsitu.geometry.Geometry
import com.rtnmsitu.geometry.LongLat
import com.rtnmsitu.geometry.MAX_MESH_SIZE
import com.rtnmsitu.geometry.MeshDirection
import com.rtnmsitu.geometry.PlantingLine
import com.rtnmsitu.geometry.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Utils {

    enum class MeshType {
        SQUARE,
        TRIANGLE
    }
    fun extractLongLat(o: ReadableMap): LongLat {
        val latitude = o.getDouble("latitude")
        val longitude = o.getDouble("longitude")
        return LongLat(longitude, latitude)
    }

    fun toDateString(d: Any, pattern:String): String? {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        return when (d) {
            is Date -> dateFormat.format(d)
            is SimpleDateFormat -> d.parse(d.toPattern())?.let { dateFormat.format(it) }
            else -> throw IllegalArgumentException("Unsupported date type: ${d::class.java}")
        }
    }

    fun lineToCoordinate(line:List<LongLat>):WritableArray{
        val resultArray = Arguments.createArray()
        line.forEach { pt ->
            val latLongMap = longLatToWritable(pt)
            resultArray.pushMap(latLongMap)
        }
        return resultArray
    }
    private fun longLatToWritable(o: LongLat): ReadableMap {
        val map = Arguments.createMap()
        map.putDouble("latitude", o.lat)
        map.putDouble("longitude", o.long)
        map.putString("fixType", o.fixType.toString())
        map.putDouble("altitude", o.altitude)
        try{
            map.putString("timeStamp", toDateString(o.timeStamp, "yyyy-MM-dd HH:mm:ss"))
        }catch (e:Exception){
            Log.e("MSITU-ERROR", e.message!!)
        }
        map.putInt("numSatellites", o.numSatellites)
        map.putDouble("hdop", o.hdop)
        map.putDouble("aboveSeaLevel", o.aboveSeaLevel)

        if (o.speed != null) {
            map.putDouble("speed", o.speed!!)
        } else {
            map.putNull("speed")
        }

        if (o.accuracy != null) {
            map.putDouble("accuracy", o.accuracy!!)
        } else {
            map.putNull("accuracy")
        }

        return map
    }

    fun toPlantingLine(points: ReadableArray):PlantingLine{
        val pts = arrayListOf<Point>()
        for (i in 0 until points.size()) {
            val p: ReadableMap = points.getMap(i)
            val point =  extractPoint(p)
            pts.add(point)
        }
        return PlantingLine(pts)
    }

    fun toPlantingLines(lines: ReadableArray): MutableList<PlantingLine> {
        val plantingLines = mutableListOf<PlantingLine>()

        for (i in 0 until lines.size()) {
            val points: ReadableArray = lines.getArray(i)
            val plantingLine = toPlantingLine(points)
            plantingLines.add(plantingLine)
        }

        return plantingLines
    }

    fun extractPoint(o: ReadableMap): Point {
        val x = o.getDouble("x")
        val y = o.getDouble("y")
        val p = Point(x, y)
        p.zone =o.getInt("zone")
        p.hemisphere = o.getString("hemisphere").toString()
        return p
    }

    suspend fun plotMesh(
        firstBasePoint: LongLat,
        secondBasePoint: LongLat,
        direction: MeshDirection,
        gapSize: Double,
        lineLength: Double,
        meshType: MeshType
    ): MutableList<PlantingLine> = withContext(Dispatchers.Default) {
        val firstPoint = Point(firstBasePoint)
        val secondPoint = Point(secondBasePoint)
        GAP_SIZE_METRES = gapSize
        MAX_MESH_SIZE = lineLength

        val projectLines = if (meshType == MeshType.TRIANGLE) {
            Geometry.generateTriangleMesh(firstPoint, secondPoint, direction) as MutableList<PlantingLine>
        } else {
            Geometry.generateSquareMesh(firstPoint, secondPoint, direction) as MutableList<PlantingLine>
        }

        return@withContext projectLines
    }

}