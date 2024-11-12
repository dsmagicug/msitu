import * as React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import DrawerNavigation from './DrawerNavigation';
import { useDispatch } from 'react-redux';
import { getConnectedDevices, setIsBTEnabled, setSelectedDevice } from '../store/bluetooth';
import RNBluetoothClassic from 'react-native-bluetooth-classic';
import Toast from 'react-native-toast-message';

const Stack = createNativeStackNavigator();

export default function AppStackNavigation() {

    const dispatch = useDispatch();
    const [btStateUpdateListener, setBTStateUpdateListener] = React.useState(null);
    const [deviceDisconnectedListener, setDeviceDisconnectedListener] =  React.useState(null);
  

    const stateChanged = (event) => {
        dispatch(setIsBTEnabled(event.enabled));
    }


    const handleDeviceDisconnected = (device)=>{
        Toast.show({
            type:"error", // could be warning really but.. these suckers didnt add it.
            text1:"Ooops!",
            text2:`BT Device ${device.name} has disconnected.`
        })
        dispatch(setSelectedDevice(null))
    }


    React.useEffect(() => {

        dispatch(getConnectedDevices())// fetch connected devices
        const btStateUpdate = RNBluetoothClassic.onStateChanged(stateChanged);
        setBTStateUpdateListener(btStateUpdate);

        // @ts-ignore thet return a BluetoothDevice instead
        const deviceDisconnected = RNBluetoothClassic.onDeviceDisconnected(handleDeviceDisconnected);
        setDeviceDisconnectedListener(deviceDisconnected)


        return () => {
            // cleanup 
            if (btStateUpdateListener) {
                btStateUpdateListener.remove();
                setBTStateUpdateListener(null);
            }

            if(deviceDisconnectedListener){
                deviceDisconnectedListener.remove();
                setDeviceDisconnectedListener(null);
            }

        }

    }, [])
    return (
        <NavigationContainer>
            <Stack.Navigator>
                <Stack.Screen
                    name="DrawerStack"
                    component={DrawerNavigation}
                    options={{ headerShown: false }}
                />
            </Stack.Navigator>
        </NavigationContainer>
    );
}
