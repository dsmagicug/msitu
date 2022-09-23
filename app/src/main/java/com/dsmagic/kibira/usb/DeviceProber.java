package com.dsmagic.kibira.usb;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;

public class DeviceProber {

    // TODO we shall modify this to support other obvious devices for now lets support our rover
    private int roverVendorId = 0x1546;
    private  int roverProductId = 0x1A9;
    ProbeTable customTable = new ProbeTable();


    public void getDeviceList(){

    }
    public void addDevices(){}


}
