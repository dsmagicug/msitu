import {
  createDrawerNavigator,
  DrawerContentScrollView,
} from '@react-navigation/drawer';
import Entypo from 'react-native-vector-icons/Entypo';
import Ionicon from 'react-native-vector-icons/Ionicons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import AppStack from './AppStack';
import React, { useState, useEffect } from 'react';
import { Dimensions, View, Text, TouchableOpacity, ScrollView, StyleSheet} from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import { setShowProjectList, setShowBTDevices, setShowAboutMsitu, setShowProjectExport } from '../store/modal';
import ProjectList from '../components/projects/ProjectList';
import BluetoothDevices from '../components/projects/BluetoothDevices';
import AboutMsituModal from '../components/misc/AboutMsituModal';
import ProjectExportModal from '../components/projects/ProjectExportModal';
import { APP_VERSION, APP_NAME, APP_SUBTITLE, getBuildInfo } from '../config/version';
import { DRAWER_MENUS } from './menus';
import Reanimated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
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

const AnimatedDrawerItem = ({ label, icon, onPress, delay = 0, description = null, badge = null, highContrastMode = false, disabled = false }) => {
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
    if (disabled) return;

    scaleValue.value = withSpring(0.95, { damping: 10, stiffness: 200 }, () => {
      scaleValue.value = withSpring(1, { damping: 15, stiffness: 150 });
    });
    onPress();
  };

  const cardStyle = {
    backgroundColor: disabled
      ? (highContrastMode ? 'rgba(200, 200, 200, 0.9)' : 'rgba(245, 245, 245, 0.9)')
      : (highContrastMode ? 'rgba(255, 255, 255, 0.9)' : 'rgba(255, 255, 255, 0.9)'),
    borderWidth: highContrastMode ? 1 : 0,
    borderColor: disabled
      ? (highContrastMode ? '#666666' : 'rgba(156, 163, 175, 0.2)')
      : (highContrastMode ? '#000000' : 'transparent'),
    shadowColor: highContrastMode ? '#000000' : '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: disabled ? 0.01 : (highContrastMode ? 0.05 : 0.02),
    shadowRadius: 2,
    elevation: disabled ? 0 : (highContrastMode ? 1 : 0),
  };

  const iconStyle = {
    backgroundColor: disabled
      ? (highContrastMode ? 'rgba(100, 100, 100, 0.05)' : 'rgba(156, 163, 175, 0.05)')
      : (highContrastMode ? 'rgba(0, 0, 0, 0.05)' : 'rgba(59, 130, 246, 0.05)'),
    borderWidth: 0,
    borderColor: 'transparent',
  };

  const labelStyle = {
    color: disabled
      ? (highContrastMode ? '#666666' : '#9ca3af')
      : (highContrastMode ? '#000000' : '#1f2937'),
    fontWeight: highContrastMode ? 'bold' : 'normal',
  };

  const descriptionStyle = {
    color: disabled
      ? (highContrastMode ? '#666666' : '#9ca3af')
      : (highContrastMode ? '#000000' : '#6b7280'),
    fontWeight: highContrastMode ? '600' : 'normal',
  };

  return (
    <Reanimated.View style={animatedStyle}>
      <TouchableOpacity
        className="flex flex-row items-center p-4 mx-3 rounded-lg mb-1"
        onPress={handlePress}
        style={cardStyle}
        disabled={disabled}
        activeOpacity={disabled ? 1 : 0.8}
      >
        <View className="mr-3 p-1 rounded" style={iconStyle}>
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
        <Ionicon name="chevron-forward" size={16} color={disabled ? (highContrastMode ? "#666666" : "#d1d5db") : (highContrastMode ? "#000000" : "#9ca3af")} />
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
      <Text className="font-avenirBold text-sm mb-2 px-6" style={titleStyle}>
        {title}
      </Text>
      {children}
    </Reanimated.View>
  );
};

// Helper function to get icon component
const getIconComponent = (iconType, iconName, color) => {
  switch (iconType) {
    case 'Ionicon':
      return <Ionicon name={iconName} size={24} color={color} />;
    case 'MaterialCommunityIcons':
      return <MaterialCommunityIcons name={iconName} size={24} color={color} />;
    case 'Entypo':
      return <Entypo name={iconName} size={24} color={color} />;
    default:
      return <Ionicon name={iconName} size={24} color={color} />;
  }
};

