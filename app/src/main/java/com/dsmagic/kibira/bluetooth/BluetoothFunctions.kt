package com.dsmagic.kibira.bluetooth


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
import android.R
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import com.dsmagic.kibira.activities.MainActivity
import com.dsmagic.kibira.activities.MainActivity.Companion.deviceList
import com.dsmagic.kibira.dataReadings.NmeaReader

class BluetoothFunctions : AdapterView.OnItemSelectedListener {

    companion object {
        // Create a BroadcastReceiver for ACTION_FOUND.
        val receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                var t = 90
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                        if (ActivityCompat.checkSelfPermission(
                                context, Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            //return
                        }
                        if (!MainActivity.bluetoothList.contains(device!!.name)) {
                            if (device.name == null) {
                                return
                            }
                            MainActivity.bluetoothList.add(device.name)
                            deviceList.add(device)
                        }
                    }
                }
                displayBluetoothDevices(context)
            }
        }

        fun displayBluetoothDevices(context: Context) {

            val activity = context as Activity
            val items = MainActivity.bluetoothList.toArray()
            val adaptor = ArrayAdapter(
                context.applicationContext, R.layout.simple_spinner_dropdown_item, items
            )

            val spinner: Spinner = activity.findViewById(com.dsmagic.kibira.R.id.spinner)
            spinner.adapter = adaptor
            spinner.onItemSelectedListener = BluetoothFunctions()

            val buttonConnect: Button = activity.findViewById(com.dsmagic.kibira.R.id.buttonConnect)
            buttonConnect.visibility = Button.VISIBLE
            buttonConnect.setOnClickListener {
                if (MainActivity.device == null) return@setOnClickListener
                if (NmeaReader.readingStarted) {
                    NmeaReader.stop()
                } else openBlueTooth(context)
            }

        }

        private fun openBlueTooth(context: Context) {
            MainActivity.device?.let {
                // Load the map
                context.let { it1 ->
                    NmeaReader.start(it1, it)
                }

            }

        }

        fun discoverBluetoothDevices(activity: Activity) {

            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            if (!bluetoothAdapter.isEnabled) {
                if (checkSelfPermission(activity.applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 2)

                                          return

                    } else {
                        activity.startActivity(enableBT)
                    }
                } else {
                    activity.startActivity(enableBT)
                }

            }
            if (ActivityCompat.checkSelfPermission(
                    activity.applicationContext, Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 3)
                return

                }

            }
                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                    bluetoothAdapter.startDiscovery()
                } else {
                    bluetoothAdapter.startDiscovery()


            }


        }


    }

    override fun onItemSelected(
        var1: AdapterView<*>?,
        view: View?,
        i: Int,
        l: Long,
    ) {
        MainActivity.device = deviceList[i]

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}