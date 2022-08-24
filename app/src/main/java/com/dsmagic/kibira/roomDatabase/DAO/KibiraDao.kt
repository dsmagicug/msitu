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

    /*Queries for the User */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Transaction
    @Query("SELECT * FROM User WHERE email = :email AND password = :password")
    suspend fun getUser(email: String, password: String): User

    @Transaction
    @Query("SELECT * FROM User WHERE id = :userID")
    suspend fun getProjectsForUser(userID: Int): List<userAndProject>

    /*Queries for the project */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project):Long

    @Transaction
    @Query("SELECT * FROM Project WHERE id = :PID ")   //gives me a single project
    suspend fun getParticularProject(PID: Int): Project

    @Transaction
    @Query("SELECT * FROM Project WHERE userID = :UID ORDER BY id DESC ")   //gives me all projects for that User, latest first
    suspend fun getAllProjects(UID: Int): List<Project>

    @Transaction
    @Query("SELECT id FROM Project WHERE gapsize = :gapsize AND name = :name")
    suspend fun getProjectID(gapsize: Double, name: String): Int

    @Query("DELETE FROM Project WHERE id = :ID ")
    suspend fun deleteProject(ID:Int):Int

    /*Queries for the points */
    @Transaction
    @Query("SELECT * FROM Coordinates  WHERE projectID = :projectID")
    suspend fun getCoordinatesForProject(projectID: Int): List<Coordinates>

    @Transaction
    @Query("SELECT * FROM Basepoints  WHERE projectID = :projectID")
    suspend fun getBasepointsForProject(projectID: Int): List<Basepoints>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordinates(coordinates: Coordinates): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBasepoints(basepoints: Basepoints): Long

    @Query("DELETE FROM Basepoints WHERE projectID = :PID")
    suspend fun deleteBasePoints(PID:Int)

    @Query("DELETE FROM Coordinates WHERE lat = :lat AND lng = :lng")
    suspend fun deleteSavedPoints(lat:Double,lng:Double)

}