// Helper function to handle menu item actions
const handleMenuItemAction = (item, navigation, dispatch) => {
  if (item.comingSoon) {
    console.log(`${item.label} - Coming Soon`);
    return;
  }

  switch (item.action) {
    case 'setShowProjectList':
      dispatch(setShowProjectList(true));
      break;
    case 'setShowBTDevices':
      dispatch(setShowBTDevices(true));
      break;
    case 'setShowAboutMsitu':
      dispatch(setShowAboutMsitu(true));
      break;
    case 'setShowProjectExport':
      dispatch(setShowProjectExport(true));
      break;
    case 'navigate':
      navigation.closeDrawer();
      navigation.navigate(item.actionParams);
      break;
    case 'console.log':
      console.log(item.label);
      break;
    default:
      console.log('Unknown action:', item.action);
  }
};

function CustomDrawerContent({ navigation, isPortrait }) {
  const dispatch = useDispatch()
  const opacityValue = useSharedValue(0);
  const scaleValue = useSharedValue(0.9);

  // Get project count from store
  const { projectList } = useSelector(store => store.project);
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
              <Text className="font-avenirBold text-2xl text-gray-800">{APP_NAME}</Text>
              <Text className="font-avenirMedium text-sm text-gray-500">{APP_SUBTITLE}</Text>
              <Text className="font-avenirMedium text-xs mt-1" style={versionStyle}>Version {APP_VERSION}</Text>
              <Text className="font-avenirMedium text-xs mt-1" style={versionStyle}>Build {getBuildInfo()}</Text>
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
              }}>{projectList?.length || 0} Projects</Text>
            </View>
          </View>
        </View>

        <ScrollView className="flex-1" showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingTop: 0 }}>
          {/* Projects Section */}
          <DrawerSection title={DRAWER_MENUS.projects.title} delay={400} highContrastMode={highContrastMode}>
            {DRAWER_MENUS.projects.items.map((item, index) => (
              <AnimatedDrawerItem
                key={item.label}
                label={item.label}
                description={item.description}
                badge={item.comingSoon ? "Coming Soon" : null}
                icon={getIconComponent(
                  item.iconType,
                  item.icon,
                  item.comingSoon ? "#9ca3af" : "#3b82f6"
                )}
                onPress={() => handleMenuItemAction(item, navigation, dispatch)}
                delay={500 + (index * 100)}
                highContrastMode={highContrastMode}
                disabled={item.comingSoon}
              />
            ))}
          </DrawerSection>

          {/* Equipment Section */}
          <DrawerSection title={DRAWER_MENUS.equipment.title} delay={500} highContrastMode={highContrastMode}>
            {DRAWER_MENUS.equipment.items.map((item, index) => (
              <AnimatedDrawerItem
                key={item.label}
                label={item.label}
                description={item.description}
                badge={item.comingSoon ? "Coming Soon" : null}
                icon={getIconComponent(
                  item.iconType,
                  item.icon,
                  item.comingSoon ? "#9ca3af" : "#3b82f6"
                )}
                onPress={() => handleMenuItemAction(item, navigation, dispatch)}
                delay={600 + (index * 100)}
                highContrastMode={highContrastMode}
                disabled={item.comingSoon}
              />
            ))}
          </DrawerSection>

          {/* Settings Section */}
          <DrawerSection title={DRAWER_MENUS.application.title} delay={800} highContrastMode={highContrastMode}>
            {DRAWER_MENUS.application.items.map((item, index) => (
              <AnimatedDrawerItem
                key={item.label}
                label={item.label}
                description={item.description}
                badge={item.comingSoon ? "Coming Soon" : null}
                icon={getIconComponent(
                  item.iconType,
                  item.icon,
                  item.comingSoon ? "#9ca3af" : "#3b82f6"
                )}
                onPress={() => handleMenuItemAction(item, navigation, dispatch)}
                delay={900 + (index * 100)}
                highContrastMode={highContrastMode}
                disabled={item.comingSoon}
              />
            ))}
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
      <ProjectExportModal
        visible={modalStore.showProjectExport}
        onClose={() => dispatch(setShowProjectExport(false))}
      />
    </>
  );
}
const styles = StyleSheet.create({
  label: { fontSize: 14, color: 'black', fontFamily: "AvenirMeduim" }
});
