

import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';

const modalSlice = createSlice({
    name: 'modals',
    initialState: {
        showCreateNewProjects: false,
        showProjectList: false,
        showBTDevices: false,
        showUSBDevices: false,
        showAboutMsitu: false,
        showProjectExport: false,
    },
    reducers: {
        setShowCreateNewProjects: (state, action) => {
            state.showCreateNewProjects = action.payload;
        },
        setShowProjectList: (state, action) => {
            state.showProjectList = action.payload;
        },
        setShowBTDevices: (state, action) => {
            state.showBTDevices = action.payload;
        },
        setShowAboutMsitu: (state, action) => {
            state.showAboutMsitu = action.payload;
        },
        setShowProjectExport: (state, action) => {
            state.showProjectExport = action.payload;
        },
    },
});

export const { setShowCreateNewProjects, setShowProjectList, setShowBTDevices, setShowAboutMsitu, setShowProjectExport } = modalSlice.actions;

export default modalSlice.reducer;