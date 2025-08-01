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
import { setShowProjectList, setShowBTDevices, setShowAboutMsitu } from "../store/modal"
import ProjectList from '../components/projects/ProjectList';
import BluetoothDevices from '../components/projects/BluetoothDevices';
import AboutMsituModal from '../components/misc/AboutMsituModal';
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
      setDrawerWidth(isPortrait ? '65%' : '35%');
    };
    const subscription = Dimensions.addEventListener('change', updateDrawerWidth);

    updateDrawerWidth();
    return () => subscription?.remove();
  }, []);

  return { drawerWidth, isPortrait };
};

const AnimatedDrawerItem = ({ label, icon, onPress, delay = 0, description = null, badge = null, highContrastMode = false }) => {
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

  const cardStyle = {
    backgroundColor: highContrastMode ? 'rgba(255, 255, 255, 0.95)' : 'rgba(255, 255, 255, 0.8)',
    borderWidth: highContrastMode ? 2 : 1,
    borderColor: highContrastMode ? '#000000' : 'rgba(59, 130, 246, 0.1)',
    shadowColor: highContrastMode ? '#000000' : '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: highContrastMode ? 0.1 : 0.05,
    shadowRadius: 8,
    elevation: highContrastMode ? 4 : 2,
  };

  const iconStyle = {
    backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(59, 130, 246, 0.1)',
    borderWidth: highContrastMode ? 1 : 0,
    borderColor: highContrastMode ? '#000000' : 'transparent',
  };

  const labelStyle = {
    color: highContrastMode ? '#000000' : '#1f2937',
    fontWeight: highContrastMode ? 'bold' : 'normal',
  };

  const descriptionStyle = {
    color: highContrastMode ? '#000000' : '#6b7280',
    fontWeight: highContrastMode ? '600' : 'normal',
  };

  return (
    <Reanimated.View style={animatedStyle}>
      <TouchableOpacity
        className="flex flex-row items-center p-5 mx-3 rounded-2xl mb-2"
        onPress={handlePress}
        style={cardStyle}
      >
        <View className="mr-3 p-1.5 rounded-md" style={iconStyle}>
          {icon}
        </View>
        <View className="flex-1">
          <Text className="font-avenirBold text-base" style={labelStyle}>{label}</Text>
          {description && (
            <Text className="font-avenirMedium text-xs mt-1" style={descriptionStyle}>{description}</Text>
          )}
        </View>
        {badge && (
          <View className="px-2 py-1 rounded-full" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.2)' : 'rgba(239, 68, 68, 0.1)' }}>
            <Text className="font-avenirBold text-xs" style={{ color: highContrastMode ? '#000000' : '#ef4444' }}>{badge}</Text>
          </View>
        )}
        <Ionicon name="chevron-forward" size={16} color={highContrastMode ? "#000000" : "#9ca3af"} />
      </TouchableOpacity>
    </Reanimated.View>
  );
};

const DrawerSection = ({ title, children, delay = 0, highContrastMode = false }) => {
  const opacityValue = useSharedValue(0);
  const translateYValue = useSharedValue(20);

  React.useEffect(() => {
    opacityValue.value = withDelay(delay, withTiming(1, { duration: 400 }));
    translateYValue.value = withDelay(delay, withSpring(0, { damping: 15, stiffness: 150 }));
  }, []);

  const animatedStyle = useAnimatedStyle(() => {
    return {
      opacity: opacityValue.value,
      transform: [{ translateY: translateYValue.value }],
    };
  });

  const titleStyle = {
    color: highContrastMode ? '#000000' : '#6b7280',
    fontWeight: highContrastMode ? 'bold' : 'normal',
  };

  return (
    <Reanimated.View style={animatedStyle}>
      <View className="mx-3 mb-3 mt-6">
        <Text className="font-avenirBold text-xs tracking-widest uppercase" style={titleStyle}>{title}</Text>
      </View>
      {children}
    </Reanimated.View>
  );
};

