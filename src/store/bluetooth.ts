import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { generateError } from '../utils';
import RNBluetoothClassic, {
  BluetoothEventType,
  BluetoothDevice,
}
  from 'react-native-bluetooth-classic';
import {
  Platform,
  PermissionsAndroid,
} from 'react-native';

interface BTState {
  scanning: boolean;
  selectedDevice:BluetoothDevice|null;
  isBluetoothEnabled: boolean;
  isBluetoothAvailable: boolean;
  connecting:boolean;
  init: boolean;
  deviceList: Array<any> | [];
  connectedDevice: any | null;
  connectedDevices: Array<any> | [];
  btError: any | null;
}

const initialState: BTState = {
  scanning: false,
  isBluetoothEnabled: false,
  connecting:false,
  selectedDevice:null,
  isBluetoothAvailable: false,
  init: false,
  btError: null,
  deviceList: [],
  connectedDevices: [],
  connectedDevice: null,
};

// Request Bluetooth Permissions
export const requestBluetoothPermissions = async () => {
  try {
    if (Platform.OS === 'android' && Platform.Version >= 31) {
      const grantedScan = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN
      );

      const grantedConnect = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT
      );

      return (
        grantedScan === PermissionsAndroid.RESULTS.GRANTED &&
        grantedConnect === PermissionsAndroid.RESULTS.GRANTED
      );
    } else {
      const grantedLocation = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
      );
      return grantedLocation === PermissionsAndroid.RESULTS.GRANTED;
    }
  } catch (err) {
    console.warn(err);
    return false;
  }
};

// Initialize Bluetooth
export const initializeBT = createAsyncThunk(
  'bluetooth/initialize',
  async (_, thunkAPI) => {
    try {
      const isEnabled = await RNBluetoothClassic.isBluetoothEnabled();
      return isEnabled;
    } catch (error) {
      return thunkAPI.rejectWithValue(generateError(error));
    }
  }
);

export const discorverDevices = createAsyncThunk(
  'bluetooth/discorverDevices',
  async (_, thunkAPI) => {
    try {
      const devices = await RNBluetoothClassic.startDiscovery();
      return devices;
    } catch (error) {
      return thunkAPI.rejectWithValue(generateError(error));
    }
  }
);

export const cancelDiscovery = createAsyncThunk(
  'bluetooth/cancelDiscovery',
  async (_, thunkAPI) => {
    try {
      const cancelled = await RNBluetoothClassic.cancelDiscovery();
      return cancelled;
    } catch (error) {
      return thunkAPI.rejectWithValue(generateError(error));
    }
  }
);


// Get connected devices
export const getConnectedDevices = createAsyncThunk(
  'bluetooth/getConnectedDevices',
  async (_, thunkAPI) => {
    try {
      const devices = await RNBluetoothClassic.getConnectedDevices();
      return devices;
    } catch (error) {
      return thunkAPI.rejectWithValue(generateError(error));
    }
  }
);


export const connectToDevice = createAsyncThunk(
  'bluetooth/connectToDevice',
  async (device:BluetoothDevice, thunkAPI) => {
    try {
      let connection = await device.isConnected();
      if (connection){
        return false
      }
      const devices = await device.connect({
        connectorType: "rfcomm",
        delimiter: "\n",
        DEVICE_CHARSET: "utf-8"
      });
      return devices;
    } catch (error) {
      return thunkAPI.rejectWithValue(generateError(error));
    }
  }
);






const bluetoothSlice = createSlice({
  name: 'bluetooth',
  initialState,
  reducers: {
    setScanning(state, action: PayloadAction<boolean>) {
      state.scanning = action.payload;
    },
    setIsBTEnabled(state, action: PayloadAction<boolean>) {
      state.isBluetoothEnabled = action.payload;
    },
    setIsBTSupported(state, action: PayloadAction<boolean>) {
      state.isBluetoothAvailable = action.payload;
    },
    setSelectedDevice(state, action: PayloadAction<BluetoothDevice | null>) {
      state.selectedDevice = action.payload;
    },
  },
  extraReducers: builder => {
    builder
      .addCase(initializeBT.fulfilled, (state, action) => {
        state.isBluetoothEnabled = action.payload;
      })
      .addCase(initializeBT.rejected, (state, action) => {
        state.btError = action.error.message || 'Failed to initialize Bluetooth';
      })
      .addCase(discorverDevices.pending, (state) => {
        state.scanning = true;
      })
      .addCase(discorverDevices.fulfilled, (state, action) => {
        state.scanning = false;
        state.deviceList = action.payload;
      })

      .addCase(discorverDevices.rejected, (state, action) => {
        state.scanning = false;
        state.btError = action.error.message || 'Failed to scan devices';
      })
      .addCase(getConnectedDevices.fulfilled, (state, action) => {
        state.connectedDevices = action.payload;
      })

      .addCase(cancelDiscovery.fulfilled, (state, action) => {
        state.scanning = false
      })
      .addCase(connectToDevice.pending, (state) => {
        state.connecting = true;
      })
      .addCase(connectToDevice.fulfilled, (state, action) => {
        state.connecting = false;
      })

      .addCase(connectToDevice.rejected, (state, action) => {
        state.connecting = false;
        state.btError = action.error.message || 'Failed to connect devices';
      })
  },
});

export const { setScanning, setIsBTEnabled, setIsBTSupported, setSelectedDevice } = bluetoothSlice.actions;
export default bluetoothSlice.reducer;
