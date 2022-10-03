package com.dsmagic.kibira.utils;

import android.content.ClipboardManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.dsmagic.kibira.MainActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

import dilivia.s2.S2LatLng;
import dilivia.s2.index.point.S2PointIndex;

public class GeneralHelper {

    public static S2PointIndex<S2LatLng> convertLineToS2(Collection<LatLng> list){
        S2PointIndex<S2LatLng> querySet = new S2PointIndex<S2LatLng>();
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

    public static float sanitizeMagnetometerBearing(Float bearing){
        if (bearing < 0) {
            return -1 * bearing + 5 ;
        }
        return 360f - bearing - 5;
    }

    public static void changeMapPosition(GoogleMap map, Float angle)  {
        CameraPosition position = map.getCameraPosition();
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(position.target)
                .zoom(position.zoom)
                .tilt(position.tilt)
                .bearing(angle+5) // tilt if further more by 5
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}
