import {
    View,
    Text,
    TouchableOpacity,
    ActivityIndicator,
    Alert
} from 'react-native'
import React, { useEffect, useState } from 'react'
import { BottomModal, ModalFooter, ModalButton, ModalContent } from 'react-native-modals';
import MCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import styles from '../../assets/styles';
import { useDispatch, useSelector } from 'react-redux';
import { ScrollView } from 'react-native-gesture-handler';
import { setSelectedDevice, requestBluetoothPermissions, discorverDevices, getBondedDevices, parseGGA, parseRMC } from '../../store/bluetooth';
import RNBluetoothClassic from 'react-native-bluetooth-classic';
import Toast from 'react-native-toast-message';
import colors from 'tailwindcss/colors';
import { parseNMEA } from '../../store/nmeaListener';

export default function BluetoothDevices({ children, show, onClose }) {

    const [connecting, setConnecting] = useState(false);
    const [disconnecting, setDisconnecting] = useState(false);
    const [connectedDeviceId, setConnectedDeviceId] = useState(null);
    const [tappedDeviceId, setTappedDeviceId] = useState(null);


    const { scanning, isBluetoothEnabled, deviceList, selectedDevice } = useSelector(store => store.bluetooth)
    const dispatch = useDispatch();

    const toggleDeviceConnectionStatus = async (device) => {
        setTappedDeviceId(device.id)
        let connection = await device.isConnected();
        if (connection) {
            // desconnect
            setDisconnecting(true);
            await device.disconnect();
            setDisconnecting(false);
            dispatch(setSelectedDevice(null));
            setConnectedDeviceId(null);
        } else {
            setConnecting(true);
            await device.connect({
                connectorType: "rfcomm",
                // delimiter: "\n",
                DEVICE_CHARSET: "utf-8"
            });
            setConnecting(false);
            dispatch(setSelectedDevice(device));
            setConnectedDeviceId(device.id)
            // listen to the data right

            /* device.onDataReceived((buffer) => {
                const sentence = buffer.data.trim();
                dispatch(parseNMEA(sentence))
            })  */
            Toast.show({
                type: "success",
                text1: "New Device Connected",
                text2: `BT device ${device.name} has connected for data receiption`
            })
        }
    }

    useEffect(()=>{
        if(!selectedDevice){
            setConnectedDeviceId(null);
        }
    },[selectedDevice])

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
        if (show) {
            dispatch(getBondedDevices())
        }
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
                        deviceList.map((device, idx) => (
                            <TouchableOpacity
                                onPress={() => {
                                    toggleDeviceConnectionStatus(device)
                                }}
                                key={idx}
                                className={`flex flex-row justify-between items-center ${connectedDeviceId === device.id ? 'bg-green-500 text-white' : 'bg-gray-100'}  p-3 rounded-sm m-1`}>

                                <Text className={`font-avenir ${connectedDeviceId === device.id ? 'font-bold text-white' : ''}`} >{device.name}</Text>
                                <View className='flex flex-row justify-end items-center gap-1'>
                                    {connectedDeviceId === device.id && <Text className='font-avenir font-bold text-white'>Connected</Text>}
                                    {
                                        tappedDeviceId === device.id && (connecting || disconnecting) && <ActivityIndicator color={colors.black} size="small" />
                                    }
                                </View>
                            </TouchableOpacity>
                        ))

                    }

                </ScrollView>
            </ModalContent>
        </BottomModal>
    )
}