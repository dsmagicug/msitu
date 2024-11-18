import { useCallback } from 'react';
import { MapMarker, MapMarkerProps, Circle } from 'react-native-maps';
import Animated, {
  Easing,
  useAnimatedProps,
  useSharedValue,
  withTiming,
} from 'react-native-reanimated';

interface LatLng {
  latitude: number;
  longitude: number;
  longitudeDelta?: number;
  latitudeDelta?: number;
}

interface AnimateOptions extends LatLng {
  duration?: number;
  easing?: (value: number) => number;
}

const getCenterOffsetForAnchor = (
  anchor: { x: number; y: number },
  markerWidth: number,
  markerHeight: number,
): { x: number; y: number } => ({
  x: markerWidth * 0.5 - markerWidth * anchor.x,
  y: markerHeight * 0.5 - markerHeight * anchor.y,
});

const MARKER_WIDTH = 50;
const MARKER_HEIGHT = 50;
const ANCHOR = { x: 1-10/MARKER_WIDTH, y: 1 - 10 / MARKER_HEIGHT };
const CENTEROFFSET = getCenterOffsetForAnchor(ANCHOR, MARKER_WIDTH, MARKER_HEIGHT);

export const useAnimatedRegion = (location: Partial<LatLng> = {}) => {
  const latitude = useSharedValue(location.latitude);
  const longitude = useSharedValue(location.longitude);
  const latitudeDelta = useSharedValue(location.latitudeDelta);
  const longitudeDelta = useSharedValue(location.longitudeDelta);

  const animatedProps = useAnimatedProps(() => ({
    coordinate: {
      latitude: latitude.value ?? 0,
      longitude: longitude.value ?? 0,
      latitudeDelta: latitudeDelta.value ?? 0,
      longitudeDelta: longitudeDelta.value ?? 0,
    },
    anchor: ANCHOR,
    centerOffset: CENTEROFFSET,
    flat: true,
  }));

  const circleAnimatedProps = useAnimatedProps(() => ({
    center: {
      latitude: latitude.value ?? 0,
      longitude: longitude.value ?? 0,
    },
  }));

  const animate = useCallback(
    (options: AnimateOptions) => {
      const { duration = 500, easing = Easing.inOut(Easing.ease) } = options;

      const animateValue = (
        value: Animated.SharedValue<number | undefined>,
        toValue?: number,
      ) => {
        if (toValue === undefined) return;

        value.value = withTiming(toValue, { duration, easing });
      };

      animateValue(latitude, options.latitude);
      animateValue(longitude, options.longitude);
      animateValue(latitudeDelta, options.latitudeDelta);
      animateValue(longitudeDelta, options.longitudeDelta);
    },
    [latitude, longitude, latitudeDelta, longitudeDelta],
  );

  return {
    markerProps: animatedProps,
    circleProps: circleAnimatedProps,
    animate,
  };
};

type MarkerProps = Omit<MapMarkerProps, 'coordinate'> & {
  coordinate?: MapMarkerProps['coordinate'];
};

export const AnimatedMarker = Animated.createAnimatedComponent(
  MapMarker as React.ComponentClass<MarkerProps>,
);


