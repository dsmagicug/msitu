import { Project } from '../models';

export const exportProjectToFile = async (project: Project): Promise<string> => {
  try {
    // Create a safe filename from project name
    const safeName = project.name.replace(/[^a-zA-Z0-9]/g, '_');
    const timestamp = new Date().toISOString().split('T')[0];
    const filename = `${safeName}_${timestamp}.json`;
    
    // Convert project to JSON string
    const projectJson = JSON.stringify(project, null, 2);
    
    // For now, we'll return the JSON string and filename
    // In a real implementation, you would write this to the device's documents directory
    console.log(`Project exported: ${filename}`);
    console.log('Project JSON:', projectJson);
    
    return projectJson;
  } catch (error) {
    console.error('Failed to export project to file:', error);
    throw error;
  }
};

export const importProjectFromFile = async (jsonData: string): Promise<Project> => {
  try {
    const project: Project = JSON.parse(jsonData);
    
    // Validate required fields
    if (!project.name || !project.basePoints) {
      throw new Error('Invalid project data: missing required fields');
    }
    
    return project;
  } catch (error) {
    console.error('Failed to import project from file:', error);
    throw error;
  }
}; 