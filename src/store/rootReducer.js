// ** Reducers Imports

import modals from "./modal"
import project from "./projects"
import bluetooth from "./bluetooth"
import nmeaListener from "./nmeaListener"
import pegging from "./pegging"
import settings from "./settings"
import usbSerial from "./usbSerial"

const rootReducer = {
    modals,
    project,
    bluetooth,
    nmeaListener,
    pegging,
    settings,
    usbSerial
};
export default rootReducer;
