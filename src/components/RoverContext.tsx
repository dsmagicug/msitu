import React, { createContext, useContext, useEffect, useRef, useState, useMemo } from 'react';
import { useSelector } from 'react-redux';
import throttle from 'lodash/throttle';
import LatLong from '../services/NMEAService';
import { FixType } from 'rtn-msitu';

// Define the context type
interface RoverContextType {
  roverLocation: LatLong | null;
}

// Create the context with a default value
const RoverContext = createContext<RoverContextType | undefined>(undefined);

export const RoverProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [roverLocation, setRoverLocation] = useState<LatLong | null>(null);
  const [dataReadListener, setDataReadListener] = useState<any>(null);

  // @ts-ignore
  const { selectedDevice } = useSelector((state) => state.bluetooth);

  // Memoize the throttled update function to prevent unnecessary re-creations
  const throttledUpdate = useRef(
    throttle((location: LatLong) => setRoverLocation(location), 100)
  ).current;

  // Memoize the context value to prevent unnecessary re-renders
  const contextValue = useMemo(() => ({
    roverLocation,
  }), [roverLocation]);

  // Function to handle incoming data
  const onReceiveData = useMemo(() => {
    return async (buffer: { data: string }) => {
      const sentence = buffer.data.trim();
      const longLat = new LatLong(sentence);
      if (longLat.fixType !== FixType.NoFixData) {
        throttledUpdate(longLat);
      }
    };
  }, [throttledUpdate]);

  useEffect(() => {
    const checkConnectionAndSetupListener = async () => {
      if (!selectedDevice) return;
      const connection = await selectedDevice.isConnected();
      if (connection) {
        const readListener = selectedDevice.onDataReceived(onReceiveData);
        setDataReadListener(readListener);
      }
    };

    checkConnectionAndSetupListener();

    /* return () => {
      if (dataReadListener) {
        dataReadListener.remove(); // Cleanup listener on unmount
      }
    }; */
  }, [selectedDevice, onReceiveData, dataReadListener]);

  return (
    <RoverContext.Provider value={contextValue}>
      {children}
    </RoverContext.Provider>
  );
};

export const useRoverLocation = (): RoverContextType => {
  const context = useContext(RoverContext);
  if (!context) {
    throw new Error('useRoverLocation must be used within a RoverProvider');
  }
  return context;
};