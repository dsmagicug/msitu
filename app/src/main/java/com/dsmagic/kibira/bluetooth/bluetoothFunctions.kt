package com.dsmagic.kibira.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.MainActivity.Companion.bluetoothList
import com.dsmagic.kibira.MainActivity.Companion.context
import com.dsmagic.kibira.MainActivity.Companion.deviceList


class bluetoothFunctions {
    companion object {

//        // Create a BroadcastReceiver for ACTION_FOUND.
//         val receiver = object : BroadcastReceiver() {
//
//            override fun onReceive(context: Context, intent: Intent) {
//                val action: String? = intent.action
//                when (action) {
//                    BluetoothDevice.ACTION_FOUND -> {
//                        val device: BluetoothDevice? =
//                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//
//                        if (ActivityCompat.checkSelfPermission(
//                                context,
//                                Manifest.permission.BLUETOOTH_CONNECT
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            // TODO: Consider calling
//                            //    ActivityCompat#requestPermissions
//                            // here to request the missing permissions, and then overriding
//                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                            //                                          int[] grantResults)
//                            // to handle the case where the user grants the permission. See the documentation
//                            // for ActivityCompat#requestPermissions for more details.
//                            //return
//                        }
//
//                        //if (!bluetoothList.contains(device!!.name)) {
//
//                        bluetoothList.add(device!!.name)
//                        deviceList.add(device)
//                        var v = device.name
//                        //}
//
//                    }
//
//                }
//                MainActivity().displayBluetoothDevices()
//            }
//        }
//         fun discoverBluetoothDevices() {
//
//            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.BLUETOOTH_SCAN
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                //return
//            }
//
//            //checkLocation()
//            if (bluetoothAdapter.isDiscovering) {
//                bluetoothAdapter.cancelDiscovery()
//                bluetoothAdapter.startDiscovery()
//            } else {
//                bluetoothAdapter.startDiscovery()
//
//            }
//
//        }
//        fun checkLocation() {
//            if (androidx.core.app.ActivityCompat.checkSelfPermission(
//                    context,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//                )
//                != android.content.pm.PackageManager.PERMISSION_GRANTED
//            ) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(
//                        thisActivity,
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    )
//                ) {
//                    ActivityCompat.requestPermissions(
//                        thisActivity,
//                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
//                    )
//                } else {
//                    ActivityCompat.requestPermissions(
//                        thisActivity,
//                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
//                    )
//                }
//            }
//        }


    }
}