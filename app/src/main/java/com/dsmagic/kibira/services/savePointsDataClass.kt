package com.dsmagic.kibira.services

data class savePointsDataClass(
    val lat: Double,
    val lng: Double,
    val project_id: Int,
    val user_id: Int
)

data class SaveBasePointsClass(
    val lat:Double,
    val lng: Double,
    val project_id: Int
)