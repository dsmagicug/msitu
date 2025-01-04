import React, { createContext, useState, useEffect, useContext, useCallback } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { throttle } from 'lodash';
import LatLong from '../services/NMEAService';
import { FixType } from 'rtn-msitu';

// Define the shape of the context value
interface RoverContextType {
  roverLocation: LatLong | null;
}

const RoverContext = createContext<RoverContextType | undefined>(undefined);

interface RoverProviderProps {
  children: React.ReactNode;
}

export const RoverProvider: React.FC<RoverProviderProps> = ({ children }) => {
  const [roverLocation, setRoverLocation] = useState<LatLong | null>(null);
  const [dataReadListener, setDataReadListener] = useState<(() => void) | null>(null);

  // Assuming these types are defined in your Redux store types
  const { activeProject, visibleLines, loading, scaledPlantingLines } = useSelector((store: any) => store.project);
  const { selectedDevice, init } = useSelector((store: any) => store.bluetooth);
  const dispatch = useDispatch();

  const onReceiveData = useCallback(async (buffer: { data: string }) => {
    const sentence = buffer.data.trim();
    const longLat = new LatLong(sentence);
    if (longLat.fixType !== FixType.NoFixData) {
      throttledUpdate(longLat);
    }
  }, []);

  const throttledUpdate = useCallback(
    throttle((location: LatLong) => setRoverLocation(location), 100),
    []
  );

  useEffect(() => {
    const checkConnectionAndSetupListener = async () => {
      if (!selectedDevice) return;
      const connection = await selectedDevice.isConnected();
      if (connection) {
        const readListener = selectedDevice.onDataReceived((buffer: { data: string }) => onReceiveData(buffer));
        setDataReadListener(() => readListener);
      }
    };

    checkConnectionAndSetupListener();

    return () => {
      console.log("Stop listening");
      if (dataReadListener) {
        dataReadListener();
      }
    };
  }, [selectedDevice, onReceiveData]);

  return (
    <RoverContext.Provider value={{ roverLocation }}>
      {children}
    </RoverContext.Provider>
  );
};

export const useRoverContext = (): RoverContextType => {
  const context = useContext(RoverContext);
  if (context === undefined) {
    throw new Error('useRoverContext must be used within a RoverProvider');
  }
  return context;
};