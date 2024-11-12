import { View, Text } from 'react-native'
import React from 'react'

const Dot = ({className}) => {
  return (
    <View className={className ? className :'w-3 h-3 rounded-full bg-blue-700'}/>
  )
}

export default Dot