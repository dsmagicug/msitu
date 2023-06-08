/*
* Written by Keeya emmanuel
* Digital Solutions
* 23rd Sept 2022
* */
package com.dsmagic.kibira.usb;

/*
 *  This file is part of Msitu.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Msitu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Msitu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Msitu. If not, see <http://www.gnu.org/licenses/>
 */

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

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
