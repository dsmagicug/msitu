import { View, TextInput, Animated, Text, StyleProp, ViewStyle, TextStyle } from 'react-native';
import { useState, useRef, useEffect } from 'react';
import colors from 'tailwindcss/colors';
import React from 'react';
import styles from '../../assets/styles';
import { KeyboardTypeOptions } from 'react-native';

interface MsTextInputProps {
    label: string;
    placeholder?: string;
    containerStyle?: StyleProp<ViewStyle>;
    onChangeText: (text: string) => void;
    initialValue?: string;
    keyboardType?:KeyboardTypeOptions;
}

export default function MsTextInput({ label, placeholder, containerStyle, onChangeText, initialValue, keyboardType }: MsTextInputProps) {

    const [value, setValue] = React.useState<string>(initialValue || "");
    
    const [isFocused, setIsFocused] = useState<boolean>(false);
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

    useEffect(() => {
        if (initialValue) {
            setValue(initialValue);
            setIsFocused(true);
        }
    }, [initialValue]);

    const containerStyles: StyleProp<ViewStyle> = containerStyle ? containerStyle : {};

    return (
        <View style={[styles.container, containerStyles]}>
            {(!isFocused || value.length < 1) && (
                <Animated.Text
                    style={[
                        styles.label,
                        styles.textMedium,
                        {
                            top: labelPosition,
                            color: isFocused ? colors.teal[900] : colors.gray[700],
                        } as TextStyle,
                    ]}
                >
                    {label}
                </Animated.Text>
            )}
            <TextInput
                placeholder={isFocused ? '' : placeholder}
                placeholderTextColor={colors.gray[600]}
                keyboardType={keyboardType? keyboardType:'default'}
                value={value}
                style={[
                    styles.input,
                    styles.textMedium,
                    { fontSize: 14 } as TextStyle
                ]}
                onChangeText={(v: string) => {
                    if (onChangeText) {
                        onChangeText(v);
                        setValue(v);
                    }
                }}
                onFocus={handleFocus}
                onBlur={handleBlur}
            />
        </View>
    );
};