import { View, Text } from 'react-native'
import React from 'react'
import { Checkbox } from 'react-native-paper';

export default function MsCheckbox({label, onPress, status}) {
    return (
        <View className='flex flex-row justify-start'>
            <Checkbox
                uncheckedColor='gray'
                color='green'
                status={status}
                onPress={() => {
                    onPress()
                }}
            />
            <View className='flex justify-center'>
                <Text className='font-avenirMedium'>{label}</Text>
            </View>
        </View>
    )
}