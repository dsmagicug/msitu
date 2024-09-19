package com.dsmagic.kibira.dataReadings

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

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
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
                socket?.connect()
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
                var pre = ""
                val b = ByteArray(4096)
                readingStarted = true

                while (!stopIt) {
                    try {
                        val ln = input?.read(b) ?: -1
                        if (ln > 0) {
                            val s = String(b, 0, ln, Charset.forName("UTF-8"))
                            val sx = pre + s
                            var i = 0
                            val sn = sx.length
                            val l = arrayListOf<String>()
                            var idx = sx.indexOf("\n", i)

                            while (idx > 0) {
                                val xs = sx.substring(i, idx)
                                l.add(xs)
                                i = idx + 1
                                if (i >= sn) break
                                idx = sx.indexOf("\n", i)
                            }
                            pre = if (i < sn) sx.substring(i) else ""

                            for (xs in l) {
                                Log.d("SENTENCE", xs)
                                try {
                                    val longlat = LongLat(xs)
                                    if (longlat.fixType != LongLat.FixType.NoFixData) {
                                        handler.post {
                                            listener.postNewLocation(longlat, longlat.fixType)
                                        }
                                    }
                                } catch (exception: Exception) {
                                    Log.e("Parsing Error", "Failed to parse longlat", exception)
                                }
                            }
                        } else {
                            Thread.sleep(100)
                        }
                    } catch (e: IOException) {
                        Log.e("Bluetooth", "Socket closed or error reading", e)
                        stopIt = true // Stop the loop on error

                    }
                }
            }
            thread!!.start()
        }

    }

}