package com.dsmagic.kibira.usb;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.dsmagic.kibira.LongLat;
import com.dsmagic.kibira.MainActivity;
import com.dsmagic.kibira.NmeaReader;
import com.dsmagic.kibira.R;
import com.dsmagic.kibira.RtkLocationSource;
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
    private final int READ_WAIT_MILLIS = 2000;
    private final int BAUD_RATE = 115200;
    private final int DATA_BITS = 8;
    byte[] buffer = new byte[8192];
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            stopIsLoadingIcon(false);
        }
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
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                                    stopIsLoadingIcon(true);
                                }
                                if (longlat.getFixType() == LongLat.FixType.RTKFloat || longlat.getFixType() == LongLat.FixType.RTKFix) {
                                    // Send it to the Location Source... BUT ONLY when we have rtk data--(more accurate than other fixtypes)
                                    handler.post(() -> listener.postNewLocation(longlat, longlat.getFixType()));
                                }
                            }
                            Log.d("FROM USB", str + "\n");
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
