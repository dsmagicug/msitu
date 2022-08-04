package com.dsmagic.kibira.services

import com.google.android.gms.maps.model.LatLng

data class savePointsDataClass(
    val list:List<LatLng>,
    val project_id: Int,
    val user_id: Int
)

data class SaveBasePointsClass(
    val lat:Double,
    val lng: Double,
    val project_id: Int
)