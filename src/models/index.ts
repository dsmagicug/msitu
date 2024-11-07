import { LatLng, PlantingLine, Point } from "rtn-msitu";

export type Project = {
    id:number;
    name:string;
    basePoints:Array<LatLng>
    center:Point,
    plantingLines:Array<PlantingLine> | []
    markedPoints:Array<LatLng> | []
    lastLineIndex:number | -1
}