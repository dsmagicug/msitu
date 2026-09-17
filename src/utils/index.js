import dayjs from 'dayjs'
import customParseFormat from 'dayjs/plugin/customParseFormat'
import utc from 'dayjs/plugin/utc'
import { useSelector } from 'react-redux';

dayjs.extend(customParseFormat);
dayjs.extend(utc);

export function parseToUTC(input, format) {
    const parsedDate = dayjs(input, format);

  if (!parsedDate.isValid()) {
    return "";
  }
  return parsedDate.utc().toISOString();
}


export function convertToUTC(input) {

    const parsedTime = dayjs(input, "HHmmss");
    const today = dayjs().startOf('day');

    const finalDate = today
        .set('hour', parsedTime.hour())
        .set('minute', parsedTime.minute())
        .set('second', parsedTime.second());
  return finalDate.utc().toISOString();
}

export const generateError = error => {
    let errorMsg = 'Failed';
    if (error.response) {
      let msg = error.response.data.message
        ? error.response.data.message
        : error.response.data.error
        ? error.response.data.error
        : error.response.data.detail
        ? error.response.data.detail
        : error.response.data;
      errorMsg = `${msg}`;
    } else {
      errorMsg = `${error.message}`;
    }
    return errorMsg;
  };


  export const convertToMeters=(value, unit)=> {
    let meters;

    switch (unit.toLowerCase()) {

      case 'meter':
        meters = value * 1;
        break;
        case 'feet':
            meters = value * 0.3048;
            break;
        case 'inches':
            meters = value * 0.0254;
            break;
        case 'miles':
            meters = value * 1609.34;
            break;
        case 'acres':
            meters = value * 4046.86;
            break;
        default:
            throw new Error("Unsupported unit. Please use 'feet', 'inches', 'miles', or 'acres'.");
    }

    return meters;
}

export const pointToString = (point) => `${point.latitude},${point.longitude}`;
export const stringToPoint = (str) => {
  const [latitude, longitude] = str.split(',').map(Number);
  return { latitude, longitude };
};

export const useHighContrastMode = () => {
    // @ts-ignore
    const { settings } = useSelector(store => store.settings);
    return settings?.highContrastMode || false;
};

export const getHighContrastStyles = (highContrastMode) => {
    return {
        text: {
            color: highContrastMode ? '#000000' : '#1f2937',
            fontWeight: highContrastMode ? 'bold' : 'normal',
        },
        subtitle: {
            color: highContrastMode ? '#000000' : '#6b7280',
            fontWeight: highContrastMode ? '600' : 'normal',
        },
        body: {
            color: highContrastMode ? '#000000' : '#374151',
            fontWeight: highContrastMode ? 'bold' : 'normal',
        },
        container: {
            backgroundColor: highContrastMode ? 'rgba(255, 255, 255, 0.98)' : 'rgba(255, 255, 255, 0.95)',
            borderWidth: highContrastMode ? 2 : 1,
            borderColor: highContrastMode ? '#000000' : 'rgba(59, 130, 246, 0.1)',
            shadowColor: highContrastMode ? '#000000' : '#000',
            shadowOffset: { width: 0, height: 2 },
            shadowOpacity: highContrastMode ? 0.2 : 0.05,
            shadowRadius: 8,
            elevation: highContrastMode ? 4 : 2,
        },
        icon: {
            color: highContrastMode ? '#000000' : '#3b82f6',
        },
        button: {
            backgroundColor: highContrastMode ? '#000000' : '#3b82f6',
            borderColor: highContrastMode ? '#000000' : 'transparent',
        }
    };
};


export const    treeEstimate= (area, meshType, gap)=> {
    if (gap <= 0) {
        return 0;
    }
    if (area < 0) {
        return 0;
    }
    const ACRES_TO_M2 = 4047; // Approximate conversion factor to match the original code

    const areaM2 = area * ACRES_TO_M2;

    let areaPerTree;
    switch (meshType.toLowerCase()) {
        case 's':
            areaPerTree = gap * gap;
            break;
        case 't':
            areaPerTree = (Math.sqrt(3) / 2) * gap * gap;
            break;
        default:
            return 0;
    }

    const estimatedTrees = areaM2 / areaPerTree;
    return Math.round(estimatedTrees);
}
