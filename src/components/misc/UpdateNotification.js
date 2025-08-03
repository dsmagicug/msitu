import React, { useEffect } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  withDelay,
} from 'react-native-reanimated';
import Ionicons from 'react-native-vector-icons/Ionicons';
import { useAppUpdate } from '../../hooks/useAppUpdate';

const UpdateNotification = ({ onUpdatePress }) => {
  const { updateAvailable, versionDetails} = useAppUpdate();

  const notificationOpacity = useSharedValue(0);
  const notificationTranslateY = useSharedValue(-100);
  const badgeScale = useSharedValue(0);

  const notificationAnimatedStyle = useAnimatedStyle(() => ({
    opacity: notificationOpacity.value,
    transform: [{ translateY: notificationTranslateY.value }],
  }));

  const badgeAnimatedStyle = useAnimatedStyle(() => ({
    transform: [{ scale: badgeScale.value }],
  }));

  useEffect(() => {
    if (updateAvailable && versionDetails) {

      // Animate in
      notificationOpacity.value = withDelay(1000, withSpring(1, { damping: 15, stiffness: 150 }));
      notificationTranslateY.value = withDelay(1000, withSpring(0, { damping: 15, stiffness: 150 }));
      badgeScale.value = withDelay(1500, withSpring(1, { damping: 10, stiffness: 200 }));
    } else {

      // Animate out
      notificationOpacity.value = withTiming(0, { duration: 300 });
      notificationTranslateY.value = withTiming(-100, { duration: 300 });
      badgeScale.value = withTiming(0, { duration: 200 });
    }
  }, [updateAvailable, versionDetails]);

  if (!updateAvailable || !versionDetails) {
    return null;
  }

  return (
    <Animated.View
      style={notificationAnimatedStyle}
      className="absolute top-4 left-4 right-4 z-50"
    >
      <TouchableOpacity
        onPress={onUpdatePress}
        className="bg-gradient-to-r from-purple-600 to-blue-600 p-4 rounded-2xl shadow-lg"
        activeOpacity={0.8}
      >
        <View className="flex-row items-center justify-between">
          <View className="flex-row items-center space-x-3">
            <View className="relative">
              <Ionicons
                name="cloud-download-outline"
                size={24}
                color="white"
              />
              <Animated.View
                style={badgeAnimatedStyle}
                className="absolute -top-1 -right-1 bg-red-500 rounded-full w-3 h-3"
              />
            </View>
            <View className="flex-1">
              <Text className="text-white font-semibold text-base">
                Update Available
              </Text>
              <Text className="text-white/80 text-sm">
                Version {versionDetails.version} is ready to install
              </Text>
            </View>
          </View>
          <Ionicons
            name="chevron-forward"
            size={20}
            color="white"
          />
        </View>
      </TouchableOpacity>
    </Animated.View>
  );
};

export default UpdateNotification;
