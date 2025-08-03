import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  Platform,
  Alert,
  ScrollView,
  Dimensions,
} from 'react-native';
import RNFS from 'react-native-fs';
import RNApkInstaller from '@dominicvonk/react-native-apk-installer';
import Reanimated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  withDelay,
  withRepeat,
  withSequence,
} from 'react-native-reanimated';
import Icon from 'react-native-vector-icons/MaterialIcons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { useSelector } from 'react-redux';

const { width: screenWidth, height: screenHeight } = Dimensions.get('window');

const UpdateAppModal = ({ visible, onClose, versionDetails }) => {
  const [progress, setProgress] = useState(0);
  const [downloading, setDownloading] = useState(false);
  const [installing, setInstalling] = useState(false);

  // Get high contrast mode from store
  // @ts-ignore
  const { settings } = useSelector(store => store.settings);
  const highContrastMode = settings?.highContrastMode || false;

  // Animation values
  const modalOpacity = useSharedValue(0);
  const modalScale = useSharedValue(0.8);
  const contentTranslateY = useSharedValue(50);
  const pulseValue = useSharedValue(1);
  const downloadIconRotation = useSharedValue(0);

  // Pulse animation for update available indicator
  useEffect(() => {
    if (visible && versionDetails) {
      pulseValue.value = withRepeat(
        withSequence(
          withTiming(1.05, { duration: 1000 }),
          withTiming(1, { duration: 1000 })
        ),
        -1,
        true
      );
    } else {
      pulseValue.value = withTiming(1);
    }
  }, [visible, versionDetails]);

  // Download icon rotation animation
  useEffect(() => {
    if (downloading) {
      downloadIconRotation.value = withRepeat(
        withTiming(360, { duration: 2000 }),
        -1,
        false
      );
    } else {
      downloadIconRotation.value = withTiming(0);
    }
  }, [downloading]);

  // Animation effects
  useEffect(() => {
    if (visible) {
      modalOpacity.value = withTiming(1, { duration: 300 });
      modalScale.value = withSpring(1, { damping: 15, stiffness: 150 });
      contentTranslateY.value = withDelay(100, withSpring(0, { damping: 15, stiffness: 150 }));
    } else {
      modalOpacity.value = withTiming(0, { duration: 200 });
      modalScale.value = withSpring(0.8, { damping: 15, stiffness: 150 });
      contentTranslateY.value = withSpring(50, { damping: 15, stiffness: 150 });
    }
  }, [visible]);

  const modalAnimatedStyle = useAnimatedStyle(() => ({
    opacity: modalOpacity.value,
  }));

  const contentAnimatedStyle = useAnimatedStyle(() => ({
    transform: [
      { scale: modalScale.value },
      { translateY: contentTranslateY.value }
    ],
  }));

  const pulseAnimatedStyle = useAnimatedStyle(() => ({
    transform: [{ scale: pulseValue.value }],
  }));

  const downloadIconAnimatedStyle = useAnimatedStyle(() => ({
    transform: [{ rotate: `${downloadIconRotation.value}deg` }],
  }));

  const downloadAndInstallApk = async () => {
    const apkUrl = versionDetails?.downloadUrl || 'https://github.com/ekeeya/files/raw/main/msitu-apk-1.0.0.apk';
    const apkPath = `${RNFS.CachesDirectoryPath}/msitu-update.apk`;

    try {
      setDownloading(true);
      setProgress(0);

      // Check permissions
      const granted = await RNApkInstaller.haveUnknownAppSourcesPermission();
      if (!granted) {
        const permissionGranted = await RNApkInstaller.showUnknownAppSourcesPermission();
        if (!permissionGranted) {
          Alert.alert(
            'Permission Required',
            'Please allow installation from unknown sources to update the app.',
            [{ text: 'OK' }]
          );
          setDownloading(false);
          return;
        }
      }

      // Download APK
      const download = RNFS.downloadFile({
        fromUrl: apkUrl,
        toFile: apkPath,
        progress: res => {
          const bytesWritten = BigInt(res.bytesWritten);
          const contentLength = BigInt(res.contentLength);

          if (contentLength > 0n) {
            const percentage = Number((bytesWritten * 100n) / contentLength);
            setProgress(parseFloat(percentage.toFixed(2)));
          }
        },
        progressDivider: 1,
      });

      const result = await download.promise;

      if (result.statusCode === 200) {
        setDownloading(false);
        setInstalling(true);

        // Install APK
        if (Platform.OS === 'android') {
          try {
            await RNApkInstaller.install(apkPath);
            setInstalling(false);
            onClose();
          } catch (err) {
            console.error('Installation error:', err);
            Alert.alert(
              'Installation Failed',
              'Failed to install the update. Please try again.',
              [{ text: 'OK' }]
            );
            setInstalling(false);
          } finally {
            // Clean up
            await RNFS.exists(apkPath).then(exists => {
              if (exists) {
                RNFS.unlink(apkPath);
              }
            });
          }
        }
      } else {
        console.log('Download failed');
        Alert.alert(
          'Download Failed',
          'Failed to download the update. Please check your internet connection.',
          [{ text: 'OK' }]
        );
        setDownloading(false);
      }
    } catch (error) {
      console.log('Error during update:', error);
      Alert.alert(
        'Update Error',
        'An error occurred during the update process. Please try again.',
        [{ text: 'OK' }]
      );
      setDownloading(false);
      setInstalling(false);
    }
  };

  const handleClose = () => {
    if (!downloading && !installing) {
      onClose();
    }
  };

  if (!visible) return null;

  const containerStyle = {
    backgroundColor: highContrastMode ? 'rgba(255, 255, 255, 0.98)' : 'rgba(0, 0, 0, 0.6)',
  };

  const modalStyle = {
    backgroundColor: highContrastMode ? '#ffffff' : '#ffffff',
    borderWidth: highContrastMode ? 2 : 0,
    borderColor: highContrastMode ? '#000000' : 'transparent',
    shadowColor: highContrastMode ? '#000000' : '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: highContrastMode ? 0.4 : 0.3,
    shadowRadius: 24,
    elevation: highContrastMode ? 12 : 8,
  };

  const textStyle = {
    color: highContrastMode ? '#000000' : '#1f2937',
    fontWeight: highContrastMode ? 'bold' : 'normal',
  };

  const subtitleStyle = {
    color: highContrastMode ? '#000000' : '#6b7280',
    fontWeight: highContrastMode ? '600' : 'normal',
  };

  return (
    <Reanimated.View
      className="absolute inset-0 z-50 justify-center items-center"
      style={[containerStyle, modalAnimatedStyle]}
    >
      <TouchableOpacity
        className="absolute inset-0"
        onPress={handleClose}
        activeOpacity={1}
      />

      <Reanimated.View
        className="mx-4 rounded-3xl overflow-hidden"
        style={[
          modalStyle,
          contentAnimatedStyle,
          { maxWidth: screenWidth * 0.95, maxHeight: screenHeight * 0.85 }
        ]}
      >
        {/* Enhanced Header with Update Badge */}
        <View
          className="p-4 pb-2"
          style={{
            backgroundColor: highContrastMode ? '#f8f9fa' : '#3b82f6'
          }}
        >
          <View className="flex flex-row items-center justify-between mb-2">
            <View className="flex-1">
              <View className="flex-row items-center mb-1">
                <Reanimated.View style={pulseAnimatedStyle}>
                  <View className="mr-2 p-1 rounded-full" style={{
                    backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(255, 255, 255, 0.2)'
                  }}>
                    <MaterialCommunityIcons
                      name="update"
                      size={20}
                      color={highContrastMode ? "#000000" : "#ffffff"}
                    />
                  </View>
                </Reanimated.View>
                <View>
                  <Text className="font-avenirBold text-xl" style={{ color: highContrastMode ? "#000000" : "#ffffff" }}>
                    Update App
                  </Text>
                  <Text className="font-avenirMedium text-xs" style={{ color: highContrastMode ? "#000000" : "#ffffff", opacity: 0.9 }}>
                    {versionDetails ? `Version ${versionDetails.version}` : 'New version ready'}
                  </Text>
                </View>
              </View>
              {versionDetails?.publishedAt && (
                <Text className="font-avenirMedium text-xs ml-8" style={{ color: highContrastMode ? "#000000" : "#ffffff", opacity: 0.75 }}>
                  Released {new Date(versionDetails.publishedAt).toLocaleDateString()}
                </Text>
              )}
            </View>
            {!downloading && !installing && (
              <TouchableOpacity
                onPress={handleClose}
                className="p-1 rounded-full"
                style={{
                  backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(255, 255, 255, 0.2)',
                  borderWidth: highContrastMode ? 1 : 0,
                  borderColor: highContrastMode ? '#000000' : 'transparent',
                }}
              >
                <Icon name="close" size={20} color={highContrastMode ? "#000000" : "#ffffff"} />
              </TouchableOpacity>
            )}
          </View>
        </View>

        {/* Enhanced Content */}
        <ScrollView className="max-h-96" showsVerticalScrollIndicator={false}>
          <View className="px-6 pb-6">
            {versionDetails ? (
              <>
                {/* Update Summary Card */}
                <View className="mb-3 pt-1">
                  <View className="p-3 rounded-2xl" style={{
                    backgroundColor: highContrastMode ? '#f8f9fa' : '#f0f9ff',
                    borderWidth: highContrastMode ? 2 : 1,
                    borderColor: highContrastMode ? '#000000' : '#0ea5e9',
                  }}>
                    <Text className="font-avenirBold text-lg mb-2" style={textStyle}>What's New</Text>
                    <Text className="font-avenirMedium text-sm leading-5" style={subtitleStyle}>
                      {versionDetails.description || 'This update includes new features, improvements, and bug fixes to enhance your experience.'}
                    </Text>
                  </View>
                </View>

                {/* Update Details */}
                <View className="mb-3">
                  <Text className="font-avenirBold text-lg mb-2" style={textStyle}>Update Details</Text>
                  <View className="space-y-1">
                    <View className="flex flex-row items-center p-2 rounded-xl" style={{
                      backgroundColor: highContrastMode ? '#f8f9fa' : '#f8fafc',
                      borderWidth: highContrastMode ? 1 : 0,
                      borderColor: highContrastMode ? '#000000' : 'transparent',
                    }}>
                      <MaterialCommunityIcons name="package-variant" size={18} color={highContrastMode ? "#000000" : "#3b82f6"} />
                      <Text className="font-avenirMedium text-sm ml-2 flex-1" style={subtitleStyle}>
                        Version: {versionDetails.version}
                      </Text>
                    </View>
                    <View className="flex flex-row items-center p-2 rounded-xl" style={{
                      backgroundColor: highContrastMode ? '#f8f9fa' : '#f8fafc',
                      borderWidth: highContrastMode ? 1 : 0,
                      borderColor: highContrastMode ? '#000000' : 'transparent',
                    }}>
                      <MaterialCommunityIcons name="file-download" size={18} color={highContrastMode ? "#000000" : "#3b82f6"} />
                      <Text className="font-avenirMedium text-sm ml-2 flex-1" style={subtitleStyle}>
                        Size: {versionDetails.size}
                      </Text>
                    </View>
                    <View className="flex flex-row items-center p-2 rounded-xl" style={{
                      backgroundColor: highContrastMode ? '#f8f9fa' : '#f8fafc',
                      borderWidth: highContrastMode ? 1 : 0,
                      borderColor: highContrastMode ? '#000000' : 'transparent',
                    }}>
                      <MaterialCommunityIcons name="calendar" size={18} color={highContrastMode ? "#000000" : "#3b82f6"} />
                      <Text className="font-avenirMedium text-sm ml-2 flex-1" style={subtitleStyle}>
                        Published: {new Date(versionDetails.publishedAt).toLocaleDateString()}
                      </Text>
                    </View>
                  </View>
                </View>

                {/* Changelog */}
                {versionDetails.changelog && versionDetails.changelog.length > 0 && (
                  <View className="mb-3">
                    <Text className="font-avenirBold text-lg mb-2" style={textStyle}>What's Changed</Text>
                    <View className="space-y-1">
                      {versionDetails.changelog.map((change, index) => (
                        <View key={index} className="flex flex-row items-start p-2 rounded-lg" style={{
                          backgroundColor: highContrastMode ? '#f8f9fa' : '#f0fdf4',
                          borderWidth: highContrastMode ? 1 : 0,
                          borderColor: highContrastMode ? '#000000' : 'transparent',
                        }}>
                          <MaterialCommunityIcons
                            name="check-circle"
                            size={14}
                            color={highContrastMode ? "#000000" : "#10b981"}
                            style={{ marginTop: 2 }}
                          />
                          <Text className="font-avenirMedium text-sm ml-2 flex-1" style={subtitleStyle}>
                            {change}
                          </Text>
                        </View>
                      ))}
                    </View>
                  </View>
                )}

                {/* Progress Section */}
                {(downloading || installing) && (
                  <View className="mb-3">
                    <View className="p-3 rounded-2xl" style={{
                      backgroundColor: highContrastMode ? '#f8f9fa' : '#fef3c7',
                      borderWidth: highContrastMode ? 2 : 1,
                      borderColor: highContrastMode ? '#000000' : '#f59e0b',
                    }}>
                      <Text className="font-avenirBold text-lg mb-2" style={textStyle}>
                        {downloading ? 'Downloading Update' : 'Installing Update'}
                      </Text>
                      <View className="space-y-2">
                        <View className="flex-row justify-between items-center">
                          <Text className="font-avenirMedium text-sm" style={subtitleStyle}>
                            Progress
                          </Text>
                          <Text className="font-avenirBold text-sm" style={textStyle}>
                            {Math.round(progress)}%
                          </Text>
                        </View>

                        <View className="h-3 bg-gray-200 rounded-full overflow-hidden">
                          <View
                            className="h-full rounded-full"
                            style={{
                              width: `${progress}%`,
                              backgroundColor: highContrastMode ? '#000000' : '#3b82f6'
                            }}
                          />
                        </View>

                        {downloading && (
                          <Text className="font-avenirMedium text-xs text-center" style={subtitleStyle}>
                            Please don't close the app during download
                          </Text>
                        )}
                      </View>
                    </View>
                  </View>
                )}
              </>
            ) : (
              <View className="py-8">
                <Text className="font-avenirMedium text-center" style={subtitleStyle}>
                  No update information available
                </Text>
              </View>
            )}
          </View>
        </ScrollView>

        {/* Enhanced Footer */}
        <View className="p-6 border-t" style={{
          borderColor: highContrastMode ? '#000000' : '#e5e7eb',
          backgroundColor: highContrastMode ? '#f8f9fa' : '#f9fafb'
        }}>
          {!downloading && !installing && versionDetails ? (
            <TouchableOpacity
              onPress={downloadAndInstallApk}
              className="py-4 px-6 rounded-2xl items-center"
              style={{
                backgroundColor: highContrastMode ? '#000000' : '#3b82f6',
                shadowColor: highContrastMode ? '#000000' : '#3b82f6',
                shadowOffset: { width: 0, height: 4 },
                shadowOpacity: 0.3,
                shadowRadius: 8,
                elevation: 6,
              }}
              activeOpacity={0.8}
            >
              <View className="flex-row items-center">
                <Reanimated.View style={downloadIconAnimatedStyle}>
                  <MaterialCommunityIcons name="download" size={24} color="white" />
                </Reanimated.View>
                <Text className="font-avenirBold text-white text-lg ml-3">
                  Download & Install Update
                </Text>
              </View>
            </TouchableOpacity>
          ) : (
            <View className="items-center">
              <Text className="font-avenirMedium text-sm text-center" style={subtitleStyle}>
                {downloading ? 'Downloading the latest version...' : 'Installing update...'}
              </Text>
              {installing && (
                <Text className="font-avenirMedium text-xs text-center mt-1" style={subtitleStyle}>
                  The app will restart automatically
                </Text>
              )}
            </View>
          )}
        </View>
      </Reanimated.View>
    </Reanimated.View>
  );
};

export default UpdateAppModal;
