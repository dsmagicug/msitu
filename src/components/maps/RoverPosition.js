import React from 'react';
import Animated from 'react-native-reanimated';
import  {
    Circle
} from 'react-native-maps';

const AnimatedCircle = Animated.createAnimatedComponent(Circle);

const RoverPosition = React.memo(({ circleProps }) => {
    return (
        <AnimatedCircle
                animatedProps={circleProps}
                radius={0.2} 
                strokeWidth={1}
                fillColor="#000C66"
                strokeColor="#000C66"
                zIndex={5}
            />
    );
});

export default RoverPosition;