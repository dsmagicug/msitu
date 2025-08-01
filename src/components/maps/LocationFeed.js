import { View, Text } from 'react-native'
import React from 'react'
import { useSelector } from 'react-redux';

const LocationFeed = React.memo(({ latLong }) => {
    const {latitude, longitude, fixType} = latLong
    
    // @ts-ignore
    const { settings } = useSelector(store => store.settings);
    const highContrastMode = settings?.highContrastMode || false;

    const containerStyle = {
        backgroundColor: highContrastMode ? 'rgba(255, 255, 255, 0.95)' : 'rgba(255, 255, 255, 0.6)',
        borderWidth: highContrastMode ? 2 : 0,
        borderColor: highContrastMode ? '#000000' : 'transparent',
        shadowColor: highContrastMode ? '#000000' : 'transparent',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: highContrastMode ? 0.3 : 0,
        shadowRadius: 4,
        elevation: highContrastMode ? 4 : 0,
    };

    const textStyle = {
        color: highContrastMode ? '#000000' : '#374151',
        fontWeight: highContrastMode ? 'bold' : 'normal',
    };

    const labelStyle = {
        color: highContrastMode ? '#000000' : '#1f2937',
        fontWeight: highContrastMode ? 'bold' : '600',
    };

    return (
        <View className="absolute bottom-4 left-1 z-10 p-2 rounded-lg w-2/5" style={containerStyle}>
            <View className="flex flex-row justify-start gap-1 items-center">
                <Text className="font-avenirBold" style={labelStyle}>Lat:</Text>
                <Text className="font-avenirMedium text-sm" style={textStyle}>{latitude}</Text>
            </View>
            <View className="flex flex-row justify-start gap-1 items-center">
                <Text className="font-avenirBold" style={labelStyle}>Long:</Text>
                <Text className="font-avenirMedium text-sm" style={textStyle}>{longitude}</Text>
            </View>
            <View className="flex flex-row justify-start gap-2 items-center">
                <Text className="font-avenirBold" style={labelStyle}>FixType:</Text>
                <Text className="font-avenirMedium text-sm" style={textStyle}>{fixType}</Text>
            </View>
          </View>
      )
});

export default LocationFeed;