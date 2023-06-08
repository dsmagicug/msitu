package com.dsmagic.kibira.roomDatabase

/*
 *  This file is part of Msitu.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Msitu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Msitu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Msitu. If not, see <http://www.gnu.org/licenses/>
 */

import android.content.Context
import androidx.room.*

import com.dsmagic.kibira.roomDatabase.DAO.KibiraDao

import com.dsmagic.kibira.roomDatabase.Entities.*

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(entities=[Project::class,
    User::class,
    Coordinates::class,

    Basepoints::class,
    AreaCoordinates::class],
    version = 3)

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