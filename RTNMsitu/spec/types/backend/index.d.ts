import { Point } from "../common"

export type PlantingLine = {
    points: Array<Point>
}

export enum FixType {
    NoFixData = "NoFixData",
    Autonomous = "Autonomous",
    Estimated = "Estimated",
    DGPS = "DGPS",
    PPS = "PPS",
    RTKFix = "RTKFix",
    RTKFloat = "RTKFloat",
    ManualInput = "ManualInput",
    Simulated = "Simulated"
}

export type LongLat = {
    latitude: number;
    longitude: number;
    fixType: FixType;
    timeStamp?: string;
    numSatellites: number;
    hdop: number;
    aboveSeaLevel: number;
    speed: number;
    accuracy: number;
}