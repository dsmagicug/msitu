package com.dsmagic.kibira.roomDatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import java.sql.Timestamp
//
//
//@Entity(tableName = "project",
//foreignKeys=arrayOf(
//    ForeignKey(entity = UserDataClass::class,
//    parentColumns = arrayOf("UID"),
//    childColumns = arrayOf("user_id"))
//))
//
@Entity(tableName = "project")
data class Project(
    @PrimaryKey(autoGenerate = true) val id:Int,
    @ColumnInfo(name ="gap_size") val gap_size:Double,
    @ColumnInfo(name = "line_length") val line_length:Double,
    @ColumnInfo(name = "name") val name:String,
    @ColumnInfo(name = "user_id") val user_id:String,


)
