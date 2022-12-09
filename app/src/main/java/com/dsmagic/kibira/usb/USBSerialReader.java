package com.dsmagic.kibira.usb;

/*
 *  This file is part of Kibira.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Kibira is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Kibira is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Kibira. If not, see <http://www.gnu.org/licenses/>
 */

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.dsmagic.kibira.dataReadings.LongLat;
import com.dsmagic.kibira.dataReadings.NmeaReader;
import com.dsmagic.kibira.dataReadings.RtkLocationSource;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class USBSerialReader {


    public UsbManager getManager() {
        return manager;
    }

    public void setManager(UsbManager manager) {
        this.manager = manager;
    }


    private UsbManager manager;
    private UsbSerialDriver driver;
    private UsbDeviceConnection connection;
    private UsbSerialPort port;
    public static final String ACTION_USB_PERMISSION = "permission";
    private final int READ_WAIT_MILLIS = 500;
    private final int BAUD_RATE = 115200;
    private final int DATA_BITS = 8;
    byte[] buffer = new byte[1024];
    private boolean isReading = true;
    private Thread thread;

    private boolean gotReadings = false;
    private DeviceProber proberProvider;
    private RtkLocationSource listener = NmeaReader.Companion.getListener();
    ProgressBar progressBar;

    private void setUSBConnection(Context context) {

        proberProvider = new DeviceProber(manager);
        UsbSerialProber prober = proberProvider.getCustomProber();

        List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }
        // Open a connection to the first available driver.
        driver = availableDrivers.get(0);

        if (connection == null) {
            PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            manager.requestPermission(driver.getDevice(), intent);
        }
    }

    public void disconnect() throws IOException {
        if (port != null) {
            port.close();
            thread.interrupt();
            connection = null;
            isReading = false;
        }
    }

    public BroadcastReceiver getUsbReceiver() {
        return usbReceiver;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void dataReaderListener() throws IOException {
        Looper looper = Looper.getMainLooper();
        Handler handler = new Handler(looper);
        thread = new Thread(() -> {
            while (isReading) {
                int len = 0;
                try {
                    len = port.read(buffer, READ_WAIT_MILLIS);
                    if (len > 0 && buffer[0] > 0) {
                        String s = new String(buffer, 0, len, StandardCharsets.UTF_8);
                        String[] l = s.split("\n");
                        for (String str : l) {
                            LongLat longlat = new LongLat(str);
                            if (longlat.getFixType() != LongLat.FixType.NoFixData) {
                                gotReadings = true;
                               // if (longlat.getFixType() == LongLat.FixType.RTKFloat || longlat.getFixType() == LongLat.FixType.RTKFix) {
                                    // Send it to the Location Source... BUT ONLY when we have rtk data--(more accurate than other fixtypes)
                                    //TODO display we have RTK fix
                                    handler.post(() -> listener.postNewLocation(longlat, longlat.getFixType()));
                               // }else{
                                    // TODO display fix type
                                //}
                            }
                            //Log.d("FROM USB", str + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void stopIsLoadingIcon(boolean string) {

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
                            Toast.makeText(context, "Device successfully connected", Toast.LENGTH_SHORT).show();
                            try {
                                port.open(connection);
                                port.setParameters(BAUD_RATE, DATA_BITS, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                                dataReaderListener();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(context, "USB Permissions not granted", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Toast.makeText(context, "USB Device Attached", Toast.LENGTH_SHORT).show();
                    setUSBConnection(context);
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Log.d("USB", "Device detached");
                    Toast.makeText(context, "USB Device unplugged", Toast.LENGTH_SHORT).show();
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
