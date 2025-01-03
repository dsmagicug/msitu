// ** Reducers Imports

import modals from "./modal"
import project from "./projects"
import bluetooth from "./bluetooth"
import nmeaListener from "./nmeaListener"
import pegging from "./pegging"

const rootReducer = {
    modals,
    project,
    bluetooth,
    nmeaListener,
    pegging
};
export default rootReducer;
