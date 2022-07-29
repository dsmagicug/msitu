package com.dsmagic.kibira.utils;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

import dilivia.s2.S2LatLng;
import dilivia.s2.index.point.S2PointIndex;

public class GeneralHelper {

    public static S2PointIndex<S2LatLng> convertLineToS2(Collection<LatLng> list){
        S2PointIndex querySet = new S2PointIndex<S2LatLng>();
       for (LatLng x : list){
           S2LatLng s2LatLng = S2LatLng.fromDegrees(x.latitude, x.longitude);
           querySet.add(s2LatLng.toPoint(),s2LatLng );

       }
       return querySet;
    }

    public static float findDistanceBtnTwoPoints(LatLng pt1, LatLng pt2){
        Location firstPoint = new Location(LocationManager.GPS_PROVIDER);
        Location secondPoint = new Location(LocationManager.GPS_PROVIDER);
        // set latLong for first point
        firstPoint.setLatitude(pt1.latitude);
        firstPoint.setLongitude(pt1.longitude);
        // set latLong for second point
        secondPoint.setLatitude(pt2.latitude);
        secondPoint.setLongitude(pt2.longitude);

        return firstPoint.distanceTo(secondPoint); // in metres
    }
}
