package com.dsmagic.kibira

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

    class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
        SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

        // below is the method for creating a database by a sqlite query
        override fun onCreate(db: SQLiteDatabase) {
            // below is a sqlite query, where column names
            // along with their data types is given
            val createProjectQuery =

            ("CREATE TABLE $TABLE_NAME ($ID_COL INTEGER PRIMARY KEY, $NAME_COl TEXT,$GAP_SIZE_COL TEXT)")

            // we are calling sqlite
            // method for executing our query
            db.execSQL(createProjectQuery)
        }

        override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
            // this method is to check if table already exists
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }

        // This method is for adding data in our database
        fun addProject(name : String, gap_size : Int ){

            // below we are creating
            // a content values variable
            val values = ContentValues()

            // we are inserting our values
            // in the form of key-value pair
            values.put(NAME_COl, name)
            values.put(GAP_SIZE_COL, gap_size)

            // here we are creating a
            // writable variable of
            // our database as we want to
            // insert value in our database
            val db = this.writableDatabase

            // all values are inserted into database
            db.insert(TABLE_NAME, null, values)

            // at last we are
            // closing our database
            db.close()
        }

        // below method is to get
        // all data from our database
        fun getProject(): Cursor? {

            // here we are creating a readable
            // variable of our database
            // as we want to read value from it
            val db = this.readableDatabase
            val list = ArrayList<project>()
            // below code returns a cursor to
            // read data from the database
            return db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        }

        companion object{
            // here we have defined variables for our database

            private val DATABASE_NAME = "Kibira"

            private val DATABASE_VERSION = 1

            val TABLE_NAME = "projects"

            val ID_COL = "id"

            val NAME_COl = "project_name"

            val GAP_SIZE_COL = "gap_size"
        }
    }

class project {

}

