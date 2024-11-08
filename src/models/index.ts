import { LatLng, PlantingLine, Point } from "rtn-msitu";

export type Project = {
    id:number;
    name:string;
    basePoints:Array<LatLng>
    gapSize:number
    lineLength:number
    gapSizeUnit:string
    lineLengthUnit:string
    createdAt:Date | null
    center:Point,
    plantingLines:Array<PlantingLine> | []
    markedPoints:Array<LatLng> | []
    lastLineIndex:number | -1
}