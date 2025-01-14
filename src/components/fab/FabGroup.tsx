import { Pressable, View, ViewStyle, TextStyle, Image, TouchableOpacity } from 'react-native';
import React from 'react';
import Animated, {
  Easing,
  Extrapolation,
  interpolate,
  useAnimatedStyle,
  useDerivedValue,
  useSharedValue,
  withDelay,
  withSpring,
  withTiming,
} from 'react-native-reanimated';
import Icon from 'react-native-vector-icons/MaterialIcons';

interface Action {
  icon: string | { uri: number };
  name: string;
  initialPosition: number;
  backgroundColor?: string;
  disabled?:boolean|undefined;
}

interface FabGroupProps {
  style?: ViewStyle | TextStyle;
  actions: Action[];
  onActionPress: (name: string) => void;
  toggleIcon?: string;
}

const FabGroup: React.FC<FabGroupProps> = ({ style, actions, onActionPress }) => {

  const isOpen = useSharedValue(true);
  const openState = useDerivedValue(() => isOpen.value);
  const progress = useDerivedValue(() =>
    openState.value ? withTiming(1) : withTiming(0)
  );
  const positions = actions.map(action => useSharedValue(action.initialPosition));

  const handlePress = () => {
    const config = {
      easing: Easing.bezier(0.68, -0.6, 0.32, 1.6),
      duration: 500,
    };

    positions.forEach((position, index) => {
      if (openState.value) {
        position.value = withDelay(50 * index, withTiming(30, config));
      } else {
        position.value = withDelay(200 - 100 * index, withSpring(actions[index].initialPosition));
      }
    });
    isOpen.value = !isOpen.value;
  };

  const plusIcon = useAnimatedStyle(() => {
    return {
      transform: [{ rotate: `${progress.value * 45}deg` }],
    };
  });

  const animatedStyles = positions.map((position, index) => {
    return useAnimatedStyle(() => {
      const scale = interpolate(
        position.value,
        [30, actions[index].initialPosition],
        [0, 1],
        Extrapolation.CLAMP
      );
      return {
        bottom: position.value,
        transform: [{ scale: scale }],
      };
    });
  });

  const renderIcon = (icon: string | { uri: number }, disabled : boolean | undefined) => {
    if (typeof icon === 'string') {
      return <Icon name={icon} size={26} color={disabled ? 'gray':'#FFF'} />;
    } else {
      //@ts-ignore
      return <Image source={icon} style={{ width: 26, height: 26, tintColor: disabled ? 'gray':'#FFF'  }} />;
    }
  };

  return (
    <View className="absolute bottom-2 right-2" style={style}>
      {actions.map((action, index) => (
        <Animated.View 
          key={index} 
          className={`bg-${action.backgroundColor ? action.backgroundColor : action.disabled ? 'blue-50':  'blue-700'} absolute bottom-3 right-5 rounded-full w-14 h-14 items-center justify-center`}
          style={animatedStyles[index]}
        >
          <TouchableOpacity 
            disabled={action.disabled}
            onPress={() => onActionPress(action.name)}>
            <View className="w-14 h-14 items-center justify-center">
              {renderIcon(action.icon, action.disabled)}
            </View>
          </TouchableOpacity>
        </Animated.View>
      ))}
      <Pressable className="bg-blue-700 absolute bottom-3 right-3 rounded-full w-16 h-16 items-center justify-center" onPress={handlePress}>
        <Animated.View className="w-16 h-16 items-center justify-center" 
          style={plusIcon}>
          <Icon name="add" size={26} color="#FFF" />
        </Animated.View>
      </Pressable>
    </View>
  );
};

export default FabGroup;