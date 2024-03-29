package com.dsmagic.kibira.dataReadings

/*
 *  This file is part of Msitu.
 *
 * https://github.com/dsmagicug/msitu.git
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

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

import kotlin.math.abs


class NmeaReader {
    companion object {
        var readingStarted = false
        var stopIt = false
        var thread: Thread? = null
        val listener = RtkLocationSource()

        // var listener : LocationSource.OnLocationChangedListener? = null
        var socket: BluetoothSocket? = null
        var input: InputStream? = null
        fun stop() {
            thread?.interrupt()
            readingStarted = false
        }

        private const val ANGLE_SIGNIFICANT_DIFF = 0.5e-7
        fun significantChange(old: Location?, new: Location): Boolean {
            return old == null || Math.abs(old.latitude - new.latitude) > ANGLE_SIGNIFICANT_DIFF ||

                    abs(old.longitude - new.longitude) > ANGLE_SIGNIFICANT_DIFF

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun start(context: Context, device: BluetoothDevice) {
            if (socket != null) {
                try {
                    socket!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                socket = null
            }


            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling

                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //  return
            }

            try {
                val uuid =
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Serial port UUID
                val socket = device.createRfcommSocketToServiceRecord(uuid)
              val con =  socket?.connect()
                input = socket?.inputStream

                Toast.makeText(context, "Device successfully paired", Toast.LENGTH_LONG).show()
                startDataListener()
            } catch (e: IOException) {

                Toast.makeText(context, "Could not Pair! Make sure device is on", Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
                stop()
            }

        }


        private fun startDataListener() {

            val looper = Looper.getMainLooper()
            val handler = Handler(looper)
            thread = Thread {
                readingStarted = true
                var buf = ""
                while (!stopIt) {
                    val n = input?.available()
                    if (n != null && n > 0) {
                        val b = ByteArray(n)
                        input?.read(b)
                        val s = String(b, Charset.forName("UTF-8"))
                        Log.d("loc_string", s)
                        buf += s
                        var i = buf.indexOf("\n", 0)

                        while (i >= 0) {
                            val xs = buf.substring(0, i);
                            Log.d("loc_string_e", xs)
                            buf = buf.substring(i + 1)
                            i = buf.indexOf("\n",0)
                            // Hand off to higher level...
                             try{
                                val longlat = LongLat(xs)
                                if (longlat.fixType != LongLat.FixType.NoFixData) {
                                    handler.post {
                                        listener.postNewLocation(longlat,longlat.fixType)
                                    }

                                }
                            }catch (exception :Exception){
                                Log.e("Error","$exception")
                            }

                        }
                    }
                }
             }
            thread!!.start()
        }
    }

}