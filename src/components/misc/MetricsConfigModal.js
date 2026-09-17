import React, { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { useSelector } from 'react-redux';
import Reanimated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  withDelay,
} from 'react-native-reanimated';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import MsTextInput from '../input/MsTextInput';

const MetricsConfigModal = ({
  visible,
  onClose,
  onApply,
  initialMeshType = 'S',
  initialGapUnit = 'meter',
  initialGapSize = 3.6,
}) => {
  // Get high contrast mode from store
  const { settings } = useSelector(store => store.settings);
  const highContrastMode = settings?.highContrastMode || false;

  const [meshType, setMeshType] = useState(initialMeshType);
  const [gapUnit, setGapUnit] = useState(initialGapUnit);
  const [gapSize, setGapSize] = useState(String(initialGapSize));

  useEffect(() => {
    if (visible) {
      setMeshType(initialMeshType);
      setGapUnit(initialGapUnit);
      setGapSize(String(initialGapSize));
    }
  }, [visible, initialMeshType, initialGapUnit, initialGapSize]);

  // Animations
  const modalOpacity = useSharedValue(0);
  const modalScale = useSharedValue(0.95);
  const contentTranslateY = useSharedValue(60);

  useEffect(() => {
    if (visible) {
      modalOpacity.value = withTiming(1, { duration: 300 });
      modalScale.value = withSpring(1, { damping: 15, stiffness: 150 });
      contentTranslateY.value = withDelay(50, withSpring(0, { damping: 15, stiffness: 150 }));
    } else {
      modalOpacity.value = withTiming(0, { duration: 200 });
      modalScale.value = withSpring(0.95, { damping: 15, stiffness: 150 });
      contentTranslateY.value = withSpring(60, { damping: 15, stiffness: 150 });
    }
  }, [visible]);

  const modalAnimatedStyle = useAnimatedStyle(() => ({
    opacity: modalOpacity.value,
  }), [modalOpacity]);

  const contentAnimatedStyle = useAnimatedStyle(() => ({
    transform: [{ scale: modalScale.value }, { translateY: contentTranslateY.value }],
  }), [modalScale, contentTranslateY]);

  const handleApply = () => {
    const parsedGap = parseFloat(gapSize);
    if (isNaN(parsedGap) || parsedGap <= 0) {
      onClose && onClose({ wasApplied: false, invalid: true });
      return;
    }
    if (onApply) {
      onApply({ meshType, gapUnit, gapSize: parsedGap });
    }
    onClose && onClose({ wasApplied: true });
  };

  if (!visible) return null;

  const containerStyle = {
    backgroundColor: highContrastMode ? 'rgba(255, 255, 255, 0.98)' : 'rgba(0, 0, 0, 0.5)',
  };

  const modalStyle = {
    backgroundColor: highContrastMode ? '#ffffff' : '#ffffff',
    borderWidth: highContrastMode ? 2 : 0,
    borderColor: highContrastMode ? '#000000' : 'transparent',
    shadowColor: highContrastMode ? '#000000' : '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: highContrastMode ? 0.3 : 0.25,
    shadowRadius: 20,
    elevation: highContrastMode ? 8 : 5,
  };

  const textStyle = {
    color: highContrastMode ? '#000000' : '#1f2937',
    fontWeight: highContrastMode ? 'bold' : 'normal',
  };

  const subtitleStyle = {
    color: highContrastMode ? '#000000' : '#6b7280',
    fontWeight: highContrastMode ? '600' : 'normal',
  };

  const chipBase = (selected) => ({
    backgroundColor: selected
      ? (highContrastMode ? '#000000' : '#22c55e')
      : (highContrastMode ? 'rgba(0,0,0,0.05)' : 'rgba(34, 197, 94, 0.1)'),
    borderWidth: highContrastMode ? 1 : (selected ? 0 : 1),
    borderColor: highContrastMode ? '#000000' : 'rgba(34, 197, 94, 0.25)',
  });

  const unitChipBase = (selected) => ({
    backgroundColor: selected
      ? (highContrastMode ? '#000000' : '#3b82f6')
      : (highContrastMode ? 'rgba(0,0,0,0.05)' : 'rgba(59, 130, 246, 0.1)'),
    borderWidth: highContrastMode ? 1 : (selected ? 0 : 1),
    borderColor: highContrastMode ? '#000000' : 'rgba(59, 130, 246, 0.25)',
  });

  return (
    <Reanimated.View
      className="absolute inset-0 z-50 justify-center items-center"
      style={[containerStyle, modalAnimatedStyle]}
    >
      <View className="absolute inset-0" />

      <Reanimated.View
        className="mx-6 rounded-3xl overflow-hidden w-full"
        style={[modalStyle, contentAnimatedStyle]}
      >
        {/* Header */}
        <View className="p-6 pb-4" style={{ backgroundColor: highContrastMode ? '#f8f9fa' : '#f9fafb' }}>
          <View className="flex flex-row items-center justify-between mb-2">
            <View className="flex-1">
              <Text className="font-avenirBold text-2xl" style={textStyle}>Metrics Configuration</Text>
              <Text className="font-avenirMedium text-sm" style={subtitleStyle}>Set mesh type and tree gap</Text>
            </View>
            <TouchableOpacity
              onPress={() => onClose && onClose({ wasApplied: false })}
              className="rounded-full"
              style={{
                backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(239, 68, 68, 0.15)',
                borderWidth: highContrastMode ? 1 : 0,
                borderColor: highContrastMode ? '#000000' : 'transparent',
                width: 40,
                height: 40,
                justifyContent: 'center',
                alignItems: 'center',
              }}
            >
              <MaterialCommunityIcons name="close" size={20} color={highContrastMode ? '#000000' : '#ef4444'} />
            </TouchableOpacity>
          </View>
        </View>

        {/* Content */}
        <ScrollView className="max-h-96" showsVerticalScrollIndicator={false}>
          <View className="px-6 pb-6 pt-2">
            {/* Mesh Type */}
            <Text className="font-avenirBold text-lg mb-2" style={textStyle}>Mesh Type</Text>
            <View className="flex-row items-center mb-4">
              <TouchableOpacity
                className="px-4 py-2 rounded-2xl mr-2"
                style={chipBase(meshType === 'S')}
                onPress={() => setMeshType('S')}
                activeOpacity={0.8}
              >
                <Text className="font-avenirBold" style={{ color: meshType === 'S' ? '#ffffff' : (highContrastMode ? '#000000' : '#16a34a') }}>Square</Text>
              </TouchableOpacity>
              <TouchableOpacity
                className="px-4 py-2 rounded-2xl"
                style={chipBase(meshType === 'T')}
                onPress={() => setMeshType('T')}
                activeOpacity={0.8}
              >
                <Text className="font-avenirBold" style={{ color: meshType === 'T' ? '#ffffff' : (highContrastMode ? '#000000' : '#16a34a') }}>Triangular</Text>
              </TouchableOpacity>
            </View>

            {/* Gap Unit */}
            <Text className="font-avenirBold text-lg mb-2" style={textStyle}>Gap Unit</Text>
            <View className="flex-row items-center mb-4">
              <TouchableOpacity
                className="px-4 py-2 rounded-2xl mr-2"
                style={unitChipBase(gapUnit === 'meter')}
                onPress={() => setGapUnit('meter')}
                activeOpacity={0.8}
              >
                <Text className="font-avenirBold" style={{ color: gapUnit === 'meter' ? '#ffffff' : (highContrastMode ? '#000000' : '#3b82f6') }}>Meters (m)</Text>
              </TouchableOpacity>
              <TouchableOpacity
                className="px-4 py-2 rounded-2xl"
                style={unitChipBase(gapUnit === 'feet')}
                onPress={() => setGapUnit('feet')}
                activeOpacity={0.8}
              >
                <Text className="font-avenirBold" style={{ color: gapUnit === 'feet' ? '#ffffff' : (highContrastMode ? '#000000' : '#3b82f6') }}>Feet (ft)</Text>
              </TouchableOpacity>
            </View>

            {/* Gap Size */}
            <Text className="font-avenirBold text-lg mb-2" style={textStyle}>Gap Size</Text>
            <MsTextInput
              label={`Gap size in ${gapUnit === 'feet' ? 'feet' : 'meters'}`}
              placeholder={gapUnit === 'feet' ? 'e.g., 12' : 'e.g., 3.6'}
              keyboardType={'numeric'}
              initialValue={String(initialGapSize)}
              onChangeText={(v) => setGapSize(v)}
              containerStyle={{ marginBottom: 8 }}
            />
            <Text className="font-avenirMedium text-xs" style={subtitleStyle}>
              This gap will be used to estimate the number of trees.
            </Text>
          </View>
        </ScrollView>

        {/* Footer */}
        <View className="p-4 border-t" style={{
          borderColor: highContrastMode ? '#000000' : '#e5e7eb',
          backgroundColor: highContrastMode ? '#f8f9fa' : '#f9fafb'
        }}>
          <View className="flex-row justify-end">
            <TouchableOpacity
              onPress={() => onClose && onClose({ wasApplied: false })}
              className="py-3 px-4 rounded-2xl mr-2"
              style={{
                backgroundColor: highContrastMode ? 'rgba(0,0,0,0.05)' : 'rgba(239, 68, 68, 0.1)',
                borderWidth: highContrastMode ? 1 : 0,
                borderColor: highContrastMode ? '#000000' : 'transparent',
              }}
              activeOpacity={0.8}
            >
              <Text className="font-avenirBold" style={{ color: highContrastMode ? '#000000' : '#ef4444' }}>Cancel</Text>
            </TouchableOpacity>
            <TouchableOpacity
              onPress={handleApply}
              className="py-3 px-4 rounded-2xl"
              style={{
                backgroundColor: highContrastMode ? '#000000' : '#22c55e',
                shadowColor: highContrastMode ? '#000000' : '#22c55e',
                shadowOffset: { width: 0, height: 4 },
                shadowOpacity: 0.3,
                shadowRadius: 8,
                elevation: 6,
              }}
              activeOpacity={0.8}
            >
              <Text className="font-avenirBold text-white">Apply</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Reanimated.View>
    </Reanimated.View>
  );
};

export default MetricsConfigModal;


