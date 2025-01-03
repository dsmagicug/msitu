import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { RTNMsitu, LatLng } from "rtn-msitu"
import { generateError } from '../utils';
import {
    Project
}
    from "../models"
import { setShowCreateNewProjects, setShowProjectList } from './modal';
import { PlantingLine } from '../../RTNMsitu';
import ProjectService from '../services/ProjectService';


export type ModeParams={
    mode:string | 'cyrus'|'normal'
    maxLines:number
}

export type SearchClosestPointParams={
    roverPosition:LatLng;
    points:Array<LatLng>
}

interface PeggingState {
    mode: string | 'cyrus'|'normal';
    maxCyrusLines:number;
    searching:boolean;
    markedPoints:Array<LatLng>|[],
    cyrusLines: Array<PlantingLine> | [],
    closestCoord:LatLng | null
    error: string | null;
}

const initialState: PeggingState = {
    searching: false,
    mode:"normal",
    maxCyrusLines:1,
    markedPoints:[],
    cyrusLines: [],
    error: null,
    closestCoord: null
}


export const searchClosestPoint = createAsyncThunk(
    'pegging/searchClosestPoint',
    async (params:SearchClosestPointParams, thunkAPI) => {
        try {
            const {roverPosition, points } = params

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
        setMode(state, action: PayloadAction<ModeParams>) {
            const {mode, maxLines} = action.payload;
            state.mode = mode;
            state.maxCyrusLines=mode === 'normal' ? 1 : maxLines;
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
                const project: Project = action.payload;
               
                state.scaledPlantingLines = project.plantingLines
                state.activeProject = project;
            })
            .addCase(searchClosestPoint.rejected, (state, action) => {
                state.searching = false;
                state.error = action.error.message ?? 'Unknown error';
            })
    },
});

export const { setMode } = peggingSlice.actions;
export default peggingSlice.reducer;