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
                strokeWidth={2}
                fillColor="yellow"
                strokeColor="yellow"
                zIndex={5}
            />
    );
});

export default RoverPosition;