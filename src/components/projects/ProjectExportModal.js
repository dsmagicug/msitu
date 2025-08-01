import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, ScrollView, Alert, Share, PermissionsAndroid, Platform } from 'react-native';
import { useSelector } from 'react-redux';
import ProjectService from '../../services/ProjectService';
import { exportProjectToFile } from '../../utils/fileUtils';
import Toast from 'react-native-toast-message';
import Icon from 'react-native-vector-icons/Ionicons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import Reanimated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  withDelay,
} from 'react-native-reanimated';
import RNFS from 'react-native-fs';

const ProjectExportModal = ({ visible, onClose }) => {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedProject, setSelectedProject] = useState(null);

  // Get high contrast mode from store
  const { settings } = useSelector(store => store.settings);
  const highContrastMode = settings?.highContrastMode || false;

  const modalOpacity = useSharedValue(0);
  const modalScale = useSharedValue(0.8);
  const contentTranslateY = useSharedValue(50);

  React.useEffect(() => {
    if (visible) {
      modalOpacity.value = withTiming(1, { duration: 300 });
      modalScale.value = withSpring(1, { damping: 15, stiffness: 150 });
      contentTranslateY.value = withDelay(100, withSpring(0, { damping: 15, stiffness: 150 }));
      loadProjects();
    } else {
      modalOpacity.value = withTiming(0, { duration: 200 });
      modalScale.value = withSpring(0.8, { damping: 15, stiffness: 150 });
      contentTranslateY.value = withSpring(50, { damping: 15, stiffness: 150 });
    }
  }, [visible]);

  const modalAnimatedStyle = useAnimatedStyle(() => {
    return {
      opacity: modalOpacity.value,
    };
  });

  const contentAnimatedStyle = useAnimatedStyle(() => {
    return {
      transform: [
        { scale: modalScale.value },
        { translateY: contentTranslateY.value }
      ],
    };
  });

  const handleClose = () => {
    onClose();
  };

  const loadProjects = async () => {
    try {
      setLoading(true);
      const projectList = await ProjectService.fetch('projects');
      setProjects(projectList);
    } catch (error) {
      console.error('Failed to load projects:', error);
      Toast.show({
        type: 'error',
        text1: 'Error',
        text2: 'Failed to load projects'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleExportProject = async (project) => {
    try {
      setLoading(true);
      
      // Request storage permission on Android
      if (Platform.OS === 'android') {
        try {
          // Check if permission is already granted
          const hasPermission = await PermissionsAndroid.check(
            PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE
          );
          
          if (!hasPermission) {
            const granted = await PermissionsAndroid.request(
              PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
              {
                title: 'Storage Permission',
                message: 'App needs access to storage to save project files to Downloads folder.',
                buttonNeutral: 'Ask Me Later',
                buttonNegative: 'Cancel',
                buttonPositive: 'OK',
              }
            );
            
            if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
              throw new Error('Storage permission denied. Cannot save project file.');
            }
          }
        } catch (permissionError) {
          console.error('Permission error:', permissionError);
          throw new Error('Failed to request storage permission. Cannot save project file.');
        }
      }

      const exportedProject = await ProjectService.exportProject(project.id);

      // Convert to JSON file
      const projectJson = await exportProjectToFile(exportedProject);
      
      // Create filename with timestamp
      const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
      const filename = `msitu-project-${project.name.replace(/\s+/g, '-')}-${timestamp}.json`;
      
      // Determine download path
      let downloadPath;
      if (Platform.OS === 'android') {
        downloadPath = `${RNFS.DownloadDirectoryPath}/${filename}`;
      } else {
        downloadPath = `${RNFS.DocumentDirectoryPath}/${filename}`;
      }
      
      // Write file to downloads
      await RNFS.writeFile(downloadPath, projectJson, 'utf8');
      
      // Show success message
      Toast.show({
        type: 'success',
        text1: 'Export Successful',
        text2: `Project saved to Downloads/${filename}`
      });
      
    } catch (error) {
      console.error('Export failed:', error);
      Toast.show({
        type: 'error',
        text1: 'Export Failed',
        text2: error.message || 'Failed to export project'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleShareProject = async (project) => {
    try {
      // Export project using ProjectService
      const exportedProject = await ProjectService.exportProject(project.id);

      // Convert to JSON string
      const projectJson = JSON.stringify(exportedProject, null, 2);

      // Share the JSON data
      await Share.share({
        message: `Msitu Project: ${project.name}\n\n${projectJson}`,
        title: `Msitu Project - ${project.name}`,
      });

    } catch (error) {
      console.error('Share failed:', error);
      Toast.show({
        type: 'error',
        text1: 'Share Failed',
        text2: error.message || 'Failed to share project'
      });
    }
  };

  if (!visible) return null;

  const containerStyle = {
    backgroundColor: highContrastMode ? 'rgba(255, 255, 255, 0.98)' : 'rgba(0, 0, 0, 0.5)',
  };

  const modalStyle = {
    backgroundColor: highContrastMode ? '#ffffff' : '#ffffff',
    borderWidth: highContrastMode ? 2 : 0,
    borderColor: highContrastMode ? '#000000' : 'transparent',
    shadowColor: highContrastMode ? '#000000' : '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: highContrastMode ? 0.3 : 0.25,
    shadowRadius: 20,
    elevation: highContrastMode ? 8 : 5,
  };

  const textStyle = {
    color: highContrastMode ? '#000000' : '#1f2937',
    fontWeight: highContrastMode ? 'bold' : 'normal',
  };

  const subtitleStyle = {
    color: highContrastMode ? '#000000' : '#6b7280',
    fontWeight: highContrastMode ? '600' : 'normal',
  };

  const projectCardStyle = {
    backgroundColor: highContrastMode ? 'rgba(255, 255, 255, 0.95)' : 'rgba(255, 255, 255, 0.8)',
    borderWidth: highContrastMode ? 2 : 1,
    borderColor: highContrastMode ? '#000000' : 'rgba(59, 130, 246, 0.1)',
  };

  const projectNameStyle = {
    color: highContrastMode ? '#000000' : '#1f2937',
    fontWeight: highContrastMode ? 'bold' : 'normal',
  };

  const projectInfoStyle = {
    color: highContrastMode ? '#000000' : '#6b7280',
    fontWeight: highContrastMode ? '600' : 'normal',
  };

  return (
    <Reanimated.View
      className="absolute inset-0 z-50 justify-center items-center"
      style={[containerStyle, modalAnimatedStyle]}
    >
      <TouchableOpacity
        className="absolute inset-0"
        onPress={handleClose}
        activeOpacity={1}
      />

      <Reanimated.View
        className="mx-6 rounded-3xl overflow-hidden"
        style={[modalStyle, contentAnimatedStyle]}
      >
        <View className="p-6 pb-4 bg-gray-50" style={{ backgroundColor: highContrastMode ? '#f8f9fa' : '#f9fafb' }}>
          <View className="flex flex-row items-center justify-between mb-4">
            <View className="flex-1">
              <Text className="font-avenirBold text-2xl" style={textStyle}>Export Projects</Text>
              <Text className="font-avenirMedium text-sm" style={subtitleStyle}>Select a project to export as JSON</Text>
              <Text className="font-avenirMedium text-xs mt-1" style={subtitleStyle}>{projects.length} projects available</Text>
            </View>
            <TouchableOpacity
              onPress={handleClose}
              className="rounded-full"
              style={{
                backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(239, 68, 68, 0.15)',
                borderWidth: highContrastMode ? 1 : 0,
                borderColor: highContrastMode ? '#000000' : 'transparent',
                width: 40,
                height: 40,
                justifyContent: 'center',
                alignItems: 'center',
              }}
            >
              <Icon name="close" size={20} color={highContrastMode ? "#000000" : "#ef4444"} />
            </TouchableOpacity>
          </View>
        </View>

        <ScrollView className="max-h-96" showsVerticalScrollIndicator={false}>
          <View className="px-6 pb-6">
            {loading ? (
              <View className="flex-1 justify-center items-center py-8">
                <Text className="font-avenirMedium text-lg" style={textStyle}>
                  Loading projects...
                </Text>
              </View>
            ) : projects.length === 0 ? (
              <View className="flex-1 justify-center items-center py-8">
                <Text className="font-avenirMedium text-lg text-center" style={textStyle}>
                  No projects found
                </Text>
                <Text className="font-avenirMedium text-sm text-center mt-2" style={projectInfoStyle}>
                  Create a project first to export it
                </Text>
              </View>
            ) : (
              <View className="space-y-4 pt-4">
                {projects.map((project) => (
                  <View
                    key={project.id}
                    className="p-4 rounded-xl"
                    style={projectCardStyle}
                  >
                    <View className="flex-row items-center justify-between">
                        <View className="flex-1 pr-4">
                          <Text className="font-avenirBold text-lg" style={projectNameStyle}>
                            {project.name}
                          </Text>
                          <Text className="font-avenirMedium text-sm" style={projectInfoStyle}>
                            Length: {project.lineLength || 'N/A'} {project.lineLengthUnit || 'm'} • Gap: {(project.gapSize || 0).toFixed(2)} {project.gapSizeUnit || 'm'}
                          </Text>
                        </View>
                        <View className="flex-row items-center space-x-8">
                        <TouchableOpacity
                          onPress={() => handleExportProject(project)}
                          disabled={loading}
                          className="p-3 rounded-lg"
                          style={{
                            backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(34, 197, 94, 0.1)',
                            borderWidth: highContrastMode ? 1 : 0,
                            borderColor: highContrastMode ? '#000000' : 'transparent',
                          }}
                        >
                          <MaterialCommunityIcons
                            name="download"
                            size={20}
                            color={highContrastMode ? "#000000" : "#22c55e"}
                          />
                        </TouchableOpacity>
                        <TouchableOpacity
                          onPress={() => handleShareProject(project)}
                          disabled={loading}
                          className="p-3 rounded-lg"
                          style={{
                            backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(59, 130, 246, 0.1)',
                            borderWidth: highContrastMode ? 1 : 0,
                            borderColor: highContrastMode ? '#000000' : 'transparent',
                          }}
                        >
                          <MaterialCommunityIcons
                            name="share-variant"
                            size={20}
                            color={highContrastMode ? "#000000" : "#3b82f6"}
                          />
                        </TouchableOpacity>
                      </View>
                    </View>
                  </View>
                ))}
              </View>
            )}
          </View>
        </ScrollView>

        {/* Fixed Footer */}
        <View className="p-4 border-t bg-gray-50" style={{
          borderColor: highContrastMode ? '#000000' : '#e5e7eb',
          backgroundColor: highContrastMode ? '#f8f9fa' : '#f9fafb'
        }}>
          <Text className="font-avenirMedium text-xs text-center" style={subtitleStyle}>
            "The best time to plant a tree was 20 years ago. The second best time is now."
          </Text>
          <Text className="font-avenirBold text-xs text-center mt-2" style={textStyle}>
            ― Chinese Proverb
          </Text>
        </View>
      </Reanimated.View>
    </Reanimated.View>
  );
};

export default ProjectExportModal;
