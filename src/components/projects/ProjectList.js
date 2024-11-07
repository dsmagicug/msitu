import { View, Text, TouchableOpacity } from 'react-native'
import React from 'react'
import { BottomModal, ModalFooter, ModalButton, ModalContent } from 'react-native-modals';
import AntDesignIcon from 'react-native-vector-icons/AntDesign'
import styles from '../../assets/styles';

export default function ProjectList({ children, show, onClose }) {
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
                {children}
            </ModalContent>
        </BottomModal>
    )
}