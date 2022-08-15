package com.dsmagic.kibira.roomDatabase

//import androidx.room.ColumnInfo
//import androidx.room.Entity
//import androidx.room.ForeignKey
//import androidx.room.ForeignKey.Companion.CASCADE
//import androidx.room.PrimaryKey
//import com.google.android.gms.maps.model.LatLng
//import java.sql.Timestamp
//
////data class PointsDataClass()
//data class savePointsDataClass(
//    val list:List<LatLng>,
//    val project_id: Int,
//    val user_id: Int
//)
//
////@Entity(
////    tableName = "BasePoints", foreignKeys = [ForeignKey(
////        entity = Project::class,
////        childColumns = ["project_id"],
////        parentColumns = ["UID"],
////        onUpdate = ForeignKey.Companion.CASCADE,
////        onDelete = ForeignKey.Companion.CASCADE
////    )]
////)
//
//@Entity(
//   tableName = "BasePoints")
//data class BasePoints(
//    @PrimaryKey(autoGenerate = true) val id: Int?,
//    @ColumnInfo(name = "latitude") val lat:Double,
//    @ColumnInfo(name = "longitude") val lng: Double,
//    @ColumnInfo(name = "project_id") val project_id:Int
//
//)
//
////@Entity(
////    tableName = "coordinates", foreignKeys = [ForeignKey(
////        entity = Project::class,
////        childColumns = ["project_id","user_id"],
////        parentColumns = ["id","user_id"],
////        onUpdate = ForeignKey.Companion.CASCADE,
////        onDelete = ForeignKey.Companion.CASCADE
////    )]
////)
//@Entity(
//   tableName = "coordinates")
//data class Coordinates(
//    @PrimaryKey(autoGenerate = true) val id:Int,
//    @ColumnInfo(name = "latitude") val lat:Double,
//    @ColumnInfo(name = "longitude") val lng: Double,
//    @ColumnInfo(name = "projectID") val projectID:Int,
//    @ColumnInfo(name = "user_id") val user_id:Int,
//
//)
