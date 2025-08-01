import React, { useEffect, useState, useRef, useCallback, useMemo } from "react";
import MapView, {
    PROVIDER_GOOGLE,
    Polyline,
    Polygon,
    Circle,
    MAP_TYPES,
    MapType
} from "react-native-maps";
import { Easing } from "react-native-reanimated";
import { throttle } from "lodash";
import { useAnimatedRegion } from "../AnimatedMarker";
import RoverPosition from "./RoverPosition";
import LatLong from "../../services/NMEAService";
import { setCyrusLines } from "../../store/pegging";
import { saveProjectMarkedPoints } from "../../store/projects";
import { useDispatch, useSelector } from "react-redux";
import { RTNMsitu } from "rtn-msitu";
import { pointToString } from "../../utils";

interface MapProps {
    initialRegion: Region;
    areaMode: boolean;
    basePoints: Array<{ latitude: number; longitude: number }>;
    visibleLines: Array<Array<LatLng>>;
    roverLocation: LatLong | null;
    planting: boolean;
    rotationDegrees?: number;
}

interface Region {
    latitude: number;
    longitude: number;
    latitudeDelta: number;
    longitudeDelta: number;
}

interface LatLng {
    latitude: number;
    longitude: number;
}

const MemoizedRoverPosition = React.memo(RoverPosition);

const MsituMapView: React.FC<MapProps> = ({ 
    initialRegion, 
    areaMode, 
    basePoints, 
    visibleLines, 
    roverLocation, 
    planting, 
    rotationDegrees = 180 
}) => {
    const mapRef = useRef<MapView>(null);
    const [polygonCoordinates, setPolygonCoordinates] = useState<LatLng[]>([]);
    const { circleProps, animate } = useAnimatedRegion(initialRegion);
    const prevRoverLocationRef = useRef<LatLng | null>(null);
    const [selectedPlantingLines, setSelectedPlantingLines] = useState<[LatLng[], number][]>([]);
    const [combinedPoints, setCombinedPoints] = useState<LatLng[]>([]);
    const [closestPoint, setClosestPoint] = useState<LatLng | null>(null);
    const [mapType, setMapType] = useState<MapType>(MAP_TYPES.HYBRID);


    const dispatch = useDispatch();
    // everything store
    const { cyrusLines, markedPoints } = useSelector((store: any) => store.pegging);
    const { activeProject } = useSelector((store: any) => store.project);
    const { settings } = useSelector((store: any) => store.settings);


    const throttledAnimate = useCallback(
        throttle((location: LatLng) => {
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

    const handleMapPress = useCallback((e: any) => {
        if (areaMode) {
            // setPolygonCoordinates((coords) => [...coords, e.nativeEvent.coordinate]);
        }
    }, [areaMode]);

    const handlePolyLineClick = (line: LatLng[], index: number) => {
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
            const combinedLines = selectedPlantingLines.reduce((acc: LatLng[], [line, index]) => {
                const filteredLine = line.filter((_, i) => i % settings.skipLines === 0); // Only include unskipped points
                return acc.concat(filteredLine);
            }, []);
            setCombinedPoints(combinedLines);
            const lines = selectedPlantingLines.map(([line, index]) => line);
            //@ts-ignore
            dispatch(setCyrusLines(lines));
        }
    }, [selectedPlantingLines]);

    useEffect(() => {
        if (!areaMode && polygonCoordinates.length > 0) {
            setPolygonCoordinates([]);
        }
    }, [areaMode]);

    const throttledPointSearch = useCallback(
        throttle(async (location: LatLng) => {
            const result = await RTNMsitu.closetPointRelativeToRoverPosition(location, combinedPoints);
            if (result) {
                setClosestPoint(result);
            }
        }, 1000),
        [combinedPoints]
    );

    useEffect(() => {
        if (roverLocation) {
            const prevRoverLocation = prevRoverLocationRef.current as LatLong;
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
            setMapType(MAP_TYPES.TERRAIN);
        } else {
            setMapType(MAP_TYPES.HYBRID);
            // set what must be set
            if (markedPoints.length > 0) {
                // reset now
                dispatch(setCyrusLines([]));
            }
            setSelectedPlantingLines([]);
        }
    }, [planting]);

    useEffect(() => {
        if (closestPoint && roverLocation) {
            let distance = RTNMsitu.distanceBtnCoords(closestPoint, roverLocation);
            //@ts-ignore assuming it will always bring a value
            if (distance <= 0.1) {
                dispatch(saveProjectMarkedPoints([closestPoint]));
            }
        }
    }, [closestPoint]);

    const useCheckPointExists = () => {
        return useCallback((pointToCheck: LatLng) => {
            if (activeProject && activeProject.markedPoints.length > 0) {
                const pointsSet = new Set(activeProject.markedPoints.map(pointToString)); // use a set, it's faster
                return pointsSet.has(pointToString(pointToCheck));
            } else {
                return false;
            }
        }, [activeProject]);
    };

    const checkPointExists = useCheckPointExists();

    const rotateMap = (degrees: number) => {
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
        rotateMap((rotationDegrees) % 360);
    }, [rotationDegrees]);

    useEffect(()=>{
        setMapType(settings.mapStyle);
    },[settings])

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
                cyrusLines.map((line: LatLng[], idx: number) => (
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
                            if (index % settings.skipLines === 0) {
                                return (
                                    <Circle
                                        key={`${idx}-${index}`}
                                        center={coord}
                                        radius={0.3}
                                        fillColor={isMarked ? 'green' : 'red'}
                                        strokeColor={isMarked ? 'green' : 'red'}
                                        strokeWidth={2}
                                        zIndex={2}
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
                    {
                        activeProject && activeProject.markedPoints.map((point:LatLng, index:number) => (
                            <Circle
                                key={`marked-${index}`}
                                center={point}
                                radius={0.3}
                                fillColor="#00FF00"
                                strokeColor="#00FF00"
                                strokeWidth={2}
                                zIndex={5}
                            />
                        ))
                    }
                    {visibleLines.map((line, idx) => (
                        <React.Fragment key={idx}>
                            <Polyline
                                coordinates={line}
                                tappable
                                onPress={() => { handlePolyLineClick(line, idx) }}
                                strokeColor={selectedPlantingLines.some(sublist => sublist[1] === idx) ? 'orange' : 'blue'}
                                strokeWidth={1.5}
                            />
                        </React.Fragment>
                    ))}
                </>
            )}
        </MapView>
    );
};

export default MsituMapView;