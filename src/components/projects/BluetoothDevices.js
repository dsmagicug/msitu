import {
    View,
    Modal,
    Text,
    TouchableOpacity,
    ActivityIndicator,
    Alert,
    Dimensions
} from 'react-native'
import React, { useEffect, useState } from 'react'
import MCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import MaterialIcons from 'react-native-vector-icons/MaterialIcons'
import Ionicon from 'react-native-vector-icons/Ionicons'
import styles from '../../assets/styles';
import { useDispatch, useSelector } from 'react-redux';
import { ScrollView } from 'react-native-gesture-handler';
import { setSelectedDevice, requestBluetoothPermissions, discorverDevices, getBondedDevices, parseGGA, parseRMC } from '../../store/bluetooth';
import RNBluetoothClassic from 'react-native-bluetooth-classic';
import Toast from 'react-native-toast-message';
import colors from 'tailwindcss/colors';
import { parseNMEA } from '../../store/nmeaListener';
import Reanimated, {
    useSharedValue,
    useAnimatedStyle,
    withSpring,
    withTiming,
    withDelay
} from 'react-native-reanimated';

const { width } = Dimensions.get('window');

export default function BluetoothDevices({ children, visible, onClose }) {

    const handleClose = () => {
      onClose();
    };

    const [connecting, setConnecting] = useState(false);
    const [disconnecting, setDisconnecting] = useState(false);
    const [connectedDeviceId, setConnectedDeviceId] = useState(null);
    const [tappedDeviceId, setTappedDeviceId] = useState(null);

    const { scanning, isBluetoothEnabled, deviceList, selectedDevice } = useSelector(store => store.bluetooth)
    // @ts-ignore
    const { settings } = useSelector(store => store.settings);
    const highContrastMode = settings?.highContrastMode || false;
    const dispatch = useDispatch();

    // Animation values
    const modalScale = useSharedValue(0.8);
    const modalOpacity = useSharedValue(0); //show
    const contentTranslateY = useSharedValue(50);

    useEffect(() => {
        if (visible) {
            modalScale.value = withSpring(1, { damping: 15, stiffness: 150 });
            modalOpacity.value = withTiming(1, { duration: 300 });
            contentTranslateY.value = withDelay(100, withSpring(0, { damping: 15, stiffness: 150 }));
        } else {
            modalScale.value = withSpring(0.8, { damping: 15, stiffness: 150 });
            modalOpacity.value = withTiming(0, { duration: 200 });
            contentTranslateY.value = withSpring(50, { damping: 15, stiffness: 150 });
        }
    }, [visible]);

    const modalAnimatedStyle = useAnimatedStyle(() => {
        return {
            transform: [{ scale: modalScale.value }],
            opacity: modalOpacity.value,
        };
    }, [modalScale, modalOpacity]);

    const contentAnimatedStyle = useAnimatedStyle(() => {
        return {
            transform: [{ translateY: contentTranslateY.value }],
        };
    }, [contentTranslateY]);

    const toggleDeviceConnectionStatus = async (device) => {
        setTappedDeviceId(device.id)
        let connection = await device.isConnected();
        if (connection) {
            // disconnect
            setDisconnecting(true);
            await device.disconnect();
            setDisconnecting(false);
            dispatch(setSelectedDevice(null));
            setConnectedDeviceId(null);
            Toast.show({
                type: "info",
                text1: "Device Disconnected",
                text2: `${device.name} has been disconnected`
            });
        } else {
            setConnecting(true);
            await device.connect({
                connectorType: "rfcomm",
                DEVICE_CHARSET: "utf-8"
            });
            setConnecting(false);
            dispatch(setSelectedDevice(device));
            setConnectedDeviceId(device.id)
            
            Toast.show({
                type: "success",
                text1: "Device Connected",
                text2: `${device.name} is now connected for data reception`
            });
        }
    }

    useEffect(()=>{
        if(!selectedDevice){
            setConnectedDeviceId(null);
        }
    },[selectedDevice])

    const openBTSettings = () => {
        Alert.alert('Bluetooth Disabled', 'Do you want to open settings to enable it?', [
            {
                text: 'CANCEL',
                onPress: () => console.log('Cancel Pressed'),
                style: 'cancel',
            },
            { text: 'OPEN SETTINGS', onPress: () => RNBluetoothClassic.openBluetoothSettings() },
        ]);
    }

    useEffect(() => {
        if (visible) {
            dispatch(getBondedDevices())
        }
        if (!isBluetoothEnabled && visible) {
            openBTSettings()
        }
    }, [isBluetoothEnabled, visible])

    const DeviceCard = React.memo(({ device, index }) => {
        const isConnected = connectedDeviceId === device.id;
        const isProcessing = tappedDeviceId === device.id && (connecting || disconnecting);
        
        const cardScale = useSharedValue(1);
        const cardOpacity = useSharedValue(0);
        const iconScale = useSharedValue(1);

        useEffect(() => {
            cardOpacity.value = withDelay(index * 100, withTiming(1, { duration: 300 }));
        }, [index]);

        // Icon animation for connection state visible
        useEffect(() => {
            if (isConnected) {
                iconScale.value = withSpring(1.1, { damping: 8, stiffness: 200 }, () => {
                    iconScale.value = withSpring(1, { damping: 15, stiffness: 150 });
                });
            } else {
                iconScale.value = withSpring(1, { damping: 15, stiffness: 150 });
            }
        }, [isConnected, iconScale]);

        const cardAnimatedStyle = useAnimatedStyle(() => {
            return {
                transform: [{ scale: cardScale.value }],
                opacity: cardOpacity.value,
            };
        }, [cardScale, cardOpacity]);

        const iconAnimatedStyle = useAnimatedStyle(() => {
            return {
                transform: [{ scale: iconScale.value }],
            };
        }, [iconScale]);

        const handlePress = () => {
            if (isProcessing) return;
            
            cardScale.value = withSpring(0.95, { damping: 10, stiffness: 200 }, () => {
                cardScale.value = withSpring(1, { damping: 15, stiffness: 150 });
            });
            toggleDeviceConnectionStatus(device);
        };

        const iconContainerStyle = {
            backgroundColor: isConnected 
                ? (highContrastMode ? '#ffffff' : 'rgba(255, 255, 255, 0.25)')
                : (highContrastMode ? '#000000' : '#eff6ff'),
            borderWidth: highContrastMode ? 1 : 0,
            borderColor: highContrastMode ? '#000000' : 'transparent',
        };

        const iconColor = isConnected 
            ? (highContrastMode ? '#000000' : '#ffffff')
            : (highContrastMode ? '#000000' : '#3b82f6');

        const chevronColor = isConnected
            ? (highContrastMode ? '#ffffff' : '#ffffff')
            : (highContrastMode ? '#ffffff' : '#9ca3af');

        return (
            <Reanimated.View style={cardAnimatedStyle}>
                <TouchableOpacity
                    onPress={handlePress}
                    disabled={isProcessing}
                    className="flex flex-row items-center p-3 rounded-xl mb-2 mx-2"
                    style={{
                        backgroundColor: isConnected 
                            ? (highContrastMode ? '#19a74d97' : '#16a34a') 
                            : (highContrastMode ? '#2c303d' : '#ffffff'),
                        borderWidth: isConnected ? 2 : 1,
                        borderColor: isConnected 
                            ? (highContrastMode ? '#000000' : '#16a34a')
                            : (highContrastMode ? '#000000' : '#e5e7eb'),
                        shadowColor: highContrastMode ? '#000000' : (isConnected ? '#16a34a' : '#000'),
                        shadowOffset: { width: 0, height: 1 },
                        shadowOpacity: highContrastMode ? 0.3 : (isConnected ? 0.2 : 0.05),
                        shadowRadius: 4,
                        elevation: highContrastMode ? 5 : (isConnected ? 3 : 1),
                    }}
                >
                    <View className="p-2 rounded-lg mr-3" style={iconContainerStyle}>
                        <Reanimated.View style={iconAnimatedStyle}>
                            <MaterialIcons
                                name={isConnected ? "bluetooth-connected" : "bluetooth"}
                                size={20}
                                color={iconColor}
                            />
                        </Reanimated.View>
                    </View>
                    
                    <View className="flex-1">
                        <Text 
                            className="font-avenirBold text-base" 
                            style={{ 
                                color: isConnected 
                                    ? (highContrastMode ? '#ffffff' : '#000000')
                                    : (highContrastMode ? '#ffffff' : '#1f2937')
                            }}
                            numberOfLines={1}
                        >
                            {device.name}
                        </Text>
                        <Text 
                            className="font-avenirMedium text-xs mt-0.5"
                            style={{ 
                                color: isConnected 
                                    ? (highContrastMode ? '#ffffff' : 'rgba(255, 255, 255, 0.9)')
                                    : (highContrastMode ? '#ffffff' : '#6b7280')
                            }}
                        >
                            {isConnected ? 'Connected' : 'Tap to connect'}
                        </Text>
                    </View>
                    
                    <View className="flex items-center justify-center ml-2">
                        {isProcessing ? (
                            <ActivityIndicator 
                                color={isConnected ? (highContrastMode ? '#ffffff' : '#ffffff') : (highContrastMode ? '#000000' : '#3b82f6')} 
                                size="small" 
                            />
                        ) : (
                            <Ionicon 
                                name={isConnected ? "checkmark-circle" : "chevron-forward"} 
                                size={20} 
                                color={chevronColor}
                            />
                        )}
                    </View>
                </TouchableOpacity>
            </Reanimated.View>
        );
    });

    return (
        <Modal
            visible={visible}
            transparent
            animationType="slide"
            onRequestClose={handleClose}
        >
            <View style={{ flex: 1, backgroundColor: 'rgba(0, 0, 0, 0.4)', justifyContent: 'flex-end' }}>
                <Reanimated.View style={modalAnimatedStyle}>
                    <View className='flex bg-white rounded-t-3xl' style={{
                        backgroundColor: highContrastMode ? '#070424' : '#ffffff',
                        borderTopLeftRadius: 24,
                        borderTopRightRadius: 24,
                    }}>
                        <View className='flex border-b bg-white rounded-t-3xl' style={{
                        borderColor: highContrastMode ? '#000000' : '#e5e7eb',
                        backgroundColor: highContrastMode ? '#070424' : '#ffffff',
                    }}>
                        <View className='flex-row justify-between items-center p-6 w-full'>
                            <View className="flex-1">
                                <Text className='font-avenirBold text-2xl' style={{
                                    color: highContrastMode ? '#ffffff' : '#1f2937'
                                }}>
                                    Bluetooth Devices
                                </Text>
                                <Text className='font-avenirMedium text-sm mt-1' style={{
                                    color: highContrastMode ? '#ffffff' : '#6b7280'
                                }}>
                                    {deviceList.length} device{deviceList.length !== 1 ? 's' : ''} found
                                </Text>
                            </View>

                            <View className='flex flex-row items-center gap-3'>
                                <TouchableOpacity
                                    onPress={async () => {
                                        if (!scanning) {
                                            if (await requestBluetoothPermissions()) {
                                                dispatch(discorverDevices())
                                            }
                                        }
                                    }}
                                    className="p-3 rounded-xl flex items-center justify-center"
                                    style={{
                                        opacity: 1,
                                        backgroundColor: scanning 
                                            ? (highContrastMode ? 'rgba(59,130,246,0.1)' : '#f3f4f6')
                                            : (highContrastMode ? 'rgba(59,130,246,0.1)' : '#eff6ff'),
                                        borderWidth: highContrastMode ? 1 : 0,
                                        borderColor: highContrastMode ? 'rgba(59,130,246,0.1)' : 'transparent',
                                    }}
                                    disabled={scanning}
                                >
                                    {scanning ? (
                                        <ActivityIndicator size="small" color={highContrastMode ? "#ffffff" : "#3b82f6"} />
                                    ) : (
                                        <MCommunityIcons name="magnify" size={20} color={highContrastMode ? "#1997ff" : "#3b82f6"} />
                                    )}
                                </TouchableOpacity>
                                
                                <TouchableOpacity
                                    onPress={() => {
                                        if (!isBluetoothEnabled) {
                                            openBTSettings()
                                        }
                                    }}
                                    className="p-3 rounded-xl flex items-center justify-center"
                                    style={{
                                        opacity: 1,
                                        backgroundColor: isBluetoothEnabled 
                                            ? (highContrastMode ? 'rgba(34,197,94,0.1)' : '#f0fdf4')
                                            : (highContrastMode ? '#fef2f2' : '#fef2f2'),
                                        borderWidth: highContrastMode ? 1 : 0,
                                        borderColor: highContrastMode ? 'rgba(34,197,94,0.1)' : 'transparent',
                                    }}
                                >
                                    <MCommunityIcons 
                                        name={isBluetoothEnabled ? "bluetooth" : "bluetooth-off"} 
                                        size={20} 
                                        color={isBluetoothEnabled 
                                            ? (highContrastMode ? 'rgb(0, 255, 94)' : '#16a34a')
                                            : (highContrastMode ? '#ff0000' : '#dc2626')
                                        } 
                                    />
                                </TouchableOpacity>
                            </View>
                        </View>
                    </View>
                        <Reanimated.View style={contentAnimatedStyle}>
                            <View className="px-6 py-2">
                                <ScrollView className='h-80' showsVerticalScrollIndicator={false}>
                                    {deviceList.length === 0 ? (
                                        <View className="flex items-center justify-center py-12">
                                            <MCommunityIcons name="bluetooth-off" size={48} color="#9ca3af" />
                                            <Text className="font-avenirMedium text-lg mt-4 text-center" style={highContrastMode ? {color: '#ffffff'} : {color: '#6b7280'}}>
                                                No devices found
                                            </Text>
                                            <Text className="font-avenirMedium text-sm mt-2 text-center" style={highContrastMode ? {color: '#ffffff'} : {color: '#6b7280'}}>
                                                Tap the scan button to discover devices
                                            </Text>
                                        </View>
                                    ) : (
                                        deviceList.map((device, idx) => (
                                            <DeviceCard 
                                                key={device.id} 
                                                device={device} 
                                                index={idx} 
                                            />
                                        ))
                                    )}
                                </ScrollView>
                            </View>
                        </Reanimated.View>
                        <View className='flex flex-row justify-center items-center border-t border-gray-200 m-2 p-1'>
                            <TouchableOpacity onPress={handleClose} className="p-3">
                                <Text style={[styles.buttonText, { color: '#3b82f6', fontWeight: '600' }]}>Close</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </Reanimated.View>
            </View>
        </Modal>
    )
}