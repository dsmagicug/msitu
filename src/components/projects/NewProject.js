import { View, Text, Image, ToastAndroid, Modal, TouchableOpacity, StyleSheet } from 'react-native'
import React, { useState, useEffect } from 'react'
import styles from '../../assets/styles';
import MsTextInput from '../input/MsTextInput';
import MsCheckbox from '../input/MsCheckbox';
import DropDownPicker from 'react-native-dropdown-picker'
import { ScrollView } from 'react-native-gesture-handler';
import { Chip } from 'react-native-paper';
import Icon from "react-native-vector-icons/MaterialCommunityIcons"
import { useDispatch, useSelector } from 'react-redux';
import AnimatedLoader from "react-native-animated-loader";
import { generateProject } from '../../store/projects';
import { convertToMeters } from '../../utils';

const modalStyles = StyleSheet.create({
    overlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.5)',
        justifyContent: 'flex-end',
    },
    box: {
        height: '90%',
        width: '100%',
        backgroundColor: '#ffffff',
        borderTopLeftRadius: 8,
        borderTopRightRadius: 8,
        overflow: 'hidden',
    },
    titleBar: {
        paddingVertical: 16,
        alignItems: 'center',
        borderBottomWidth: StyleSheet.hairlineWidth,
        borderBottomColor: '#CCD0D5',
    },
    footer: {
        flexDirection: 'row',
        borderTopWidth: StyleSheet.hairlineWidth,
        borderTopColor: '#CCD0D5',
    },
    footerButton: {
        flex: 1,
        alignItems: 'center',
        paddingVertical: 16,
    },
    footerButtonBordered: {
        borderLeftWidth: StyleSheet.hairlineWidth,
        borderLeftColor: '#CCD0D5',
    },
    disabledText: {
        color: '#C5C6C5',
    },
});

const contrastBox = {
    height: '90%',
    width: '100%',
    backgroundColor: '#070424',
    borderTopLeftRadius: 8,
    borderTopRightRadius: 8,
    overflow: 'hidden',
}
const contrastTitleBar = {
    color: '#ffffff',
    paddingVertical: 16,
    alignItems: 'center',
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#CCD0D5'
}

const contrastBtnText = {
    fontFamily: "AvenirBold",
    color: '#ffffff'
}

