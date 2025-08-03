import { useNavigation } from '@react-navigation/native';
import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    Image,
    ScrollView,
    useWindowDimensions,
    ActivityIndicator,
    Alert
} from 'react-native';
import {MAP_TYPES} from 'react-native-maps'
import Icon from 'react-native-vector-icons/MaterialIcons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { Switch } from 'react-native-paper';
import MsTextInput from '../../components/input/MsTextInput';
import Reanimated, {
    useAnimatedStyle,
    useSharedValue,
    withTiming,
    withSpring,
    withDelay
} from 'react-native-reanimated';
import { Picker } from '@react-native-picker/picker';
import styles from '../../assets/styles';
import { useDispatch, useSelector } from 'react-redux';
import { Settings, defaultSettings, loadSettings, saveSettings, toggleHighContrastMode } from '../../store/settings';
import Toast from 'react-native-toast-message';
import UpdateAppModal from '../../components/misc/UpdateAppModal';
import { useAppUpdate } from '../../hooks/useAppUpdate';

const AnimatedSettingCard = ({ children, index = 0, highContrast = false }: { children: React.ReactNode; index?: number; highContrast?: boolean }) => {
    const scaleValue = useSharedValue(0);
    const opacityValue = useSharedValue(0);

    useEffect(() => {
        scaleValue.value = withDelay(index * 100, withSpring(1, { damping: 15, stiffness: 150 }));
        opacityValue.value = withDelay(index * 100, withTiming(1, { duration: 300 }));
    }, []);

    const animatedStyle = useAnimatedStyle(() => {
        return {
            transform: [{ scale: scaleValue.value }],
            opacity: opacityValue.value,
        };
    });

    return (
        <Reanimated.View style={animatedStyle}>
            <View className="p-4 bg-white rounded-xl mb-3"
                style={{
                    shadowColor: '#000',
                    shadowOffset: { width: 0, height: 2 },
                    shadowOpacity: highContrast ? 0.15 : 0.05,
                    shadowRadius: 8,
                    elevation: highContrast ? 4 : 2,
                    borderWidth: highContrast ? 2 : 1,
                    borderColor: highContrast ? 'rgba(0, 0, 0, 0.2)' : 'rgba(59, 130, 246, 0.1)',
                }}
            >
                {children}
            </View>
        </Reanimated.View>
    );
};

