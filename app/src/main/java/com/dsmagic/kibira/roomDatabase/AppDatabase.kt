package com.dsmagic.kibira.roomDatabase
//
//import android.content.Context
//import androidx.room.*
//import com.dsmagic.kibira.roomDatabase.DAO.BasePointsDAO
//import com.dsmagic.kibira.roomDatabase.DAO.CoordinatesDAO
//
//@Database(entities=[Project::class,UserDataClass::class,Coordinates::class,BasePoints::class], version = 1)
//abstract class AppDatabase:RoomDatabase() {
//    abstract fun coordinatesDAO(): CoordinatesDAO
//    abstract fun basepointsDAO(): BasePointsDAO
//
//    companion object{
//        @Volatile
//        private var instance :AppDatabase? = null
//
//        fun dbInstance(c:Context):AppDatabase{
//            val tempInstance = instance
//            if(tempInstance != null){
//                return tempInstance
//            }
//        synchronized(this){
//            val newInstance = Room.databaseBuilder(
//                c.applicationContext,
//                AppDatabase::class.java,
//                "kibira"
//            ).build()
//            instance = newInstance
//            return newInstance
//
//        }
//
//        }
//    }
//
//}