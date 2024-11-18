import React, { useEffect, useState, useRef } from "react";
import MapView, {
    PROVIDER_GOOGLE,
    Polyline,
    Polygon,
    Circle,
    MAP_TYPES,
} from "react-native-maps";
import { Easing } from "react-native-reanimated";
import { throttle } from "lodash";
import { useAnimatedRegion } from "../../components/AnimatedMarker";
import RoverPosition from "./RoverPosition";


const MsituMapView = ({ initialRegion, areaMode, visibleLines, roverLocation }) => {
    const mapRef = useRef(null);
    const [polygonCoordinates, setPolygonCoordinates] = useState([]);
    const {  circleProps, animate } = useAnimatedRegion(initialRegion);

    // Throttle the animation
    const throttledAnimate = useRef(
        throttle((location) => {
            animate({
                latitude: location.latitude,
                longitude: location.longitude,
                duration: 100,
                easing: Easing.linear,
            });
        }, 100)
    ).current;

    const handleMapPress = (e) => {
        if (areaMode) {
            setPolygonCoordinates((coords) => [...coords, e.nativeEvent.coordinate]);
        }
    };

    useEffect(() => {
        if (!areaMode && polygonCoordinates.length > 0) {
            setPolygonCoordinates([]);
        }
    }, [areaMode]);

    useEffect(() => {
        if (roverLocation) {
            throttledAnimate(roverLocation);
        }
    }, [roverLocation]);

    return (
        <MapView
            ref={mapRef}
            provider={PROVIDER_GOOGLE}
            mapType={MAP_TYPES.SATELLITE}
            onPress={handleMapPress}
            region={initialRegion}
            style={{ flex: 1 }}
        >
            {/* Polygon */}
            {polygonCoordinates.length > 0 && (
                <Polygon
                    coordinates={polygonCoordinates}
                    strokeColor="blue"
                    fillColor="rgba(135, 206, 250, 0.3)"
                    strokeWidth={2}
                />
            )}

            {/* Dots for Polygon */}
            {polygonCoordinates.map((coord, index) => (
                <Circle
                    key={index}
                    center={coord}
                    radius={0.5}
                    strokeWidth={8}
                    fillColor="skyblue"
                    strokeColor="skyblue"
                    zIndex={1}
                />
            ))}

            {/* Animated Circle */}
            <RoverPosition circleProps={circleProps} />

            {/* Polylines and Circles */}
            {visibleLines.map((line, idx) => (
                <React.Fragment key={idx}>
                    <Polyline
                        coordinates={line}
                        tappable
                        onPress={() => console.log(line)}
                        strokeColor="blue"
                        strokeWidth={1.5}
                    />
                    {line.map((coord, index) => (
                        <Circle
                            key={`${idx}-${index}`}
                            center={coord}
                            radius={0.3}
                            strokeColor="red"
                            strokeWidth={2}
                            zIndex={1}
                        />
                    ))}
                </React.Fragment>
            ))}
        </MapView>
    );
};

export default MsituMapView;
