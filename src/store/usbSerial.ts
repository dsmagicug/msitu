import { UsbSerialManager, Device, Parity, UsbSerial } from "react-native-usb-serialport-for-android";
import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { generateError } from '../utils';
import {
    Platform,
    PermissionsAndroid,
} from 'react-native';

export const DEFAULT_BAUD_RATE = 115200;
export const DATA_BITS = 8;
export const STOP_BITS = 1;
export const PARITY = Parity.None;

interface USBState {
    loading: boolean
    disconnecting:boolean
    connecting: boolean;
    usbSerialport: UsbSerial | null;
    init: boolean;
    deviceList: Array<Device> | [];
    usbError: any | null;
}

const initialState: USBState = {
    loading: false,
    disconnecting:false,
    connecting: false,
    usbSerialport: null,
    deviceList: [],
    init: false,
    usbError: null
};

// Request Bluetooth Permissions
export const requestUSBSerialPermissions = async () => {
    try {
        if (Platform.OS === 'android' && Platform.Version >= 19) { // support upto KITKAT, if not buy a new phone
            const granted = await PermissionsAndroid.request(
                PermissionsAndroid.PERMISSIONS.USBSERIAL
            );
            return granted === PermissionsAndroid.RESULTS.GRANTED;
        }
    } catch (err) {
        console.warn(err);
        return false;
    }
};


export const getDevices = createAsyncThunk(
    'usbSerial/getDevices',
    async (_, thunkAPI) => {
        try {
            const devices = await UsbSerialManager.list();
            return devices;
        } catch (error) {
            return thunkAPI.rejectWithValue(generateError(error));
        }
    }
);

export const openDevicePort = createAsyncThunk(
    'usbSerial/openDevicePort',
    async (device: Device, thunkAPI) => {
        try {
            const granted = await UsbSerialManager.tryRequestPermission(device.deviceId);
            if (!granted) {
                return thunkAPI.rejectWithValue(generateError("USB Permission Denied"));
            }
            const usbSerialport = await UsbSerialManager.open(device.deviceId,
                { baudRate: DEFAULT_BAUD_RATE, parity: PARITY, dataBits: DATA_BITS, stopBits: STOP_BITS });
            return usbSerialport;
        } catch (error) {
            return thunkAPI.rejectWithValue(generateError(error));
        }
    }
);


export const closeDevicePort = createAsyncThunk(
    'usbSerial/closeDevicePort',
    async (usbSerialport: UsbSerial, thunkAPI) => {
        try {
            await usbSerialport.close();
            return true;
        } catch (error) {
            return thunkAPI.rejectWithValue(generateError(error));
        }
    }
);





const usbSerialSlice = createSlice({
    name: 'usbSerial',
    initialState,
    reducers: {
        setConnecting(state, action: PayloadAction<boolean>) {
            state.connecting = action.payload;
        }
    },
    extraReducers: builder => {
        builder
            .addCase(getDevices.pending, (state) => {
                state.loading = true;
            })
            .addCase(getDevices.fulfilled, (state, action) => {
                state.loading = false;
                state.deviceList = action.payload;
            })
            .addCase(getDevices.rejected, (state, action) => {
                state.loading = false;
                state.usbError = action.error.message || 'Error happened during usb device probing';
            })
            .addCase(openDevicePort.pending, (state) => {
                state.connecting = true;
            })
            .addCase(openDevicePort.fulfilled, (state, action) => {
                state.connecting = false;
                state.usbSerialport = action.payload;
            })
            .addCase(openDevicePort.rejected, (state, action) => {
                state.connecting = false;
                state.usbError = action.error.message || 'Error closing USB port';
            })
            .addCase(closeDevicePort.pending, (state) => {
                state.disconnecting = true;
            })
            .addCase(closeDevicePort.fulfilled, (state) => {
                state.disconnecting = false;
                state.usbSerialport = null;
            })
            .addCase(closeDevicePort.rejected, (state, action) => {
                state.disconnecting = false;
                state.usbError = action.error.message || 'Error closing USB port';
            })
    },
});

export const { setConnecting } = usbSerialSlice.actions;
export default usbSerialSlice.reducer;