function CustomDrawerContent({ navigation, isPortrait }) {
  const dispatch = useDispatch()
  const opacityValue = useSharedValue(0);
  const scaleValue = useSharedValue(0.9);
  
  // Get project count from store
  const { projects } = useSelector(store => store.project);
  // Get high contrast mode from store
  // @ts-ignore
  const { settings } = useSelector(store => store.settings);
  const highContrastMode = settings?.highContrastMode || false;

  React.useEffect(() => {
    opacityValue.value = withTiming(1, { duration: 600 });
    scaleValue.value = withSpring(1, { damping: 15, stiffness: 150 });
  }, []);

  const animatedStyle = useAnimatedStyle(() => {
    return {
      opacity: opacityValue.value,
      transform: [{ scale: scaleValue.value }],
    };
  });

  // High contrast styles
  const headerStyle = {
    backgroundColor: highContrastMode ? 'rgba(255, 255, 255, 0.98)' : 'rgba(248, 250, 252, 0.98)',
    borderBottomWidth: highContrastMode ? 2 : 0,
    borderBottomColor: highContrastMode ? '#000000' : 'transparent',
  };

  const titleStyle = {
    color: highContrastMode ? '#000000' : '#1f2937',
    fontWeight: highContrastMode ? 'bold' : 'normal',
  };

  const subtitleStyle = {
    color: highContrastMode ? '#000000' : '#6b7280',
    fontWeight: highContrastMode ? '600' : 'normal',
  };

  const versionStyle = {
    color: highContrastMode ? '#000000' : '#9ca3af',
    fontWeight: highContrastMode ? '600' : 'normal',
  };

  return (
    <Reanimated.View style={[{ flexGrow: 1 }, animatedStyle]}>
      <DrawerContentScrollView contentContainerStyle={{ flexGrow: 1, paddingTop: 0 }}>
        {/* Fixed Header */}
        <View className="px-4 pt-8 pb-4" style={[{ position: 'sticky', top: 0, zIndex: 10 }, headerStyle]}>
          <View className="flex flex-row items-center justify-between mb-3">
            <View className="flex-1">
              <Text className="font-avenirBold text-2xl" style={titleStyle}>Msitu</Text>
              <Text className="font-avenirMedium text-sm" style={subtitleStyle}>Survey & Planting</Text>
              <Text className="font-avenirMedium text-xs mt-1" style={versionStyle}>Version 1.0.0</Text>
            </View>
            <TouchableOpacity 
              onPress={() => navigation.closeDrawer()}
              className="p-2 rounded-full"
              style={{ 
                backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                borderWidth: highContrastMode ? 1 : 0,
                borderColor: highContrastMode ? '#000000' : 'transparent',
              }}
            >
              <Ionicon name="close" size={24} color={highContrastMode ? "#000000" : "#ef4444"} />
            </TouchableOpacity>
          </View>
          
          {/* Project Count */}
          <View className="flex flex-row gap-3">
            <View className="flex-1 p-4 rounded-xl" style={{ 
              backgroundColor: highContrastMode ? 'rgba(255, 255, 255, 0.95)' : 'rgba(34, 197, 94, 0.1)',
              borderWidth: highContrastMode ? 2 : 0,
              borderColor: highContrastMode ? '#000000' : 'transparent',
            }}>
              <Text className="font-avenirBold text-2xl" style={{ 
                color: highContrastMode ? '#000000' : '#16a34a',
                fontWeight: highContrastMode ? 'bold' : 'normal',
              }}>{projects?.length || 0}</Text>
              <Text className="font-avenirMedium text-sm" style={{ 
                color: highContrastMode ? '#000000' : '#16a34a',
                fontWeight: highContrastMode ? '600' : 'normal',
              }}>Projects</Text>
            </View>
          </View>
        </View>

        <ScrollView className="flex-1 px-2" showsVerticalScrollIndicator={false}>
          {/* Projects Section */}
          <DrawerSection title="Project Management" delay={100} highContrastMode={highContrastMode}>
            <AnimatedDrawerItem
              label="My Projects"
              description="View and manage your projects"
              icon={<AntDesignIcon name="folderopen" size={24} color="#3b82f6" />}
              onPress={() => dispatch(setShowProjectList(true))}
              delay={200}
              highContrastMode={highContrastMode}
            />
            <AnimatedDrawerItem
              label="Import Projects"
              description="Import from cloud or local files"
              icon={<Entypo name="download" size={24} color="#3b82f6" />}
              onPress={() => console.log("Import Projects")}
              delay={300}
              highContrastMode={highContrastMode}
            />
            <AnimatedDrawerItem
              label="Export Projects"
              description="Share and backup your data"
              icon={<MaterialCommunityIcons name="code-json" size={24} color="#3b82f6" />}
              onPress={() => console.log("Export Projects")}
              delay={400}
              highContrastMode={highContrastMode}
            />
          </DrawerSection>

          {/* Equipment Section */}
          <DrawerSection title="GNSS Equipment" delay={500} highContrastMode={highContrastMode}>
            <AnimatedDrawerItem
              label="Bluetooth Devices"
              description="Connect to GNSS receivers"
              icon={<Ionicon name="bluetooth" size={24} color="#3b82f6" />}
              onPress={() => dispatch(setShowBTDevices(true))}
              delay={600}
              highContrastMode={highContrastMode}
            />
            <AnimatedDrawerItem
              label="USB Serial"
              description="Direct USB connections"
              icon={<MaterialCommunityIcons name="usb-port" size={24} color="#3b82f6" />}
              onPress={() => console.log("USB Serial")}
              delay={700}
              highContrastMode={highContrastMode}
            />
          </DrawerSection>
          
          {/* Settings Section */}
          <DrawerSection title="Application" delay={800} highContrastMode={highContrastMode}>
            <AnimatedDrawerItem
              label="Settings"
              description="Configure app preferences"
              icon={<Ionicon name="settings-outline" size={24} color="#3b82f6" />}
              onPress={() => {
                navigation.closeDrawer();
                navigation.navigate("Settings");
              }}
              delay={900}
              highContrastMode={highContrastMode}
            />
            <AnimatedDrawerItem
              label="About Msitu"
              description="Version and information"
              icon={<Ionicon name="information-circle-outline" size={24} color="#3b82f6" />}
              onPress={() => dispatch(setShowAboutMsitu(true))}
              delay={1000}
              highContrastMode={highContrastMode}
            />
          </DrawerSection>
        </ScrollView>
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
            overlayColor: 'rgba(0, 0, 0, 0.4)',
            drawerStyle: {
              marginTop: 50,
              marginBottom: 0,
              width: drawerWidth,
              backgroundColor: 'rgba(248, 250, 252, 0.98)',
              borderTopRightRadius: 0,
              borderBottomRightRadius: 0,
              shadowColor: '#000',
              shadowOffset: { width: 4, height: 0 },
              shadowOpacity: 0.15,
              shadowRadius: 20,
              elevation: 15,
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
      <AboutMsituModal
        visible={modalStore.showAboutMsitu}
        onClose={() => dispatch(setShowAboutMsitu(false))}
      />
    </>
  );
}
const styles = StyleSheet.create({
  label: { fontSize: 14, color: 'black', fontFamily: "AvenirMeduim" }
});
