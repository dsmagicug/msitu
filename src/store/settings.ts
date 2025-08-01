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
    highContrastMode: boolean
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
    mapStyle: MAP_TYPES.SATELLITE,
    highContrastMode: false
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
            const settingsToSave = {
                ...settings,
                highContrastMode: settings.highContrastMode ? 1 : 0
            };
            
            if (id) {
                await ProjectService.update("settings", id, settingsToSave);
                return settings;
            } else {
                const insertRows = await ProjectService.save("settings", [settingsToSave]);
                const { insertId } = insertRows[0];
                const data = {
                    ...settings,
                    id: insertId
                }
                return data as Settings;
            }

        } catch (error) {
            console.error('Error saving settings:', error);
            // Handle database errors properly without using generateError
            const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
            return thunkAPI.rejectWithValue(errorMessage);
        }
    },
);


export const loadSettings = createAsyncThunk(
    'settings/loadSettings',
    async (_, thunkAPI) => {
        try {
            const settings = await ProjectService.fetch("settings", "*");
            if (settings && settings.length > 0) {
                const dbSettings = settings[0];
                // Convert database boolean (0/1) back to boolean
                return {
                    ...dbSettings,
                    highContrastMode: Boolean(dbSettings.highContrastMode)
                } as Settings;
            } else {
                thunkAPI.dispatch(saveSettings(defaultSettings))
            }
            return defaultSettings;
        } catch (error) {
            console.error('Error loading settings:', error);
            // Handle database errors properly without using generateError
            const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
            return thunkAPI.rejectWithValue(errorMessage);
        }
    },
);

export const settingSlice = createSlice({
    name: 'settings',
    initialState,
    reducers: {
        toggleHighContrastMode: (state) => {
            if (state.settings && typeof state.settings === 'object' && 'highContrastMode' in state.settings) {
                (state.settings as Settings).highContrastMode = !(state.settings as Settings).highContrastMode;
            }
        },
        setHighContrastMode: (state, action) => {
            if (state.settings && typeof state.settings === 'object' && 'highContrastMode' in state.settings) {
                (state.settings as Settings).highContrastMode = action.payload;
            }
        }
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
export const { toggleHighContrastMode, setHighContrastMode } = settingSlice.actions;