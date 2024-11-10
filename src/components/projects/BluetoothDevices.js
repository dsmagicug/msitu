import {
    View,
    Text,
    TouchableOpacity,
    ToastAndroid,
    ActivityIndicator,
    Alert
} from 'react-native'
import React, { useEffect, useState } from 'react'
import { BottomModal, ModalFooter, ModalButton, ModalContent } from 'react-native-modals';
import MCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import styles from '../../assets/styles';
import { useDispatch, useSelector } from 'react-redux';
import { ScrollView } from 'react-native-gesture-handler';
import { setScanning,setIsBTEnabled, requestBluetoothPermissions, discorverDevices } from '../../store/bluetooth';
import RNBluetoothClassic from 'react-native-bluetooth-classic';

export default function BluetoothDevices({ children, show, onClose }) {

    const [btStateUpdateListener, setBTStateUpdateListener] =  useState(null)

    const { scanning, isBluetoothEnabled, deviceList } = useSelector(store => store.bluetooth)
    const dispatch = useDispatch();

    useEffect(() => {
        
        const onStateChange = (event) => {
            console.log(event)
            dispatch(setIsBTEnabled(event.enabled));
        }
        const btStateUpdate = RNBluetoothClassic.onStateChanged(onStateChange);
        setBTStateUpdateListener(btStateUpdate)
        return () => {
            // cleanup 
            if(btStateUpdateListener){
                btStateUpdateListener.remove();
            }
            
        }
    }, [])
    const openBTSettings = () => {
        
        Alert.alert('Bluetooth Disabled', 'Do you want to open setting to enable it?', [
            {
                text: 'CANCEL',
                onPress: () => console.log('Cancel Pressed'),
                style: 'cancel',
            },
            { text: 'OPEN SETTINGS', onPress: () => RNBluetoothClassic.openBluetoothSettings() },
        ]);
    }
    useEffect(() => {
        if (!isBluetoothEnabled && show) {
            openBTSettings()
        }
    }, [isBluetoothEnabled, show])

    return (
        <BottomModal
            visible={show}
            modalTitle={
                <View className='flex border-b-hairline border-b-gray-300'>
                    <View className='flex-row justify-between items-center p-2 w-full' >
                        <Text className='font-avenirBold text-black text-2xl'>BT Devices</Text>

                        <View className='flex flex-row justify-end'>
                            <View className='flex flex-row justify-start'>
                                <TouchableOpacity
                                    onPress={async () => {
                                        if (!scanning) {
                                            if (await requestBluetoothPermissions()) {
                                                dispatch(discorverDevices())
                                            }

                                        }
                                    }}
                                    className='p-1 w-10 flex items-center border-r justify-center border-r-gray-300'>
                                    {
                                        scanning ?
                                            <ActivityIndicator size="small" />
                                            :
                                            <MCommunityIcons name="magnify" size={20} color="teal-800" />}
                                    <Text style={{ fontSize: 6 }} className="font-avenirBold text-teal-800">
                                        SCAN
                                    </Text>
                                </TouchableOpacity>
                                <View>
                                    <TouchableOpacity
                                        onPress={() => {
                                            if (!isBluetoothEnabled) {
                                                openBTSettings()
                                            }
                                        }}
                                        className='p-1 w-10 flex items-center justify-center rounded-lg'>
                                        {
                                            isBluetoothEnabled ?
                                                <MCommunityIcons name="bluetooth" size={20} color="green" />
                                                :
                                                <MCommunityIcons name="bluetooth-off" size={20} color="red" />
                                        }
                                        <Text style={{ fontSize: 6 }} className={`font-avenirBold ${isBluetoothEnabled ? 'text-green-600' : 'text-red-600'}`}>
                                            {isBluetoothEnabled ? 'ON' : 'OFF'}
                                        </Text>
                                    </TouchableOpacity>

                                </View>
                            </View>
                        </View>
                    </View>

                </View>
            }
            footer={
                <ModalFooter>
                    <ModalButton
                        text="CLOSE"
                        textStyle={styles.buttonText}
                        onPress={() => { onClose() }}
                    />
                </ModalFooter>
            }
        >
            <ModalContent>
                <ScrollView className='mt-3 h-64'>
                    {
                        deviceList.map((device, idx)=>(
                            <TouchableOpacity 
                                key={idx}
                                className='bg-gray-100 p-3 rounded-sm m-1'>
                                <Text>{device.name}</Text>
                            </TouchableOpacity>
                        ))
                        
                    }
                    
                </ScrollView>
            </ModalContent>
        </BottomModal>
    )
}