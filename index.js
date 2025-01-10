/**
 * @format
 */

import {AppRegistry} from 'react-native';
import App from './App';
import {name as appName} from './app.json';
import ProjectService from './src/services/ProjectService';

import {
    configureReanimatedLogger,
    ReanimatedLogLevel,
  } from 'react-native-reanimated';


configureReanimatedLogger({
    level: ReanimatedLogLevel.warn,
    strict: false
  });

ProjectService.init();
AppRegistry.registerComponent(appName, () => App);
