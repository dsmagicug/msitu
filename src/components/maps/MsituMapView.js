import React, { useEffect, useState, useRef, useCallback, useMemo } from "react";
import MapView, {
    PROVIDER_GOOGLE,
    Polyline,
    Polygon,
    Circle,
    MAP_TYPES,
    Marker
} from "react-native-maps";
import { Easing } from "react-native-reanimated";
import { throttle, debounce } from "lodash";
import { useAnimatedRegion } from "../../components/AnimatedMarker";
import RoverPosition from "./RoverPosition";
import LatLong from "../../services/NMEAService";
import { useSelector } from "react-redux";
import { searchClosestPoint } from "../../store/pegging";

// Memoize RoverPosition to prevent unnecessary re-renders
const MemoizedRoverPosition = React.memo(RoverPosition);

const MsituMapView = React.memo(({ initialRegion, areaMode, basePoints, visibleLines, roverLocation }) => {
    const mapRef = useRef(null);
    const [polygonCoordinates, setPolygonCoordinates] = useState([]);
    const { circleProps, animate } = useAnimatedRegion(initialRegion);
    const prevRoverLocationRef = useRef(null);
    const [selectedPlanitingLines, setSelectedPlantingLines] =  useState([])
    const [combinedPoints, setCombinedPoints] = useState([])
    const {mode, maxCyrusLines} = useSelector(store=>store.pegging)

    // Throttle the animation and memoize it with useCallback
    const throttledAnimate = useCallback(
        throttle((location) => {
            animate({
                latitude: location.latitude,
                longitude: location.longitude,
                duration: 100,
                easing: Easing.linear,
            });
        }, 100),
        [animate]
    );

    // Memoize circleProps to prevent unnecessary re-renders when roverLocation changes
    const memoizedCircleProps = useMemo(() => ({
        ...circleProps,
        center: roverLocation ? { latitude: roverLocation.latitude, longitude: roverLocation.longitude } : circleProps.center,
    }), [roverLocation, circleProps]);

    const handleMapPress = useCallback((e) => {
        if (areaMode) {
            // setPolygonCoordinates((coords) => [...coords, e.nativeEvent.coordinate]);
        }
    }, [areaMode]);


    const handlePolyLineClick = (line, index) => {
        setSelectedPlantingLines((prevSelectedLines) => {
            const existingIndex = prevSelectedLines.findIndex(item => item[1] === index);
            if (existingIndex !== -1) {
                return [
                    ...prevSelectedLines.slice(0, existingIndex),
                    ...prevSelectedLines.slice(existingIndex + 1)
                ];
            } else {
                return [...prevSelectedLines, [line, index]];
            }
        });
    };

    useEffect(() => {
        if (selectedPlanitingLines.length > 0) {
            const combinedLines = selectedPlanitingLines.reduce((acc, [line, index]) => {
                return acc.concat(line);
            }, []);
            setCombinedPoints(combinedLines); 
        }
    }, [selectedPlanitingLines]);

    useEffect(() => {
        if (!areaMode && polygonCoordinates.length > 0) {
            setPolygonCoordinates([]);
        }
    }, [areaMode, polygonCoordinates]);

    const debouncedDispatch = useRef(
        debounce((location) => {
            if (combinedPoints.length > 0){
                dispatch(searchClosestPoint(location, combinedPoints));
            }
        }, 1000) 
    ).current;

    useEffect(() => {
        if (roverLocation) {
            const prevRoverLocation = prevRoverLocationRef.current;
            if (LatLong.significantChange(prevRoverLocation, roverLocation)) {
                throttledAnimate(roverLocation);
                // also debounce for 1 second
                debouncedDispatch(roverLocation);
            }
            prevRoverLocationRef.current = roverLocation;
        }
    }, [roverLocation, throttledAnimate]);

    return (
        <MapView
            ref={mapRef}
            provider={PROVIDER_GOOGLE}
            mapType={MAP_TYPES.SATELLITE}
            onPress={handleMapPress}
            region={initialRegion}
            style={{ flex: 1 }}
        >

            {/* Base Points as Pins */}
            {basePoints && basePoints.map((point, index) => (
                <Circle 
                    key={`base-point-${index}`}
                    center={{ latitude: point.latitude, longitude: point.longitude }}
                    radius={0.2}
                    strokeWidth={1}
                    fillColor="#00FF00"
                    strokeColor="#00FF00"
                    zIndex={1}
                />
            ))}

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
            <MemoizedRoverPosition circleProps={memoizedCircleProps} />

            {/* Polylines and Circles */}
            {visibleLines.map((line, idx) => (
                <React.Fragment key={idx}>
                    <Polyline
                        coordinates={line}
                        tappable
                        onPress={()=>{handlePolyLineClick(line, idx)}}
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
});

export default MsituMapView;