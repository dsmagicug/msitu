import { View, Text, TouchableOpacity, ActivityIndicator, Dimensions, Alert } from 'react-native'
import React, { useEffect, useState } from 'react'
import { BottomModal, ModalFooter, ModalButton, ModalContent } from 'react-native-modals';
import AntDesignIcon from 'react-native-vector-icons/AntDesign'
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import styles from '../../assets/styles';
import { fetchProjects, loadProject, deleteProject, clearActiveProject } from '../../store/projects';
import { useDispatch, useSelector } from 'react-redux';
import { ScrollView } from 'react-native-gesture-handler';
import Reanimated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  withDelay
} from 'react-native-reanimated';
import ProjectService from '../../services/ProjectService';
import { importProjectFromFile } from '../../utils/fileUtils';
import Toast from 'react-native-toast-message';
import { pick , types} from '@react-native-documents/picker'
import RNFS from 'react-native-fs';

const AnimatedProjectItem = ({ project, index, onOpen, onDelete, onDeselect, isActive }) => {
  const scaleValue = useSharedValue(0);
  const opacityValue = useSharedValue(0);

  React.useEffect(() => {
    scaleValue.value = withDelay(index * 100, withSpring(1, { damping: 15, stiffness: 150 }));
    opacityValue.value = withDelay(index * 100, withTiming(1, { duration: 300 }));
  }, []);

  // Debug logging to see project data

  const animatedStyle = useAnimatedStyle(() => {
    return {
      transform: [{ scale: scaleValue.value }],
      opacity: opacityValue.value,
    };
  });

  const handlePress = (action) => {
    scaleValue.value = withSpring(0.95, { damping: 10, stiffness: 200 }, () => {
      scaleValue.value = withSpring(1, { damping: 15, stiffness: 150 });
    });

    if (action === 'open') {
      onOpen();
    } else if (action === 'delete') {
      onDelete();
    } else if (action === 'deselect') {
      onDeselect();
    }
  };

  return (
    <Reanimated.View style={animatedStyle}>
      <View className="flex flex-row justify-between p-3 mt-1 mb-1 mx-2 rounded-xl relative overflow-hidden"
        style={{
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          borderWidth: isActive ? 2 : 1,
          borderColor: isActive ? '#16a34a' : 'rgba(59, 130, 246, 0.1)',
          shadowColor: '#000',
          shadowOffset: { width: 0, height: 1 },
          shadowOpacity: 0.05,
          shadowRadius: 2,
          elevation: 1,
        }}
      >
        <View className="flex-1 pr-2">
          <View className="flex-row items-center mb-1">
            <Text className='font-avenirBold text-base' style={{ color: isActive ? '#16a34a' : '#1f2937' }}>
              #{project.id}-{project.name}
            </Text>
          </View>

          <View className="flex-row items-center justify-between">
            <Text className='font-avenirMedium text-xs' style={{ color: '#6b7280' }}>
              Length: {project.lineLength || 'N/A'} {project.lineLengthUnit || 'm'} • Gap: {(project.gapSize || 0).toFixed(2)} {project.gapSizeUnit || 'm'}
            </Text>
          </View>
        </View>

        <View className='flex flex-row justify-end gap-1'>
          {isActive ? (
            <TouchableOpacity
              onPress={() => handlePress('deselect')}
              className='p-2 rounded-lg flex flex-row justify-center items-center'
              style={{
                backgroundColor: 'rgba(239, 68, 68, 0.1)',
                borderWidth: 1,
                borderColor: 'rgba(239, 68, 68, 0.2)',
              }}
            >
              <MaterialCommunityIcons name="close-circle-outline" size={14} color="#ef4444" />
              <Text className='font-avenirBold text-xs text-red-600 ml-1'>Deselect</Text>
            </TouchableOpacity>
          ) : (
            <TouchableOpacity
              onPress={() => handlePress('open')}
              className='p-2 flex flex-row justify-center items-center'
            >
              <MaterialCommunityIcons name="open-in-new" size={14} color="#3b82f6" />
              <Text className='font-avenirBold text-xs text-blue-600 ml-1'>Open</Text>
            </TouchableOpacity>
          )}
          <TouchableOpacity
            onPress={() => handlePress('delete')}
            className='p-2 flex flex-row justify-center items-center'
          >
            <MaterialCommunityIcons name="trash-can-outline" size={14} color="#ef4444" />
            <Text className='font-avenirBold text-xs text-red-600 ml-1'>Delete</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Reanimated.View>
  );
};

