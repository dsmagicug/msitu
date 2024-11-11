// ** Reducers Imports

import modals from "./modal"
import project from "./projects"
import bluetooth from "./bluetooth"
import nmeaListener from "./nmeaListener"

const rootReducer = {
    modals,
    project,
    bluetooth,
    nmeaListener
};
export default rootReducer;
