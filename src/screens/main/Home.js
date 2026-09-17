import { Text, View, Alert, ToastAndroid } from 'react-native';
import Geolocation from '@react-native-community/geolocation';
import React, { useEffect, useState, useMemo } from 'react';
import { convertLinesToLatLong, setLock, setIndices, setScaledPlanitingLines } from '../../store/projects';
import { useDispatch, useSelector } from 'react-redux';
import AnimatedLoader from 'react-native-animated-loader';
import { initializeBT } from '../../store/bluetooth';
import MsituMapView from '../../components/maps/MsituMapView.tsx';
import LocationFeed from '../../components/maps/LocationFeed';
import TopNavBar from '../../components/misc/TopNavBar';
import styles from '../../assets/styles';
import LatLong from '../../services/NMEAService';
import {FixType, RTNMsitu} from 'rtn-msitu';
import NewProject from '../../components/projects/NewProject';
import { setShowCreateNewProjects } from '../../store/modal';
import FabGroup from '../../components/fab/FabGroup';
import { loadSettings } from '../../store/settings';
import {treeEstimate, convertToMeters} from "../../utils";
import MetricsConfigModal from '../../components/misc/MetricsConfigModal';


const Home = ({ navigation }) => {

  const [areaMode, setAreaMode] = useState(false);
  const [planting, setPlanting] = useState(false);
  const [area, setArea] = useState(0.00);
  const [treeCount, setTreeCount] = useState(0);
  const [showPlantingConfig, setShowPlantingConfig] = useState(false);
  const [meshType, setMeshType] = useState('S');
  const [gapUnit, setGapUnit] = useState('meter');
  const [gapSize, setGapSize] = useState(3.6);
  const [polygonCoordinates, setPolygonCoordinates] = useState([]);
  const [initialRegion, setInitialRegion] = useState({
    latitude: 0.04694938133710109,
    longitude: 32.46314182880414,
    latitudeDelta: 0.01,
    longitudeDelta: 0.01,
  });
  const [roverLocation, setRoverLocation] = useState(null);

  const [mapRotateDegrees, setMapRotateDegrees] =  useState(180);
  const [mapType, setMapType] = useState('SATELLITE');

  const { activeProject, visibleLines, loading, scaledPlantingLines, lock } = useSelector(store => store.project);
  const { selectedDevice } = useSelector(store => store.bluetooth);
  const [activeMinusLines, setActiveMinusLines] =  useState(false);
  const [activePlusLines, setActivePlusLines] =  useState(true);
  const { cyrusLines } = useSelector(store => store.pegging);
  const {settings } = useSelector(store => store.settings);
  const { init } = useSelector(store => store.bluetooth);
  const modalStore = useSelector(selector => selector.modals);
  const dispatch = useDispatch();

  // Poll-and-drain the rover stream instead of consuming `onDataReceived`.
  // `onDataReceived` replays every buffered message in order, so when the
  // native read/emit cadence falls behind the socket (seen after the
  // bluetooth-classic + React Native upgrade) the position walks through a
  // growing backlog of old fixes and only catches up ~30-40s later. Draining
  // the buffer on each tick and keeping only the newest valid fix keeps the
  // shown position real-time, matching what tools like SW Maps display.
  useEffect(() => {
    if (!selectedDevice) { return; }
    let cancelled = false;
    let timeoutId;

    // Sampling cadence. Small enough to feel live, large enough to keep
    // JS-thread/bridge traffic low. One setRoverLocation per tick caps map
    // re-renders at ~1000/POLL_INTERVAL_MS per second.
    const POLL_INTERVAL_MS = 80;
    // Max messages drained per tick. Bounds the read/parse work a single tick
    // can do so it can never monopolise the JS thread; any overflow is dumped.
    const MAX_READS_PER_TICK = 60;

    const poll = async () => {
      try {
        if (!(await selectedDevice.isConnected())) { return; }

        let available = await selectedDevice.available();
        if (available && available > 0) {
          let latest = null;
          let reads = 0;
          while (available > 0 && reads < MAX_READS_PER_TICK && !cancelled) {
            const message = await selectedDevice.read();
            reads++;
            if (message) {
              const parsed = new LatLong(String(message).trim());
              if (parsed.fixType !== FixType.NoFixData) {
                latest = parsed;
              }
            }
            available = await selectedDevice.available();
          }

          // Fell behind past our per-tick budget: dump the stale remainder so
          // we never replay an old backlog or chain unbounded reads on a tick.
          if (available > 0 && reads >= MAX_READS_PER_TICK && !cancelled) {
            await selectedDevice.clear();
          }

          if (latest && !cancelled) {
            setRoverLocation(latest);
          }
        }
      } catch (e) {
        // swallow transient read errors; the next tick retries
      } finally {
        // Self-schedule instead of setInterval so a slow tick can never
        // overlap the next one: at most one drain is ever in flight, with a
        // fixed gap between ticks. This is what keeps the main thread safe.
        if (!cancelled) {
          timeoutId = setTimeout(poll, POLL_INTERVAL_MS);
        }
      }
    };

    poll();
    return () => {
      cancelled = true;
      clearTimeout(timeoutId);
    };
  }, [selectedDevice]);

  useEffect(() => {
    !init && dispatch(initializeBT());
  }, [init]);

  useEffect(() => {
    if (activeProject && !lock) {
      if (scaledPlantingLines.length > 0) {
        const payload = {
          linePoints: scaledPlantingLines,
          center: activeProject.center,
        };
        dispatch(convertLinesToLatLong(payload));
      }
    }
  }, [activeProject, scaledPlantingLines, lock]);

  useEffect(() => {
    // Request permission and get the current location
    Geolocation.requestAuthorization(() => {
      getCurrentLocation();
    });
    getCurrentLocation();
    //@ts-ignore
    dispatch(loadSettings());
  }, []);

  const getCurrentLocation = () => {
    Geolocation.getCurrentPosition(
      position => {
        const { latitude, longitude } = position.coords;
        setInitialRegion({
          latitude,
          longitude,
          latitudeDelta: 0.01,
          longitudeDelta: 0.01,
        });
      },
      error => Alert.alert('Error', error.message),
      { enableHighAccuracy: false, timeout: 200000, maximumAge: 5000 }
    );
  };


  const basePoints = useMemo(() => {
    if (activeProject && activeProject.basePoints) {
      return activeProject.basePoints;
    }
    return [];
  }, [activeProject]);


  const centerMe = () => {

    if (roverLocation) {
      const { latitude, longitude } = roverLocation;
      setInitialRegion({
        latitude,
        longitude,
        latitudeDelta: 0.01,
        longitudeDelta: 0.01,
      });
    }
    else {
      getCurrentLocation();
    }
  };


  useEffect(() => {
    if (!areaMode && polygonCoordinates.length > 0) {
      setPolygonCoordinates([]);
    }
  }, [areaMode, polygonCoordinates]);

  useEffect(() => {
    if (areaMode) {
      setShowPlantingConfig(true);
    } else {
      setArea(0.00);
      setTreeCount(0);
    }
  }, [areaMode]);


  const handleIconClick = (action) => {
    if(action === 'center'){
      centerMe();
    }
    else if(action === 'rotate'){
        if(mapRotateDegrees === 360){
          setMapRotateDegrees(0);
        }else{
          setMapRotateDegrees(mapRotateDegrees + 90);
        }
    }
    else if(action === 'plus'){
      const displayLines = settings.displayLineCount;
      const startIndex = activeProject.forwardIndex + 1;
      let endIndex = startIndex + displayLines;
      if (endIndex > activeProject.lineCount) {
        endIndex = activeProject.lineCount;
      }

      const nextNLines = activeProject.plantingLines.slice(startIndex, endIndex);

      dispatch(setIndices({
        forwardIndex: endIndex - 1, // Set to last displayed index
        backwardIndex: activeProject.forwardIndex,
      }));
      dispatch(setLock(false));
      dispatch(setScaledPlanitingLines(nextNLines));
    }
    else if (action === 'minus') {
      let endIndex = activeProject.backwardIndex + 1;
      const displayLines = settings.displayLineCount;
      let startIndex = endIndex - displayLines;
      if (startIndex <= 0) {
        startIndex = 0;
        endIndex = Math.min(displayLines - 1, activeProject.lineCount);
      }
      const prevNLines = activeProject.plantingLines.slice(startIndex, endIndex);

      dispatch(setIndices({
        forwardIndex: activeProject.backwardIndex,
        backwardIndex: startIndex,
      }));
      dispatch(setLock(false));
      dispatch(setScaledPlanitingLines(prevNLines));
    }
    else if(action === 'plant'){
      dispatch(setLock(true)); //  so we do not have to call convertLinesToLatLong
      const truth = !planting;
      if(truth){
        if(cyrusLines.length > 0){
          setPlanting(true);
        }else{
          ToastAndroid.showWithGravity(
                          'Ooops! Please Select at least one line to peg',
                          ToastAndroid.SHORT,
                          ToastAndroid.TOP,
                    );
        }
      }
      else{
        setPlanting(false);
      }
    }
    else if(action === 'refresh'){
      setMapType(mapType === 'SATELLITE' ? 'TERRAIN' : 'SATELLITE');
    }
    else{
      setAreaMode(!areaMode);
    }
  };

  useEffect(()=>{
    if(activeProject){
      if(activeProject.backwardIndex <= 0){
        setActiveMinusLines(false);
      }else{
        setActiveMinusLines(true);
      }
      if(activeProject.forwardIndex >= activeProject.lineCount){
        setActivePlusLines(false);
      }else{
        setActivePlusLines(true);
      }
    }

  }, [activeProject]);
  return (
    <View className="flex-1 relative">
      {/* Map View */}
      <MsituMapView
        basePoints={basePoints}
        planting={planting}
        initialRegion={initialRegion}
        areaMode={areaMode}
        roverLocation={roverLocation}
        pointerEvents={modalStore.showCreateNewProjects ? 'none' : 'auto'}
        scrollEnabled={!modalStore.showCreateNewProjects}
        zoomEnabled={!modalStore.showCreateNewProjects}
        rotateEnabled={!modalStore.showCreateNewProjects}
        pitchEnabled={!modalStore.showCreateNewProjects}
        onPolygonCoordsChange={(coords)=>{
          const a = RTNMsitu.calculateArea(coords,1.0);
          setArea(a);
          const gapMeters = gapUnit === 'feet' ? convertToMeters(gapSize, 'feet') : gapSize;
          const estimated = treeEstimate(a, meshType, gapMeters);
          setTreeCount(estimated);
        }}
        visibleLines={visibleLines}
        rotationDegrees={mapRotateDegrees}
        mapType={mapType}
      />

      {/* Overlay View at the Top */}
      <TopNavBar
          hideNewProject={cyrusLines.length > 0}
          navigation={navigation}
        />

      {/* Left and Right Views Fixed at the Bottom */}
      <View className="flex flex-col gap-2 absolute bottom-4 left-4 z-10">

        <View className="flex flex-row justify-start">
          {areaMode &&
            <View className="bg-white/70 rounded mx-1 w-auto px-1">
              <View className="flex flex-row justify-start gap-1 align-baseline">
                <Text className="font-avenirBold">Area:</Text>
                <Text className="font-avenirMedium">{area} acres</Text>
              </View>
              <View className="flex flex-row justify-start gap-1 align-baseline">
                <Text className="font-avenirBold">Trees:</Text>
                <Text className="font-avenirMedium">{treeCount}</Text>
              </View>
            </View>}
        </View>
        <AnimatedLoader
          visible={loading}
          overlayColor="rgba(255,255,255,0.75)"
          animationStyle={styles.lottie}
          animationType="slide"
          speed={1}>
          <Text className="font-avenirMedium">Loading...</Text>
        </AnimatedLoader>

      </View>

      {
        (roverLocation && selectedDevice) &&
        <LocationFeed latLong={roverLocation} />
      }

      <NewProject
        roverLocation={roverLocation}
        onClose={() => dispatch(setShowCreateNewProjects(false))}
        visible={modalStore.showCreateNewProjects} />
      <MetricsConfigModal
        visible={showPlantingConfig}
        onClose={(result) => {
          setShowPlantingConfig(false);
          const isValid = result && result.wasApplied === true;
          if (!isValid) {
            setAreaMode(false);
          }
        }}
        onApply={({ meshType: mt, gapUnit: gu, gapSize: gs }) => {
          setMeshType(mt);
          setGapUnit(gu);
          setGapSize(gs);
          // If we already have an area, re-evaluate tree count instantly
          if (area && area > 0) {
            const gapMeters = gu === 'feet' ? convertToMeters(gs, 'feet') : gs;
            const estimated = treeEstimate(area, mt, gapMeters);
            setTreeCount(estimated);
          }
        }}
        initialMeshType={meshType}
        initialGapUnit={gapUnit}
        initialGapSize={gapSize}
      />
      <FabGroup
        actions={[
          {
            icon: require('../../assets/forward.png'),
            name: 'plus',
            disabled: activeProject === null || !activePlusLines || planting,
            initialPosition: 340,
          },
          {
            icon: require('../../assets/backward.png'),
            name: 'minus',
            disabled: activeProject === null || !activeMinusLines || planting,
            initialPosition: 280,
          },
          {
            icon: require('../../assets/360.png'),
            name: 'rotate',
            disabled: activeProject === null || !roverLocation,
            initialPosition: 400,
          },
          {
            icon: require('../../assets/center.png'),
            name: 'center',
            initialPosition: 160,
          },
          {
            icon: require('../../assets/plant.png'),
            name: 'plant',
            backgroundColor: planting ? 'green-500' : null,
            disabled: cyrusLines.length === 0,
            initialPosition: 220,
          },
          {
            icon: require('../../assets/compass.png'),
            name: 'area',
            backgroundColor: areaMode ? 'green-500' : null,
            disabled: cyrusLines.length > 0,
            initialPosition: cyrusLines.length > 0 ? 0 : 100,
          },
          {
            icon: require('../../assets/map.png'),
            name: 'refresh',
            initialPosition: 500,
          },
        ]}
        onActionPress={handleIconClick}
      />
    </View>
  );
};
export default Home;
