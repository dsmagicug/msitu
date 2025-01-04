import React, { useEffect, useState, useRef, useCallback, useMemo } from "react";
import MapView, {
    PROVIDER_GOOGLE,
    Polyline,
    Polygon,
    Circle,
    MAP_TYPES
} from "react-native-maps";
import { Easing } from "react-native-reanimated";
import { throttle, debounce } from "lodash";
import { useAnimatedRegion } from "../../components/AnimatedMarker";
import RoverPosition from "./RoverPosition";
import LatLong from "../../services/NMEAService";
import { searchClosestPoint, setCyrusLines } from "../../store/pegging";
import { useDispatch, useSelector } from "react-redux";

const MemoizedRoverPosition = React.memo(RoverPosition);

const MsituMapView = React.memo(({ initialRegion, areaMode, basePoints, visibleLines, roverLocation, planting }) => {
    const mapRef = useRef(null);
    const [polygonCoordinates, setPolygonCoordinates] = useState([]);
    const { circleProps, animate } = useAnimatedRegion(initialRegion);
    const prevRoverLocationRef = useRef(null);
    const [selectedPlantingLines, setSelectedPlantingLines] = useState([]);
    const [combinedPoints, setCombinedPoints] = useState([]);
    const { cyrusLines } = useSelector(store => store.pegging);
    const [mapType, setMapType] = useState(MAP_TYPES.SATELLITE)

    const dispatch = useDispatch();

    const throttledAnimate = useCallback(
        throttle((location) => {
            animate({
                latitude: location.latitude,
                longitude: location.longitude,
                duration: 300,
                easing: Easing.linear,
            });
        }, 100),
        [animate]
    );

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
        if (selectedPlantingLines.length > 0) {
            const combinedLines = selectedPlantingLines.reduce((acc, [line, index]) => {
                return acc.concat(line);
            }, []);
            setCombinedPoints(combinedLines);
            const lines = selectedPlantingLines.map(([line, index]) => line);
            dispatch(setCyrusLines(lines));
        }
    }, [selectedPlantingLines]);

    useEffect(() => {
        if (!areaMode && polygonCoordinates.length > 0) {
            setPolygonCoordinates([]);
        }
    }, [areaMode]);

    const debouncedDispatch = useRef(
        debounce((location) => {
            if (combinedPoints.length > 0) {
                dispatch(searchClosestPoint(location, combinedPoints));
            }
        }, 1000)
    ).current;

    useEffect(() => {
        if (roverLocation) {
            const prevRoverLocation = prevRoverLocationRef.current;
            if (LatLong.significantChange(prevRoverLocation, roverLocation)) {
                throttledAnimate(roverLocation);
                debouncedDispatch(roverLocation);
            }
            prevRoverLocationRef.current = roverLocation;
        }
    }, [roverLocation]);

    useEffect(() => {
        if (mapRef.current && initialRegion) {
            mapRef.current.animateCamera({
                center: initialRegion,
                zoom: planting ? 21 : 20,  
                pitch: 0,
                heading: 0,
                altitude: 0
            }, { duration: 1000 });
        }
    }, [initialRegion, planting, mapType]);

    useEffect(()=>{
        if(planting){
            setMapType(MAP_TYPES.TERRAIN)
        }else{
            setMapType(MAP_TYPES.SATELLITE)
        }
    }, [planting])

    return (
        <MapView
            ref={mapRef}
            provider={PROVIDER_GOOGLE}
            showsCompass={false}
            loadingEnabled
            mapType={mapType}
            onPress={handleMapPress}
            region={initialRegion}
            camera={{
                center: initialRegion,
                zoom:  planting ? 21 : 20, 
                heading: 0,
                pitch: 0,
                altitude: 0
            }}
            style={{ flex: 1 }}
        >
            <MemoizedRoverPosition circleProps={memoizedCircleProps} />

            {planting ? (
                // Planting mode content
                cyrusLines.map((line, idx) => (
                    <React.Fragment key={idx}>
                        <Polyline
                            coordinates={line}
                            strokeColor="green"
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
                ))
            ) : (
                // Normal mode content
                <>
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

                    {polygonCoordinates.length > 0 && (
                        <Polygon
                            coordinates={polygonCoordinates}
                            strokeColor="blue"
                            fillColor="rgba(135, 206, 250, 0.3)"
                            strokeWidth={2}
                        />
                    )}

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

                    {visibleLines.map((line, idx) => (
                        <React.Fragment key={idx}>
                            <Polyline
                                coordinates={line}
                                tappable
                                onPress={() => { handlePolyLineClick(line, idx) }}
                                strokeColor={selectedPlantingLines.some(sublist => sublist[1] === idx) ? 'orange' : 'blue'}
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
                </>
            )}
        </MapView>
    );
});

export default MsituMapView;