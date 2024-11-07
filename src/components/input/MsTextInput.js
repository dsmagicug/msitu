import { View, TextInput, Animated, Text } from 'react-native'
import { useState, useRef } from 'react'
import colors from 'tailwindcss/colors';
import React from 'react'
import styles from '../../assets/styles';

export default function MsTextInput({ label, placeholder, containerStyle, onChangeText }) {

    const [value, setValue] = React.useState("");

    const [isFocused, setIsFocused] = useState(false);
    const animatedLabelPosition = useRef(new Animated.Value(0)).current;

    const handleFocus = () => {
        setIsFocused(true);
        Animated.timing(animatedLabelPosition, {
            toValue: 1,
            duration: 200,
            useNativeDriver: false,
        }).start();
    };

    const handleBlur = () => {
        setIsFocused(false);
        if (!value) {
            Animated.timing(animatedLabelPosition, {
                toValue: 0,
                duration: 200,
                useNativeDriver: false,
            }).start();
        }
    };

    const labelPosition = animatedLabelPosition.interpolate({
        inputRange: [0, 1],
        outputRange: [18, -5],
    });
    const containerStyles = containerStyle ? containerStyle : {}
    return (

        <View style={[styles.container, containerStyles]}>
            {(!isFocused || value.length < 1) &&
            (<Animated.Text
                style={[
                    styles.label,
                    styles.textMedium,
                    {
                        top: labelPosition,
                        color: isFocused ? colors.teal[900] : colors.gray[700],
                    },
                ]}
            >
                {label}
            </Animated.Text>)}
            <TextInput
                placeholder={isFocused ? '' : placeholder}
                placeholderTextColor={colors.gray[600]}
                style={[
                    styles.input,
                    styles.textMedium,
                    { fontSize: 14 }
                ]}
                onChangeText={(v) => {
                    if (onChangeText) {
                        onChangeText(v);
                        setValue(v)
                    }
                }}
                onFocus={handleFocus}
                onBlur={handleBlur}
            />
        </View>
    );
};