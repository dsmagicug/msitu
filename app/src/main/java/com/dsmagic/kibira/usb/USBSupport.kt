package com.dsmagic.kibira.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import com.dsmagic.kibira.LongLat
import com.dsmagic.kibira.NmeaReader
import com.dsmagic.kibira.notifications.NotifyUserSignals.Companion.handler
import com.felhr.usbserial.SerialInputStream
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.nio.charset.Charset


class USBSupport {
companion object{
    val ACTION_USB_PERMISSION = "Permission"
    lateinit var mUsbManager: UsbManager
    var mDevice:UsbDevice? = null
    var mSerial:UsbSerialDevice? = null
    var mConnection:UsbDeviceConnection? = null
    var serialInputStream: SerialInputStream? = null

    fun disconnect(){
mSerial!!.syncClose()
    }
    fun isReadingData(context: Context){
        val usbDevices:HashMap<String,UsbDevice>? = mUsbManager.deviceList
        if(!usbDevices!!.isEmpty()){
            var keep = true
            usbDevices.forEach { device ->
                mDevice = device.value
                var deviceVendorId:Int? = mDevice!!.vendorId
//                if(deviceVendorId == 112){
//
//                }
                val intent:PendingIntent = PendingIntent.getBroadcast(context,0,Intent(
                    ACTION_USB_PERMISSION),0)
                mUsbManager.requestPermission(mDevice,intent)

                keep = false
                Log.d("Serial","Connection Successful here")

if(!keep){
    return
}
            }

        } else {
            Log.d("Serial","No usb devices")
        }
    }

     val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action!! == ACTION_USB_PERMISSION) {
                synchronized(this) {
                   // val accessory: UsbAccessory? = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY)
            val granted:Boolean = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (granted) {
                          mConnection = mUsbManager.openDevice(mDevice)
                            mSerial = UsbSerialDevice.createUsbSerialDevice(mDevice, mConnection)
                        if(mSerial != null){
                            if(mSerial!!.syncOpen()){
                                mSerial!!.setBaudRate(115200)
//                                mSerial!!.setDataBits(UsbSerialDevice.DATA_BITS_8)
//                                mSerial!!.setStopBits(UsbSerialDevice.STOP_BITS_1)
//                                mSerial!!.setParity(UsbSerialDevice.PARITY_NONE)
                                mSerial!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                                serialInputStream  = mSerial!!.inputStream
                                startReading()

                            }else {
                    Log.d("Serial","Port not open")
                            }
                        }else {

                        }


                    } else {
                        Log.d("Serial", "Permission not granted")
                    }
                }
            } else if(intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED){
                isReadingData(context!!)
            }
            else if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED){
            disconnect()
            }
        }
    }

    fun startReading(){
        GlobalScope.launch(Dispatchers.IO) {
            while(true){
                if(serialInputStream == null){
                    return@launch
                }
                val value = serialInputStream!!.read()

                if(value != -1 && value > 0){
                    val b = ByteArray(value)
                    Log.d("Serial","$b")
                    serialInputStream!!.read(b)
                    b.filter {
                        it > 0
                    }
                    val s = String(b, Charset.forName("UTF-8"))
                    Log.d("Serial"," string $s")
                    val l =
                        s.split("\n") // Into lines... Crude. What if we read only up to part of sentence??
                    for (xs in l) {
                        // Hand off to higher level...
                        val longlat = LongLat(xs)
                        if (longlat.fixType != LongLat.FixType.NoFixData) {

                            if(longlat.fixType == LongLat.FixType.RTKFloat || longlat.fixType == LongLat.FixType.RTKFix){
                                // Send it to the Location Source... BUT ONLY when we have rtk data--(more accurate than other fixtypes)
                                handler.post {
                                    NmeaReader.listener.postNewLocation(longlat,longlat.fixType)
                                }
                            }
                        }
                    }
                }

            }
        }


    }
}


}