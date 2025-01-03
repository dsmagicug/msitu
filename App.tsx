import "./global.css"

import React, { useEffect, useState } from 'react';
import { StatusBar } from 'react-native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { ModalPortal } from 'react-native-modals';
import { AlertNotificationRoot } from 'react-native-alert-notification';
import AppNavigation from './src/navigation/AppNavigation.js';
import { Provider } from 'react-redux';
import { store } from './src/store/store.js';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { PaperProvider } from 'react-native-paper';
import Toast from 'react-native-toast-message';
import RNBluetoothClassic, {BluetoothDeviceEvent,  BluetoothEventSubscription} from 'react-native-bluetooth-classic'

function App(): React.JSX.Element {

  const [btGenericErrorListener, setBTGenericErrorListener] =  useState<BluetoothEventSubscription | null>(null);
  

  const handleOnError = (event:BluetoothDeviceEvent) => {
    Toast.show({
        type:"error",
        text1:"Bluetooth Error",
        text2:"A generic BT error just occured"
        
    })
}

  useEffect(()=>{
            const onBtError = RNBluetoothClassic.onError(handleOnError);
            setBTGenericErrorListener(onBtError);
            return () => {
              // cleanup 
              if(btGenericErrorListener){
                  btGenericErrorListener.remove();
                  setBTGenericErrorListener(null);
              }
          }
  },[])
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <StatusBar animated translucent backgroundColor="transparent" barStyle="dark-content" />
        <Provider store={store}>
          
            <PaperProvider>
            <AlertNotificationRoot>
             
              <AppNavigation />
              </AlertNotificationRoot>
            </PaperProvider>
            <Toast />
          <ModalPortal />
        </Provider>
      </SafeAreaProvider>

    </GestureHandlerRootView>
  );
}

export default App;