package com.dsmagic.kibira.roomDatabase

import android.content.Context
import androidx.room.*

import com.dsmagic.kibira.roomDatabase.DAO.KibiraDao
import com.dsmagic.kibira.roomDatabase.Entities.Basepoints
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.dsmagic.kibira.roomDatabase.Entities.User
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(entities=[Project::class,
    User::class,
    Coordinates::class,
    Basepoints::class],
    version = 1)
abstract class AppDatabase:RoomDatabase() {
    abstract fun kibiraDao(): KibiraDao

    companion object{
        @Volatile
        private var instance :AppDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun dbInstance(c:Context):AppDatabase{
            val tempInstance = instance
            if(tempInstance != null){
                return tempInstance
            }
        synchronized(this){
            val newInstance = Room.databaseBuilder(
                c.applicationContext,
                AppDatabase::class.java,
                "kibira"
            ).build()
            instance = newInstance
            return newInstance

        }

        }
    }

}