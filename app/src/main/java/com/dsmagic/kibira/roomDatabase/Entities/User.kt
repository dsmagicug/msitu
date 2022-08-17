package com.dsmagic.kibira.roomDatabase.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val id:Int?,
    val name:String,
    val password:String,
    val email:String
)
