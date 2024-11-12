import { Text, View, Alert, TouchableOpacity } from "react-native";
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import Geolocation from '@react-native-community/geolocation';
import React, { useEffect, useState} from "react";
import ProjectList from '../../components/projects/ProjectList';
import { convertLinesToLatLong } from "../../store/projects";
import { useDispatch, useSelector } from 'react-redux';

import { initializeBT } from "../../store/bluetooth";
import MsituMapView from '../../components/maps/MsituMapView';
import LocationFeed from "../../components/maps/LocationFeed";
import TopNavBar from "../../components/misc/TopNavBar";
const Home = ({ navigation }) => {


  const [createProject, setCreateProject] = useState(false)
  const [areaMode, setAreaMode] = useState(false);
  const [area, setArea] = useState(0.00)
  const [polygonCoordinates, setPolygonCoordinates] = useState([])
  const [initialRegion, setInitialRegion] = useState({
    latitude: 0.04694938133710109,
    longitude: 32.46314182880414,
    latitudeDelta: 0.01,
    longitudeDelta: 0.01,
  })
  const [plantingPoints, setPlantingPoints] = useState([])
  const [plantingLines, setPlantingLines] = useState([])

  const { activeProject, visibleLines, loading } = useSelector(store => store.project)
  const { roverLocation } = useSelector(store => store.nmeaListener)
  const { selectedDevice } = useSelector(store => store.bluetooth)


  const { init } = useSelector(store => store.bluetooth)

  const dispatch = useDispatch()



  useEffect(() => {
    !init && dispatch(initializeBT())
  }, [init])

  useEffect(() => {
    if (activeProject) {
      if (activeProject.plantingLines.length > 0) {
        const payload = {
          linePoints: activeProject.plantingLines,
          center: activeProject.center
        }
        dispatch(convertLinesToLatLong(payload));
      }
    }
  }, [activeProject])

  useEffect(() => {
    // Request permission and get the current location
    Geolocation.requestAuthorization(() => {
      getCurrentLocation()
    });
    getCurrentLocation();
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
      error => Alert.alert("Error", error.message),
      { enableHighAccuracy: false, timeout: 200000, maximumAge: 5000 }
    );
  };


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
      getCurrentLocation()
    }
  }

  
  useEffect(() => {
    if (!areaMode && polygonCoordinates.length > 0) {
      setPolygonCoordinates([])
    }
  }, [areaMode, polygonCoordinates])



  return (

    <View className="flex-1 relative">
      {/* Map View */}
      <MsituMapView
        initialRegion={initialRegion}
        areaMode={areaMode}
        visibleLines={visibleLines}
      />

      {/* Overlay View at the Top */}
      <TopNavBar navigation={navigation}/>

      {/* Left and Right Views Fixed at the Bottom */}
      <View className="flex flex-col gap-2 absolute bottom-4 left-4 z-10">

        <View className="flex flex-row justify-start">
          <TouchableOpacity
            onPress={() => setAreaMode(!areaMode)}
            className={`${areaMode ? 'bg-green-400' : 'bg-white'} p-2 rounded`}>
            <MaterialCommunityIcons name="ruler-square-compass" size={30} color={`${areaMode ? 'white' : 'black'}`} />
          </TouchableOpacity>
          {areaMode &&
            <View className="bg-white/40 rounded mx-1 w-32 px-1">
              <View className='flex flex-row justify-start gap-1 align-baseline'>
                <Text className='font-avenirBold'>Area:</Text>
                <Text className='font-avenirMedium'>{area} sq m</Text>
              </View>
              <View className='flex flex-row justify-start gap-1 align-baseline'>
                <Text className='font-avenirBold'>Trees:</Text>
                <Text className='font-avenirMedium'>500</Text>
              </View>
            </View>}
        </View>
      </View>

      {
        (roverLocation && selectedDevice) &&
        <LocationFeed latLong={roverLocation}/>
      }

      <ProjectList
        show={createProject}
        onClose={() => { setCreateProject(false) }}
      >
        <Text>Prpject List here</Text>
      </ProjectList>

    </View>

  )
}
export default Home;
