import { 
    View, 
    Text, 
    TouchableOpacity, 
    ActivityIndicator, NativeModules, NativeEventEmitter, Platform  } from 'react-native'
import React, { useEffect, useState } from 'react'
import { BottomModal, ModalFooter, ModalButton, ModalContent } from 'react-native-modals';
import MCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import styles from '../../assets/styles';
import { useDispatch, useSelector } from 'react-redux';
import { ScrollView } from 'react-native-gesture-handler';
import BleManager from 'react-native-ble-manager';

export default function BluetoothDevices({ children, show, onClose }) {

    const [isBluetoothEnabled, setIsBluetoothEnabled] = useState(false);
      
    
    const dispatch = useDispatch()

    useEffect(() => {
        if (show) {
        // Set up the event emitter
        const bleManagerEmitter = new NativeEventEmitter(NativeModules.BleManager);
    
        // Event listener for Bluetooth state update
        const handleUpdateState = (state) => {
          // Check if Bluetooth is on or off
          setIsBluetoothEnabled(state === 'on');
        };
    
        // Add listener for Bluetooth state changes
        const stateListener = bleManagerEmitter.addListener(
          'BleManagerDidUpdateState',
          ({ state }) => handleUpdateState(state)
        );
    
        // Trigger a Bluetooth state check
        BleManager.checkState();
    
        return () => {
          stateListener.remove();
        };
            
        }
    }, [show])
    return (
        <BottomModal
            visible={show}
            modalTitle={
                <View className='flex border-b-hairline border-b-gray-300'>
                    <View className='flex-row justify-between items-center p-2 w-full' >
                        <Text className='font-avenirBold text-black text-2xl'>BT Devices</Text>
                        <TouchableOpacity
                        className='p-4 rounded-lg'>
                            {
                                isBluetoothEnabled ?
                                <MCommunityIcons name="bluetooth" size={24} color="green" />
                                :
                                <MCommunityIcons name="bluetooth-off" size={24} color="red" />
                            }
                        
                    </TouchableOpacity>
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
                    
                </ScrollView>
            </ModalContent>
        </BottomModal>
    )
}