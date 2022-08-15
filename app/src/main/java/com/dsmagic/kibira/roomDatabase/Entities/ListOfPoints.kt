package com.dsmagic.kibira.roomDatabase.Entities

import androidx.room.ColumnInfo

data class ListOfPoints(
    @ColumnInfo(name = "latitude") val lat:Double,
    @ColumnInfo(name = "longitude") val lng: Double
)
