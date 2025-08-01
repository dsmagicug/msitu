import { useNavigation } from '@react-navigation/native';
import React, { useState, useEffect } from 'react';
import {
    View, 
    Text, 
    TouchableOpacity, 
    Image, 
    ScrollView,
    Dimensions,
    useWindowDimensions,
    ActivityIndicator
} from 'react-native';
import {MAP_TYPES} from 'react-native-maps'
import Icon from 'react-native-vector-icons/MaterialIcons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import colors from 'tailwindcss/colors';
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
import { Settings, defaultSettings, loadSettings, saveSettings } from '../../store/settings';

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
   
    const fadeAnim = useSharedValue(mode ? 1 : 0);
    const saveButtonOpacity = useSharedValue(0);
    const saveButtonTranslateY = useSharedValue(100);

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
            setMapStyle(localSettings.mapStyle)
            setHasUnsavedChanges(false);
        }
    },[settings])

    useEffect(()=>{
        let appMode = mode ? "planting" :"survey"
        setLocalSettings({...localSettings, appMode})
        setHasUnsavedChanges(true);
    },[mode])

    const onToggleAppMode = () => setMode(!mode);

    const handleSave = () => {
        //@ts-ignore
        dispatch(saveSettings(localSettings))
        setHasUnsavedChanges(false);
    };

    const handleSettingChange = (newSettings: Settings) => {
        setLocalSettings(newSettings);
        setHasUnsavedChanges(true);
    };

    const toggleHighContrast = () => {
        setHighContrastMode(!highContrastMode);
        setHasUnsavedChanges(true);
    };

    // High contrast text styles
    const titleStyle = highContrastMode ? { color: '#000000', fontWeight: 'bold' as const } : { color: '#1f2937' };
    const subtitleStyle = highContrastMode ? { color: '#000000' } : { color: '#6b7280' };
    const bodyTextStyle = highContrastMode ? { color: '#000000' } : { color: '#374151' };

    return (
        <View className="h-full" style={{ backgroundColor: highContrastMode ? "#ffffff" : "#f8fafc" }}>
            {/* Enhanced Header */}
            <Reanimated.View 
                className={`h-${isPortrait ? "1/5" : "1/4"} bg-gradient-to-b from-blue-50 to-white`}
                style={highContrastMode ? { backgroundColor: '#ffffff', borderBottomWidth: 2, borderBottomColor: '#000000' } : {}}
            >
                <View className={`flex flex-col p-4 mt-${isPortrait ? '8':'4'} gap-3`}>
                    <View className="flex flex-row items-center justify-between">
                        <TouchableOpacity 
                            onPress={() => navigation.goBack()}
                            className="rounded-full w-12 h-12 items-center justify-center"
                            style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(59, 130, 246, 0.1)' }}
                        >
                            <Icon color={highContrastMode ? "#000000" : "#3b82f6"} name='keyboard-arrow-left' size={24} />
                        </TouchableOpacity>
                        <View className="items-center">
                            <Text className='font-avenirBold text-2xl' style={titleStyle}>Settings</Text>
                            <Text className='font-avenirMedium text-sm' style={subtitleStyle}>Configure your app preferences</Text>
                        </View>
                        <View className="w-12 h-12" />
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
                                        label="Points to skip"
                                        initialValue={`${localSettings.skipLines}`}
                                        keyboardType='number-pad'
                                        onChangeText={(value) => {
                                            if (value.length > 0){
                                                handleSettingChange({...localSettings, skipLines:parseInt(value)})
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
                                        label="Visible lines count"
                                        initialValue={`${localSettings.displayLineCount}`}
                                        keyboardType='number-pad'
                                        onChangeText={(value) => {
                                            if (value.length > 0){
                                                handleSettingChange({...localSettings, displayLineCount:parseInt(value)})
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

                <View className='mb-4'>
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
        </View>
    );
};

export default SettingsScreen;