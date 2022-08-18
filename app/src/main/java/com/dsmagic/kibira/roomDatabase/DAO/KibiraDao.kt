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
    suspend fun insertProject(project: Project): Long

    @Transaction
    @Query("SELECT * FROM Project WHERE id = :PID ")   //gives me a single project
    suspend fun getParticularProject(PID: Int): Project

    @Transaction
    @Query("SELECT * FROM Project WHERE userID = :UID ")   //gives me all projects for that User
    suspend fun getAllProjects(UID: Int): List<Project>

    @Transaction
    @Query("SELECT id FROM Project WHERE gapsize = :gapsize AND name = :name")
    suspend fun getProjectID(gapsize: Double, name: String): Int

    /*Queries for the points */
    @Transaction
    @Query("SELECT * FROM Project  WHERE id = :projectID")
    suspend fun getCoordinatesForProject(projectID: Int): List<projectAndCoordinates>

    @Transaction
    @Query("SELECT * FROM Project  WHERE id = :projectID")
    suspend fun getBasepointsForProject(projectID: Int): List<projectWithBasepoints>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordinates(coordinates: Coordinates): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBasepoints(basepoints: Basepoints): Long

}