import React from 'react';
import { View, Text, TouchableOpacity, ScrollView, Linking } from 'react-native';
import { useSelector } from 'react-redux';
import Icon from 'react-native-vector-icons/MaterialIcons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import Reanimated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  withDelay,
  interpolate,
  Extrapolation
} from 'react-native-reanimated';

const AboutMsituModal = ({ visible, onClose }) => {
  // Get high contrast mode from store
  // @ts-ignore
  const { settings } = useSelector(store => store.settings);
  const highContrastMode = settings?.highContrastMode || false;

  const modalOpacity = useSharedValue(0);
  const modalScale = useSharedValue(0.8);
  const contentTranslateY = useSharedValue(50);

  React.useEffect(() => {
    if (visible) {
      modalOpacity.value = withTiming(1, { duration: 300 });
      modalScale.value = withSpring(1, { damping: 15, stiffness: 150 });
      contentTranslateY.value = withDelay(100, withSpring(0, { damping: 15, stiffness: 150 }));
    } else {
      modalOpacity.value = withTiming(0, { duration: 200 });
      modalScale.value = withSpring(0.8, { damping: 15, stiffness: 150 });
      contentTranslateY.value = withSpring(50, { damping: 15, stiffness: 150 });
    }
  }, [visible]);

  const modalAnimatedStyle = useAnimatedStyle(() => {
    return {
      opacity: modalOpacity.value,
    };
  });

  const contentAnimatedStyle = useAnimatedStyle(() => {
    return {
      transform: [
        { scale: modalScale.value },
        { translateY: contentTranslateY.value }
      ],
    };
  });

  const handleClose = () => {
    onClose();
  };

  const openWebsite = () => {
    Linking.openURL('https://msitu.tech/').catch(err => console.error('Error opening website:', err));
  };

  const openGitHub = () => {
    Linking.openURL('https://github.com/dsmagicug/msitu').catch(err => console.error('Error opening GitHub:', err));
  };

  const openEmail = () => {
    Linking.openURL('mailto:msitu@msitu.tech').catch(err => console.error('Error opening email:', err));
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

  const linkStyle = {
    color: highContrastMode ? '#000000' : '#3b82f6',
    fontWeight: highContrastMode ? 'bold' : '600',
  };

  return (
    <Reanimated.View 
      className="absolute inset-0 z-50 justify-center items-center"
      style={[containerStyle, modalAnimatedStyle]}
    >
      <TouchableOpacity 
        className="absolute inset-0" 
        onPress={handleClose}
        activeOpacity={1}
      />
      
      <Reanimated.View 
        className="mx-6 rounded-3xl overflow-hidden"
        style={[modalStyle, contentAnimatedStyle]}
      >
        {/* Fixed Header */}
        <View className="p-6 pb-4 bg-gray-50" style={{ backgroundColor: highContrastMode ? '#f8f9fa' : '#f9fafb' }}>
          <View className="flex flex-row items-center justify-between mb-4">
            <View className="flex-1">
              <Text className="font-avenirBold text-2xl" style={textStyle}>Msitu</Text>
              <Text className="font-avenirMedium text-sm" style={subtitleStyle}>Every tree planter's companion</Text>
              <Text className="font-avenirMedium text-xs mt-1" style={subtitleStyle}>Version 1.0.0</Text>
            </View>
            <TouchableOpacity 
              onPress={handleClose}
              className="p-2 rounded-full"
              style={{ 
                backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                borderWidth: highContrastMode ? 1 : 0,
                borderColor: highContrastMode ? '#000000' : 'transparent',
              }}
            >
              <Icon name="close" size={24} color={highContrastMode ? "#000000" : "#ef4444"} />
            </TouchableOpacity>
          </View>
        </View>

        {/* Scrollable Content */}
        <ScrollView className="max-h-80" showsVerticalScrollIndicator={false}>
          <View className="px-6 pb-6">
            {/* Mission */}
            <View className="mb-6 pt-4">
              <Text className="font-avenirBold text-lg mb-3" style={textStyle}>Our Mission</Text>
              <Text className="font-avenirMedium text-sm leading-5" style={subtitleStyle}>
                "The true meaning of life is to plant trees, under whose shade you do not expect to sit." 
                <Text className="font-avenirBold"> ― Nelson Henderson</Text>
              </Text>
            </View>

            {/* Problem & Solution */}
            <View className="mb-6">
              <Text className="font-avenirBold text-lg mb-3" style={textStyle}>The Challenge</Text>
              <Text className="font-avenirMedium text-sm leading-5 mb-3" style={subtitleStyle}>
                Traditional tree planting using ropes and manual measurement is tedious, inaccurate, and time-consuming. 
                Between 2001-2002, Uganda lost 290ha of tree cover - a 15% decrease.
              </Text>
              
              <Text className="font-avenirBold text-lg mb-3" style={textStyle}>Our Solution</Text>
              <Text className="font-avenirMedium text-sm leading-5" style={subtitleStyle}>
                Msitu uses Real Time Kinematic (RTK) technology to achieve <Text className="font-avenirBold">2cm accuracy</Text> in GPS positioning, 
                making tree planting seamless, precise, and enjoyable.
              </Text>
            </View>

            {/* Technology */}
            <View className="mb-6">
              <Text className="font-avenirBold text-lg mb-3" style={textStyle}>Technology</Text>
              <View className="space-y-3">
                <View className="flex flex-row items-center">
                  <MaterialCommunityIcons name="cellphone" size={20} color={highContrastMode ? "#000000" : "#3b82f6"} />
                  <Text className="font-avenirMedium text-sm ml-3" style={subtitleStyle}>Android App with Bluetooth/USB connectivity</Text>
                </View>
                <View className="flex flex-row items-center">
                  <MaterialCommunityIcons name="satellite-variant" size={20} color={highContrastMode ? "#000000" : "#3b82f6"} />
                  <Text className="font-avenirMedium text-sm ml-3" style={subtitleStyle}>RTK Rover for precise positioning</Text>
                </View>
                <View className="flex flex-row items-center">
                  <MaterialCommunityIcons name="antenna" size={20} color={highContrastMode ? "#000000" : "#3b82f6"} />
                  <Text className="font-avenirMedium text-sm ml-3" style={subtitleStyle}>Base Station for GPS corrections</Text>
                </View>
              </View>
            </View>

            {/* Results */}
            <View className="mb-6">
              <Text className="font-avenirBold text-lg mb-3" style={textStyle}>Impact</Text>
              <Text className="font-avenirMedium text-sm leading-5" style={subtitleStyle}>
                • Reduced pegging time from <Text className="font-avenirBold">15 minutes to under 7 minutes</Text> per line{'\n'}
                • Completed 20-acre plot in <Text className="font-avenirBold">under 2 days</Text> vs traditional week{'\n'}
                • Planted over <Text className="font-avenirBold">100 acres</Text> of forestry{'\n'}
                • Improved land efficiency with triangular mesh pattern
              </Text>
            </View>

            {/* Additional Information */}
            <View className="mb-6">
              <Text className="font-avenirBold text-lg mb-3" style={textStyle}>How It Works</Text>
              <Text className="font-avenirMedium text-sm leading-5 mb-3" style={subtitleStyle}>
                Msitu uses a hybrid approach that combines the precision of RTK technology with practical field efficiency. 
                Instead of marking every point, we mark key reference points that serve as a skeleton for traditional rope methods.
              </Text>
              <Text className="font-avenirMedium text-sm leading-5" style={subtitleStyle}>
                This approach reduces the time spent on pegging while maintaining the accuracy and precision that RTK technology provides, 
                making it accessible for both hobby and commercial foresters.
              </Text>
            </View>

            {/* Environmental Impact */}
            <View className="mb-6">
              <Text className="font-avenirBold text-lg mb-3" style={textStyle}>Environmental Impact</Text>
              <Text className="font-avenirMedium text-sm leading-5" style={subtitleStyle}>
                By making tree planting more efficient and accessible, Msitu contributes to global reforestation efforts. 
                Each tree planted helps combat climate change, restore biodiversity, and create sustainable ecosystems for future generations.
              </Text>
            </View>

            {/* Links */}
            <View className="space-y-3 mb-6">
              <TouchableOpacity onPress={openWebsite} className="flex flex-row items-center p-3 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.05)' : 'rgba(59, 130, 246, 0.05)' }}>
                <MaterialCommunityIcons name="web" size={20} color={highContrastMode ? "#000000" : "#3b82f6"} />
                <Text className="font-avenirMedium text-sm ml-3" style={linkStyle}>Visit Website</Text>
              </TouchableOpacity>
              
              <TouchableOpacity onPress={openGitHub} className="flex flex-row items-center p-3 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.05)' : 'rgba(59, 130, 246, 0.05)' }}>
                <MaterialCommunityIcons name="github" size={20} color={highContrastMode ? "#000000" : "#3b82f6"} />
                <Text className="font-avenirMedium text-sm ml-3" style={linkStyle}>Open Source Repository</Text>
              </TouchableOpacity>
              
              <TouchableOpacity onPress={openEmail} className="flex flex-row items-center p-3 rounded-lg" style={{ backgroundColor: highContrastMode ? 'rgba(0, 0, 0, 0.05)' : 'rgba(59, 130, 246, 0.05)' }}>
                <MaterialCommunityIcons name="email" size={20} color={highContrastMode ? "#000000" : "#3b82f6"} />
                <Text className="font-avenirMedium text-sm ml-3" style={linkStyle}>Contact: msitu@msitu.tech</Text>
              </TouchableOpacity>
            </View>
          </View>
        </ScrollView>

        {/* Fixed Footer */}
        <View className="p-4 border-t bg-gray-50" style={{ 
          borderColor: highContrastMode ? '#000000' : '#e5e7eb',
          backgroundColor: highContrastMode ? '#f8f9fa' : '#f9fafb'
        }}>
          <Text className="font-avenirMedium text-xs text-center" style={subtitleStyle}>
            "A nation that destroys its soils destroys itself. Forests are the lungs of our land, purifying the air and giving fresh strength to our people."
          </Text>
          <Text className="font-avenirBold text-xs text-center mt-2" style={textStyle}>
            ― Franklin D. Roosevelt
          </Text>
        </View>
      </Reanimated.View>
    </Reanimated.View>
  );
};

export default AboutMsituModal; 