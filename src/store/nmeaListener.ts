import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { generateError } from '../utils';

import { LongLat, RTNMsitu, FixType } from 'rtn-msitu';
import LatLong from '../services/MNEAService';

interface NMEAState {
    converting: boolean;
    roverLocation: LongLat | null;
    error: any | null;
}

const initialState: NMEAState = {
    converting: false,
    roverLocation: null,
    error: null
};


// Initialize Bluetooth
export const parseNMEA = createAsyncThunk(
    'nmeaListener/parseNMEA',
    (sentence: string, thunkAPI) => {
      // Return a promise directly
      return new Promise((resolve, reject) => {
        LatLong.asyncParse(sentence)
          .then((longlat) => {
            resolve(longlat);
          })
          .catch((error) => {
            reject(thunkAPI.rejectWithValue(generateError(error)));
          });
      });
    }
  );



const nmeaListenerSlice = createSlice({
    name: 'nmeaListener',
    initialState,
    reducers: {
        setConverting(state, action: PayloadAction<boolean>) {
            state.converting = action.payload;
        }
    },
    extraReducers: builder => {
        builder
            .addCase(parseNMEA.fulfilled, (state, action) => {
                const longLat = action.payload as LatLong;
                if(longLat.fixType !== FixType.NoFixData){
                    state.roverLocation = longLat;
                }
               
            })
            .addCase(parseNMEA.rejected, (state, action) => {
                state.error = action.error.message || 'Failed to parse NMEA sentence';
            })
            
    },
});

export const { setConverting } = nmeaListenerSlice.actions;
export default nmeaListenerSlice.reducer;
