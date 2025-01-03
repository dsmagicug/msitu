import { FixType } from "rtn-msitu";
import { convertToUTC, parseToUTC } from "../utils";

 class LatLong {
    latitude: number = 0;
    longitude: number = 0;
    fixType: FixType = FixType.NoFixData;
    timeStamp?: string;
    numSatellites: number = 0;
    hdop: number = 0;
    aboveSeaLevel: number = 0;
    speed: number = 0;
    accuracy: number = 0;

    private lastVerticalAccuracy: number = 0;
    private lastHorizontalAccuracy: number = 0;
    

    constructor(sentence: string) {
        const l = sentence.split(",");
        if (l.length === 0 || l[0].length === 0) return;

        const sType = l[0].substring(1).toLowerCase();

        if (sType.endsWith("rmc")) {
            this.initRMC(l);
        } else if (sType.endsWith("gga")) {
            this.initGGA(l);
        } else if (sType.endsWith("gst")) {
            if (l.length > 7) {
                if (l[6] !== "" && l[7] !== "") {
                    this.lastVerticalAccuracy = parseFloat(l[6]);
                    this.lastHorizontalAccuracy = parseFloat(l[7]);
                    this.fixType = FixType.NoFixData;
                }
            }
        } else {
            this.fixType = FixType.NoFixData;
        }

        this.accuracy = (this.lastHorizontalAccuracy + this.lastVerticalAccuracy) / 2;
    }

    private initRMC(l: string[]): void {
        const mandatoryIndices = [1, 2, 3, 4, 5, 6, 7, 9, 12];
        if (mandatoryIndices.some(index => !l[index])) return;

        const dd = `${l[9]}-${l[1]}+0000`;
        this.timeStamp = parseToUTC(dd, "DDMMYY-HHmmss.SSZ")

        this.latitude = this.parseDegrees(l[3], l[4]);
        this.longitude = this.parseDegrees(l[5], l[6]);
        this.speed = parseFloat(l[7]) * 1.8 * 1000; // Knots to km/hr

        this.fixType = l[2] === "V" ? FixType.NoFixData : this.mapFixType(l[12]);
    }

    private initGGA(l: string[]): void {
        const mandatoryIndices = [1, 2, 3, 4, 5, 6, 7, 9];
        if (mandatoryIndices.some(index => !l[index])) return;

        this.timeStamp = convertToUTC(l[1])

        this.latitude = this.parseDegrees(l[2], l[3]);
        this.longitude = this.parseDegrees(l[4], l[5]);
        this.fixType = this.mapFixType(parseInt(l[6]));
        this.numSatellites = parseInt(l[7]);
        this.hdop = parseFloat(l[8]);
        this.aboveSeaLevel = parseFloat(l[9]);
    }

    private mapFixType(value: string | number): FixType {
        return {
            "A": FixType.Autonomous,
            "D": FixType.DGPS,
            "E": FixType.Estimated,
            "F": FixType.RTKFloat,
            "M": FixType.ManualInput,
            "R": FixType.RTKFix,
            "S": FixType.Simulated,
            1: FixType.Autonomous,
            2: FixType.DGPS,
            3: FixType.PPS,
            4: FixType.RTKFix,
            5: FixType.RTKFloat,
            6: FixType.Estimated,
            7: FixType.ManualInput,
            8: FixType.Simulated
        }[value] || FixType.NoFixData;
    }

    private parseDegrees(v: string, indicator: string): number {
        let offset = v.indexOf('.') - 2;
        if (offset < 0) offset = 0;

        const degrees = offset === 0 ? 0 : parseInt(v.substring(0, offset), 10);
        const minutes = parseFloat(v.substring(offset)) / 60.0;
        const sign = (indicator === "N" || indicator === "E") ? 1 : -1;

        return sign * (degrees + minutes);
    }

    static significantChange(oldLocation:LatLong, newLocation:LatLong){
        if (!oldLocation || ! newLocation){
            return true
        }
        const ANGLE_SIGNIFICANT_DIFF = 0.5e-7;
        return (
            !oldLocation || 
            Math.abs(oldLocation.latitude - newLocation.latitude) > ANGLE_SIGNIFICANT_DIFF ||
            Math.abs(oldLocation.longitude - newLocation.longitude) > ANGLE_SIGNIFICANT_DIFF
        );

    }

    static async asyncParse(sentence: string): Promise<LatLong> {
        try {
       
          const instance = new LatLong(sentence);
          return Promise.resolve(instance); 
        } catch (error) {
          return Promise.reject(error);
        }
      }
}

export default LatLong;