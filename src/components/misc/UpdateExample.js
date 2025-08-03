import React, { useState } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import UpdateButton from './UpdateButton';
import UpdateNotification from './UpdateNotification';
import UpdateAppModal from './UpdateAppModal';

/**
 * Example component showing how to integrate app updates
 * This can be used in any screen where you want update functionality
 */
const UpdateExample = () => {
  const [showModal, setShowModal] = useState(false);

  return (
    <View className="p-4">
      {/* Option 1: Update Button - Can be placed anywhere */}
      <View className="mb-6">
        <Text className="text-lg font-bold mb-3">Update Button Examples:</Text>
        
        {/* Default button */}
        <View className="mb-3">
          <Text className="text-sm text-gray-600 mb-2">Default Button:</Text>
          <UpdateButton />
        </View>

        {/* Compact button */}
        <View className="mb-3">
          <Text className="text-sm text-gray-600 mb-2">Compact Button:</Text>
          <UpdateButton variant="compact" />
        </View>

        {/* Icon only button */}
        <View className="mb-3">
          <Text className="text-sm text-gray-600 mb-2">Icon Only Button:</Text>
          <UpdateButton variant="icon-only" />
        </View>

        {/* Custom button */}
        <View className="mb-3">
          <Text className="text-sm text-gray-600 mb-2">Custom Button:</Text>
          <UpdateButton 
            variant="default"
            onPress={() => setShowModal(true)}
          />
        </View>
      </View>

      {/* Option 2: Update Notification - Shows at top of screen */}
      <View className="mb-6">
        <Text className="text-lg font-bold mb-3">Update Notification:</Text>
        <Text className="text-sm text-gray-600 mb-2">
          This notification appears at the top when an update is available
        </Text>
        <UpdateNotification onUpdatePress={() => setShowModal(true)} />
      </View>

      {/* Option 3: Manual Modal Trigger */}
      <View className="mb-6">
        <Text className="text-lg font-bold mb-3">Manual Modal Trigger:</Text>
        <TouchableOpacity
          onPress={() => setShowModal(true)}
          className="bg-blue-500 px-4 py-2 rounded-lg"
        >
          <Text className="text-white text-center">Show Update Modal</Text>
        </TouchableOpacity>
      </View>

      {/* Update Modal */}
      <UpdateAppModal
        visible={showModal}
        onClose={() => setShowModal(false)}
        versionDetails={{
          version: '1.1.0',
          size: '~61MB',
          description: 'Bug fixes and performance improvements',
        }}
      />
    </View>
  );
};

export default UpdateExample; 