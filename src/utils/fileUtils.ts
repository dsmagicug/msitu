import { Project } from '../models';
import RNFS from 'react-native-fs';
import { Platform } from 'react-native';
import Toast from "react-native-toast-message";

export const exportProjectToFile = async (project: Project): Promise<string> => {
  try {
    const safeName = project.name.replace(/[^a-zA-Z0-9]/g, '_');
    const timestamp = new Date().toISOString().split('T')[0];
    const filename = `${safeName}_${timestamp}.json`;

    // Convert project to JSON string
    const projectJson = JSON.stringify(project, null, 2);

    // Determine save path based on platform
    let savePath = `${RNFS.DownloadDirectoryPath}/${filename}`;
    await RNFS.writeFile(savePath, projectJson, 'utf8');
    Toast.show({
      type: 'success',
      text1: 'Export Successful',
      text2: `Project exported to ${Platform.OS === 'android' ? 'Downloads' : 'Files'}/${filename}`,
    });
    return projectJson;
  } catch (error) {
    console.error('Failed to export project to file:', error);
    throw error;
  }
};

export const importProjectFromFile = async (jsonData: string): Promise<Project> => {
  try {
    const project: Project = JSON.parse(jsonData);
    if (!project.name || !project.basePoints) {
      throw new Error('Invalid project data: missing required fields');
    }
    return project;
  } catch (error) {
    console.error('Failed to import project from file:', error);
    throw error;
  }
};
