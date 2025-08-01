import { View, Text, TouchableOpacity } from 'react-native'
import React from 'react'
import { useDispatch, useSelector } from 'react-redux';
import { setShowCreateNewProjects } from '../../store/modal';
import Icon from 'react-native-vector-icons/Ionicons';
import AntDesignIcon from 'react-native-vector-icons/AntDesign'
import MCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import MaterialIcons from 'react-native-vector-icons/MaterialIcons'
import Reanimated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  interpolate,
  Extrapolation
} from 'react-native-reanimated';
import { APP_NAME } from '../../config/version';

export default function TopNavBar({ navigation, hideNewProject }) {

    const dispatch = useDispatch()
    const { activeProject, loading } = useSelector(store => store.project)
    const { isBluetoothEnabled, selectedDevice } = useSelector(store => store.bluetooth)
    // @ts-ignore
    const { settings } = useSelector(store => store.settings);
    const highContrastMode = settings?.highContrastMode || false;

    const scaleValue = useSharedValue(1);
    const opacityValue = useSharedValue(0);

    React.useEffect(() => {
        opacityValue.value = withTiming(1, { duration: 500 });
    }, []);

    const handlePress = () => {
        scaleValue.value = withSpring(0.95, { damping: 10, stiffness: 200 }, () => {
            scaleValue.value = withSpring(1, { damping: 15, stiffness: 150 });
        });
        navigation.openDrawer();
    };

    const handleNewProject = () => {
        scaleValue.value = withSpring(0.95, { damping: 10, stiffness: 200 }, () => {
            scaleValue.value = withSpring(1, { damping: 15, stiffness: 150 });
        });
        dispatch(setShowCreateNewProjects(true));
    };

    const animatedStyle = useAnimatedStyle(() => {
        return {
            transform: [{ scale: scaleValue.value }],
            opacity: opacityValue.value,
        };
    });

    const bluetoothStatusStyle = useAnimatedStyle(() => {
        const backgroundColor = isBluetoothEnabled 
            ? selectedDevice ? 'rgba(34, 197, 94, 0.15)' : 'rgba(59, 130, 246, 0.15)'
            : 'rgba(239, 68, 68, 0.15)';
        
        return {
            backgroundColor: withTiming(backgroundColor, { duration: 300 }),
        };
    });

    const containerStyle = {
        backgroundColor: highContrastMode ? 'rgba(255, 255, 255, 0.98)' : 'rgba(255, 255, 255, 0.95)',
        backdropFilter: 'blur(10px)',
        shadowColor: highContrastMode ? '#000000' : '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: highContrastMode ? 0.2 : 0.1,
        shadowRadius: 8,
        elevation: highContrastMode ? 8 : 5,
        borderWidth: highContrastMode ? 1 : 0,
        borderColor: highContrastMode ? '#000000' : 'transparent',
    };

    const textStyle = {
        color: highContrastMode ? '#000000' : '#1f2937',
        fontWeight: highContrastMode ? 'bold' : 'normal',
    };

    const subtitleStyle = {
        color: highContrastMode ? '#000000' : '#6b7280',
        fontWeight: highContrastMode ? '600' : 'normal',
    };

    return (
        <Reanimated.View 
            className="absolute flex flex-row justify-between top-10 left-2 right-2 p-3 rounded-2xl items-center z-10"
            style={[animatedStyle, containerStyle]}
        >
            <TouchableOpacity
                className='p-3 rounded-xl'
                onPress={handlePress}
                style={{
                    backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(59, 130, 246, 0.1)',
                    borderWidth: 1,
                    borderColor: highContrastMode ? 'rgba(0, 0, 0, 0.2)' : 'rgba(59, 130, 246, 0.2)',
                }}
            >
                <Icon name="menu-outline" size={24} color={highContrastMode ? "#000000" : "#3b82f6"} />
            </TouchableOpacity>
            
            <View className="flex-1 mx-4">
                <Text className='font-avenirBold text-xl text-center' style={textStyle}>
                    {activeProject ? activeProject.name : `${APP_NAME} [Cyrus Mode]`}
                </Text>
                {activeProject && (
                    <Text className='font-avenirMedium text-sm text-center' style={subtitleStyle}>
                        {loading ? 'Loading...' : 'Ready'}
                    </Text>
                )}
            </View>
            
            <View className='flex flex-row items-center gap-2'>
                <Reanimated.View
                    className='p-2 rounded-lg flex items-center justify-center'
                    style={bluetoothStatusStyle}
                >
                    <View className="flex items-center">
                        {
                            isBluetoothEnabled ? (
                                selectedDevice ? (
                                    <>
                                        <MaterialIcons name="bluetooth-connected" size={16} color={highContrastMode ? "#000000" : "#16a34a"} />
                                        <Text className="font-avenirMedium text-center mt-1"
                                            style={{
                                                color: highContrastMode ? '#000000' : '#16a34a',
                                                fontSize: 8
                                            }}
                                        >
                                            {selectedDevice.name.length > 8 ? selectedDevice.name.substring(0, 8) + '...' : selectedDevice.name}
                                        </Text>
                                    </>
                                ) : (
                                    <MCommunityIcons name="bluetooth" size={16} color={highContrastMode ? "#000000" : "#2563eb"} />
                                )
                            ) : (
                                <MCommunityIcons name="bluetooth-off" size={16} color={highContrastMode ? "#000000" : "#dc2626"} />
                            )
                        }
                    </View>
                </Reanimated.View>

                {!hideNewProject && (
                    <TouchableOpacity
                        onPress={handleNewProject}
                        className='p-3 rounded-xl flex items-center justify-center'
                        style={{
                            backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(34, 197, 94, 0.1)',
                            borderWidth: 1,
                            borderColor: highContrastMode ? 'rgba(0, 0, 0, 0.2)' : 'rgba(34, 197, 94, 0.2)',
                        }}
                    >
                        <AntDesignIcon name="addfolder" size={24} color={highContrastMode ? "#000000" : "#22c55e"} />
                    </TouchableOpacity>
                )}
            </View>
        </Reanimated.View>
    )
}
