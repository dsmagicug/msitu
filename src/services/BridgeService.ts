import {RTNMsitu} from "rtn-msitu"
import type { Point, LatLng, LongLat, MeshDirection, MeshType, PlantingLine} from "rtn-msitu"

interface Bridge{

    toPoint(coord:LatLng): Point| null;

    lineToCoords(
        points:Array<Point>,
        center:Point):Promise<Array<LongLat> | []>

    linesToCoords(
        lines:Array<Array<Point>>,
        center:Point
    ):Promise<Array<Array<LongLat>> | []>

    generateMesh(
        first: LatLng,
        second: LatLng,
        meshDirection:MeshDirection,
        meshType:MeshType,
        gapSize:number,
        lineLength:number
    ):Promise<Array<PlantingLine>>;

}


class BridgeService implements Bridge {

    lineToCoords(points: Point[], center: Point): Promise<LongLat[] | []> {
        return new Promise<LongLat[] | []>((resolve, reject) => {
            RTNMsitu.lineToCoords(points, center).then(result=>{
                resolve(result)
            }).catch(error=>{
                reject(error)
            })
        });
    }


    linesToCoords(lines: Point[][], center: Point): Promise<[] | LongLat[][]> {
        return new Promise<[] | LongLat[][]>((resolve, reject) => {
            RTNMsitu.linesToCoords(lines, center).then(result=>{
                resolve(result)
            }).catch(error=>{
                reject(error)
            })
        });
    }
    generateMesh(first: LatLng, second: LatLng, meshDirection: MeshDirection, meshType: MeshType, gapSize: number, lineLength: number): Promise<PlantingLine[]> {
        return new Promise<PlantingLine[]> ((resolve, reject) => {
            RTNMsitu.generateMesh(first, second, meshDirection, meshType, gapSize,lineLength).then(result=>{
                resolve(result)
            }).catch(error=>{
                reject(error)
            })
        });
    }

    public  toPoint(coordinate: LatLng ):Point | null{
       return RTNMsitu.toPoint(coordinate)
    }


}

export default BridgeService