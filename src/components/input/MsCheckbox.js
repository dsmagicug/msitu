import { View, Text } from 'react-native'
import React from 'react'
import { Checkbox } from 'react-native-paper';
import { useDispatch, useSelector } from 'react-redux';

export default function MsCheckbox({label, onPress, status, highContrast}) {

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
                <Text className='font-avenirMedium' style={{color: highContrast ? '#ffffff' : '#000000'}}>{label}</Text>
            </View>
        </View>
    )
}