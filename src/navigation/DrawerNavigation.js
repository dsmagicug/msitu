import {
  createDrawerNavigator,
  DrawerContentScrollView,
  DrawerItem,
} from '@react-navigation/drawer';
import Entypo from 'react-native-vector-icons/Entypo';
import Ionicon from 'react-native-vector-icons/Ionicons'
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import AntDesignIcon from 'react-native-vector-icons/AntDesign'
import AppStack from './AppStack';
import React, { useState, useEffect } from 'react';
import { Dimensions, View, Text, TouchableOpacity, ScrollView, StyleSheet } from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import Divider from '../components/utilities/Divider';
import NewProject from '../components/projects/NewProject';
import { setShowCreateNewProjects, setShowProjectList, setShowBTDevices } from "../store/modal"
import ProjectList from '../components/projects/ProjectList';
import BluetoothDevices from '../components/projects/BluetoothDevices';
import { scanDevices } from '../store/bluetooth';
const Drawer = createDrawerNavigator();

const useDrawerWidth = () => {
  const [drawerWidth, setDrawerWidth] = useState('50%');
  const [isPortrait, setIsPotrait] = useState(true);

  useEffect(() => {
    const updateDrawerWidth = () => {
      const { width, height } = Dimensions.get('window');
      const isPortrait = height >= width;
      setIsPotrait(isPortrait)
      setDrawerWidth(isPortrait ? '55%' : '30%');
    };
    const subscription = Dimensions.addEventListener('change', updateDrawerWidth);

    updateDrawerWidth();
    return () => subscription?.remove();
  }, []);

  return { drawerWidth, isPortrait };
};
function CustomDrawerContent({ navigation, isPortrait }) {
  const dispatch = useDispatch()
  return (
    <DrawerContentScrollView contentContainerStyle={{ flexGrow: 1 }}>
      {/* Close Button */}
      <View className="absolute top-2 right-2 z-10">
        <TouchableOpacity onPress={() => navigation.closeDrawer()}>
          <Ionicon name="caret-back-circle-outline" size={30} color="black" />
        </TouchableOpacity>
      </View>

      {/* Scrollable Content */}
      <ScrollView className="flex-1">
        {/* Projects Section */}
        <View className="flex flex-col">
          <View className="mx-3 mb-2">
            <Text className="uppercase font-avenirBold text-gray-500">Projects</Text>
          </View>
          <DrawerItem
            label="Projects"
            labelStyle={styles.label}
            icon={() => <AntDesignIcon name="folderopen" size={24} color="black" />}
            onPress={() => {
              dispatch(setShowProjectList(true))
            }}
          />
          <DrawerItem
            label="Import Projects"
            labelStyle={styles.label}
            icon={() => <Entypo name="download" size={24} color="black" />}
            onPress={() => {
              console.log("Nope");
            }}
          />
          <DrawerItem
            label="Export Projects"
            labelStyle={styles.label}
            icon={() => <MaterialCommunityIcons name="code-json" size={24} color="black" />}
            onPress={() => {
              console.log("Nope");
            }}
          />
          <Divider />
        </View>

        {/* GNSS Equipment Section */}
        <View className="flex flex-col">
          <View className="mx-3 mb-2">
            <Text className="uppercase font-avenirBold text-gray-500">GNSS EQUIPMENT</Text>
          </View>
          <DrawerItem
            label="Bluetooth"
            labelStyle={styles.label}
            icon={() => <Ionicon name="bluetooth" size={24} color="black" />}
            onPress={() => {
              dispatch(scanDevices())
              dispatch(setShowBTDevices(true))
            }}
          />
          <DrawerItem
            label="USB Serial"
            labelStyle={styles.label}
            icon={() => <MaterialCommunityIcons name="usb-port" size={24} color="black" />}
            onPress={() => {
              console.log("Nope");
            }}
          />
          <Divider />
        </View>
        {/* Misc Section */}
        {!isPortrait &&
          (<View className="flex">
            <View className="mx-3 mb-2 mt-5">
              <Text className="uppercase font-avenirBold text-gray-500">Misc.</Text>
            </View>
            <DrawerItem
              label="Settings"
              labelStyle={styles.label}
              icon={() => <Ionicon name="settings-outline" size={24} color="black" />}
              onPress={() => {
                console.log("Nope");
              }}
            />
            <DrawerItem
              label="About Msitu"
              labelStyle={styles.label}
              icon={() => <Ionicon name="information-circle-outline" size={24} color="black" />}
              onPress={() => {
                console.log("Nope");
              }}
            />
          </View>)}
      </ScrollView>
      {isPortrait && (
        <View className='flex absolute bottom-0 w-full border h-auto border-b-0 border-r-0 border-t-gray-300 my-2'>
          <View className='flex flex-col'>
            <View className='mx-3 mb-2 mt-5'>
              <Text className="uppercase font-avenirBold text-gray-500">MIsc.</Text>
            </View>
            <DrawerItem
              label="Settings"
              labelStyle={styles.label}
              icon={() => <Ionicon name="settings-outline" size={24} color="black" />}
              onPress={() => {
                console.log("Nope");
              }}
            />
            <DrawerItem
              label="About Msitu"
              labelStyle={styles.label}
              icon={() => <Ionicon name="information-circle-outline" size={24} color="black" />}
              onPress={() => {
                console.log("Nope");
              }}
            />
          </View>
        </View>
      )}
    </DrawerContentScrollView>
  );
}

export default function DrawerNavigation() {
  const { drawerWidth, isPortrait } = useDrawerWidth();
  const modalStore = useSelector(selector => selector.modals);
  const dispatch = useDispatch()
  return (
    <>
      <Drawer.Navigator
        drawerContent={props => <CustomDrawerContent {...props} isPortrait={isPortrait} />}>
        <Drawer.Screen
          options={{
            headerShown: false,
            overlayColor: 'transparent',
            drawerStyle: {
              marginTop: 35,
              width: drawerWidth,
            },
          }}
          name="Drawer"
          component={AppStack}
        />
      </Drawer.Navigator>
      <NewProject
        onClose={() => dispatch(setShowCreateNewProjects(false))}
        show={modalStore.showCreateNewProjects} />
        

      <ProjectList
        show={modalStore.showProjectList}
        onClose={() => dispatch(setShowProjectList(false))}
      />
      <BluetoothDevices
         show={modalStore.showBTDevices}
         onClose={() => dispatch(setShowBTDevices(false))}
      />
    </>
  );
}
const styles = StyleSheet.create({
  label: { fontSize: 14, color: 'black', fontFamily: "AvenirMeduim" }
});
