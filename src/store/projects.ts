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
import { Vibration } from "react-native"
import { Settings } from './settings';

export type IndexPayLoad = {
    forwardIndex: number
    backwardIndex: number
}

interface ProjectState {
    loading: boolean;
    fetching: boolean;
    lock: boolean;
    generating: boolean;
    scaledPlantingLines: Array<PlantingLine> | [],
    error: string | null;
    activeProject: Project | null;
    projectList: Array<Project>
    visibleLines: Array<LatLng> | []
}

const initialState: ProjectState = {
    loading: false,
    fetching: false,
    lock: false,
    generating: false,
    scaledPlantingLines: [],
    error: null,
    activeProject: null,
    projectList: [],
    visibleLines: []
}

export const fetchProjects = createAsyncThunk(
    'project/fetchProjects',
    async (_, thunkAPI) => {
        try {
            return await ProjectService.fetch("projects", ["id", "name", "basePoints","gapSize","lineLength", "gapSizeUnit", "lineLengthUnit"]);
        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);


export const loadProject = createAsyncThunk(
    'project/loadProject',
    async (id: number, thunkAPI) => {
        try {
            const project = await ProjectService.getById("projects", id);
            thunkAPI.dispatch(setShowProjectList(false))
            let jsProject ={
                    ...project,
                    plantingLines:JSON.parse(project.plantingLines),
                    center: JSON.parse(project.center),
                    basePoints: JSON.parse(project.basePoints),
                    markedPoints: JSON.parse(project.markedPoints)
            }  as Project

            //@ts-ignore
            const {settings} = thunkAPI.getState().settings as Settings;
            return {project:jsProject, settings};
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
            const results = await RTNMsitu.generateMesh(firstPoint, secondPoint, lineDirection, meshType, parseFloat(gapSize), parseFloat(lineLength)) as string;
            // lets process our lines
            const lines = JSON.parse(results)
            const basePoints = [firstPoint as LatLng, secondPoint as LatLng]
            let project = {
                name,
                basePoints,
                // @ts-ignore
                center: lines[0][0], // the very first one is the center
                plantingLines: lines as Array<PlantingLine>,
                markedPoints: [],
                forwardIndex: 9,
                backwardIndex:0,
                gapSize:gapSize,
                lineLength:lineLength,
                lineCount:results.length
            } as Project
            //save project to DB
            const insertRows = await ProjectService.save("projects", [{
                ...project,
                center: JSON.stringify(project.center),
                basePoints: JSON.stringify(project.basePoints),
                plantingLines: JSON.stringify(project.plantingLines),
                markedPoints: JSON.stringify([])
            }])
            const { insertId } = insertRows[0];
            project["id"] = insertId
            thunkAPI.dispatch(setShowCreateNewProjects(false));
            //@ts-ignore
            const {settings} = thunkAPI.getState().settings as Settings;
            return {project, settings};
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
            const { linePoints, center } = params
            const coordLines = await RTNMsitu.linesToCoords(linePoints, center);
            return coordLines;

        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);


export const deleteProject = createAsyncThunk(
    'project/deleteProject',
    async (id:number, thunkAPI) => {
        try {
            await ProjectService.deleteItem(id, "projects");
            return id;
        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);

export const projectSlice = createSlice({
    name: 'project',
    initialState,
    reducers: {
        setLoading: (state, action: PayloadAction<boolean>) => {
            state.loading = action.payload;
        },
        setLock: (state, action: PayloadAction<boolean>) => {
            state.lock = action.payload;
        },
        setIndices: (state, action: PayloadAction<IndexPayLoad>) => {
            if(state.activeProject){
                state.activeProject.forwardIndex = action.payload.forwardIndex;
                state.activeProject.backwardIndex = action.payload.backwardIndex;
            }
        },
        setScaledPlanitingLines: (state, action: PayloadAction<Array<PlantingLine>>) => {
            state.scaledPlantingLines = action.payload
        },
        saveProjectMarkedPoints: (state, action: PayloadAction<Array<LatLng>>) => {
            const ONE_SECOND_IN_MS = 1000;
            if (state.activeProject) {
                // best way wound be to just append but I have decided to use Set, a bit much faster
                const newPoints = action.payload;
                let allPoints = [...state.activeProject.markedPoints, ...newPoints];
                const uniquePointsSet = new Set(allPoints.map(point =>
                    `${point.latitude},${point.longitude}`
                ));
                state.activeProject.markedPoints = Array.from(uniquePointsSet, str => {
                    const [latitude, longitude] = str.split(',').map(Number);
                    return { latitude, longitude };
                });
                // save these to db parmanently
                ProjectService.update("projects", state.activeProject.id, { markedPoints: JSON.stringify(state.activeProject.markedPoints) })
                    .then((result) => {
                        console.log(result)
                    }).catch((error: any) => {
                        console.error(error)
                    })
                Vibration.vibrate(1 * ONE_SECOND_IN_MS); // vibrate regardless, peg-kids are slow
            }
        },
        clearActiveProject: (state) => {
            state.activeProject = null;
            state.scaledPlantingLines = [];
            state.visibleLines = [];
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
                const {project, settings} = action.payload;
                state.scaledPlantingLines = project.plantingLines.slice(0, settings.displayLineCount)
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
            }).addCase(fetchProjects.pending, state => {
                state.fetching = true;
            })
            .addCase(fetchProjects.fulfilled, (state, action) => {
                state.fetching = false;
                const crudProjects = action.payload;
                if (crudProjects !== undefined) {
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
                state.lock = false;
                // @ts-ignore
                const {project, settings} = action.payload;
                console.log(settings)
                state.scaledPlantingLines = project.plantingLines.slice(0, settings.displayLineCount)
                state.activeProject = project;
            })
            .addCase(loadProject.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message ?? 'Unknown error';
            })
            .addCase(deleteProject.fulfilled, (state, action) => {
                if(state.activeProject && state.activeProject.id === action.payload){
                    state.activeProject = null;
                }
                state.projectList = state.projectList.filter(p => p.id !== action.payload);
            })
            .addCase(deleteProject.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message ?? 'Unknown error';
            })

    },
});

export const { setLoading, saveProjectMarkedPoints, setLock, setIndices, setScaledPlanitingLines, clearActiveProject } = projectSlice.actions;
export default projectSlice.reducer;
