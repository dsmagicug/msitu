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

export default function TopNavBar({ navigation, hideNewProject }) {

    const dispatch = useDispatch()
    const { activeProject, loading } = useSelector(store => store.project)
    const { isBluetoothEnabled, selectedDevice } = useSelector(store => store.bluetooth)

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

    return (
        <Reanimated.View
            className="absolute flex flex-row justify-between top-10 left-2 right-2 p-3 rounded-2xl items-center z-10"
            style={[
                animatedStyle,
                {
                    backgroundColor: 'rgba(255, 255, 255, 0.95)',
                    backdropFilter: 'blur(10px)',
                    shadowColor: '#000',
                    shadowOffset: { width: 0, height: 2 },
                    shadowOpacity: 0.1,
                    shadowRadius: 8,
                    elevation: 5,
                }
            ]}
        >
            <TouchableOpacity
                className='p-3 rounded-xl'
                onPress={handlePress}
                style={{
                    backgroundColor: 'rgba(59, 130, 246, 0.1)',
                    borderWidth: 1,
                    borderColor: 'rgba(59, 130, 246, 0.2)',
                }}
            >
                <Icon name="menu-outline" size={24} color="#3b82f6" />
            </TouchableOpacity>

            <View className="flex-1 mx-4">
                <Text className='font-avenirBold text-xl text-gray-800 text-center'>
                    {activeProject ? activeProject.name : 'Msitu [Cyrus Mode]'}
                </Text>
                {activeProject && (
                    <Text className='font-avenirMedium text-sm text-gray-500 text-center'>
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
                                        <MaterialIcons name="bluetooth-connected" size={16} color="#16a34a" />
                                        <Text style={{ fontSize: 6 }} className="font-avenirMedium text-center mt-1 text-gray-600"
                                            numberOfLines={1}
                                            ellipsizeMode="tail"
                                        >
                                            {selectedDevice.name.length > 8 ? selectedDevice.name.substring(0, 8) + '...' : selectedDevice.name}
                                        </Text>
                                    </>
                                ) : (
                                    <MCommunityIcons name="bluetooth" size={16} color="#2563eb" />
                                )
                            ) : (
                                <MCommunityIcons name="bluetooth-off" size={16} color="#dc2626" />
                            )
                        }
                    </View>
                </Reanimated.View>

                {!hideNewProject && (
                    <TouchableOpacity
                        onPress={handleNewProject}
                        className='p-3 rounded-xl flex items-center justify-center'
                        style={{
                            backgroundColor: 'rgba(34, 197, 94, 0.1)',
                            borderWidth: 1,
                            borderColor: 'rgba(34, 197, 94, 0.2)',
                        }}
                    >
                        <AntDesignIcon name="addfolder" size={24} color="#22c55e" />
                    </TouchableOpacity>
                )}
            </View>
        </Reanimated.View>
    )
}
