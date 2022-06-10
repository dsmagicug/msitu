package com.dsmagic.kibira

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

class NmeaReader {
    companion object {
        var readingStarted = false
        var stopIt = false
        var thread: Thread? = null
        val listener = RtkLocationSource()
        // var listener : LocationSource.OnLocationChangedListener? = null
         var socket : BluetoothSocket? = null
        var input: InputStream? = null
        fun stop() {
            thread?.interrupt()
            readingStarted = false
        }

        private const val ANGLE_SIGNIFICANT_DIFF = 0.5e-7
        fun significantChange(old: Location?, new: Location): Boolean {

            return old == null || Math.abs(old.latitude - new.latitude) > ANGLE_SIGNIFICANT_DIFF ||
                    Math.abs(old.longitude - new.longitude) > ANGLE_SIGNIFICANT_DIFF
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
//            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Serial port UUID
//            val socket = device.createRfcommSocketToServiceRecord(uuid)
//            socket.connect()
//            input = socket.inputStream
//            Log.d("bt", "Bluetooth connect complete")
//            startDataListener()
            try{
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Serial port UUID
                val socket = device.createRfcommSocketToServiceRecord(uuid)
               val conn = socket?.connect()
                Log.d("uuid","$socket")
                if (conn != null) {
                   // Toast.makeText(MainActivity(),"Paired",Toast.LENGTH_LONG).show()
                        Log.d("conn","coonected")
                }else{
                 //Toast.makeText(MainActivity(),"Could not Pair",Toast.LENGTH_LONG).show()
                }
                input = socket?.inputStream
                startDataListener()
            }catch (e:IOException){
                e.printStackTrace()
            }

        }

        private fun startDataListener() {
           
            val looper = Looper.getMainLooper()
            val handler = Handler(looper)


            thread = Thread {
                readingStarted = true
                while (!stopIt) {
                    val n = input?.available()
                    if (n != null && n > 0) {
                        val b = ByteArray(n)
                        input?.read(b)
                        val s = String(b, Charset.forName("UTF-8"))
                        val l =
                            s.split("\n") // Into lines... Crude. What if we read only up to part of sentence??
                        for (xs in l) {
                            // Hand off to higher level...
                            val longlat = LongLat(xs)
                            if (longlat.fixType != LongLat.FixType.NoFixData) {
                                // Send it to the Location Source...
                                handler.post {

                                    listener.postNewLocation(longlat)
                                }
                            }
                        }
                    }
                }
            }

            thread!!.start()
        }
    }
}