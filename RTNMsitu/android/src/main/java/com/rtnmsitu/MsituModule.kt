import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import com.rtnmsitu.NativeRTNMsituSpec
import com.rtnmsitu.geometry.Geometry
import com.rtnmsitu.geometry.MeshDirection
import com.rtnmsitu.geometry.Point
import com.rtnmsitu.utils.Mapper
import com.rtnmsitu.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@ReactModule(name = "RTNMsitu")
class MsituModule(reactContext: ReactApplicationContext) : NativeRTNMsituSpec(reactContext) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun getName(): String {
        return NAME
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    override fun toPoint(coord: ReadableMap?): WritableMap? {
        try {
            val latLong = coord?.let { Utils.extractLongLat(it) }
            val point = latLong?.let { Point(it) }
            return Mapper.toWritableMap(point)
        }catch (e:Exception){
            return null
        }
    }

    override fun lineToCoords(points: ReadableArray, center: ReadableMap, promise: Promise) {
        try {

            val line = Utils.toPlantingLine(points)
            val centerPoint = Utils.extractPoint(center)
            val coordLine = Geometry.generateLongLatLine(centerPoint, line)
            promise.resolve(Utils.lineToCoordinate(coordLine))
        }catch (e:Exception){
            promise.reject("Error", e.message)
        }
    }

    override fun linesToCoords(lines: ReadableArray, center: ReadableMap, promise: Promise) {
        try {
            val plantingLines = Utils.toPlantingLines(lines)
            val centerPoint = Utils.extractPoint(center)
            val coordLines = Geometry.generateLongLatLines(centerPoint, plantingLines)
            val resultArray = Arguments.createArray()
            coordLines.forEach{line->
                resultArray.pushArray(Utils.lineToCoordinate(line))
            }
            promise.resolve(resultArray)
        }catch (e:Exception){
            promise.reject("Error", e.message)
        }

    }


    override fun generateMesh(
        first: ReadableMap?,
        second: ReadableMap?,
        meshDirection: String?,
        meshType: String,
        gapSize: Double,
        lineLength: Double,
        promise: Promise
    ) {
        try {
            val firstPoint = first?.let { Utils.extractLongLat(it) }
            val secondPoint = second?.let { Utils.extractLongLat(it) }
            val direction = meshDirection?.let { MeshDirection.valueOf(it) }

            if (firstPoint == null || secondPoint == null || direction == null) {
                promise.reject("InvalidArgument", "First, Second, and MeshDirection must not be null")
                return
            }

            // Use coroutineScope to launch a coroutine off the main thread
            coroutineScope.launch {
                try {
                    val lines = Utils.plotMesh(firstPoint, secondPoint, direction, gapSize, lineLength, Utils.MeshType.valueOf(meshType))

                    // Convert lines to WritableArray
                    val resultArray = Arguments.createArray()
                    lines.forEach { line ->
                        val lineArray = Arguments.createArray()
                        line.points.forEach { point ->
                            val pointMap = Arguments.createMap().apply {
                                putDouble("x", point.x)
                                putDouble("y", point.y)
                                putInt("zone", point.zone)
                                putString("hemisphere", point.hemisphere)
                            }
                            lineArray.pushMap(pointMap)
                        }
                        resultArray.pushArray(lineArray)
                    }
                    promise.resolve(resultArray)
                } catch (e: Exception) {
                    promise.reject("MeshGenerationError", e.message)
                }
            }
        } catch (e: Exception) {
            promise.reject("Error", e.message)
        }
    }

    companion object {
        const val NAME = "RTNMsitu"
    }
}
