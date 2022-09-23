package com.dsmagic.kibira.usb;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialProber;


import java.util.ArrayList;
import java.util.HashMap;


public class DeviceProber {

    private final UsbManager manager;


    private UsbSerialProber customProber;
    public ArrayList<UsbDevice> getDevices() {
        return devices;
    }

    private final ArrayList<UsbDevice> devices =  new ArrayList<>();


    private ProbeTable customTable ;

    private void setProber(){
        customProber= new UsbSerialProber(customTable);
    }

    public UsbSerialProber getCustomProber() {
        return customProber;
    }

    // TODO some devices are automatically supported but for our case lets support these rovers manually since no automatic drivers loaded for them
    public DeviceProber(UsbManager manager) {
        this.manager = manager;
        getDeviceList();
        addAllDevices(this.getDevices());
        setProber();
    }

    public void getDeviceList(){
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        if(!deviceList.isEmpty()){
            devices.addAll(deviceList.values());
        }
    }
    public void addDeviceToProberTable(UsbDevice device){
        customTable = new ProbeTable();
        int vendorId= device.getVendorId();
        int productId = device.getProductId();
        customTable.addProduct(vendorId, productId, CdcAcmSerialDriver.class);
    }

    private void addAllDevices(ArrayList<UsbDevice> availableDevices){
        if(!availableDevices.isEmpty()){
            for(UsbDevice device: availableDevices){
                addDeviceToProberTable(device);
            }
        }
    }





}
