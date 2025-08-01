import {
    View,
    Text,
    TouchableOpacity,
    ActivityIndicator,
    Alert,
    Dimensions
} from 'react-native'
import React, { useEffect, useState } from 'react'
import { BottomModal, ModalFooter, ModalButton, ModalContent } from 'react-native-modals';
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
    withDelay,
    interpolate,
    Extrapolation
} from 'react-native-reanimated';

const { width } = Dimensions.get('window');

export default function BluetoothDevices({ children, show, onClose }) {

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
    const modalOpacity = useSharedValue(0);
    const contentTranslateY = useSharedValue(50);

    useEffect(() => {
        if (show) {
            modalScale.value = withSpring(1, { damping: 15, stiffness: 150 });
            modalOpacity.value = withTiming(1, { duration: 300 });
            contentTranslateY.value = withDelay(100, withSpring(0, { damping: 15, stiffness: 150 }));
        } else {
            modalScale.value = withSpring(0.8, { damping: 15, stiffness: 150 });
            modalOpacity.value = withTiming(0, { duration: 200 });
            contentTranslateY.value = withSpring(50, { damping: 15, stiffness: 150 });
        }
    }, [show]);

    const modalAnimatedStyle = useAnimatedStyle(() => {
        return {
            transform: [{ scale: modalScale.value }],
            opacity: modalOpacity.value,
        };
    });

    const contentAnimatedStyle = useAnimatedStyle(() => {
        return {
            transform: [{ translateY: contentTranslateY.value }],
        };
    });

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
        if (show) {
            dispatch(getBondedDevices())
        }
        if (!isBluetoothEnabled && show) {
            openBTSettings()
        }
    }, [isBluetoothEnabled, show])

    const DeviceCard = React.memo(({ device, index }) => {
        const isConnected = connectedDeviceId === device.id;
        const isProcessing = tappedDeviceId === device.id && (connecting || disconnecting);
        
        const cardScale = useSharedValue(1);
        const cardOpacity = useSharedValue(0);
        const backgroundColor = useSharedValue(0);
        const borderColor = useSharedValue(0);
        const iconScale = useSharedValue(1);
        const textColor = useSharedValue(0);
        const subtitleColor = useSharedValue(0);

        useEffect(() => {
            cardOpacity.value = withDelay(index * 100, withTiming(1, { duration: 300 }));
        }, [index]);

        // Smooth transitions for connection state changes
        useEffect(() => {
            backgroundColor.value = withTiming(isConnected ? 1 : 0, { duration: 400 });
            borderColor.value = withTiming(isConnected ? 1 : 0, { duration: 400 });
            textColor.value = withTiming(isConnected ? 1 : 0, { duration: 400 });
            subtitleColor.value = withTiming(isConnected ? 1 : 0, { duration: 400 });
        }, [isConnected, backgroundColor, borderColor, textColor, subtitleColor]);

        // Icon animation for connection state
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
        });

        const backgroundAnimatedStyle = useAnimatedStyle(() => {
            const bgColor = interpolate(
                backgroundColor.value,
                [0, 1],
                [highContrastMode ? '#ffffff' : '#ffffff', highContrastMode ? '#000000' : '#16a34a']
            );
            const borderColorValue = interpolate(
                borderColor.value,
                [0, 1],
                [highContrastMode ? '#000000' : '#e5e7eb', highContrastMode ? '#000000' : '#16a34a']
            );
            
            return {
                backgroundColor: bgColor,
                borderColor: borderColorValue,
                borderWidth: highContrastMode ? 2 : 1,
                shadowColor: highContrastMode ? '#000000' : (isConnected ? '#16a34a' : '#000'),
                shadowOffset: { width: 0, height: 1 },
                shadowOpacity: highContrastMode ? 0.3 : (isConnected ? 0.2 : 0.05),
                shadowRadius: 4,
                elevation: highContrastMode ? 5 : (isConnected ? 3 : 1),
            };
        });

        const iconAnimatedStyle = useAnimatedStyle(() => {
            return {
                transform: [{ scale: iconScale.value }],
            };
        });

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

        const textColorValue = interpolate(
            textColor.value,
            [0, 1],
            [highContrastMode ? '#000000' : '#1f2937', highContrastMode ? '#ffffff' : '#ffffff']
        );

        const subtitleColorValue = interpolate(
            subtitleColor.value,
            [0, 1],
            [highContrastMode ? '#000000' : '#6b7280', highContrastMode ? '#ffffff' : 'rgba(255, 255, 255, 0.9)']
        );

        const chevronColor = isConnected 
            ? (highContrastMode ? '#ffffff' : '#ffffff')
            : (highContrastMode ? '#000000' : '#9ca3af');

        return (
            <Reanimated.View style={cardAnimatedStyle}>
                <TouchableOpacity
                    onPress={handlePress}
                    disabled={isProcessing}
                    className="flex flex-row items-center p-3 rounded-xl mb-2 mx-2"
                    style={{
                        backgroundColor: isConnected 
                            ? (highContrastMode ? '#000000' : '#16a34a') 
                            : (highContrastMode ? '#ffffff' : '#ffffff'),
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
                        <MaterialIcons 
                            name={isConnected ? "bluetooth-connected" : "bluetooth"} 
                            size={20} 
                            color={iconColor}
                            style={iconAnimatedStyle}
                        />
                    </View>
                    
                    <View className="flex-1">
                        <Text 
                            className="font-avenirBold text-base" 
                            style={{ 
                                color: isConnected 
                                    ? (highContrastMode ? '#ffffff' : '#ffffff')
                                    : (highContrastMode ? '#000000' : '#1f2937')
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
                                    : (highContrastMode ? '#000000' : '#6b7280')
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
        <BottomModal
            visible={show}
            onTouchOutside={onClose}
            modalTitle={
                <Reanimated.View style={modalAnimatedStyle}>
                    <View className='flex border-b bg-white rounded-t-3xl' style={{
                        borderColor: highContrastMode ? '#000000' : '#e5e7eb',
                        backgroundColor: highContrastMode ? '#ffffff' : '#ffffff',
                    }}>
                        <View className='flex-row justify-between items-center p-6 w-full'>
                            <View className="flex-1">
                                <Text className='font-avenirBold text-2xl' style={{
                                    color: highContrastMode ? '#000000' : '#1f2937'
                                }}>
                                    Bluetooth Devices
                                </Text>
                                <Text className='font-avenirMedium text-sm mt-1' style={{
                                    color: highContrastMode ? '#000000' : '#6b7280'
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
                                        backgroundColor: scanning 
                                            ? (highContrastMode ? '#000000' : '#f3f4f6')
                                            : (highContrastMode ? '#000000' : '#eff6ff'),
                                        borderWidth: highContrastMode ? 1 : 0,
                                        borderColor: highContrastMode ? '#000000' : 'transparent',
                                    }}
                                    disabled={scanning}
                                >
                                    {scanning ? (
                                        <ActivityIndicator size="small" color={highContrastMode ? "#ffffff" : "#3b82f6"} />
                                    ) : (
                                        <MCommunityIcons name="magnify" size={20} color={highContrastMode ? "#ffffff" : "#3b82f6"} />
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
                                        backgroundColor: isBluetoothEnabled 
                                            ? (highContrastMode ? '#000000' : '#f0fdf4')
                                            : (highContrastMode ? '#000000' : '#fef2f2'),
                                        borderWidth: highContrastMode ? 1 : 0,
                                        borderColor: highContrastMode ? '#000000' : 'transparent',
                                    }}
                                >
                                    <MCommunityIcons 
                                        name={isBluetoothEnabled ? "bluetooth" : "bluetooth-off"} 
                                        size={20} 
                                        color={isBluetoothEnabled 
                                            ? (highContrastMode ? '#ffffff' : '#16a34a')
                                            : (highContrastMode ? '#ffffff' : '#dc2626')
                                        } 
                                    />
                                </TouchableOpacity>
                            </View>
                        </View>
                    </View>
                </Reanimated.View>
            }
            footer={
                <ModalFooter>
                    <ModalButton
                        text="Close"
                        textStyle={[styles.buttonText, { color: '#3b82f6', fontWeight: '600' }]}
                        onPress={() => { onClose() }}
                    />
                </ModalFooter>
            }
        >
            <Reanimated.View style={contentAnimatedStyle}>
                <ModalContent>
                    <ScrollView className='h-80' showsVerticalScrollIndicator={false}>
                        {deviceList.length === 0 ? (
                            <View className="flex items-center justify-center py-12">
                                <MCommunityIcons name="bluetooth-off" size={48} color="#9ca3af" />
                                <Text className="font-avenirMedium text-gray-500 text-lg mt-4 text-center">
                                    No devices found
                                </Text>
                                <Text className="font-avenirMedium text-gray-400 text-sm mt-2 text-center">
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
                </ModalContent>
            </Reanimated.View>
        </BottomModal>
    )
}