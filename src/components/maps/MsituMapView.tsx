import React, {useEffect, useState, useRef, useCallback, useMemo} from 'react'; //pointer
import MapView, {
  PROVIDER_GOOGLE,
  Polyline,
  Polygon,
  Circle,
  MAP_TYPES,
  MapType,
  Marker,
} from 'react-native-maps';
import {Easing} from 'react-native-reanimated';
import {throttle} from 'lodash';
import {useAnimatedRegion} from '../AnimatedMarker';
import RoverPosition from './RoverPosition';
import LatLong from '../../services/NMEAService';
import {setCyrusLines} from '../../store/pegging';
import {saveProjectMarkedPoints} from '../../store/projects';
import {useDispatch, useSelector} from 'react-redux';
import {pointToString} from '../../utils';
import {Project} from '../../models';

interface MapProps {
  initialRegion: Region;
  areaMode: boolean;
  basePoints: Array<{latitude: number; longitude: number}>;
  visibleLines: Array<Array<LatLng>>;
  roverLocation: LatLong | null;
  pointerEvents: 'none' | 'box-none' | 'box-only' | 'auto';
  scrollEnabled: boolean;
  zoomEnabled: boolean;
  rotateEnabled: boolean;
  pitchEnabled: boolean;
  planting: boolean;
  rotationDegrees?: number;
  onPolygonCoordsChange?: (coords: Array<LatLng>) => void;
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

// Equirectangular metres between two coords. Accurate to sub-mm at the short
// distances used for pegging, and avoids a native bridge round-trip on the
// hot path (which was contending with the high-frequency rover polling).
const metersBetween = (a: LatLng, b: LatLng): number => {
  const R = 6371000;
  const lat0 = (a.latitude * Math.PI) / 180;
  const dLat = ((b.latitude - a.latitude) * Math.PI) / 180;
  const dLon = (((b.longitude - a.longitude) * Math.PI) / 180) * Math.cos(lat0);
  return R * Math.sqrt(dLat * dLat + dLon * dLon);
};

const MemoizedRoverPosition = React.memo(RoverPosition);

const MsituMapView: React.FC<MapProps> = ({
  initialRegion,
  areaMode,
  basePoints,
  visibleLines,
  roverLocation,
  pointerEvents,
  scrollEnabled,
  zoomEnabled,
  rotateEnabled,
  pitchEnabled,
  planting,
  rotationDegrees = 180,
  onPolygonCoordsChange,
}) => {
  const mapRef = useRef<MapView>(null);
  const [polygonCoordinates, setPolygonCoordinates] = useState<LatLng[]>([]);
  const {circleProps, animate} = useAnimatedRegion(initialRegion);
  const prevRoverLocationRef = useRef<LatLng | null>(null);
  const [selectedPlantingLines, setSelectedPlantingLines] = useState<
    [LatLng[], number][]
  >([]);
  const [combinedPoints, setCombinedPoints] = useState<LatLng[]>([]);
  const [closestPoint, setClosestPoint] = useState<LatLng | null>(null);
  const [mapType, setMapType] = useState<MapType>(MAP_TYPES.HYBRID);

  const dispatch = useDispatch();
  // everything store
  const {cyrusLines, markedPoints} = useSelector((store: any) => store.pegging);
  const {activeProject} = useSelector((store: any) => store.project);
  const {settings} = useSelector((store: any) => store.settings);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const throttledAnimate = useCallback(
    throttle((location: LatLng) => {
      animate({
        latitude: location.latitude,
        longitude: location.longitude,
        duration: 300,
        easing: Easing.linear,
      });
    }, 100),
    [animate],
  );

  const memoizedCircleProps = useMemo(() => {
    try {
      // Ensure we have valid coordinates
      let validCenter;

      // Check roverLocation first
      if (
        roverLocation &&
        !isNaN(roverLocation.latitude) &&
        !isNaN(roverLocation.longitude) &&
        roverLocation.latitude >= -90 &&
        roverLocation.latitude <= 90 &&
        roverLocation.longitude >= -180 &&
        roverLocation.longitude <= 180
      ) {
        validCenter = {
          latitude: roverLocation.latitude,
          longitude: roverLocation.longitude,
        };
      }
      // Check circleProps.center as fallback
      else if (
        circleProps &&
        circleProps.center &&
        !isNaN(circleProps.center.latitude) &&
        !isNaN(circleProps.center.longitude) &&
        circleProps.center.latitude >= -90 &&
        circleProps.center.latitude <= 90 &&
        circleProps.center.longitude >= -180 &&
        circleProps.center.longitude <= 180
      ) {
        validCenter = circleProps.center;
      }
      // Fallback to initialRegion if available
      else if (
        initialRegion &&
        !isNaN(initialRegion.latitude) &&
        !isNaN(initialRegion.longitude) &&
        initialRegion.latitude >= -90 &&
        initialRegion.latitude <= 90 &&
        initialRegion.longitude >= -180 &&
        initialRegion.longitude <= 180
      ) {
        validCenter = {
          latitude: initialRegion.latitude,
          longitude: initialRegion.longitude,
        };
      } else {
        validCenter = {latitude: 0, longitude: 0};
      }

      return {
        ...circleProps,
        center: validCenter,
      };
    } catch (error) {
      return {
        ...circleProps,
        center: {latitude: 0, longitude: 0},
      };
    }
  }, [roverLocation, circleProps, initialRegion]);

  const handleMapPress = useCallback(
    (e: any) => {
      if (areaMode) {
        const newCoord = e.nativeEvent ? e.nativeEvent.coordinate : null;
        // polygonCoordinates
        if (newCoord) {
          setPolygonCoordinates(coords => {
            return [...coords, newCoord];
          });
        }
      }
    },
    [areaMode],
  );

    useEffect(() => {
        if (areaMode && polygonCoordinates.length > 0 && onPolygonCoordsChange) {
            onPolygonCoordsChange(polygonCoordinates);
        }
    }, [areaMode, onPolygonCoordsChange, polygonCoordinates]);


  const handlePolyLineClick = (line: LatLng[], index: number) => {
    setSelectedPlantingLines(prevSelectedLines => {
      const existingIndex = prevSelectedLines.findIndex(
        item => item[1] === index,
      );
      if (existingIndex !== -1) {
        return [
          ...prevSelectedLines.slice(0, existingIndex),
          ...prevSelectedLines.slice(existingIndex + 1),
        ];
      } else {
        return [...prevSelectedLines, [line, index]];
      }
    });
  };

  useEffect(() => {
    if (selectedPlantingLines.length > 0) {
      const combinedLines = selectedPlantingLines.reduce(
        (acc: LatLng[], [line, _]) => {
          const filteredLine = line.filter(
            (_, i) => i % settings.skipLines === 0,
          ); // Only include unskipped points
          return acc.concat(filteredLine);
        },
        [],
      );
      setCombinedPoints(combinedLines);
      const lines = selectedPlantingLines.map(([line, _]) => line);
      //@ts-ignore
      dispatch(setCyrusLines(lines));
    }
  }, [dispatch, selectedPlantingLines, settings.skipLines]);

  useEffect(() => {
    if (!areaMode && polygonCoordinates.length > 0) {
      setPolygonCoordinates([]);
    }
  }, [areaMode, polygonCoordinates.length]);

  // Find the nearest peg entirely in JS. This used to call a native module
  // over the bridge on every move; combined with the high-frequency rover
  // polling, that bridge contention made pegging lag badly. A local scan over
  // combinedPoints is effectively free and updates in real time — no throttle
  // or async needed. setClosestPoint keeps the same object reference when the
  // nearest peg is unchanged, so it only re-renders when the peg actually flips.
  const findClosestPoint = useCallback(
    (location: LatLng) => {
      if (combinedPoints.length === 0) {
        return;
      }
      const lat0 = location.latitude;
      const lon0 = location.longitude;
      const cosLat = Math.cos((lat0 * Math.PI) / 180);
      let closest: LatLng | null = null;
      let minSq = Infinity;
      for (let i = 0; i < combinedPoints.length; i++) {
        const p = combinedPoints[i];
        const dLat = p.latitude - lat0;
        const dLon = (p.longitude - lon0) * cosLat;
        const sq = dLat * dLat + dLon * dLon;
        if (sq < minSq) {
          minSq = sq;
          closest = p;
        }
      }
      if (closest) {
        setClosestPoint(closest);
      }
    },
    [combinedPoints],
  );

  useEffect(() => {
    if (roverLocation) {
      const prevRoverLocation = prevRoverLocationRef.current as LatLong;
      if (LatLong.significantChange(prevRoverLocation, roverLocation)) {
        throttledAnimate(roverLocation);
        if (planting && combinedPoints.length > 0) {
          findClosestPoint(roverLocation);
        }
      }
      prevRoverLocationRef.current = roverLocation;
    }
  }, [
    roverLocation,
    combinedPoints,
    throttledAnimate,
    planting,
    findClosestPoint,
  ]);

  useEffect(() => {
    if (mapRef.current && initialRegion) {
      mapRef.current.animateCamera(
        {
          center: initialRegion,
          zoom: 21,
          pitch: 0,
          heading: 0,
          altitude: 0,
        },
        {duration: 1000},
      );
    }
  }, [initialRegion, planting, mapType]);

  useEffect(() => {
    if (mapRef.current && activeProject) {
      const project = activeProject as Project;
      const centerCoords: LatLng = project.basePoints[0];
      mapRef.current.animateCamera(
        {
          center: centerCoords,
          zoom: 21,
          pitch: 0,
          heading: 0,
          altitude: 0,
        },
        {duration: 1000},
      );
    }
    // Key on the project identity, NOT the whole object: marking a point
    // mutates activeProject.markedPoints (a new reference), and re-running
    // this would snap the camera back to heading 0 / base point and undo the
    // user's manual pinch-rotate. We only want to recenter when a different
    // project actually loads.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [(activeProject as Project)?.id]);

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
  }, [dispatch, markedPoints.length, planting]);

  // Set of already-marked pegs, rebuilt only when the marked points actually
  // change — not a fresh Set per peg per render (and no stale closure, so pegs
  // turn green as soon as they're marked).
  const markedSet = useMemo(() => {
    const pts =
      (activeProject && (activeProject as Project).markedPoints) || [];
    return new Set(pts.map(pointToString));
  }, [activeProject]);

  useEffect(() => {
    if (closestPoint && roverLocation) {
      const distance = metersBetween(closestPoint, roverLocation);
      // Only mark once: without the markedSet guard, standing within 0.1m of a
      // peg re-dispatches every rover tick (~12/s), each rebuilding activeProject
      // and re-rendering all pegs for no reason.
      if (distance <= 0.1 && !markedSet.has(pointToString(closestPoint))) {
        dispatch(saveProjectMarkedPoints([closestPoint]));
      }
    }
  }, [closestPoint, dispatch, roverLocation, markedSet]);

  // Memoised planting overlay (lines + pegs). Moving the rover updates
  // closestPoint every tick; keeping the pegs in a memo means those hundreds
  // of native circles are NOT re-rendered on every move — only the separate
  // highlight circle follows the rover, so planting feels as smooth as normal
  // mode. Recomputes only when the lines, spacing, or marked set change.
  const plantingPegs = useMemo(
    () =>
      cyrusLines.map((line: LatLng[], idx: number) => (
        <React.Fragment key={idx}>
          <Polyline
            coordinates={line}
            strokeColor="#00fa2a65"
            strokeWidth={3.5}
          />
          {line.map((coord, index) => {
            if (index % settings.skipLines !== 0) {
              return null;
            }
            const isMarked = markedSet.has(pointToString(coord));
            return (
              <Circle
                key={`${idx}-${index}`}
                center={coord}
                radius={0.3}
                fillColor={isMarked ? '#00C853' : '#FF3B30'}
                strokeColor={isMarked ? '#00C853' : '#FFFFFF'}
                strokeWidth={1.5}
                zIndex={2}
              />
            );
          })}
        </React.Fragment>
      )),
    [cyrusLines, settings.skipLines, markedSet],
  );

  // Apply an explicit rotation only when `rotationDegrees` actually changes
  // (i.e. the user taps the rotate control) — NOT on every render/roverLocation
  // update. Otherwise the map continuously re-applies this heading and fights
  // the user's own pinch-rotate / tilt gestures, snapping back to it.
  useEffect(() => {
    if (mapRef.current && roverLocation) {
      mapRef.current.animateCamera(
        {
          heading: rotationDegrees % 360,
          pitch: 0,
          zoom: 21,
          center: roverLocation,
        },
        {duration: 1000},
      );
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rotationDegrees]);

  useEffect(() => {
    setMapType(settings.mapStyle);
  }, [settings]);

  // @ts-ignore
  // @ts-ignore
  return (
    <MapView
      ref={mapRef}
      provider={PROVIDER_GOOGLE}
      showsCompass={false}
      loadingEnabled
      mapType={mapType}
      onPress={handleMapPress}
      region={initialRegion}
      pointerEvents={pointerEvents}
      scrollEnabled={scrollEnabled}
      zoomEnabled={zoomEnabled}
      rotateEnabled={rotateEnabled}
      pitchEnabled={pitchEnabled}
      style={{flex: 1}}>
      <MemoizedRoverPosition
        // @ts-ignore
        color={planting ? '#0722eb' : '#FFFF00'}
        circleProps={memoizedCircleProps}
      />

      {planting ? (
        // Planting mode: pegs are memoised (plantingPegs) so they don't
        // re-render as the rover moves; only the highlight below follows it,
        // keeping movement as smooth as normal mode.
        <>
          {plantingPegs}
          {closestPoint && (
            <Circle
              center={closestPoint}
              radius={0.45}
             // fillColor={'#FFEA00'}
              strokeColor={'#FFEA00'}
              strokeWidth={2.5}
              zIndex={4}
            />
          )}
        </>
      ) : (
        // Normal mode content
        <>
          {basePoints &&
            basePoints.map((point, index) => (
              <Circle
                key={`base-point-${index}`}
                center={{latitude: point.latitude, longitude: point.longitude}}
                radius={0.3}
                strokeWidth={2}
                fillColor="#00FF00"
                strokeColor="#000000"
                zIndex={10}
              />
            ))}
          {polygonCoordinates.length >= 3 && (
            <Polygon
              coordinates={polygonCoordinates}
              strokeColor="blue"
              fillColor="rgba(233, 250, 135, 0.3)"
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
          {activeProject &&
            activeProject.markedPoints.map((point: LatLng, index: number) => (
              <Circle
                key={`marked-${index}`}
                center={point}
                radius={0.3}
                fillColor="#1e7102"
                strokeColor="#000000"
                strokeWidth={2}
                zIndex={5}
              />
            ))}
          {visibleLines.map((line, idx) => (
            <React.Fragment key={idx}>
              <Polyline
                coordinates={line}
                tappable={!areaMode}
                onPress={
                  !areaMode
                    ? () => {
                        handlePolyLineClick(line, idx);
                      }
                    : undefined
                }
                strokeColor={
                  selectedPlantingLines.some(sublist => sublist[1] === idx)
                    ? '#0400fb'
                    : '#1310e34f'
                }
                strokeWidth={3}
              />
            </React.Fragment>
          ))}
        </>
      )}
    </MapView>
  );
};

export default MsituMapView;
