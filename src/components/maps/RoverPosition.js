import React from 'react';
import Animated from 'react-native-reanimated';
import  {
    Circle
} from 'react-native-maps';

const AnimatedCircle = Animated.createAnimatedComponent(Circle);

const RoverPosition = React.memo(({ circleProps, color="#000C66"}) => {
    return (
        <AnimatedCircle
                animatedProps={circleProps}
                radius={0.2} 
                strokeWidth={1}
                fillColor={color}
                strokeColor={color}
                zIndex={20}
            />
    );
});

export default RoverPosition;