package com.dsmagic.kibira.roomDatabase.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Coordinates(
    @PrimaryKey(autoGenerate = true)
    val id:Int?,
    val lat:Double,
    val lng:Double,
    val projectID:Int,
)
