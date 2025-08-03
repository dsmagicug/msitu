import { useState, useEffect } from 'react';
import { Platform, Alert } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';

const CURRENT_VERSION = '1.0.0'; // Update this when releasing new versions
const CONFIG_URL = 'https://github.com/ekeeya/files/raw/main/config.json';
const BASE_APK_URL = 'https://github.com/ekeeya/files/raw/main/msitu-apk-';

export const useAppUpdate = () => {
  const [updateAvailable, setUpdateAvailable] = useState(false);
  const [versionDetails, setVersionDetails] = useState(null);
  const [isChecking, setIsChecking] = useState(false);
  const [lastCheckTime, setLastCheckTime] = useState(null);

  // Check for updates
  const checkForUpdates = async (force = false) => {
    if (Platform.OS !== 'android') return;

    try {
      setIsChecking(true);

      // Check if we should skip this check (avoid checking too frequently)
      if (!force) {
        const lastCheck = await AsyncStorage.getItem('lastUpdateCheck');
        if (lastCheck) {
          const timeDiff = Date.now() - parseInt(lastCheck);
          // Only check once per hour
          if (timeDiff < 3600000) {
            setIsChecking(false);
            return;
          }
        }
      }

      // Fetch config.json from GitHub files repository
      const response = await fetch(CONFIG_URL);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const config = await response.json();
      
      // Extract version information from config
      const latestVersion = config.currentVersion;
      const currentVersion = CURRENT_VERSION;

      // Compare versions (simple string comparison for semantic versions)
      if (latestVersion && latestVersion !== currentVersion) {
        // Check if latest version is greater than current
        const isNewer = compareVersions(latestVersion, currentVersion);
        
        if (isNewer) {
          setVersionDetails({
            version: latestVersion,
            size: config.size || '~61MB',
            description: `Update from ${config.previousVersion} to ${latestVersion}`,
            downloadUrl: config.downloadUrl || `${BASE_APK_URL}${latestVersion}.apk`,
            publishedAt: config.publishedAt || new Date().toISOString(),
            changelog: config.changelog || [],
          });

          setUpdateAvailable(true);
        } else {
          setUpdateAvailable(false);
          setVersionDetails(null);
        }
      } else {
        setUpdateAvailable(false);
        setVersionDetails(null);
      }

      // Store check time
      await AsyncStorage.setItem('lastUpdateCheck', Date.now().toString());
      setLastCheckTime(new Date());

    } catch (error) {
      console.log('Error checking for updates:', error);
      // Don't show error to user for background checks
    } finally {
      setIsChecking(false);
    }
  };

  // Simple version comparison function
  const compareVersions = (version1, version2) => {
    const v1Parts = version1.split('.').map(Number);
    const v2Parts = version2.split('.').map(Number);
    
    // Pad with zeros if needed
    const maxLength = Math.max(v1Parts.length, v2Parts.length);
    while (v1Parts.length < maxLength) v1Parts.push(0);
    while (v2Parts.length < maxLength) v2Parts.push(0);
    
    for (let i = 0; i < maxLength; i++) {
      if (v1Parts[i] > v2Parts[i]) return true;
      if (v1Parts[i] < v2Parts[i]) return false;
    }
    
    return false; // Versions are equal
  };

  // Check for updates on app start
  useEffect(() => {
    checkForUpdates();
  }, []);

  // Show update notification
  const showUpdateNotification = () => {
    if (!updateAvailable || !versionDetails) return;

    Alert.alert(
      'Update Available',
      `A new version (${versionDetails.version}) is available. Would you like to update now?`,
      [
        {
          text: 'Update Later',
          style: 'cancel',
        },
        {
          text: 'Update Now',
          onPress: () => {
            // This will be handled by the modal
            return true; // Return true to indicate user wants to update
          },
        },
      ]
    );
  };

  // Force check for updates
  const forceCheckForUpdates = () => {
    checkForUpdates(true);
  };

  return {
    updateAvailable,
    versionDetails,
    isChecking,
    lastCheckTime,
    checkForUpdates: forceCheckForUpdates,
    showUpdateNotification,
  };
}; 