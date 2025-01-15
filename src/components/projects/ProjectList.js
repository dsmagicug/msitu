import { View, Text, TouchableOpacity, ActivityIndicator } from 'react-native'
import React, { useEffect } from 'react'
import { BottomModal, ModalFooter, ModalButton, ModalContent } from 'react-native-modals';
import AntDesignIcon from 'react-native-vector-icons/AntDesign'
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'
import styles from '../../assets/styles';
import { fetchProjects, loadProject, deleteProject } from '../../store/projects';
import { useDispatch, useSelector } from 'react-redux';
import { ScrollView } from 'react-native-gesture-handler';
export default function ProjectList({ children, show, onClose }) {


    // const navigation =  useNavigation()
    const dispatch = useDispatch()
    const { fetching, projectList } = useSelector(store => store.project)
    useEffect(() => {
        if (show) {
            dispatch(fetchProjects())
        }
    }, [show])
    return (
        <BottomModal
            visible={show}
            modalTitle={
                <View className='flex flex-row justify-between items-center border-b-hairline border-b-gray-300'>
                    <View className='p-4'>
                        <Text className='font-avenirBold text-black text-2xl'>Project List</Text>
                    </View>
                    <TouchableOpacity
                        className='p-4 rounded-lg'>
                        <AntDesignIcon name="addfolder" size={24} color="teal" />
                    </TouchableOpacity>
                </View>
            }
            footer={
                <ModalFooter>
                    <ModalButton
                        text="CLOSE"
                        textStyle={styles.buttonText}
                        onPress={() => { onClose() }}
                    />
                </ModalFooter>
            }
        >
            <ModalContent>
                <ScrollView className='mt-3 h-64'>
                    <View className='flex justify-center'>
                        {fetching && <ActivityIndicator size="small" />}
                    </View>
                    {projectList.map((project, idx) => (
                        <View
                            className="flex flex-row justify-between p-2 mt-1 mb-1 mx-1 bg-gray-100"
                            key={idx}
                        >
                            <Text className='font-avenirMedium'>{project.name}</Text>
                            <View className='flex flex-row justify-end gap-1'>
                                <TouchableOpacity
                                    onPress={() => { 
                                        dispatch(loadProject(project.id)) 
                                        //navigation.closeDrawer()
                                    }}
                                    className='bg-white/100 border border-blue-600 p-1 gap-1 rounded flex flex-row justify-center items-center'>
                                    <MaterialCommunityIcons name="open-in-new" size={14} color="blue" />
                                    <Text className='font-avenirBold text-sm text-blue-600'>Open</Text>
                                </TouchableOpacity>
                                {/* <TouchableOpacity
                                    onPress={() => { console.log("Export") }}
                                    className='bg-white/100 border border-teal-600 p-1 gap-1 rounded flex flex-row justify-center items-center'>
                                    <MaterialCommunityIcons name="code-json" size={14} color="teal" />
                                    <Text className='font-avenir text-sm text-teal-900'>Export</Text>
                                </TouchableOpacity> */}
                                <TouchableOpacity
                                    onPress={() => { dispatch(deleteProject(project.id)) }}
                                    className='bg-white/100 border border-red-600 p-1 gap-1 rounded flex flex-row justify-center items-center'>
                                    <MaterialCommunityIcons name="trash-can-outline" size={14} color="red" />
                                    <Text className='font-avenirBold text-sm text-red-600'>Delete</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    ))}
                </ScrollView>
            </ModalContent>
        </BottomModal>
    )
}