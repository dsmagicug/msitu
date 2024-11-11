import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { generateError } from '../utils';

import { LongLat, RTNMsitu, FixType } from 'rtn-msitu';

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
    'bluetooth/initialize',
    async (line: string, thunkAPI) => {
        try {
            const longlat = await RTNMsitu.nmeaToLongLat(line);
            return longlat;
        } catch (error) {
            return thunkAPI.rejectWithValue(generateError(error));
        }
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
                console.log(action.payload)
                const longLat = action.payload as LongLat;
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
