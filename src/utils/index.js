import moment from 'moment'


export function parseToUTC(input, format) {
  const parsedDate = moment(input, format);

  if (!parsedDate.isValid()) {
    return "";
  }
  return parsedDate.utc().toISOString();
}


export function convertToUTC(input) {
  
  const parsedTime = moment(input, "HHmmss");
  const today = moment().startOf('day'); 

  const finalDate = today.set({
    hour: parsedTime.hours(),
    minute: parsedTime.minutes(),
    second: parsedTime.seconds(),
  });
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
