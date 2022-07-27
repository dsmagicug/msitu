package com.dsmagic.kibira.utils;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.math3.geometry.spherical.twod.S2Point;

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
}
