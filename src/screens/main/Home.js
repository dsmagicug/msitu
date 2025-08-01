import { Text, View, Alert, ToastAndroid } from 'react-native';
import Geolocation from '@react-native-community/geolocation';
import React, { useEffect, useState, useRef, useMemo } from 'react';
import { convertLinesToLatLong, setLock, setIndices, setScaledPlanitingLines } from '../../store/projects';
import { useDispatch, useSelector } from 'react-redux';
import AnimatedLoader from 'react-native-animated-loader';
import { throttle } from 'lodash';
import { initializeBT } from '../../store/bluetooth';
import MsituMapView from '../../components/maps/MsituMapView';
import LocationFeed from '../../components/maps/LocationFeed';
import TopNavBar from '../../components/misc/TopNavBar';
import styles from '../../assets/styles';
import LatLong from '../../services/NMEAService';
import { FixType } from 'rtn-msitu';
import NewProject from '../../components/projects/NewProject';
import { setShowCreateNewProjects } from '../../store/modal';
import FabGroup from '../../components/fab/FabGroup';
import { loadSettings } from '../../store/settings';


const Home = ({ navigation }) => {

  const [areaMode, setAreaMode] = useState(false);
  const [planting, setPlanting] = useState(false);
  const [area, setArea] = useState(0.00);
  const [polygonCoordinates, setPolygonCoordinates] = useState([]);
  const [initialRegion, setInitialRegion] = useState({
    latitude: 0.04694938133710109,
    longitude: 32.46314182880414,
    latitudeDelta: 0.01,
    longitudeDelta: 0.01,
  });
  const [roverLocation, setRoverLocation] = useState(null);
  const [dataReadListener, setDataReadListener] = useState(null);

  const [mapRotateDegrees, setMapRotateDegrees] =  useState(180);
  const [mapType, setMapType] = useState('SATELLITE');

  const { activeProject, visibleLines, loading, scaledPlantingLines, lock, forwardIndex, backwardIndex, totalLines } = useSelector(store => store.project);
  const { selectedDevice } = useSelector(store => store.bluetooth);
  const [activeMinusLines, setActiveMinusLines] =  useState(false);
  const [activePlusLines, setActivePlusLines] =  useState(true);
  const { cyrusLines } = useSelector(store => store.pegging);
  const {settings } = useSelector(store => store.settings);
  const { init } = useSelector(store => store.bluetooth);
  const modalStore = useSelector(selector => selector.modals);
  const onReceiveData = async (buffer) => {
    const sentence = buffer.data.trim();
    const longLat = new LatLong(sentence);
    if (longLat.fixType !== FixType.NoFixData) {
      throttledUpdate(longLat);
    }
  };

  const dispatch = useDispatch();

  const throttledUpdate = useRef(
    throttle((location) => {
      setRoverLocation(location);
    }, 100)
  ).current;

  useEffect(() => {
    const checkConnectionAndSetupListener = async () => {
      if (!selectedDevice) {return;}
      const connection = await selectedDevice.isConnected();
      if (connection) {
        const readListener = selectedDevice.onDataReceived((buffer) => onReceiveData(buffer));
        setDataReadListener(readListener);
      }
    };

    checkConnectionAndSetupListener();

    return () => {
      if (dataReadListener) {
        dataReadListener.remove();
      }
    };
  }, [selectedDevice, throttledUpdate]);

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
            <View className="bg-white/70 rounded mx-1 w-32 px-1">
              <View className="flex flex-row justify-start gap-1 align-baseline">
                <Text className="font-avenirBold">Area:</Text>
                <Text className="font-avenirMedium">{area} sq m</Text>
              </View>
              <View className="flex flex-row justify-start gap-1 align-baseline">
                <Text className="font-avenirBold">Trees:</Text>
                <Text className="font-avenirMedium">500</Text>
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
        show={modalStore.showCreateNewProjects} />
      <FabGroup
        actions={[
          {
            icon: require('../../assets/forward.png'),
            name: 'plus',
            disabled:activeProject === null || !activePlusLines,
            initialPosition: 340,
          },
          {
            icon: require('../../assets/backward.png'),
            name: 'minus',
            disabled:activeProject === null || !activeMinusLines,
            initialPosition: 280,
          },
          {
            icon: require('../../assets/360.png'),
            name: 'rotate',
            disabled:activeProject === null || !roverLocation,
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
            backgroundColor:planting ? 'green-500' : null,
            disabled:cyrusLines.length === 0,
            initialPosition: 220,
          },
          {
            icon: require('../../assets/compass.png'),
            name: 'area',
            backgroundColor:areaMode ? 'green-500' : null,
            disabled:cyrusLines.length > 0,
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
