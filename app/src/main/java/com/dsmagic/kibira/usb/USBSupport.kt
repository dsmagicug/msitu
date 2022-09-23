package com.dsmagic.kibira.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.hoho.android.usbserial.driver.*
import java.io.IOException
import java.io.InputStream
import java.util.HashMap


class USBSupport {
    companion object {
        lateinit var manager: UsbManager
        var driver: UsbSerialDriver? = null
        var mDevice: UsbDevice? = null
        private var connection: UsbDeviceConnection? = null
        private var port: UsbSerialPort? = null
        const val ACTION_USB_PERMISSION = "Permission"
        var mSerial:UsbSerialDevice? = null
        private const val READ_WAIT_MILLIS = 2000
        var serialInputStream: InputStream? = null
        var buffer = ByteArray(8192)

        fun disconnect() {
            port!!.close()
        }

        fun setUSBConnection(context: Context) {
            val usbDevices: HashMap<String, UsbDevice>? = manager.deviceList
            if (usbDevices!!.isNotEmpty()) {
                usbDevices.forEach { device ->
                    mDevice = device.value
                    val intent: PendingIntent = PendingIntent.getBroadcast(
                        context, 0, Intent(
                            ACTION_USB_PERMISSION
                        ), 0
                    )
                    manager.requestPermission(mDevice, intent)

                    Log.d("Serial", "Connection Successful here")

                }
            }
        }

        val usbReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action!! == ACTION_USB_PERMISSION) {
                    synchronized(this) {
                        val granted: Boolean =
                            intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        if (granted) {
                            //port = driver?.ports?.get(0)
                            connection = manager.openDevice(mDevice)
                            mSerial = UsbSerialDevice.createUsbSerialDevice(mDevice, connection)
                            if(mSerial != null){
                                if(mSerial!!.syncOpen()){
                                    mSerial!!.setBaudRate(115200)
                                    mSerial!!.setDataBits(UsbSerialDevice.DATA_BITS_8)
                                    mSerial!!.setStopBits(UsbSerialDevice.STOP_BITS_1)
                                    mSerial!!.setParity(UsbSerialDevice.PARITY_NONE)
                                    mSerial!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_XON_XOFF)
                                    serialInputStream = mSerial!!.inputStream as InputStream

                                }else {
                                    Log.d("Serial","Port not open")
                                }
                            } else {
                                Log.d("Serial","Port is null")
                            }
                        } else {
                            Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT)
                                .show()
                            Log.d("Serial", "Permission not granted")
                        }
                    }
                } else if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                    setUSBConnection(context!!)
                } else if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                    disconnect()
                }
            }
        }


    }


}