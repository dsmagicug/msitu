import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { RTNMsitu, LatLng, LongLat } from "rtn-msitu"
import { generateError } from '../utils';
import { Vibration } from "react-native"

export type ModeParams = {
    maxLines: number
}

export type SearchClosestPointParams = {
    roverPosition: LatLng;
    points: Array<LatLng>
}

interface PeggingState {
    searching: boolean;
    skipPoints:number;
    markedPoints: Array<LatLng> | [],
    maxCyrusLines: number,
    cyrusLines: Array<Array<LongLat>> | [],
    closestCoord: LatLng | null
    error: string | null;
}

const initialState: PeggingState = {
    searching: false,
    skipPoints:5,
    markedPoints: [],
    maxCyrusLines: 1,
    cyrusLines: [],
    error: null,
    closestCoord: null
}


export const searchClosestPoint = createAsyncThunk(
    'pegging/searchClosestPoint',
    async (params: SearchClosestPointParams, thunkAPI) => {
        try {
            const { roverPosition, points } = params
            // @ts-ignore
            const result = await RTNMsitu.closetPointRelativeToRoverPosition(roverPosition, points);
            return result as LatLng;

        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);


export const peggingSlice = createSlice({
    name: 'project',
    initialState,
    reducers: {
        setCyrusLines(state, action: PayloadAction<Array<Array<LongLat>>>) {
            state.cyrusLines = action.payload
        },

        setSkipPoints(state, action: PayloadAction<number>) {
            state.skipPoints = action.payload
        },
        markPoint: (state, action: PayloadAction<LatLng>) => {
            const ONE_SECOND_IN_MS = 1000;
            const newPoint = action.payload;
            const pointExists = state.markedPoints.some(point =>
                point.latitude === newPoint.latitude &&
                point.longitude === newPoint.longitude
            );
            if (!pointExists) {
                // @ts-ignore
                state.markedPoints.push(newPoint);
            }
            Vibration.vibrate(1 * ONE_SECOND_IN_MS); // vibrate regardless, peg-kids are slow
        },
        resetMarkedPoints(state, _) {
            state.markedPoints = []
        },
    },
    extraReducers: builder => {
        builder
            .addCase(searchClosestPoint.pending, state => {
                state.searching = true;
            })
            .addCase(searchClosestPoint.fulfilled, (state, action) => {
                state.searching = false;
                // @ts-ignore
                state.closestCoord = action.payload

            })
            .addCase(searchClosestPoint.rejected, (state, action) => {
                state.searching = false;
                state.error = action.error.message ?? 'Unknown error';
            })
    },
});

export const { setCyrusLines, markPoint, resetMarkedPoints, setSkipPoints } = peggingSlice.actions;
export default peggingSlice.reducer;