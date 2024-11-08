import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { generateError } from '../utils';

import BleManager, {
    BleDisconnectPeripheralEvent,
    BleManagerDidUpdateValueForCharacteristicEvent,
    BleScanCallbackType,
    BleScanMatchMode,
    BleScanMode,
    Peripheral,
    PeripheralInfo,
  } from 'react-native-ble-manager';


const SECONDS_TO_SCAN_FOR = 15;
const SERVICE_UUIDS: string[] = [];
const ALLOW_DUPLICATES = true;

interface BTState {
    scanning: boolean;
    init:boolean;
    deviceList:Array<any> | []
    btError:any| null
}

const initialState: BTState = {
    scanning: false,
    init:false,
    btError:null,
    deviceList: [],

}


export const initializeBT = createAsyncThunk(
    'bluethooth/initialize',
    async (_, thunkAPI) => {
        try {
           await  BleManager.start({ forceLegacy: false })
           console.log("Module Intialized")
           return true

        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);


export const scanDevices = createAsyncThunk(
    'bluethooth/scanDevices',
    async (_, thunkAPI) => {
        try {
            const result =    await BleManager.scan([], 5, true)
            console.log(result)
            return true
        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);



export const bluetoothSlice = createSlice({
    name: 'bluetooth',
    initialState,
    reducers: {
        setScanning: (state, action) => {
            state.scanning = action.payload;
        },
    },
    extraReducers: builder => {
        builder
        
            .addCase(initializeBT.fulfilled, (state, action) => {
                state.init = true;
            })
            .addCase(initializeBT.rejected, (state, action) => {
                state.init = false;
                state.btError = action.error.message ?? 'BT module init error';
            })
            .addCase(scanDevices.pending, state => {
                state.scanning = true;
            })
            .addCase(scanDevices.fulfilled, (state, action) => {
                state.scanning = false;
            })
            .addCase(scanDevices.rejected, (state, action) => {
                state.scanning = false;
                state.btError = action.error.message ?? 'Unknown error';
            })
            
    },
});

// export const { setScanning } = bluetoothSlice.actions;
export default bluetoothSlice.reducer;