export default function ProjectList({ children, show, onClose }) {
    const dispatch = useDispatch()
    const { fetching, projectList, activeProject } = useSelector(store => store.project)
    const [isPortrait, setIsPortrait] = useState(true);
    const [importing, setImporting] = useState(false);

    const handleImportProject = async () => {
        try {
            setImporting(true);

            // Pick JSON file
            const result = await pick({
                allowMultiSelection: false,
                type: [types.json],
            });
            if (result && result.length > 0) {
                const file = result[0];
                const jsonData = await RNFS.readFile(file.uri, 'utf8');

                // Parse and validate the project
                const project = await importProjectFromFile(jsonData);
                console.log('Importing project:', project.name);
                // Prepare project data for database with async stringification
                const projectData = {
                    ...project,
                    id:null,
                    center: project.center ? JSON.stringify(project.center) : null,
                    basePoints: project.basePoints ? JSON.stringify(project.basePoints) : [],
                    plantingLines: project.plantingLines ? JSON.stringify(project.plantingLines) : [],
                    markedPoints: project.markedPoints ? JSON.stringify(project.markedPoints) : null,
                    createdAt: project.createdAt ? new Date(project.createdAt).toISOString() : new Date().toISOString()
                };

                // Save to database
                const insertRows = await ProjectService.save("projects", [projectData]);
                const { insertId } = insertRows[0];
                if(insertId){
                    // Refresh project list
                    dispatch(fetchProjects());
                    Toast.show({
                        type: 'success',
                        text1: 'Import Successful',
                        text2: `Project "${project.name}" imported successfully`
                    });
                }
            }

        } catch (error) {
            console.error('Import error:', error);
            Toast.show({
                type: 'error',
                text1: 'Import Failed',
                text2: error.message || 'Failed to import project'
            });
        } finally {
            setImporting(false);
        }
    };

    const handleDeleteProject = (project) => {
        Alert.alert(
            'Delete Project',
            `Are you sure you want to delete "${project.name}"? This action cannot be undone.`,
            [
                {
                    text: 'Cancel',
                    style: 'cancel',
                },
                {
                    text: 'Delete',
                    style: 'destructive',
                    onPress: () => {
                        dispatch(deleteProject(project.id));
                        Toast.show({
                            type: 'success',
                            text1: 'Project Deleted',
                            text2: `Project "${project.name}" has been deleted`
                        });
                    },
                },
            ],
            { cancelable: true }
        );
    };

    useEffect(() => {
        const updateOrientation = () => {
            const { width, height } = Dimensions.get('window');
            setIsPortrait(height > width);
        };

        updateOrientation();
        const subscription = Dimensions.addEventListener('change', updateOrientation);

        return () => subscription?.remove();
    }, []);

    useEffect(() => {
        if (show) {
            dispatch(fetchProjects())
        }
    }, [show])

    return (
        <BottomModal
            visible={show}
            onTouchOutside={onClose}
            modalTitle={
                <View className='flex flex-row justify-between items-center border-b border-gray-200 m-2 p-1'
                  style={{
                    backgroundColor: 'rgba(255, 255, 255, 0.98)',
                    borderTopLeftRadius: 20,
                    borderTopRightRadius: 20,
                    minHeight: 60,
                  }}
                >
                    <View className="flex-1 mr-3">
                        <Text className='font-avenirBold text-gray-800 text-xl'>Project List</Text>
                        <Text className='font-avenirMedium text-gray-500 text-sm mt-1'>
                          {projectList.length} project{projectList.length !== 1 ? 's' : ''} found
                        </Text>
                    </View>
                    <View className="flex flex-row gap-2">
                        <TouchableOpacity
                            className='p-3 rounded-xl'
                            style={{
                              backgroundColor: 'rgba(59, 130, 246, 0.1)',
                              borderWidth: 1,
                              borderColor: 'rgba(59, 130, 246, 0.2)',
                              minWidth: 50,
                              minHeight: 50,
                              justifyContent: 'center',
                              alignItems: 'center',
                            }}
                            onPress={handleImportProject}
                            disabled={importing}
                        >
                            {importing ? (
                                <ActivityIndicator size="small" color="#3b82f6" />
                            ) : (
                                <MaterialCommunityIcons name="import" size={24} color="#3b82f6" />
                            )}
                        </TouchableOpacity>
                        <TouchableOpacity
                            className='p-3 rounded-xl'
                            style={{
                              backgroundColor: 'rgba(34, 197, 94, 0.1)',
                              borderWidth: 1,
                              borderColor: 'rgba(34, 197, 94, 0.2)',
                              minWidth: 50,
                              minHeight: 50,
                              justifyContent: 'center',
                              alignItems: 'center',
                            }}
                            onPress={() => {
                              // TODO: Add new project functionality
                              console.log('New Project button pressed');
                            }}
                        >
                            <AntDesignIcon name="addfolder" size={24} color="#22c55e" />
                        </TouchableOpacity>
                    </View>
                </View>
            }
            footer={
                <ModalFooter>
                    <ModalButton
                        text="CLOSE"
                        textStyle={[styles.buttonText, { color: '#3b82f6' }]}
                        onPress={() => { onClose() }}
                    />
                </ModalFooter>
            }
        >
            <ModalContent>
                <ScrollView
                    className='mt-2 px-2'
                    style={{
                        height: isPortrait ? 250 : 200,
                        maxHeight: isPortrait ? 250 : 200
                    }}
                    contentContainerStyle={{
                        paddingBottom: 10,
                        alignItems: 'flex-start'
                    }}
                >
                    {fetching && (
                      <View className="flex items-center justify-center py-6 w-full">
                        <ActivityIndicator size="large" color="#3b82f6" />
                        <Text className="font-avenirMedium text-gray-600 mt-2">Loading projects...</Text>
                      </View>
                    )}
                    {!fetching && projectList.length === 0 && (
                      <View className="flex items-center justify-center py-6 w-full">
                        <MaterialCommunityIcons name="folder-open-outline" size={40} color="#9ca3af" />
                        <Text className="font-avenirMedium text-gray-500 mt-2">No projects found</Text>
                        <Text className="font-avenirMedium text-gray-400 text-sm text-center mt-1">
                          Create a new project to get started
                        </Text>
                      </View>
                    )}
                    {!fetching && projectList.length > 0 && (
                      <View className="w-full">
                        {projectList.map((project, idx) => (
                            <AnimatedProjectItem
                                key={idx}
                                project={project}
                                index={idx}
                                onOpen={() => dispatch(loadProject(project.id))}
                                onDelete={() => handleDeleteProject(project)}
                                isActive={activeProject && activeProject.id === project.id}
                                onDeselect={() => dispatch(clearActiveProject())}
                            />
                        ))}
                      </View>
                    )}
                </ScrollView>
            </ModalContent>
        </BottomModal>
    )
}
