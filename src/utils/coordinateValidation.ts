interface LatLng {
  latitude: number;
  longitude: number;
}

/**
 * Validates if a coordinate object has valid latitude and longitude values
 * This function is designed to be minification-resistant
 */
export const isValidCoordinate = (coord: any): coord is LatLng => {
  try {
    // Check if coord exists and has required properties
    if (!coord || typeof coord !== 'object') {
      return false;
    }
    
    // Check latitude
    const lat = coord.latitude;
    if (typeof lat !== 'number' || isNaN(lat) || lat < -90 || lat > 90) {
      return false;
    }
    
    // Check longitude
    const lng = coord.longitude;
    if (typeof lng !== 'number' || isNaN(lng) || lng < -180 || lng > 180) {
      return false;
    }
    
    return true;
  } catch (error) {
    // If any error occurs during validation, consider it invalid
    return false;
  }
};

/**
 * Returns a safe coordinate object, with fallback to default values if invalid
 */
export const getSafeCoordinate = (coord: any, fallback: LatLng = { latitude: 0, longitude: 0 }): LatLng => {
  if (isValidCoordinate(coord)) {
    return coord;
  }
  return fallback;
}; 