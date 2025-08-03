import React from 'react';
import { View, Text, TouchableOpacity, Alert } from 'react-native';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withSequence,
  withTiming,
} from 'react-native-reanimated';
import Ionicons from 'react-native-vector-icons/Ionicons';
import colors from 'tailwindcss/colors';
import { useAppUpdate } from '../../hooks/useAppUpdate';

const UpdateButton = ({ 
  style, 
  showBadge = true, 
  variant = 'default', // 'default', 'compact', 'icon-only'
  onPress 
} = {}) => {
  const { updateAvailable, versionDetails, isChecking, checkForUpdates } = useAppUpdate();
  
  // Animation values
  const scale = useSharedValue(1);
  const rotation = useSharedValue(0);

  // Animated styles
  const animatedStyle = useAnimatedStyle(() => ({
    transform: [
      { scale: scale.value },
      { rotate: `${rotation.value}deg` },
    ],
  }));

  const handlePress = async () => {
    // Trigger press animation
    scale.value = withSequence(
      withSpring(0.95, { duration: 100 }),
      withSpring(1, { duration: 100 })
    );

    if (onPress) {
      // If parent provided onPress, use it
      onPress();
      return;
    }

    // Default behavior: check for updates and show alert
    rotation.value = withSequence(
      withTiming(360, { duration: 1000 }),
      withTiming(0, { duration: 0 })
    );
    
    await checkForUpdates();
    
    if (updateAvailable && versionDetails) {
      Alert.alert(
        'Update Available',
        `Version ${versionDetails.version} is available.`,
        [{ text: 'OK' }]
      );
    } else {
      Alert.alert(
        'No Updates',
        'You are using the latest version of the app.',
        [{ text: 'OK' }]
      );
    }
  };

  const renderContent = () => {
    switch (variant) {
      case 'compact':
        return (
          <View className="flex-row items-center space-x-2">
            <Ionicons
              name={isChecking ? "refresh" : "cloud-download-outline"}
              size={16}
              color={colors.purple['600']}
            />
            <Text className="text-sm font-medium text-purple-600">
              {isChecking ? 'Checking...' : 'Check for Updates'}
            </Text>
          </View>
        );

      case 'icon-only':
        return (
          <Ionicons
            name={isChecking ? "refresh" : "cloud-download-outline"}
            size={24}
            color={colors.purple['600']}
          />
        );

      default:
        return (
          <View className="flex-row items-center space-x-3">
            <Ionicons
              name={isChecking ? "refresh" : "cloud-download-outline"}
              size={20}
              color="white"
            />
            <Text className="text-white font-semibold">
              {isChecking ? 'Checking...' : 'Check for Updates'}
            </Text>
            {updateAvailable && showBadge && (
              <View className="bg-red-500 rounded-full w-2 h-2" />
            )}
          </View>
        );
    }
  };

  const getButtonStyle = () => {
    switch (variant) {
      case 'compact':
        return "bg-purple-50 border border-purple-200 px-4 py-2 rounded-xl";
      case 'icon-only':
        return "bg-purple-50 p-3 rounded-full";
      default:
        return "bg-gradient-to-r from-purple-600 to-blue-600 px-6 py-3 rounded-2xl shadow-lg";
    }
  };

  return (
    <Animated.View style={[animatedStyle, style]}>
      <TouchableOpacity
        onPress={handlePress}
        className={getButtonStyle()}
        activeOpacity={0.8}
        disabled={isChecking}
      >
        {renderContent()}
      </TouchableOpacity>
    </Animated.View>
  );
};

export default UpdateButton; 