import { View, TextInput, Animated, Text, StyleProp, ViewStyle, TextStyle } from 'react-native';
import { useState, useRef, useEffect } from 'react';
import colors from 'tailwindcss/colors';
import React from 'react';
import styles from '../../assets/styles';
import { KeyboardTypeOptions } from 'react-native';
import Reanimated, { 
  useSharedValue, 
  useAnimatedStyle, 
  withTiming, 
  withSpring,
  interpolate,
  Extrapolation
} from 'react-native-reanimated';

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
    
    const animatedLabelPosition = useSharedValue(initialValue ? 1 : 0);
    const animatedBorderWidth = useSharedValue(0);
    const animatedScale = useSharedValue(1);
    const animatedOpacity = useSharedValue(0.6);

    const handleFocus = () => {
        setIsFocused(true);
        animatedLabelPosition.value = withSpring(1, { damping: 15, stiffness: 150 });
        animatedBorderWidth.value = withTiming(2, { duration: 200 });
        animatedScale.value = withSpring(1.02, { damping: 15, stiffness: 150 });
        animatedOpacity.value = withTiming(1, { duration: 200 });
    };

    const handleBlur = () => {
        setIsFocused(false);
        animatedBorderWidth.value = withTiming(0, { duration: 200 });
        animatedScale.value = withSpring(1, { damping: 15, stiffness: 150 });
        animatedOpacity.value = withTiming(0.6, { duration: 200 });
        
        if (!value) {
            animatedLabelPosition.value = withSpring(0, { damping: 15, stiffness: 150 });
        }
    };

    const labelAnimatedStyle = useAnimatedStyle(() => {
        const translateY = interpolate(
            animatedLabelPosition.value,
            [0, 1],
            [18, -5],
            Extrapolation.CLAMP
        );
        
        const scale = interpolate(
            animatedLabelPosition.value,
            [0, 1],
            [1, 0.85],
            Extrapolation.CLAMP
        );

        return {
            transform: [{ translateY }, { scale }],
            color: isFocused ? colors.blue[600] : colors.gray[600],
        };
    });

    const containerAnimatedStyle = useAnimatedStyle(() => {
        return {
            transform: [{ scale: animatedScale.value }],
            opacity: animatedOpacity.value,
        };
    });

    const borderAnimatedStyle = useAnimatedStyle(() => {
        return {
            borderBottomWidth: animatedBorderWidth.value,
            borderBottomColor: colors.blue[500],
        };
    });

    useEffect(() => {
        if (initialValue) {
            setValue(initialValue);
            setIsFocused(true);
            animatedLabelPosition.value = withSpring(1, { damping: 15, stiffness: 150 });
        }
    }, [initialValue]);

    const containerStyles: StyleProp<ViewStyle> = containerStyle ? containerStyle : {};

    return (
        <Reanimated.View style={[styles.container, containerStyles, containerAnimatedStyle]}>
            <Reanimated.Text
                style={[
                    styles.label,
                    styles.textMedium,
                    labelAnimatedStyle,
                ]}
            >
                {label}
            </Reanimated.Text>
            <Reanimated.View style={[styles.inputContainer, borderAnimatedStyle]}>
                <TextInput
                    placeholder={isFocused ? '' : placeholder}
                    placeholderTextColor={colors.gray[400]}
                    keyboardType={keyboardType? keyboardType:'default'}
                    value={value}
                    style={[
                        styles.input,
                        styles.textMedium,
                        { fontSize: 14, color: colors.gray[800] } as TextStyle
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
            </Reanimated.View>
        </Reanimated.View>
    );
};