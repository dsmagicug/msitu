import { View, Text, TouchableOpacity, ActivityIndicator, Dimensions } from 'react-native'
import React, { useEffect, useState } from 'react'
import { BottomModal, ModalFooter, ModalButton, ModalContent } from 'react-native-modals';
import AntDesignIcon from 'react-native-vector-icons/AntDesign'
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import styles from '../../assets/styles';
import { fetchProjects, loadProject, deleteProject } from '../../store/projects';
import { useDispatch, useSelector } from 'react-redux';
import { ScrollView } from 'react-native-gesture-handler';
import Reanimated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  withDelay
} from 'react-native-reanimated';

const AnimatedProjectItem = ({ project, index, onOpen, onDelete }) => {
  const scaleValue = useSharedValue(0);
  const opacityValue = useSharedValue(0);

  React.useEffect(() => {
    scaleValue.value = withDelay(index * 100, withSpring(1, { damping: 15, stiffness: 150 }));
    opacityValue.value = withDelay(index * 100, withTiming(1, { duration: 300 }));
  }, []);

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
    }
  };

  return (
    <Reanimated.View style={animatedStyle}>
      <View className="flex flex-row justify-between p-2 mt-1 mb-1 mx-2 rounded-lg"
        style={{
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          borderWidth: 1,
          borderColor: 'rgba(59, 130, 246, 0.1)',
          shadowColor: '#000',
          shadowOffset: { width: 0, height: 1 },
          shadowOpacity: 0.05,
          shadowRadius: 2,
          elevation: 1,
        }}
      >
        <View className="flex-1">
          <Text className='font-avenirBold text-gray-800 text-base mb-0.5'>#{project.id}-{project.name}</Text>
        </View>
        <View className='flex flex-row justify-end gap-1'>
          <TouchableOpacity
            onPress={() => handlePress('open')}
            className='p-2 rounded-md flex flex-row justify-center items-center'
            style={{
              backgroundColor: 'rgba(59, 130, 246, 0.1)',
              borderWidth: 1,
              borderColor: 'rgba(59, 130, 246, 0.2)',
            }}
          >
            <MaterialCommunityIcons name="open-in-new" size={14} color="#3b82f6" />
            <Text className='font-avenirBold text-xs text-blue-600 ml-1'>Open</Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => handlePress('delete')}
            className='p-2 rounded-md flex flex-row justify-center items-center'
            style={{
              backgroundColor: 'rgba(239, 68, 68, 0.1)',
              borderWidth: 1,
              borderColor: 'rgba(239, 68, 68, 0.2)',
            }}
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
    const { fetching, projectList } = useSelector(store => store.project)
    const [isPortrait, setIsPortrait] = useState(true);

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
            modalTitle={
                <View className='flex flex-row justify-between items-center border-b border-gray-200 m-2 p-1'
                  style={{
                    backgroundColor: 'rgba(255, 255, 255, 0.98)',
                    borderTopLeftRadius: 20,
                    borderTopRightRadius: 20,
                  }}
                >
                    <View>
                        <Text className='font-avenirBold text-gray-800 text-2xl'>Project List</Text>
                        <Text className='font-avenirMedium text-gray-500 text-sm mt-1'>
                          {projectList.length} project{projectList.length !== 1 ? 's' : ''} found
                        </Text>
                    </View>
                    <TouchableOpacity
                        className='p-3 rounded-xl mr-3'
                        style={{
                          backgroundColor: 'rgba(34, 197, 94, 0.1)',
                          borderWidth: 1,
                          borderColor: 'rgba(34, 197, 94, 0.2)',
                        }}
                    >
                        <AntDesignIcon name="addfolder" size={24} color="#22c55e" />
                    </TouchableOpacity>
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
                                onDelete={() => dispatch(deleteProject(project.id))}
                            />
                        ))}
                      </View>
                    )}
                </ScrollView>
            </ModalContent>
        </BottomModal>
    )
}
