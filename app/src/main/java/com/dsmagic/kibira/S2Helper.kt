package com.dsmagic.kibira

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import dilivia.s2.S1ChordAngle
import dilivia.s2.S2LatLng
import dilivia.s2.S2Point
import dilivia.s2.index.point.PointData
import dilivia.s2.index.point.S2ClosestPointQuery
import dilivia.s2.index.point.S2PointIndex
import dilivia.s2.index.shape.MutableS2ShapeIndex
import dilivia.s2.index.shape.S2ClosestEdgeQuery
import dilivia.s2.region.S2Polyline


class S2Helper {
    // Helper class for S2 geometry stuff
    companion object {
        fun makeS2PointFromLngLat(loc: LatLng): S2Point {
            return S2LatLng.fromDegrees(loc.latitude, loc.longitude).toPoint()
        }

        fun makeS2PolyLine(l: List<LatLng>, pointsIndex: S2PointIndex<S2LatLng>): S2Polyline {
            return S2Polyline(l.map {
                val p = S2LatLng.fromDegrees(it.latitude, it.longitude)
                val pt = p.toPoint()
                pointsIndex.add(pt, p)
                pt // Val returned...
            })
        }

        fun addS2Polyline2Index(i: Int, index: MutableS2ShapeIndex, line: S2Polyline) {
            val s = S2Polyline.Shape(i, line)
            index.add(s)
        }

        fun findClosestLine(index: MutableS2ShapeIndex, loc: LatLng, ptList: List<Any?>): Any? {
            val q = S2ClosestEdgeQuery(index)
            val p = S2ClosestEdgeQuery.PointTarget(makeS2PointFromLngLat(loc))
            val r = q.findClosestEdge(p)
            if (r.isEmpty() || r.shapeId < 0)
                return null
            else if (r.shapeId > ptList.size)
                return null
            Log.d("closest", "Found closest point $r")
            return ptList[r.shapeId]
        }

        fun isPointWithInPlantingRadius(
            index: S2PointIndex<*>,
            pointLoc: LatLng,
            roverLoc: LatLng,
        ): Boolean {
            val target = S2ClosestPointQuery(index)
            val p = S2ClosestPointQuery.S2ClosestPointQueryPointTarget(makeS2PointFromLngLat(pointLoc))

         val result =  S1ChordAngle(makeS2PointFromLngLat(pointLoc), makeS2PointFromLngLat(roverLoc))
            return target.isDistanceLess(p,result)

        }

        fun findClosestPointOnLine(index: S2PointIndex<*>, loc: LatLng): Any? {
            val q = S2ClosestPointQuery(index)
            val p = S2ClosestPointQuery.S2ClosestPointQueryPointTarget(makeS2PointFromLngLat(loc))
            val r = q.findClosestPoint(p)
            if (r.isEmpty() || r.pointData == null)
                return null
            return r.data()
        }

    }
}