export default function NewProject({ visible, onClose, roverLocation }) {

    const [checkedFirstPoint, setCheckedFirstPoint] = useState(false)
    const [checkedSecondPoint, setCheckedSecondPoint] = useState(false)
    const [meshType, setMeshType] = useState("")
    const [lineLengthUnit, setLineLengthUnit] = useState("")
    const [gapSizeUnit, setGapSizeUnit] = useState("")
    const [projectName, setProjectName] = useState("")
    const [lineDirection, setLineDirection] = useState("");
    const [lineLength, setLineLength] = useState(500)
    const [gapSize, setGapSize] = useState(3)
    const [firstPoint, setFirstPoint] = useState(null)
    const [secondPoint, setSecondPoint] = useState(null)
    const [basePoints, setBasePoints] = useState([])
    const [roverNotFound, setRoverNotFound] = useState(false)

    const [openLineLengthUnit, setOpenLineLengthUnit] = useState(false)
    const [lineLengthUnitItems, setLineLengthUnitItems] = useState([
        { label: 'Units', value: '' },
        { label: 'Feet', value: 'feet' },
        { label: 'Metres', value: 'meter' },
        { label: 'Acres', value: 'acres' },
        { label: 'Miles', value: 'miles' },
    ])

    const [openGapSizeUnit, setOpenGapSizeUnit] = useState(false)
    const [gapSizeUnitItems, setGapSizeUnitItems] = useState([
        { label: 'Units', value: '' },
        { label: 'Feet', value: 'feet' },
        { label: 'Inches', value: 'inches' },
        { label: 'Metres', value: 'meter' },
    ])

    const [openLineDirection, setOpenLineDirection] = useState(false)
    const [lineDirectionItems, setLineDirectionItems] = useState([
        { label: 'Line Draw Direction', value: '' },
        { label: 'Left', value: 'RIGHT' },
        { label: 'Right', value: 'LEFT' },
    ])

    const dispatch = useDispatch()
    const { generating } = useSelector(store => store.project)
    const { settings } = useSelector(store => store.settings);
    const highContrastMode = settings?.highContrastMode || false;

    const constructProject = () => {
        if (!firstPoint) {
            ToastAndroid.showWithGravity(
                `Sorry! First base point was not captured`,
                ToastAndroid.SHORT,
                ToastAndroid.TOP,
            );
            return
        }
        if (!secondPoint) {
            ToastAndroid.showWithGravity(
                'Sorry! Second base point was not captured',
                ToastAndroid.SHORT,
                ToastAndroid.TOP,
            );
            return
        }
        const payload = {
            firstPoint,
            secondPoint,
            lineDirection,
            meshType,
            name: projectName,
            gapSize: convertToMeters(parseFloat(gapSize), gapSizeUnit),
            lineLength: convertToMeters(parseFloat(lineLength), lineLengthUnit)
        }
        dispatch(generateProject(payload))
    }

    useEffect(() => {
        if (!checkedFirstPoint) {
            setFirstPoint(null)
            setSecondPoint(null) // making sure, first point is selected first
            setBasePoints([])
        } else {
            try{
                setFirstPoint(roverLocation)
                setBasePoints([{ latitude: roverLocation.latitude, longitude: roverLocation.longitude }])
                ToastAndroid.showWithGravity(
                    `First base Point selected`,
                    ToastAndroid.SHORT,
                    ToastAndroid.CENTER,
                );
                setRoverNotFound(false)
            } catch (err){
                setRoverNotFound(true);
            }
        }
    }, [checkedFirstPoint])

    useEffect(() => {
        let points = basePoints;
        if (!checkedSecondPoint) {
            setSecondPoint(null)
            points.pop()
        } else {

            try{
                setSecondPoint(roverLocation);
                if (basePoints.length == 0) {
                    ToastAndroid.showWithGravity(
                        `Select the second point only if the first is selected.`,
                        ToastAndroid.SHORT,
                        ToastAndroid.CENTER,
                    );
                    return;
                }
                else if (basePoints.length > 1) {
                    points[1] = { latitude: roverLocation.latitude, longitude: roverLocation.longitude }
                } else {
                    points.push({ latitude: roverLocation.latitude, longitude: roverLocation.longitude })
                }
                ToastAndroid.showWithGravity(
                    `Second base Point selected`,
                    ToastAndroid.SHORT,
                    ToastAndroid.CENTER,
                );
                setRoverNotFound(false)
                setBasePoints(points)
            } catch(err){
                setRoverNotFound(true);
            }
        }
    }, [checkedSecondPoint])
    return (
        <Modal
            visible={visible}
            animationType="slide"
            transparent={true}
            onRequestClose={onClose}
        >
            <View style={modalStyles.overlay}>
                <View style={highContrastMode ? contrastBox : modalStyles.box}>
                    <View style={highContrastMode ? contrastTitleBar : modalStyles.titleBar}>
                        <Text style={highContrastMode ? contrastBtnText : styles.buttonText}>Create New Project</Text>
                    </View>
                    <View
                        style={{
                            flex: 1,
                            marginTop: 5,
                        }}
                    >
                        <ScrollView
                            style={{ flex: 1, width: '100%' }}
                            nestedScrollEnabled={true}
                            scrollEnabled={true}
                            contentContainerStyle={{ alignItems: 'center', paddingBottom: 16 }}
                            keyboardShouldPersistTaps="handled"
                            keyboardDismissMode="on-drag"
                        >
                            <View style={{ width: '100%' }}>
                                <MsTextInput
                                    onChangeText={setProjectName}
                                    label="Project Name" />
                                <View className='flex-row justify-between w-full gap-x-1 mt-3'>
                                    <MsTextInput
                                        containerStyle={{ width: 150 }}
                                        keyboardType="decimal-pad"
                                        label="Line Length"
                                        onChangeText={setLineLength}

                                    />
                                    <DropDownPicker
                                        style={{ 
                                            width: 130, 
                                            marginTop: 5, 
                                            backgroundColor: highContrastMode ? '#070424' : '#ffffff',
                                            borderColor: highContrastMode ? '#070424' : '#ffffff'
                                        }}
                                        containerStyle={{backgroundColor: highContrastMode ? '#070424' : '#ffffff'}}
                                        textStyle={{ fontFamily: 'AvenirMedium', color: highContrastMode ? '#ffffff' : '#000000' }}
                                        open={openLineLengthUnit}
                                        value={lineLengthUnit}
                                        items={lineLengthUnitItems}
                                        setOpen={setOpenLineLengthUnit}
                                        setValue={setLineLengthUnit}
                                        setItems={setLineLengthUnitItems}
                                        onOpen={() => { setOpenGapSizeUnit(false); setOpenLineDirection(false) }}
                                        listMode="SCROLLVIEW"
                                        arrowIconStyle={{
                                            tintColor: highContrastMode ? '#ffffff' : '#000000'
                                        }}
                                        arrowIconContainerStyle={{
                                            backgroundColor: highContrastMode ? '#070424' : '#ffffff'
                                        }}
                                        dropDownContainerStyle={{
                                            backgroundColor: highContrastMode ? '#070424' : '#ffffff'
                                        }}
                                        zIndex={3000}
                                        zIndexInverse={1000}
                                    />
                                </View>
                                <View className='flex-row justify-between w-full gap-x-1 mt-3'>
                                    <MsTextInput
                                        keyboardType="decimal-pad"
                                        onChangeText={setGapSize}
                                        label="Gap Size"
                                        containerStyle={{ width: 150 }}
                                    />
                                    <DropDownPicker
                                        style={{ 
                                            width: 130, 
                                            marginTop: 5, 
                                            backgroundColor: highContrastMode ? '#070424' : '#ffffff',
                                            borderColor: highContrastMode ? '#070424' : '#ffffff'
                                        }}
                                        containerStyle={{backgroundColor: highContrastMode ? '#070424' : '#ffffff'}}
                                        textStyle={{ fontFamily: 'AvenirMedium', color: highContrastMode ? '#ffffff' : '#000000' }}
                                        open={openGapSizeUnit}
                                        value={gapSizeUnit}
                                        items={gapSizeUnitItems}
                                        setOpen={setOpenGapSizeUnit}
                                        setValue={setGapSizeUnit}
                                        setItems={setGapSizeUnitItems}
                                        onOpen={() => { setOpenLineLengthUnit(false); setOpenLineDirection(false) }}
                                        listMode="SCROLLVIEW"
                                        arrowIconStyle={{
                                            tintColor: highContrastMode ? '#ffffff' : '#000000'
                                        }}
                                        arrowIconContainerStyle={{
                                            backgroundColor: highContrastMode ? '#070424' : '#ffffff'
                                        }}
                                        dropDownContainerStyle={{
                                            backgroundColor: highContrastMode ? '#070424' : '#ffffff'
                                        }}
                                        zIndex={2000}
                                        zIndexInverse={2000}
                                    />
                                </View>

                                <View className='flex flex-col items-center mt-4'>
                                    <View className='flex flex-row justify-between gap-5'>
                                        <Chip
                                            textStyle={styles.buttonText}
                                            selectedColor={meshType === "TRIANGLE" ? 'green' : 'black'}
                                            icon={() => (<Icon name={meshType === "TRIANGLE" ? `triangle` : `triangle-outline`} color={meshType === "TRIANGLE" ? 'green' : 'black'}
                                                size={16} />)}
                                            onPress={() => setMeshType("TRIANGLE")}
                                        >Triangular Grid</Chip>

                                        <Chip
                                            textStyle={styles.buttonText}
                                            selected={false}
                                            selectedColor={meshType === "SQUARE" ? 'green' : 'black'}
                                            onPress={() => setMeshType("SQUARE")}
                                            icon={() => (<Icon name={meshType === "SQUARE" ? `square` : `square-outline`} color={meshType === "SQUARE" ? 'green' : 'black'}
                                                size={16} />)}
                                        >Square Grid</Chip>
                                    </View>
                                    {
                                        meshType.length > 0 &&
                                        <View className='justify-center items-center h-40 w-80 mt-2 rounded'>
                                            {meshType === "TRIANGLE" ?
                                                <Image
                                                    resizeMode='contain'
                                                    source={require('../../assets/tmesh.png')}
                                                    className="h-32 w-72 rounded"
                                                />
                                                :
                                                <Image
                                                    resizeMode='contain'
                                                    source={require('../../assets/mmesh.png')}
                                                    className="h-32 w-72 rounded"
                                                />
                                            }
                                        </View>
                                    }
                                </View>
                                <View className='w-full mt-2 border-b mx-2 border-teal-900'>
                                    <DropDownPicker 
                                        style={{ 
                                            width: 300, 
                                            marginTop: 5, 
                                            backgroundColor: highContrastMode ? '#070424' : '#ffffff',
                                            borderColor: highContrastMode ? '#070424' : '#ffffff'
                                        }}
                                        containerStyle={{backgroundColor: highContrastMode ? '#070424' : '#ffffff'}}
                                        textStyle={{ fontFamily: 'AvenirMedium', color: highContrastMode ? '#ffffff' : '#000000' }}
                                        open={openLineDirection}
                                        value={lineDirection}
                                        items={lineDirectionItems}
                                        setOpen={setOpenLineDirection}
                                        setValue={setLineDirection}
                                        setItems={setLineDirectionItems}
                                        onOpen={() => { setOpenLineLengthUnit(false); setOpenGapSizeUnit(false) }}
                                        listMode="SCROLLVIEW"
                                        arrowIconStyle={{
                                            tintColor: highContrastMode ? '#ffffff' : '#000000'
                                        }}
                                        arrowIconContainerStyle={{
                                            backgroundColor: highContrastMode ? '#070424' : '#ffffff'
                                        }}
                                        dropDownContainerStyle={{
                                            backgroundColor: highContrastMode ? '#070424' : '#ffffff'
                                        }}
                                        zIndex={1000}
                                        zIndexInverse={3000}
                                    />
                                </View>
                                <View className='w-full mt-3'>
                                    <MsCheckbox
                                        uncheckedColor='gray'
                                        disabled={true}
                                        label='First Base Point (check to auto fill)'
                                        color='green'
                                        status={checkedFirstPoint ? 'checked' : 'unchecked'}
                                        onPress={() => {
                                            setCheckedFirstPoint(!checkedFirstPoint);
                                            const isActive = !checkedFirstPoint;
                                        }}
                                        highContrast={highContrastMode}
                                    />
                                    <MsCheckbox
                                        uncheckedColor='gray'
                                        disabled
                                        label='Second Base Point (check to auto fill)'
                                        color='green'
                                        status={checkedSecondPoint ? 'checked' : 'unchecked'}
                                        onPress={() => {
                                            setCheckedSecondPoint(!checkedSecondPoint);
                                        }}
                                        highContrast={highContrastMode}
                                    />
                                    {
                                        (roverLocation === null)
                                        &&
                                        <Text className='mx-3 text-xs font-avenirBold text-yellow-600'>Make sure rover is connected..</Text>
                                    }
                                    {
                                        basePoints.length > 0 &&
                                        <Text className='mx-3 text-xs font-avenirBold text-yellow-600'>{JSON.stringify(basePoints)}</Text>
                                    }

                                </View>

                               { roverNotFound &&
                               <View style={{
                                    marginTop: 20,
                                    alignItems: 'center'
                               }}>
                                    <Text style={{fontFamily: 'avenirBold', fontSize: 20, fontWeight: 'bold', color: '#f10000'}}>WARNING: Rover not connected!</Text>
                               </View>
                               }
                            </View>
                        </ScrollView>
                        <AnimatedLoader
                            visible={generating}
                            overlayColor="rgba(255,255,255,0.75)"
                            animationStyle={styles.lottie}
                            animationType="slide"
                            speed={1}>
                            <Text className="font-avenirMedium">Generating Mesh...</Text>
                        </AnimatedLoader>
                    </View>
                    <View style={modalStyles.footer}>
                        <TouchableOpacity
                            style={modalStyles.footerButton}
                            onPress={() => { onClose() }}
                        >
                            <Text style={[styles.buttonText, { color: 'red' }]}>CANCEL</Text>
                        </TouchableOpacity>
                        <TouchableOpacity
                            style={[modalStyles.footerButton, modalStyles.footerButtonBordered]}
                            disabled={generating}
                            onPress={() => {
                                constructProject()
                            }}
                        >
                            <Text style={[styles.buttonText, highContrastMode ? {color: '#1000f2'} : {color: '#1000f2'} ,generating && modalStyles.disabledText]}>CREATE</Text>
                        </TouchableOpacity>
                    </View>
                </View>
            </View>
        </Modal>
    )
}