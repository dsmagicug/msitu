import React, { useEffect, useState, useRef, useCallback, useMemo } from "react";
import MapView, {
    PROVIDER_GOOGLE,
    Polyline,
    Polygon,
    Circle,
    MAP_TYPES} from "react-native-maps";
import { Easing } from "react-native-reanimated";
import { throttle } from "lodash";
import { useAnimatedRegion } from "../../components/AnimatedMarker";
import RoverPosition from "./RoverPosition";
import LatLong from "../../services/NMEAService";
import { setCyrusLines } from "../../store/pegging";
import { saveProjectMarkedPoints } from "../../store/projects"
import { useDispatch, useSelector } from "react-redux";
import { RTNMsitu } from "rtn-msitu"
import { pointToString } from "../../utils";

const MemoizedRoverPosition = React.memo(RoverPosition);

const MsituMapView = React.memo(({ initialRegion, areaMode, basePoints, visibleLines, roverLocation, planting, rotationDegrees = 180 }) => {
    const mapRef = useRef(null);
    const [polygonCoordinates, setPolygonCoordinates] = useState([]);
    const { circleProps, animate } = useAnimatedRegion(initialRegion);
    const prevRoverLocationRef = useRef(null);
    const [selectedPlantingLines, setSelectedPlantingLines] = useState([]);
    const [combinedPoints, setCombinedPoints] = useState([]);
    const { cyrusLines, markedPoints, skipPoints } = useSelector(store => store.pegging);
    const { activeProject } = useSelector(store => store.project)
    const [closestPoint, setClosestPoint] = useState(null)
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
                const filteredLine = line.filter((_, i) => i % skipPoints === 0); // Only include unskipped points
                return acc.concat(filteredLine);
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

    const throttledPointSearch = useCallback(
        throttle(async (location) => {
            const result = await RTNMsitu.closetPointRelativeToRoverPosition(location, combinedPoints);
            if (result) {
                setClosestPoint(result)
            }
        }, 1000),
        [combinedPoints]
    );


    useEffect(() => {
        if (roverLocation) {
            const prevRoverLocation = prevRoverLocationRef.current;
            if (LatLong.significantChange(prevRoverLocation, roverLocation)) {
                throttledAnimate(roverLocation);
                if (planting && combinedPoints.length > 0) {
                    throttledPointSearch(roverLocation);
                }
            }
            prevRoverLocationRef.current = roverLocation;
        }
    }, [roverLocation, combinedPoints]);

    useEffect(() => {
        if (mapRef.current && initialRegion) {
            mapRef.current.animateCamera({
                center: initialRegion,
                zoom: 21,
                pitch: 0,
                heading: 0,
                altitude: 0
            }, { duration: 1000 });
        }
    }, [initialRegion, planting, mapType]);

    useEffect(() => {
        if (planting) {
            setMapType(MAP_TYPES.TERRAIN)
        } else {
            setMapType(MAP_TYPES.SATELLITE)
            // set what must be set
            if (markedPoints.length > 0) {
                // reset now
                dispatch(setCyrusLines([]))
            }
            setSelectedPlantingLines([])

        }
    }, [planting])

    useEffect(() => {

        if (closestPoint && roverLocation) {
            const distance = RTNMsitu.distanceBtnCoords(closestPoint, roverLocation)
            if (distance <= 0.1) {
                dispatch(saveProjectMarkedPoints([closestPoint]))
            }
        }
    }, [closestPoint])


    const useCheckPointExists = () => {
        return useCallback((pointToCheck) => {
            if (activeProject && activeProject.markedPoints.length > 0) {
                const pointsSet = new Set(activeProject.markedPoints.map(pointToString)); // use a set, it's faster
                return pointsSet.has(pointToString(pointToCheck));
            } else {
                return false;
            }
        }, [activeProject]);
    };

    const checkPointExists = useCheckPointExists();

    const rotateMap = (degrees) => {
        if (mapRef.current && roverLocation) {
            mapRef.current.animateCamera({
                heading: degrees,
                pitch: 0,
                zoom: 21,
                center: roverLocation
            }, { duration: 1000 });
        }
    };


    useEffect(() => {
        rotateMap((rotationDegrees) % 360)
    }, [rotationDegrees])


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
                zoom: 21,
                heading: 0,
                pitch: 0,
                altitude: 0
            }}
            style={{ flex: 1 }}
        >
            <MemoizedRoverPosition
                color={planting ? '#000C66' : '#FFFF00'}
                circleProps={memoizedCircleProps} />

            {planting ? (
                // Planting mode content
                cyrusLines.map((line, idx) => (
                    <React.Fragment key={idx}>
                        {
                            closestPoint &&
                            <Circle

                                center={closestPoint}
                                radius={0.6}
                                strokeColor={"blue"}
                                strokeWidth={1}
                                zIndex={1}
                            />
                        }
                        <Polyline
                            coordinates={line}
                            strokeColor="green"
                            strokeWidth={1.5}
                        />
                        {line.map((coord, index) => {
                            const isMarked = checkPointExists(coord);
                            if(index % skipPoints === 0){
                                return (
                                    <Circle
                                        key={`${idx}-${index}`}
                                        center={coord}
                                        radius={0.3}
                                        fillColor={isMarked ? 'green' : 'red'}
                                        strokeColor={isMarked ? 'green' : 'red'}
                                        strokeWidth={2}
                                        zIndex={1}
                                    />
                                );
                            }
                            
                        })}
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
                            fillColor="#FFFF00"
                            strokeColor="#FFFF00"
                            zIndex={10}
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
                            {line.map((coord, index) => {
                                const isMarked = checkPointExists(coord);
                                return (
                                    <Circle
                                        key={`${idx}-${index}`}
                                        center={coord}
                                        radius={0.3}
                                        fillColor={isMarked ? '#00FF00' : 'red'}
                                        strokeColor={isMarked ? '#00FF00' : 'red'}
                                        strokeWidth={2}
                                        zIndex={1}
                                    />
                                );
                            })}
                        </React.Fragment>
                    ))}
                </>
            )}
        </MapView>
    );
});

export default MsituMapView;