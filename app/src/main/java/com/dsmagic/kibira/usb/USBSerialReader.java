package com.dsmagic.kibira.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class USBSerialReader {

    public UsbManager getManager() {
        return manager;
    }

    public void setManager(UsbManager manager) {
        this.manager = manager;
    }

    public String getACTION_USB_PERMISSION() {
        return ACTION_USB_PERMISSION;
    }

    private UsbManager manager;
    private UsbSerialDriver driver;
    private UsbDeviceConnection connection;
    private UsbSerialPort port;
    public static final String ACTION_USB_PERMISSION = "permission";
    private static final int READ_WAIT_MILLIS = 2000;
    byte[] buffer = new byte[8192];
    private boolean isReading = true;


    private void setUSBConnection(Context context){
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x1546, 0x1A9, CdcAcmSerialDriver.class);
        UsbSerialProber prober = new UsbSerialProber(customTable);
        List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }
        // Open a connection to the first available driver.
        driver = availableDrivers.get(0);

        if (connection == null){
            PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            manager.requestPermission(driver.getDevice(), intent);
        }

        Log.d("USB", "Device successfully connected");
        Toast.makeText(context, "Device successfully connected", Toast.LENGTH_SHORT).show();

    }

    private void disconnect() throws IOException {
        if (port != null){
            port.close();
            isReading = false;
        }
    }


    public BroadcastReceiver getUsbReceiver() {
        return usbReceiver;
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                        if (granted) {
                            port = driver.getPorts().get(0);
                            connection = manager.openDevice(driver.getDevice());
                            try {
                                port.open(connection);
                                port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                                while(isReading){

                                   int len =  port.read(buffer, READ_WAIT_MILLIS);
                                   byte[] cleanBytes = new byte[8192];
                                   int index = 0;
                                    for(byte b : buffer){
                                        byte i = (byte) (b & 0xFF);
                                        cleanBytes[index] = i;
                                        index +=1;
                                    }
                                   if (len > 0){
                                       String s = new String(cleanBytes,0,len, StandardCharsets.UTF_8);
                                       Log.d("USB", s);
                                   }

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(context, "USB Permissions not granted", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Log.d("USB", "Device attached");
                    setUSBConnection(context);
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Log.d("USB", "Device detached");
                    try {
                        disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };


}
