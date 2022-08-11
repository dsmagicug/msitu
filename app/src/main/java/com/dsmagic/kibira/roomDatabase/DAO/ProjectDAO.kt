package com.dsmagic.kibira.roomDatabase.DAO
//
//import androidx.room.*
//import com.dsmagic.kibira.roomDatabase.Project
//
//
//@Dao
//interface ProjectDAO {
//    @Query("SELECT * FROM PROJECTS WHERE user_id :uid")
//    suspend fun projects(uid: Int): Project
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun addProject(Project: Project)
//
//    @Delete
//    suspend fun delete(Project: Project)
//}