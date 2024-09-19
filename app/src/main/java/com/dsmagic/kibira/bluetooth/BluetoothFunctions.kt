package com.dsmagic.kibira.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.dsmagic.kibira.activities.MainActivity
import com.dsmagic.kibira.dataReadings.NmeaReader

class BluetoothFunctions : AdapterView.OnItemSelectedListener {

    companion object {
        const val REQUEST_BLUETOOTH_CONNECT = 1
        private var bluetoothReceiverRegistered = false

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_CONNECT)
                            return
                        }

                        device?.let {
                            val deviceName = it.name
                            if (deviceName != null && !MainActivity.bluetoothList.contains(deviceName)) {
                                MainActivity.bluetoothList.add(deviceName)
                                MainActivity.deviceList.add(it)
                            }
                        }
                    }
                }
                displayBluetoothDevices(context)
            }
        }

        fun displayBluetoothDevices(context: Context) {
            val activity = context as Activity
            // Filter out any nulls from the list
            val items = MainActivity.bluetoothList.toList().toTypedArray()
            val adaptor = ArrayAdapter(context.applicationContext, android.R.layout.simple_spinner_dropdown_item, items)

            val spinner: Spinner = activity.findViewById(com.dsmagic.kibira.R.id.spinner)
            spinner.adapter = adaptor
            spinner.onItemSelectedListener = BluetoothFunctions()

            val buttonConnect: Button = activity.findViewById(com.dsmagic.kibira.R.id.buttonConnect)
            buttonConnect.visibility = Button.VISIBLE
            buttonConnect.setOnClickListener {
                if (MainActivity.device == null) return@setOnClickListener
                if (NmeaReader.readingStarted) {
                    NmeaReader.stop()
                } else openBluetooth(context)
            }
        }

        private fun openBluetooth(context: Context) {
            MainActivity.device?.let {
                NmeaReader.start(context, it)
            }
        }

        fun discoverBluetoothDevices(activity: Activity) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            if (!bluetoothAdapter.isEnabled) {
                if (ActivityCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
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

            if (ActivityCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 3)
                    return
                }
            }

            // Only start discovery if the device list is empty
            if (MainActivity.deviceList.isEmpty()) {
                if (bluetoothAdapter.isDiscovering) {
                    //bluetoothAdapter.cancelDiscovery()
                    Toast.makeText(activity, "Still scanning for BT devices....", Toast.LENGTH_LONG)
                        .show()
                }
                else{
                    bluetoothAdapter.startDiscovery()
                }
            }
        }

        fun registerBluetoothReceiver(context: Context) {
            if (!bluetoothReceiverRegistered) {
                val filter = IntentFilter()
                filter.addAction(BluetoothDevice.ACTION_FOUND)
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                context.registerReceiver(receiver, filter)
                bluetoothReceiverRegistered = true
            }
        }

        fun unregisterBluetoothReceiver(context: Context) {
            if (bluetoothReceiverRegistered) {
                context.unregisterReceiver(receiver)
                bluetoothReceiverRegistered = false
            }
        }

        private fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemSelected(var1: AdapterView<*>?, view: View?, i: Int, l: Long) {
        MainActivity.device = MainActivity.deviceList[i]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        // No action needed
    }
}
