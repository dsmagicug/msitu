import { useNavigation } from '@react-navigation/native';
import React, { useState, useEffect } from 'react';
import {
    View, 
    Text, 
    TouchableOpacity, 
    Image, 
    ScrollView,
    Dimensions,
    useWindowDimensions
} from 'react-native';
import {MAP_TYPES} from 'react-native-maps'
import Icon from 'react-native-vector-icons/MaterialIcons';
import colors from 'tailwindcss/colors';
import { Switch } from 'react-native-paper';
import MsTextInput from '../../components/input/MsTextInput';
import Animated, { useAnimatedStyle, useSharedValue, withTiming } from 'react-native-reanimated';
import { Picker } from '@react-native-picker/picker';
import styles from '../../assets/styles';
const { width: screenWidth } = Dimensions.get("screen");

const Settings: React.FC = () => {
    const navigation = useNavigation();
    const [mode, setMode] = useState<boolean>(true);
    const [isPortrait, setIsPortrait] = useState<boolean>(true);
    const [mapStyle, setMapStyle] = useState<string>("SATELLITE")
   
    const { width: screenWidth } = useWindowDimensions();
    const fadeAnim = useSharedValue(mode ? 1 : 0);

    useEffect(() => {
        fadeAnim.value = withTiming(mode ? 1 : 0, { duration: 500 });
    }, [mode, fadeAnim]);

    const animatedStyle = useAnimatedStyle(() => {
        return {
            opacity: fadeAnim.value,
            height: fadeAnim.value === 0 ? 0 : 'auto',
        };
    });


    const { width, height } = useWindowDimensions();




    // Track orientation
    useEffect(() => {
        setIsPortrait(height > width);
    }, [width, height]);

    const onToggleAppMode = () => setMode(!mode);

    return (
        <View className="h-full" style={{ backgroundColor: "#f5f5f5" }}>
            {/* Top Header */}
            <View className={`h-${isPortrait ? "1/6 mt-15" : "1/4"} bg-white`}>
                <View className={`flex flex-col p-2 mt-${isPortrait ? '5':'3'} gap-5`}>
                    <TouchableOpacity onPress={() => navigation.goBack()}>
                        <Icon color={colors.gray["500"]} name='keyboard-arrow-left' size={40} />
                    </TouchableOpacity>
                    <Text className='font-avenirBold mx-3 text-3xl'>Settings</Text>
                </View>
            </View>

            {/* Bottom View */}
            <ScrollView className="text-black">
                <View className='mx-5 mt-8 flex flex-col'>
                    <Text className='font-avenirMedium'>APP MODES</Text>
                    <View className='flex flex-row justify-between items-center mt-2 p-4 bg-white rounded-md'>
                        <View className='flex flex-row justify-start gap-10'>
                            <Image source={require("../../assets/plant.png")} style={{ width: 26, height: 26, tintColor: colors.gray[600] }} />
                            <Text className='font-avenir'>Planting Mode</Text>
                        </View>
                        <Switch value={mode} onValueChange={onToggleAppMode} />
                    </View>
                    <View className='flex flex-row justify-between items-center mt-2 p-4 bg-white rounded-md'>
                        <View className='flex flex-row justify-start gap-10'>
                            <Image source={require("../../assets/surveying.png")} style={{ width: 26, height: 26, tintColor: colors.gray[600] }} />
                            <Text className='font-avenir'>Survey Mode</Text>
                        </View>
                        <Switch value={!mode} onValueChange={onToggleAppMode} />
                    </View>
                </View>

                <Animated.View style={animatedStyle}>
                    <View className='mx-5 mt-5 flex flex-col'>
                        <Text className='font-avenirMedium'>PLANTING LINE SETTINGS</Text>
                        <View className='flex flex-col mt-2 p-4 bg-white rounded-md'>
                            <View className='flex flex-row justify-start gap-5 items-center'>
                                <Image source={require("../../assets/point.png")} style={{ width: 26, height: 26, tintColor: colors.gray[600] }} />
                                <MsTextInput
                                    label="No. of points to skip"
                                    initialValue="5"
                                    containerStyle={{ width: screenWidth / 1.5 }}
                                    onChangeText={(e) => {}}
                                />
                            </View>
                            <Text className='text-xs font-avenir mt-1 text-justify text-teal-800'>
                                During planting, how many points should be skipped between pegs?
                            </Text>
                        </View>
                        <View className='flex flex-col mt-2 p-4 bg-white rounded-md'>
                            <View className='flex flex-row justify-start gap-5 items-center'>
                                <Image source={require("../../assets/steel-mesh.png")} style={{ width: 26, height: 26, tintColor: colors.gray[600] }} />
                                <MsTextInput
                                    label="No. of visible lines"
                                    initialValue="10"
                                    containerStyle={{ width: screenWidth / 1.5 }}
                                    onChangeText={(e) => {}}
                                />
                            </View>
                            <Text className='text-xs font-avenir mt-1 text-justify text-teal-800'>
                            This represents the number of visible planting lines displayed when the project loads.
                            </Text>
                        </View>
                    </View>
                </Animated.View>

                <View className='mx-5 mt-8 flex flex-col'>
                    <Text className='font-avenirMedium'>CLOUD SETTINGS</Text>
                    <View className='flex flex-col mt-2 p-4 bg-white rounded-md'>
                        <View className='flex flex-row justify-between items-center'>
                            <View className='flex flex-row justify-start items-center gap-4'>
                            <Image source={require("../../assets/server.png")} style={{ width: 26, height: 26, tintColor: colors.gray[600] }} />
                            <View className='w-9/12'>
                            <MsTextInput
                                label="Cloud Sync API"
                                onChangeText={(e) => console.log(e)}
                            />
                            </View>
                            </View>
                            <TouchableOpacity className='flex mt-5 p-2 border rounded-lg border-teal-600 items-center'>
                                <Text className='font-avenirMedium text-xs text-teal-600'>Sync Now.</Text>
                            </TouchableOpacity>
                        </View>
                        <Text className='text-xs font-avenir mt-1 text-justify text-teal-800'>
                            Projects will be uploaded to this API whenever an internet connection is available.
                        </Text>
                    </View>
                </View>

                <View className='mx-5 mt-8 flex flex-col'>
                    <Text className='font-avenirMedium'>MAP VIEW SETTINGS</Text>
                    <View className='flex flex-col mt-2 p-4 bg-white rounded-md'>
                        <View className='flex flex-row justify-between items-center'>
                            <View className='flex flex-row justify-start items-center gap-4'>
                            <Image source={require("../../assets/map.png")} style={{ width: 26, height: 26, tintColor: colors.gray[600] }} />
                            <View className='w-9/12'>
                            <Picker
                                style={{ width: screenWidth/2, marginTop: 5 }}
                                selectedValue={mapStyle}
                                onValueChange={(itemValue, itemIndex) =>
                                    setMapStyle(itemValue)
                                }>
                                <Picker.Item style={{ ...styles.textMedium }} label="SETELLITE" value={MAP_TYPES.SATELLITE} />
                                <Picker.Item style={{ ...styles.textMedium }} label="TERRAIN" value={MAP_TYPES.TERRAIN} />
                                <Picker.Item style={{ ...styles.textMedium }} label="HYBRID" value={MAP_TYPES.HYBRID} />
                                <Picker.Item style={{ ...styles.textMedium }} label="HYBRID FLYOVER" value={MAP_TYPES.HYBRID_FLYOVER} />
                                <Picker.Item style={{ ...styles.textMedium }} label="STANDARD" value={MAP_TYPES.STANDARD} />
                            </Picker>
                            </View>
                            </View>
                        </View>
                        <Text className='text-xs font-avenir mt-1 text-justify text-teal-800'>
                            Set map View style, default is SATELLITE
                        </Text>
                    </View>
                </View>
            </ScrollView>
        </View>
    );
};
export default Settings;
