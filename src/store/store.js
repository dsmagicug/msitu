import rootReducer from './rootReducer';
import {configureStore} from '@reduxjs/toolkit';
import loggerMiddleware from 'redux-logger';

const store = configureStore({
  reducer: rootReducer,
  middleware: getDefaultMiddleware => {
    return getDefaultMiddleware({
      serializableCheck: false,
    }).concat(loggerMiddleware);
  },
});
  
export {store};
