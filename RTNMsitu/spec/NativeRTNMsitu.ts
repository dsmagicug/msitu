import { TurboModule, TurboModuleRegistry } from 'react-native';
import { type PlantingLine, type LongLat } from './types/backend';
import { LatLng } from 'react-native-maps';

export interface Spec extends TurboModule {

    toPoint(coord:{latitude: number; longitude: number }):
    {x:number, y:number, zone:number, hemisphere:string}| null;

    lineToCoords(
        points:Array<{x:number, y:number, zone:number, hemisphere:string}>,
        center:{x:number, y:number, zone:number, hemisphere:string}):
        Promise<Array<LongLat>>;

    linesToCoords(
        lines:Array<Array<{x:number, y:number, zone:number, hemisphere:string}>>,
        center:{x:number, y:number, zone:number, hemisphere:string}
    ):Promise<Array<Array<LongLat>>>

    generateMesh(
        first: { latitude: number; longitude: number },
        second: { latitude: number; longitude: number },
        meshDirection:string,
        meshType:string,
        gapSize:number,
        lineLength:number
    ):Promise<string>;

    closetPointRelativeToRoverPosition(
        roverLocation:{ latitude: number; longitude: number },
        points:Array<{latitude: number; longitude: number}>
    ):Promise<LatLng>

    distanceBtnCoords(pt1:{latitude: number; longitude: number }, pt2:{latitude: number; longitude: number }): number| null;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RTNMsitu',) as Spec;
