import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { generateError } from '../utils';
import { MAP_TYPES } from 'react-native-maps';
import ProjectService from '../services/ProjectService';

export type Settings = {
    id?: number
    appMode: string | 'planting' | 'mode'
    skipLines: number
    displayLineCount: number
    cloudApi: string
    mapStyle: string
}



interface SettingState {
    loading: boolean;
    saving: boolean;
    settings: Settings | {}
    error: string | null;
}
export const defaultSettings: Settings = {
    appMode: "planting",
    skipLines: 5,
    displayLineCount: 10,
    cloudApi: "https://api.msitu.com/import",
    mapStyle: MAP_TYPES.SATELLITE
}

const initialState: SettingState = {
    loading: false,
    saving: false,
    settings: defaultSettings,
    error: null
}


export const saveSettings = createAsyncThunk(
    'settings/saveSettings',
    async (settings: Settings, thunkAPI) => {
        try {
            console.log(settings)
            const { id } = settings;
            if (id) {
                await ProjectService.update("settings", id, settings);
                return settings;
            } else {
                const insertRows = await ProjectService.save("settings", [settings]);
                const { insertId } = insertRows[0];
                const data = {
                    ...settings,
                    id: insertId
                }
                return data as Settings;
            }


        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);


export const loadSettings = createAsyncThunk(
    'settings/loadSettings',
    async (_, thunkAPI) => {
        try {
            const settings = await ProjectService.fetch("settings", "*");
            if (settings && settings.length > 0) {
                return settings[0] as Settings
            } else {
                thunkAPI.dispatch(saveSettings(defaultSettings))
            }
            return defaultSettings;
        } catch (error) {
            thunkAPI.rejectWithValue(generateError(error));
        }
    },
);

export const settingSlice = createSlice({
    name: 'settings',
    initialState,
    reducers: {

    },
    extraReducers: builder => {
        builder
            .addCase(saveSettings.pending, state => {
                state.saving = true;
            })
            .addCase(saveSettings.fulfilled, (state, action) => {
                state.saving = false;
                state.settings = action.payload as Settings

            })
            .addCase(saveSettings.rejected, (state, action) => {
                state.saving = false;
                state.error = action.error.message ?? 'error';
            })
            .addCase(loadSettings.pending, state => {
                state.loading = true;
            })
            .addCase(loadSettings.fulfilled, (state, action) => {
                state.loading = false;
                state.settings = action.payload as Settings

            })
            .addCase(loadSettings.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message ?? 'error';
            })
    },
});
export default settingSlice.reducer;