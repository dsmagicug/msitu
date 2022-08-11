package com.dsmagic.kibira.roomDatabase.DAO
//
//import androidx.room.*
//import com.dsmagic.kibira.roomDatabase.BasePoints
//import com.dsmagic.kibira.roomDatabase.Coordinates
//import com.dsmagic.kibira.roomDatabase.Project
//
//@Dao
//interface CoordinatesDAO {
//    @Query("SELECT * FROM coordinates WHERE project_id :pid")
//    suspend fun coordinates(pid: Int): Coordinates
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun addCoordinates(coordinates: Coordinates)
//
//    @Delete
//    suspend fun delete(coordinates: Coordinates)
//}
//
//@Dao
//interface BasePointsDAO {
//    @Query("SELECT * FROM coordinates WHERE project_id :pid")
//    suspend fun coordinates(pid: Int): Coordinates
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun addBasePoints(basepoints: BasePoints)
//
//    @Delete
//    suspend fun delete(basepoints: BasePoints)
//}