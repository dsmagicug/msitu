import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { Point, RTNMsitu, type LatLng } from "rtn-msitu"
import { generateError } from '../utils';
import {
    Project
}
    from "../models"
import { setShowCreateNewProjects } from './modal';
import { PlantingLine } from '../../RTNMsitu';
import ProjectService from '../services/ProjectService';


interface ProjectState {
    loading: boolean;
    fetching:boolean;
    generating: boolean;
    scaledPlantingLines: Array<PlantingLine> | [],
    error: string | null;
    activeProject: Project | null;
    projectList: Array<Project>
    visibleLines:Array<LatLng> | []
}

const initialState: ProjectState = {
    loading: false,
    fetching:false,
    generating: false,
    scaledPlantingLines: [],
    error: null,
    activeProject: null,
    projectList: [],
    visibleLines:[]
}

export const fetchProjects = createAsyncThunk(
    'project/fetchProjects',
    async (_, thunkAPI) => {
        try {
            const projects = await ProjectService.fetch("projects", ["id", "name", "basePoints"]);
            return projects;
        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);

export const generateProject = createAsyncThunk(
    'project/generateProject',
    async (params, thunkAPI) => {
        try {
            // @ts-ignore
            const { firstPoint, name, secondPoint, lineDirection, meshType, gapSize, lineLength } = params;
            const results = await RTNMsitu.generateMesh(firstPoint, secondPoint, lineDirection, meshType, parseFloat(gapSize), parseFloat(lineLength)) as Array<PlantingLine>;
            // lets process our lines
            const basePoints = [firstPoint as LatLng, secondPoint as LatLng]
            let project = {
                name,
                basePoints,
                // @ts-ignore
                center:results[0][0], // the very first one is the center
                plantingLines: results as Array<PlantingLine>,
                markedPoints: [],
                lastLineIndex: -1

            } as Project
            //save project to DB
            const insertRows = await ProjectService.save("projects", [{
                ...project,
                center:JSON.stringify(project.center),
                basePoints: JSON.stringify(project.basePoints),
                plantingLines: JSON.stringify(project.plantingLines),
                markedPoints: JSON.stringify([])
            }])
            const { insertId } = insertRows[0];
            project["id"] = insertId
            thunkAPI.dispatch(setShowCreateNewProjects(false));
            project["plantingLines"] = project.plantingLines.slice(0, 10)
            return project;
        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);


export const convertLinesToLatLong = createAsyncThunk(
    'project/convertLinesToLatLong',
    async (params, thunkAPI) => {
        try {
            // @ts-ignore
            const {linePoints , center } = params
            const coordLines = await RTNMsitu.linesToCoords(linePoints, center);
            return coordLines;

        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);


export const projectSlice = createSlice({
    name: 'project',
    initialState,
    reducers: {
        setLoading: (state, action) => {
            state.loading = action.payload;
        },
    },
    extraReducers: builder => {
        builder
            .addCase(generateProject.pending, state => {
                state.generating = true;
            })
            .addCase(generateProject.fulfilled, (state, action) => {
                state.generating = false;
                 // @ts-ignore
                const project: Project = action.payload;
               
                state.scaledPlantingLines = project.plantingLines
                state.activeProject = project;
            })
            .addCase(generateProject.rejected, (state, action) => {
                state.generating = false;
                state.error = action.error.message ?? 'Unknown error';
            })
            .addCase(convertLinesToLatLong.pending, state => {
                state.loading = true;
            })
            .addCase(convertLinesToLatLong.fulfilled, (state, action) => {
                state.loading = false;
                 // @ts-ignore
                state.visibleLines = action.payload ? action.payload : [];
            })
            .addCase(convertLinesToLatLong.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message ?? 'Unknown error';
            }) .addCase(fetchProjects.pending, state => {
                state.fetching = true;
            })
            .addCase(fetchProjects.fulfilled, (state, action) => {
                state.fetching = false;
                const crudProjects =  action.payload;
                if (crudProjects !== undefined){
                    state.projectList = crudProjects;
                }
            })
            .addCase(fetchProjects.rejected, (state, action) => {
                state.fetching = false;
                state.error = action.error.message ?? 'Unknown error';
            })
            
    },
});

export const { setLoading } = projectSlice.actions;
export default projectSlice.reducer;