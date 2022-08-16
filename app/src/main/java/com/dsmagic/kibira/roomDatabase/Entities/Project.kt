package com.dsmagic.kibira.roomDatabase.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val gapsize:Double,
    val lineLength:Double,
    val userID:Int
)
