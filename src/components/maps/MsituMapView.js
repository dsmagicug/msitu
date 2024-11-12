import MapView, {
    PROVIDER_GOOGLE,
    Polyline,
    Polygon,
    Circle,
    MAP_TYPES
} from 'react-native-maps';
import React, { useEffect, useState, useRef } from "react";
import Animated, { Easing } from "react-native-reanimated"
import { AnimatedMarker, useAnimatedRegion } from '../../components/AnimatedMarker';
import Dot from '../../components/maps/Dot';
import { useSelector } from 'react-redux';

const AnimatedCircle = Animated.createAnimatedComponent(Circle);
const MsituMapView = ({initialRegion,  areaMode, visibleLines }) => {

    const mapRef = useRef(null);
    const roverPositionRef = useRef(null);
    const [polygonCoordinates, setPolygonCoordinates] = useState([])

    const { markerProps, circleProps, animate } = useAnimatedRegion(initialRegion);

    const { roverLocation } = useSelector(store => store.nmeaListener)



    const handlePolylineClick = (line) => {
        console.log(line)
    };

    useEffect(() => {
        if (mapRef.current) {
            mapRef.current.animateToRegion(initialRegion, 1000);
        }

    }, [initialRegion]);

    const handleMapPress = (e) => {
        if (areaMode) {
            const newCoordinate = e.nativeEvent.coordinate;
            setPolygonCoordinates([...polygonCoordinates, newCoordinate]);
        }

    };

    useEffect(() => {
        if (!areaMode && polygonCoordinates.length > 0) {
            setPolygonCoordinates([])
        }
    }, [areaMode, polygonCoordinates])

    useEffect(() => {
        if (roverLocation) {
            animate({
                latitude: roverLocation.latitude,
                longitude: roverLocation.longitude,
                duration: 10,
                easing: Easing.linear
            });
        }
    }, [roverPositionRef, roverLocation])

    return (

        <MapView
            ref={mapRef}
            provider={PROVIDER_GOOGLE}
            onPress={handleMapPress}
            mapType={MAP_TYPES.SATELLITE}
            showsUserLocation={false}
            showsScale={false}
            showsMyLocationButton={true}
            region={initialRegion}
            style={{ flex: 1 }}

        >
            {polygonCoordinates.length > 0 && (
                <Polygon
                    coordinates={polygonCoordinates}
                    strokeColor="blue"
                    fillColor="rgba(135, 206, 250, 0.3)"
                    strokeWidth={2}
                />
            )}

            {
                polygonCoordinates.map((coord, index) => (
                    <Circle
                        key={index}
                        center={coord}
                        radius={0.5}
                        strokeWidth={8}
                        fillColor="skyblue"
                        strokeColor="skyblue"
                        zIndex={1}
                    />
                ))
            }

            {/* <AnimatedMarker animatedProps={markerProps}>
            <Dot className="w-3 h-3 rounded-full bg-yellow-200" />
            </AnimatedMarker> */}
            
            <AnimatedCircle
                animatedProps={circleProps}
                radius={0.4} 
                strokeWidth={2}
                fillColor="yellow"
                strokeColor="yellow"
                zIndex={1}
            />

            {
                visibleLines.map((line, idx) => (
                    <React.Fragment key={idx}>
                        <Polyline
                            coordinates={line}
                            tappable={true}
                            onPress={() => handlePolylineClick(line)}
                            strokeColor="blue"
                            strokeWidth={1.5}
                        />
                        {
                            line.map((coord, index) => (
                                <Circle
                                    key={`${idx}-${index}`}
                                    center={coord}
                                    radius={0.4}
                                    fillColor="red"
                                    strokeColor="red"
                                    zIndex={1}
                                />
                            ))
                        }

                    </React.Fragment>
                ))
            }

        </MapView>

    )
}
export default MsituMapView;