const SettingsScreen: React.FC = () => {
    const navigation = useNavigation();
    const [mode, setMode] = useState<boolean>(true);
    const [isPortrait, setIsPortrait] = useState<boolean>(true);
    const [mapStyle, setMapStyle] = useState<string>("satellite")
    const [localSettings, setLocalSettings] = useState<Settings>(defaultSettings)
    const [hasUnsavedChanges, setHasUnsavedChanges] = useState<boolean>(false);
    const [highContrastMode, setHighContrastMode] = useState<boolean>(false);
    const [showUpdateModal, setShowUpdateModal] = useState<boolean>(false);

    // Get update information
    const { updateAvailable, versionDetails, checkForUpdates, isChecking } = useAppUpdate();

    const fadeAnim = useSharedValue(mode ? 1 : 0);
    const saveButtonOpacity = useSharedValue(0);
    const saveButtonTranslateY = useSharedValue(100);

    // Auto-check for updates when screen loads
    useEffect(() => {
        const autoCheckUpdates = async () => {
            try {
                await checkForUpdates();
            } catch (error) {
                // Silent error handling for production
            }
        };
        autoCheckUpdates();
    }, []);

    // Show toast when update is found
    useEffect(() => {
        if (updateAvailable && versionDetails && !isChecking) {
            Toast.show({
                type: 'info',
                text1: 'Update Available',
                text2: `Version ${(versionDetails as any).version} is ready to download`,
                position: 'top',
                visibilityTime: 4000,
                autoHide: true,
                topOffset: 50,
                props: {
                    backgroundColor: '#3b82f6', // Blue background
                }
            });
        }
    }, [updateAvailable, versionDetails, isChecking]);

    useEffect(() => {
        fadeAnim.value = withTiming(mode ? 1 : 0, { duration: 300 });
    }, [mode, fadeAnim]);

    useEffect(() => {
        if (hasUnsavedChanges) {
            saveButtonOpacity.value = withSpring(1, { damping: 15, stiffness: 150 });
            saveButtonTranslateY.value = withSpring(0, { damping: 15, stiffness: 150 });
        } else {
            saveButtonOpacity.value = withSpring(0, { damping: 15, stiffness: 150 });
            saveButtonTranslateY.value = withSpring(100, { damping: 15, stiffness: 150 });
        }
    }, [hasUnsavedChanges]);

    const animatedStyle = useAnimatedStyle(() => {
        return {
            opacity: fadeAnim.value,
            height: fadeAnim.value === 0 ? 0 : 'auto',
        };
    });

    const saveButtonAnimatedStyle = useAnimatedStyle(() => {
        return {
            opacity: saveButtonOpacity.value,
            transform: [{ translateY: saveButtonTranslateY.value }],
        };
    });

    const { width, height } = useWindowDimensions();

    // @ts-ignore
    const {loading, saving, settings} = useSelector(store=>store.settings)

    const dispatch = useDispatch()

    useEffect(()=>{
         // @ts-ignore
        dispatch(loadSettings())
    },[])

    useEffect(() => {
        setIsPortrait(height > width);
    }, [width, height]);

    useEffect(()=>{
        if(settings){
            setLocalSettings(settings)
            setMapStyle(settings.mapStyle)
            setHighContrastMode(settings.highContrastMode || false)
            setMode(settings.appMode === "planting")
            setHasUnsavedChanges(false);
        }
    },[settings])

    const onToggleAppMode = () => {
        const newMode = !mode;
        setMode(newMode);
        const appMode = newMode ? "planting" : "survey";
        setLocalSettings({...localSettings, appMode});
        setHasUnsavedChanges(true);
    };

    const handleSave = async () => {
        try {
            // Ensure the store state is synced with local settings
            const settingsToSave = {
                ...localSettings,
                highContrastMode: highContrastMode,
                // Validate and set defaults for invalid values
                skipLines: localSettings.skipLines < 1 ? 1 : localSettings.skipLines,
                displayLineCount: localSettings.displayLineCount < 1 ? 10 : localSettings.displayLineCount
            };
            //@ts-ignore
            await dispatch(saveSettings(settingsToSave)).unwrap();
            setHasUnsavedChanges(false);
            Toast.show({
                type: 'success',
                text1: 'Settings saved successfully!',
                text2: 'Your app preferences have been updated.',
            });
        } catch (error) {
            console.error('Failed to save settings:', error);
            // You could add a toast notification here to show the error
            // For now, we'll just log it and keep the unsaved changes state
            setHasUnsavedChanges(true);
            Toast.show({
                type: 'error',
                text1: 'Failed to save settings',
                text2: 'Please try again or check your internet connection.',
            });
        }
    };

    const handleSettingChange = (newSettings: Settings) => {
        setLocalSettings(newSettings);
        setHasUnsavedChanges(true);
    };

    const toggleHighContrast = async () => {
        const newHighContrastMode = !highContrastMode;
        setHighContrastMode(newHighContrastMode);
        const updatedSettings = {...localSettings, highContrastMode: newHighContrastMode};
        setLocalSettings(updatedSettings);

        // Immediately update the store for instant effect across the app
        dispatch(toggleHighContrastMode());

        // Automatically save the settings
        try {
            //@ts-ignore
            await dispatch(saveSettings(updatedSettings)).unwrap();
            setHasUnsavedChanges(false);
            Toast.show({
                type: 'success',
                text1: 'High Contrast Mode',
                text2: 'Your high contrast mode preference has been updated.',
            });
        } catch (error) {
            console.error('Failed to save high contrast setting:', error);
            // Keep the unsaved changes state if save fails
            setHasUnsavedChanges(true);
            Toast.show({
                type: 'error',
                text1: 'Failed to save high contrast mode',
                text2: 'Please try again or check your internet connection.',
            });
        }
    };

    // High contrast text styles
    const titleStyle = highContrastMode ? { color: '#000000', fontWeight: 'bold' as const } : { color: '#1f2937' };
    const subtitleStyle = highContrastMode ? { color: '#000000' } : { color: '#6b7280' };
    const bodyTextStyle = highContrastMode ? { color: '#000000' } : { color: '#374151' };
    const textStyle = highContrastMode ? { color: '#000000' } : { color: '#374151' };

    return (
        <View className="h-full" style={{ backgroundColor: highContrastMode ? "#ffffff" : "#f8fafc" }}>
            {/* Enhanced Header */}
            <Reanimated.View
                className={`h-${isPortrait ? "1/5" : "1/4"} bg-gradient-to-b from-blue-50 to-white`}
                style={highContrastMode ? { backgroundColor: '#ffffff', borderBottomWidth: 2, borderBottomColor: '#000000' } : {}}
            >
                <View className={`flex flex-col p-4 gap-3`} style={{ marginTop: isPortrait ? 40 : 32 }}>
                    <View className="flex flex-row items-center justify-between">
                        <TouchableOpacity
                            onPress={() => navigation.goBack()}
                            className="rounded-full w-14 h-14 items-center justify-center"
                            style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(59, 130, 246, 0.1)' }}
                        >
                            <Icon color={highContrastMode ? "#000000" : "#3b82f6"} name='keyboard-arrow-left' size={28} />
                        </TouchableOpacity>
                        <View className="items-center">
                            <Text className='font-avenirBold text-2xl' style={titleStyle}>Settings</Text>
                            <Text className='font-avenirMedium text-sm' style={subtitleStyle}>Configure your app preferences</Text>
                        </View>
                        <View className="w-14 h-14" />
                    </View>
                </View>
            </Reanimated.View>

            {/* Enhanced Content */}
            <ScrollView className="flex-1 px-4 pb-8" showsVerticalScrollIndicator={false}>
                {/* High Contrast Toggle */}
                <View className='mt-6 mb-4'>
                    <Text className='font-avenirBold text-lg mb-3 px-1' style={titleStyle}>DISPLAY</Text>

                    <AnimatedSettingCard index={0} highContrast={highContrastMode}>
                        <View className='flex flex-row justify-between items-center'>
                            <View className='flex flex-row items-center gap-4'>
                                <View className="p-2 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(59, 130, 246, 0.1)' }}>
                                    <MaterialCommunityIcons name="contrast" size={20} color={highContrastMode ? "#000000" : "#3b82f6"} />
                                </View>
                                <View>
                                    <Text className='font-avenirBold' style={titleStyle}>High Contrast Mode</Text>
                                    <Text className='font-avenirMedium text-xs' style={subtitleStyle}>Better visibility in bright light</Text>
                                </View>
                            </View>
                            <Switch
                                value={highContrastMode}
                                onValueChange={toggleHighContrast}
                                color={highContrastMode ? "#000000" : "#3b82f6"}
                            />
                        </View>
                    </AnimatedSettingCard>
                </View>

                <View className='mt-6 mb-4'>
                    <Text className='font-avenirBold text-lg mb-3 px-1' style={titleStyle}>APP MODES</Text>

                    <AnimatedSettingCard index={1} highContrast={highContrastMode}>
                        <View className='flex flex-row justify-between items-center'>
                            <View className='flex flex-row items-center gap-4'>
                                <View className="p-2 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(34, 197, 94, 0.1)' }}>
                                    <Image source={require("../../assets/plant.png")} style={{ width: 20, height: 20, tintColor: highContrastMode ? "#000000" : "#16a34a" }} />
                                </View>
                                <View>
                                    <Text className='font-avenirBold' style={titleStyle}>Planting Mode</Text>
                                    <Text className='font-avenirMedium text-xs' style={subtitleStyle}>For tree planting operations</Text>
                                </View>
                            </View>
                            <Switch
                                value={mode}
                                onValueChange={onToggleAppMode}
                                color={highContrastMode ? "#000000" : "#16a34a"}
                            />
                        </View>
                    </AnimatedSettingCard>

                    <AnimatedSettingCard index={2} highContrast={highContrastMode}>
                        <View className='flex flex-row justify-between items-center'>
                            <View className='flex flex-row items-center gap-4'>
                                <View className="p-2 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(59, 130, 246, 0.1)' }}>
                                    <Image source={require("../../assets/surveying.png")} style={{ width: 20, height: 20, tintColor: highContrastMode ? "#000000" : "#3b82f6" }} />
                                </View>
                                <View>
                                    <Text className='font-avenirBold' style={titleStyle}>Survey Mode</Text>
                                    <Text className='font-avenirMedium text-xs' style={subtitleStyle}>For land surveying tasks</Text>
                                </View>
                            </View>
                            <Switch
                                value={!mode}
                                onValueChange={onToggleAppMode}
                                color={highContrastMode ? "#000000" : "#3b82f6"}
                            />
                        </View>
                    </AnimatedSettingCard>
                </View>

                <Reanimated.View style={animatedStyle}>
                    <View className='mb-4'>
                        <Text className='font-avenirBold text-lg mb-3 mt-6 px-1' style={titleStyle}>PLANTING LINE SETTINGS</Text>

                        <AnimatedSettingCard index={3} highContrast={highContrastMode}>
                            <View className='flex flex-row items-center gap-4 mb-3'>
                                <View className="p-2 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(168, 85, 247, 0.1)' }}>
                                    <Image source={require("../../assets/point.png")} style={{ width: 20, height: 20, tintColor: highContrastMode ? "#000000" : "#a855f7" }} />
                                </View>
                                <View className="flex-1">
                                    <MsTextInput
                                        key={`skipLines-${localSettings.skipLines}`}
                                        label="Points to skip"
                                        initialValue={`${localSettings.skipLines}`}
                                        keyboardType='number-pad'
                                        onChangeText={(value) => {
                                            if (value.length > 0){
                                                const numValue = parseInt(value);
                                                handleSettingChange({...localSettings, skipLines: numValue})
                                            } else {
                                                // Handle empty input - set to minimum
                                                handleSettingChange({...localSettings, skipLines: 1})
                                            }
                                        }}
                                    />
                                </View>
                            </View>
                            <Text className='text-xs font-avenirMedium leading-4' style={bodyTextStyle}>
                                Number of points to skip between pegs during planting operations
                            </Text>
                        </AnimatedSettingCard>

                        <AnimatedSettingCard index={4} highContrast={highContrastMode}>
                            <View className='flex flex-row items-center gap-4 mb-3'>
                                <View className="p-2 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(245, 158, 11, 0.1)' }}>
                                    <Image source={require("../../assets/steel-mesh.png")} style={{ width: 20, height: 20, tintColor: highContrastMode ? "#000000" : "#f59e0b" }} />
                                </View>
                                <View className="flex-1">
                                    <MsTextInput
                                        key={`displayLines-${localSettings.displayLineCount}`}
                                        label="Visible lines count"
                                        initialValue={`${localSettings.displayLineCount}`}
                                        keyboardType='number-pad'
                                        onChangeText={(value) => {
                                            if (value.length > 0){
                                                const numValue = parseInt(value);
                                                handleSettingChange({...localSettings, displayLineCount: numValue})
                                            } else {
                                                // Handle empty input - set to minimum
                                                handleSettingChange({...localSettings, displayLineCount: 10})
                                            }
                                        }}
                                    />
                                </View>
                            </View>
                            <Text className='text-xs font-avenirMedium leading-4' style={bodyTextStyle}>
                                Number of planting lines displayed when project loads
                            </Text>
                        </AnimatedSettingCard>
                    </View>
                </Reanimated.View>

                {/*<View className='mb-4'>
                    <Text className='font-avenirBold text-lg mb-3 mt-6 px-1' style={titleStyle}>CLOUD SETTINGS</Text>

                    <AnimatedSettingCard index={5} highContrast={highContrastMode}>
                        <View className='flex flex-row items-center gap-4 mb-3'>
                            <View className="p-2 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(236, 72, 153, 0.1)' }}>
                                <Image source={require("../../assets/server.png")} style={{ width: 20, height: 20, tintColor: highContrastMode ? "#000000" : "#ec4899" }} />
                            </View>
                            <View className="flex-1">
                                <MsTextInput
                                    label="Cloud Sync API"
                                    keyboardType='url'
                                    initialValue={localSettings.cloudApi}
                                    onChangeText={(value) => handleSettingChange({...localSettings, cloudApi:value})}
                                />
                            </View>
                        </View>
                        <View className="flex flex-row justify-between items-center">
                            <Text className='text-xs font-avenirMedium flex-1 leading-4' style={bodyTextStyle}>
                                Projects will be uploaded to this API when internet is available
                            </Text>
                            <TouchableOpacity
                                className='px-4 py-2 rounded-lg items-center'
                                style={{
                                    backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(236, 72, 153, 0.1)',
                                    borderWidth: 1,
                                    borderColor: highContrastMode ? 'rgba(0, 0, 0, 0.3)' : 'rgba(236, 72, 153, 0.2)'
                                }}
                            >
                                <Text className='font-avenirBold text-xs' style={{ color: highContrastMode ? '#000000' : '#ec4899' }}>Sync Now</Text>
                            </TouchableOpacity>
                        </View>
                    </AnimatedSettingCard>
                </View>

                <View className='mb-4'>
                    <Text className='font-avenirBold text-lg mb-3 mt-6 px-1' style={titleStyle}>MAP VIEW SETTINGS</Text>

                    <AnimatedSettingCard index={6} highContrast={highContrastMode}>
                        <View className='flex flex-row items-center gap-4 mb-3'>
                            <View className="p-2 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(34, 197, 94, 0.1)' }}>
                                <Image source={require("../../assets/map.png")} style={{ width: 20, height: 20, tintColor: highContrastMode ? "#000000" : "#22c55e" }} />
                            </View>
                            <View className="flex-1">
                                <Text className='font-avenirBold mb-2' style={titleStyle}>Map Style</Text>
                                <View className="border border-gray-200 rounded-lg overflow-hidden" style={{ borderColor: highContrastMode ? '#000000' : '#e5e7eb' }}>
                                    <Picker
                                        style={{ height: 60 }}
                                        selectedValue={mapStyle}
                                        onValueChange={(itemValue, _) => {
                                            setMapStyle(itemValue)
                                            handleSettingChange({...localSettings, mapStyle:itemValue})
                                        }}
                                        itemStyle={{ height: 60, fontSize: 16, color: highContrastMode ? '#000000' : '#374151' }}
                                    >
                                        <Picker.Item style={{ ...styles.textMedium, fontSize: 16, color: highContrastMode ? '#000000' : '#374151' }} label="SATELLITE" value={MAP_TYPES.SATELLITE} />
                                        <Picker.Item style={{ ...styles.textMedium, fontSize: 16, color: highContrastMode ? '#000000' : '#374151' }} label="TERRAIN" value={MAP_TYPES.TERRAIN} />
                                        <Picker.Item style={{ ...styles.textMedium, fontSize: 16, color: highContrastMode ? '#000000' : '#374151' }} label="HYBRID" value={MAP_TYPES.HYBRID} />
                                        <Picker.Item style={{ ...styles.textMedium, fontSize: 16, color: highContrastMode ? '#000000' : '#374151' }} label="HYBRID FLYOVER" value={MAP_TYPES.HYBRID_FLYOVER} />
                                        <Picker.Item style={{ ...styles.textMedium, fontSize: 16, color: highContrastMode ? '#000000' : '#374151' }} label="STANDARD" value={MAP_TYPES.STANDARD} />
                                    </Picker>
                                </View>
                            </View>
                        </View>
                        <Text className='text-xs font-avenirMedium leading-4' style={bodyTextStyle}>
                            Choose your preferred map view style (default: SATELLITE)
                        </Text>
                    </AnimatedSettingCard>
                </View>*/}

                {/* App Updates Section */}
                <View className='mb-4'>
                    <Text className='font-avenirBold text-lg mb-3 mt-6 px-1' style={titleStyle}>APP UPDATES</Text>

                    <AnimatedSettingCard index={7} highContrast={highContrastMode}>
                        <View className='flex flex-row items-center gap-4 mb-4'>
                            <View className="p-2 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(59, 130, 246, 0.1)' }}>
                                <Image source={require("../../assets/refresh.png")} style={{ width: 20, height: 20, tintColor: highContrastMode ? "#000000" : "#3b82f6" }} />
                            </View>
                            <View className="flex-1">
                                <View className="flex-row items-center mb-1">
                                    <Text className='font-avenirBold' style={titleStyle}>App Updates</Text>
                                    {isChecking && (
                                        <View className="ml-2">
                                            <ActivityIndicator size="small" color={highContrastMode ? "#000000" : "#3b82f6"} />
                                        </View>
                                    )}
                                    {updateAvailable && !isChecking && (
                                        <View className="ml-2 px-2 py-1 rounded-full" style={{
                                            backgroundColor: highContrastMode ? '#000000' : '#ef4444'
                                        }}>
                                            <Text className="text-xs font-avenirBold text-white">NEW</Text>
                                        </View>
                                    )}
                                </View>
                                <Text className='text-xs font-avenirMedium leading-4' style={bodyTextStyle}>
                                    {isChecking
                                        ? 'Checking for updates...'
                                        : updateAvailable && versionDetails
                                            ? `Version ${(versionDetails as any).version} is available for download`
                                            : 'Keep your app up to date with the latest features and bug fixes'
                                    }
                                </Text>
                            </View>
                        </View>

                        {isChecking ? (
                            <View className="items-center py-3">
                                <ActivityIndicator size="small" color={highContrastMode ? "#000000" : "#3b82f6"} />
                                <Text className="text-xs font-avenirMedium mt-2" style={subtitleStyle}>
                                    Checking for updates...
                                </Text>
                            </View>
                        ) : updateAvailable ? (
                            <View className="space-y-3">


                                {/* Update Button */}
                                <TouchableOpacity
                                    onPress={() => setShowUpdateModal(true)}
                                    className="py-3 px-4 rounded-xl items-center border-2"
                                    style={{
                                        backgroundColor: 'transparent',
                                        borderColor: highContrastMode ? '#dc2626' : '#f97316',
                                    }}
                                    activeOpacity={0.8}
                                >
                                    <View className="flex-row items-center">
                                        <MaterialCommunityIcons
                                            name="cloud-download"
                                            size={18}
                                            color={highContrastMode ? '#dc2626' : '#f97316'}
                                        />
                                        <Text className="text-sm font-avenirBold ml-2" style={{ color: highContrastMode ? '#dc2626' : '#f97316' }}>
                                            Update Now
                                        </Text>
                                    </View>
                                </TouchableOpacity>
                            </View>
                        ) : (
                            <TouchableOpacity
                                onPress={async () => {
                                    // Force check for updates first
                                    await checkForUpdates();

                                    // Then show modal if update is available
                                    if (updateAvailable && versionDetails) {
                                        setShowUpdateModal(true);
                                    } else {
                                        Alert.alert(
                                            'No Updates',
                                            'You are using the latest version of the app.',
                                            [{ text: 'OK' }]
                                        );
                                    }
                                }}
                                className="bg-gray-50 border border-gray-200 px-4 py-3 rounded-xl"
                                style={{
                                    backgroundColor: highContrastMode ? '#f8f9fa' : '#f9fafb',
                                    borderColor: highContrastMode ? '#000000' : '#e5e7eb',
                                }}
                                activeOpacity={0.8}
                            >
                                <View className="flex-row items-center justify-center space-x-2">
                                    <MaterialCommunityIcons
                                        name="cloud-download-outline"
                                        size={16}
                                        color={highContrastMode ? "#000000" : "#6b7280"}
                                    />
                                    <Text className="text-sm font-avenirMedium" style={{ color: highContrastMode ? "#000000" : "#6b7280" }}>
                                        Check for Updates
                                    </Text>
                                </View>
                            </TouchableOpacity>
                        )}
                    </AnimatedSettingCard>
                </View>
            </ScrollView>

            {/* Smart Save Button - Only shows when there are unsaved changes */}
            <Reanimated.View
                className="absolute bottom-4 left-4 right-4"
                style={saveButtonAnimatedStyle}
            >
                <TouchableOpacity
                    onPress={handleSave}
                    className="rounded-2xl py-4 items-center"
                    style={{
                        backgroundColor: highContrastMode ? '#000000' : '#3b82f6',
                        shadowColor: highContrastMode ? '#000000' : '#3b82f6',
                        shadowOffset: { width: 0, height: 4 },
                        shadowOpacity: 0.3,
                        shadowRadius: 8,
                        elevation: 6,
                    }}
                >
                    <View className="flex-row items-center">
                        {saving ? (
                            <ActivityIndicator size="small" color="white" />
                        ) : (
                            <MaterialCommunityIcons name="content-save" size={20} color="white" />
                        )}
                        <Text className="font-avenirBold text-white text-base ml-2">
                            {saving ? 'Saving...' : 'Save Changes'}
                        </Text>
                    </View>
                </TouchableOpacity>
            </Reanimated.View>

            {/* Update Modal */}
            <UpdateAppModal
                visible={showUpdateModal}
                onClose={() => setShowUpdateModal(false)}
                versionDetails={versionDetails}
            />

            <Toast />
        </View>
    );
};

export default SettingsScreen;
