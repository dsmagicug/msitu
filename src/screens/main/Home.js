import MapView, { PROVIDER_GOOGLE, Polyline, Polygon, Circle, MAP_TYPES } from 'react-native-maps';
import { Text, View, Alert, TouchableOpacity } from "react-native";
import Icon from 'react-native-vector-icons/Ionicons';
import AntDesignIcon from 'react-native-vector-icons/AntDesign'
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import MaterialIcons from 'react-native-vector-icons/MaterialIcons'
import Geolocation from '@react-native-community/geolocation';
import React, { useEffect, useState, useRef } from "react";
import ProjectList from '../../components/projects/ProjectList';
import { setShowCreateNewProjects } from "../../store/modal"
import {convertLinesToLatLong} from "../../store/projects";
import { useDispatch, useSelector } from 'react-redux';


import { initializeBT } from "../../store/bluetooth";
const Home = ({ navigation }) => {

  const mapRef = useRef(null);

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
  const {init} =  useSelector(store=>store.bluetooth)

  const dispatch = useDispatch()
  const handlePolylineClick = (line) => {
    console.log(line)
  };

  useEffect(() => {
    if (mapRef.current) {
      mapRef.current.animateToRegion(initialRegion, 1000);
    }
    
  }, [initialRegion]);

  useEffect(()=>{
    !init && dispatch(initializeBT())
  },[init])

  useEffect(()=>{
    if (activeProject){
      if (activeProject.plantingLines.length > 0){
        const payload = {
          linePoints:activeProject.plantingLines,
          center:activeProject.center
        }
        dispatch(convertLinesToLatLong(payload));
      }
    }
  },[activeProject])

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

  const handleMapPress = (e) => {
    if (areaMode) {
      const newCoordinate = e.nativeEvent.coordinate;
      console.log(newCoordinate)
      setPolygonCoordinates([...polygonCoordinates, newCoordinate]);
    }

  };

  useEffect(() => {
    if (!areaMode && polygonCoordinates.length > 0) {
      setPolygonCoordinates([])
    }
  }, [areaMode, polygonCoordinates])

  return (

    <View className="flex-1 relative">
      {/* Map View */}
      <MapView
        ref={mapRef}
        provider={PROVIDER_GOOGLE}
        onPress={handleMapPress}
        mapType={MAP_TYPES.TERRAIN}
        showsUserLocation={false}
        showsScale={true}
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
          ))
        }

        {
          visibleLines.map((line, idx) => (
            <React.Fragment  key={idx}>
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
                    radius={0.3}
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

      {/* Overlay View at the Top */}
      <View className="absolute flex flex-row justify-between top-10 left-2 right-2 bg-white/70 p-2 rounded-lg items-center z-10">
        <TouchableOpacity
          className='bg-white/90 p-2 border border-teal-800 rounded-lg'
          onPress={() => {
            navigation.openDrawer();
          }}
        >
          <Icon name="menu-outline" size={24} color="teal" />
        </TouchableOpacity>
        <Text className='font-avenirBold text-xl'>{activeProject && activeProject.name}</Text>
        <View className='flex flex-row'>
          <TouchableOpacity
            onPress={() => dispatch(setShowCreateNewProjects(true))}
            className='bg-white/100 border border-teal-800 p-2 rounded-lg'>
            <AntDesignIcon name="addfolder" size={24} color="teal" />
          </TouchableOpacity>
        </View>
      </View>

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

      <View className="absolute bottom-4 right-4 z-10">
        <TouchableOpacity
          onPress={() => getCurrentLocation()}
          className={`bg-white p-2 rounded`}>

          <MaterialIcons name="my-location" size={30} color="green" />
        </TouchableOpacity>
      </View>



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
