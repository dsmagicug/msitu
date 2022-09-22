package com.dsmagic.kibira.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*


class USBSupport {
companion object{
    val ACTION_USB_PERMISSION = "Permission"
    lateinit var mUsbManager: UsbManager
    var mDevice:UsbDevice? = null
    var mSerial:UsbSerialDevice? = null
    var mConnection:UsbDeviceConnection? = null
    var serialInputStream: InputStream? = null
    var usbThread: Thread? = null
    var usbInterface:UsbInterface? = null
    var mEndPoint:UsbEndpoint? = null

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

        @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action!! == ACTION_USB_PERMISSION) {
                synchronized(this) {
                   // val accessory: UsbAccessory? = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY)
            val granted:Boolean = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (granted) {
                        for (i in 0 until mDevice!!.interfaceCount) {
                            val ourInterface: UsbInterface = mDevice!!.getInterface(i)
                            if (ourInterface.interfaceClass == UsbConstants.USB_CLASS_CDC_DATA) {
                                usbInterface = ourInterface
                                break
                            }
                        }

                        for( i in 0 until usbInterface!!.endpointCount) {
                            val ep: UsbEndpoint = usbInterface!!.getEndpoint(i)
                            if (ep.direction == UsbConstants.USB_DIR_IN) {
                               if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                    mEndPoint = ep
                                }
                            }
                        }
                        mConnection = mUsbManager.openDevice(mDevice)
                       mConnection!!.claimInterface(usbInterface,true)

                        var r:UsbRequest? = null
                        r!!.endpoint

                       // startReading()

                       // mConnection!!.bulkTransfer(usbInterface!!.getEndpoint(1),b,11023,0)
                          // mSerial = UsbSerialDevice.createUsbSerialDevice(mDevice, mConnection)
//                        mDevice?.getInterface(0)?.also { intf ->
//                            intf.getEndpoint(0)?.also { endpoint ->
//                                mUsbManager.openDevice(mDevice)?.apply {
//                                    claimInterface(intf, true)
//                                    startReading()
//                                    //bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT) //do in another thread
//                                }
//                            }
                        if(mSerial != null){
                            if(mSerial!!.syncOpen()){
                                mSerial!!.setBaudRate(115200)
                                mSerial!!.setDataBits(UsbSerialDevice.DATA_BITS_8)
                                mSerial!!.setStopBits(UsbSerialDevice.STOP_BITS_1)
                                mSerial!!.setParity(UsbSerialDevice.PARITY_NONE)
                                mSerial!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_XON_XOFF)
                                serialInputStream  = mSerial!!.inputStream as InputStream

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

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @OptIn(ExperimentalCoroutinesApi::class)
    fun startReading(){
        var t = mConnection!!.rawDescriptors
        usbThread = Thread{
           // var myByte =
           // mConnection!!.bulkTransfer(mEndPoint,b,11023,0)
           // GlobalScope.launch(Dispatchers.IO) {
            while (true){
                var t = mConnection!!.rawDescriptors
               // val b = ByteArray(y)
                var read =  mConnection!!.bulkTransfer(mEndPoint, t, t.size, 0);
                if(read < 0){
                    return@Thread
                } else{
                    //u.read(b)
                    val s = String(t, Charset.forName("UTF-8"))
                    Log.d("Data","$s")
                    val l =
                        s.split("\n")
                }
            }
            /*    while(true){
                    if(serialInputStream == null){
                        return@Thread
                    }

                  val u =  serialInputStream as InputStream
                    val value2 = u.available()
                   val value = serialInputStream!!.read()

                    if(value2 != -1 && value2 > 0){
                        val b = ByteArray(y)
                    var read =  mConnection!!.bulkTransfer(mEndPoint, b, b.size, 0);
                       if(read < 0){
                          return@Thread
                       } else{
                           u.read(b)
                           val s = String(b, Charset.forName("UTF-8"))
                           Log.d("Data","$s")
                           val l =
                               s.split("\n")
                       }


//                    val t  =  LinkedList<Byte>(b.toMutableList())
//                        t.filter {
//                            it > 0
//                        }
//
//                   val y = b.filter {
//                        it > 0
//                    }



                          // Into lines... Crude. What if we read only up to part of sentence??
//                    for (xs in l) {
//                        // Hand off to higher level...
//                        val longlat = LongLat(xs)
//                        if (longlat.fixType != LongLat.FixType.NoFixData) {
//
//                            if(longlat.fixType == LongLat.FixType.RTKFloat || longlat.fixType == LongLat.FixType.RTKFix){
//                                // Send it to the Location Source... BUT ONLY when we have rtk data--(more accurate than other fixtypes)
//                                handler.post {
//                                    NmeaReader.listener.postNewLocation(longlat,longlat.fixType)
//                                }
//                            }
//                        }
//                    }
                    }

                }*/
           // }
        }
        usbThread!!.start()


    }
}


}