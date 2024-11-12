import { View, Text } from 'react-native'
import React from 'react'

export default function LocationFeed({latLong}) {
    const {latitude, longitude, fixType} = latLong
  return (
    <View className="absolute bottom-4 right-1 z-10 bg-white/60 p-2 rounded-lg w-2/5">
        <View className="flex flex-row justify-start gap-1 items-center">
            <Text className="font-avenirBold">Lat:</Text>
            <Text className="font-avenirMedium text-sm" >{latitude}</Text>
        </View>
        <View className="flex flex-row justify-start gap-1 items-center">
            <Text className="font-avenirBold">Long:</Text>
            <Text className="font-avenirMedium text-sm">{longitude}</Text>
        </View>
        <View className="flex flex-row justify-start gap-2 items-center">
            <Text className="font-avenirBold">FixType:</Text>
            <Text className="font-avenirMedium text-sm">{fixType}</Text>
        </View>
      </View>
  )
}