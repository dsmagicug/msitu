package com.dsmagic.kibira.roomDatabase.DAO

import androidx.room.*
import com.dsmagic.kibira.roomDatabase.Entities.Basepoints
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.dsmagic.kibira.roomDatabase.Entities.User
import com.dsmagic.kibira.roomDatabase.relations.projectAndCoordinates
import com.dsmagic.kibira.roomDatabase.relations.projectWithBasepoints
import com.dsmagic.kibira.roomDatabase.relations.userAndProject

@Dao
interface KibiraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE )
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE )
    suspend fun insertProject(project: Project)

    @Insert(onConflict = OnConflictStrategy.REPLACE )
    suspend fun insertCoordinates(coordinates: Coordinates)

    @Insert(onConflict = OnConflictStrategy.REPLACE )
    suspend fun insertBasepoints(basepoints: Basepoints)

    @Transaction
    @Query("SELECT * FROM User WHERE id = :userID")
    suspend fun getProjectsForUser(userID:Int):List<userAndProject>

    @Transaction
    @Query("SELECT lat, lng FROM Project  WHERE id = :projectID")
    suspend fun getCoordinatesForProject(projectID:Int):List<projectAndCoordinates>

    @Transaction
    @Query("SELECT * FROM Project  WHERE id = :projectID")
    suspend fun getBasepointsForProject(projectID:Int):List<projectWithBasepoints>


}