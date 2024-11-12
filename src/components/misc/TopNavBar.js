import { View, Text, TouchableOpacity } from 'react-native'
import React from 'react'
import { useDispatch, useSelector } from 'react-redux';
import { setShowCreateNewProjects } from '../../store/modal';
import Icon from 'react-native-vector-icons/Ionicons';
import AntDesignIcon from 'react-native-vector-icons/AntDesign'
import MCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import MaterialIcons from 'react-native-vector-icons/MaterialIcons'

export default function TopNavBar({ navigation }) {

    const dispatch = useDispatch()
    const { activeProject, loading } = useSelector(store => store.project)
    const { isBluetoothEnabled, selectedDevice } = useSelector(store => store.bluetooth)

    return (
        <View className="absolute flex flex-row justify-between top-10 left-2 right-2 bg-white/70 p-2 rounded-lg items-center z-10">
            <TouchableOpacity
                className='bg-white/90 p-2 border border-teal-800 rounded-lg'
                onPress={() => {
                    navigation.openDrawer();
                }}
            >
                <Icon name="menu-outline" size={24} color="teal" />
            </TouchableOpacity>
            <Text className='font-avenirBold text-xl'>{activeProject && activeProject.name}</Text>
            <View className='flex flex-row'>
                <TouchableOpacity

                    className='p-1 flex items-center justify-center rounded-lg'>
                    {
                        isBluetoothEnabled ? (
                            selectedDevice ? (
                                <MaterialIcons name="bluetooth-connected" size={20} color="green" />
                            ) : (
                                <MCommunityIcons name="bluetooth" size={20} color="green" />
                            )
                        ) : (
                            <MCommunityIcons name="bluetooth-off" size={20} color="red" />
                        )
                    }
                    <Text style={{ fontSize: 6 }} className={`font-avenirBold ${isBluetoothEnabled ? 'text-green-600' : 'text-red-600'}`}>
                        {isBluetoothEnabled ? (selectedDevice ? `${selectedDevice.name}` : 'ON') : 'OFF'}
                    </Text>
                </TouchableOpacity>

                <TouchableOpacity
                    onPress={() => dispatch(setShowCreateNewProjects(true))}
                    className='flex items-center bg-white/90 border-l  border-l-gray-400 p-2'>
                    <AntDesignIcon name="addfolder" size={24} color="teal" />
                </TouchableOpacity>
            </View>
        </View>
    )
}