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
import { Dimensions, View, Text, TouchableOpacity, ScrollView, StyleSheet} from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import Divider from '../components/utilities/Divider';
import { setShowProjectList, setShowBTDevices } from "../store/modal"
import ProjectList from '../components/projects/ProjectList';
import BluetoothDevices from '../components/projects/BluetoothDevices';
import Reanimated, { 
  useSharedValue, 
  useAnimatedStyle, 
  withSpring,
  withTiming,
  interpolate,
  Extrapolation,
  withDelay
} from 'react-native-reanimated';

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

const AnimatedDrawerItem = ({ label, icon, onPress, delay = 0 }) => {
  const scaleValue = useSharedValue(0);
  const opacityValue = useSharedValue(0);

  React.useEffect(() => {
    scaleValue.value = withDelay(delay, withSpring(1, { damping: 15, stiffness: 150 }));
    opacityValue.value = withDelay(delay, withTiming(1, { duration: 300 }));
  }, []);

  const animatedStyle = useAnimatedStyle(() => {
    return {
      transform: [{ scale: scaleValue.value }],
      opacity: opacityValue.value,
    };
  });

  const handlePress = () => {
    scaleValue.value = withSpring(0.95, { damping: 10, stiffness: 200 }, () => {
      scaleValue.value = withSpring(1, { damping: 15, stiffness: 150 });
    });
    onPress();
  };

  return (
    <Reanimated.View style={animatedStyle}>
      <TouchableOpacity
        className="flex flex-row items-center p-4 mx-2 rounded-xl mb-1"
        onPress={handlePress}
        style={{
          backgroundColor: 'rgba(59, 130, 246, 0.05)',
          borderWidth: 1,
          borderColor: 'rgba(59, 130, 246, 0.1)',
        }}
      >
        <View className="mr-3 p-2 rounded-lg" style={{ backgroundColor: 'rgba(59, 130, 246, 0.1)' }}>
          {icon}
        </View>
        <Text className="font-avenirMedium text-gray-800 text-base">{label}</Text>
      </TouchableOpacity>
    </Reanimated.View>
  );
};

function CustomDrawerContent({ navigation, isPortrait }) {
  const dispatch = useDispatch()
  const opacityValue = useSharedValue(0);

  React.useEffect(() => {
    opacityValue.value = withTiming(1, { duration: 500 });
  }, []);

  const animatedStyle = useAnimatedStyle(() => {
    return {
      opacity: opacityValue.value,
    };
  });

  return (
    <Reanimated.View style={[{ flexGrow: 1 }, animatedStyle]}>
      <DrawerContentScrollView contentContainerStyle={{ flexGrow: 1 }}>
        <View className="absolute top-2 right-2 z-10">
          <TouchableOpacity 
            onPress={() => navigation.closeDrawer()}
            className="p-2 rounded-full"
            style={{ backgroundColor: 'rgba(239, 68, 68, 0.1)' }}
          >
            <Ionicon name="caret-back-circle-outline" size={30} color="#ef4444" />
          </TouchableOpacity>
        </View>

        <ScrollView className="flex-1 mt-12">
          <View className="flex flex-col">
            <View className="mx-3 mb-4">
              <Text className="uppercase font-avenirBold text-gray-500 text-sm tracking-wider">Projects</Text>
            </View>
            <AnimatedDrawerItem
              label="Projects"
              icon={<AntDesignIcon name="folderopen" size={24} color="#3b82f6" />}
              onPress={() => dispatch(setShowProjectList(true))}
              delay={100}
            />
            <AnimatedDrawerItem
              label="Import Projects"
              icon={<Entypo name="download" size={24} color="#3b82f6" />}
              onPress={() => console.log("Nope")}
              delay={200}
            />
            <AnimatedDrawerItem
              label="Export Projects"
              icon={<MaterialCommunityIcons name="code-json" size={24} color="#3b82f6" />}
              onPress={() => console.log("Nope")}
              delay={300}
            />
            <Divider />
          </View>

          <View className="flex flex-col">
            <View className="mx-3 mb-4">
              <Text className="uppercase font-avenirBold text-gray-500 text-sm tracking-wider">GNSS EQUIPMENT</Text>
            </View>
            <AnimatedDrawerItem
              label="Bluetooth"
              icon={<Ionicon name="bluetooth" size={24} color="#3b82f6" />}
              onPress={() => dispatch(setShowBTDevices(true))}
              delay={400}
            />
            <AnimatedDrawerItem
              label="USB Serial"
              icon={<MaterialCommunityIcons name="usb-port" size={24} color="#3b82f6" />}
              onPress={() => console.log("Nope")}
              delay={500}
            />
            <Divider />
          </View>
          
          {!isPortrait && (
            <View className="flex">
              <View className="mx-3 mb-4 mt-5">
                <Text className="uppercase font-avenirBold text-gray-500 text-sm tracking-wider">Misc.</Text>
              </View>
              <AnimatedDrawerItem
                label="Settings"
                icon={<Ionicon name="settings-outline" size={24} color="#3b82f6" />}
                onPress={() => console.log("Nope")}
                delay={600}
              />
              <AnimatedDrawerItem
                label="About Msitu"
                icon={<Ionicon name="information-circle-outline" size={24} color="#3b82f6" />}
                onPress={() => console.log("Nope")}
                delay={700}
              />
            </View>
          )}
        </ScrollView>
        
        {isPortrait && (
          <View className='flex absolute bottom-0 w-full border h-auto border-b-0 border-r-0 border-t-gray-300 my-2'>
            <View className='flex flex-col'>
              <View className='mx-3 mb-4 mt-5'>
                <Text className="uppercase font-avenirBold text-gray-500 text-sm tracking-wider">Misc.</Text>
              </View>
              <AnimatedDrawerItem
                label="Settings"
                icon={<Ionicon name="settings-outline" size={24} color="#3b82f6" />}
                onPress={() => navigation.navigate("Settings")}
                delay={600}
              />
              <AnimatedDrawerItem
                label="About Msitu"
                icon={<Ionicon name="information-circle-outline" size={24} color="#3b82f6" />}
                onPress={() => console.log("Nope")}
                delay={700}
              />
            </View>
          </View>
        )}
      </DrawerContentScrollView>
    </Reanimated.View>
  );
}

export default function DrawerNavigation(props) {
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
            overlayColor: 'rgba(0, 0, 0, 0.5)',
            drawerStyle: {
              marginTop: 35,
              width: drawerWidth,
              backgroundColor: 'rgba(255, 255, 255, 0.98)',
              borderTopRightRadius: 20,
              borderBottomRightRadius: 20,
              shadowColor: '#000',
              shadowOffset: { width: 2, height: 0 },
              shadowOpacity: 0.1,
              shadowRadius: 10,
              elevation: 10,
            },
          }}
          name="Drawer"
          component={AppStack}
        />
      </Drawer.Navigator>
        

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
