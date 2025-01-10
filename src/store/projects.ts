import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Point, RTNMsitu,  LatLng } from "rtn-msitu"
import { generateError } from '../utils';
import {
    Project
}
    from "../models"
import { setShowCreateNewProjects, setShowProjectList } from './modal';
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


export const loadProject = createAsyncThunk(
    'project/loadProject',
    async (id:number, thunkAPI) => {
        try {
            const project = await ProjectService.getById("projects", id);
            thunkAPI.dispatch(setShowProjectList(false))// close the project list dialog
            return project;
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
        setLoading: (state, action:PayloadAction<boolean>) => {
            state.loading = action.payload;
        },
        saveProjectMarkedPoints: (state, action: PayloadAction<Array<LatLng>>) => {
            if (state.activeProject) {
                const newPoints = action.payload;
                let allPoints = [...state.activeProject.markedPoints, ...newPoints];
                const uniquePointsSet = new Set(allPoints.map(point => 
                    `${point.latitude},${point.longitude}`
                ));
                state.activeProject.markedPoints = Array.from(uniquePointsSet, str => {
                    const [latitude, longitude] = str.split(',').map(Number);
                    return { latitude, longitude };
                });
            }
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
            .addCase(loadProject.pending, state => {
                state.loading = true;
              
            })
            .addCase(loadProject.fulfilled, (state, action) => {
                state.loading = false;
                let project = action.payload;
                const plantingLines = JSON.parse(project.plantingLines)
                project ={
                    ...project,
                    center:JSON.parse(project.center),
                    basePoints: JSON.parse(project.basePoints),
                    markedPoints: [
                    {
                        "longitude": 32.46331336833333,
                        "latitude": 0.046942785
                    },
                    {
                        "longitude": 32.463272307083116,
                        "latitude": 0.046891103641127536
                    },
                    {
                        "longitude": 32.46328087413975,
                        "latitude": 0.04694776452549391
                    },
                    {
                        "longitude": 32.46326034351479,
                        "latitude": 0.04692192384608531
                    },{
                        "longitude": 32.463135537284884,
                        "latitude": 0.04708598391822104
                    }
                ],
                    plantingLines:plantingLines
                }as Project
                
                state.scaledPlantingLines = plantingLines.slice(0, 10)
                state.activeProject = project;
            })
            .addCase(loadProject.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message ?? 'Unknown error';
            })
            
    },
});

export const { setLoading, saveProjectMarkedPoints } = projectSlice.actions;
export default projectSlice.reducer;