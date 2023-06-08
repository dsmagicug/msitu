package com.dsmagic.kibira.roomDatabase.sharing
/*
 *  This file is part of Msitu.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Msitu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Msitu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Msitu. If not, see <http://www.gnu.org/licenses/>
 */

import android.content.ContentResolver
import android.net.Uri
import com.google.gson.JsonParser
import org.json.JSONArray
import java.io.InputStream
import java.util.*


class ImportProject {

   companion object{
       val REQUEST_CODE = 1
       lateinit var projectObj: ProjectDTO

           fun getImportedProjectObject(uri:Uri, contentResolver: ContentResolver): ProjectDTO? {
           val inputStream: InputStream? = contentResolver.openInputStream(uri)
           val scanner: Scanner = Scanner(inputStream).useDelimiter("\\A")
           val jsonString :String
           if (scanner.hasNext()) {
               jsonString= scanner.next()
           }else {
               jsonString = ""
           }
           if(jsonString !=""){
               return mapFromJson(jsonString)
           }
           else{
               return null
           }
       }
       fun mapFromJson(jsonString:String):ProjectDTO{
           val jsonParser = JsonParser()
           val jsonObject = jsonParser.parse(jsonString).asJsonObject
           projectObj = ProjectDTO()
            projectObj.name = jsonObject.get("name").toString()
           projectObj.gapsizeunits = jsonObject.get("gapsizeunits").toString()
           projectObj.gapsize = jsonObject.get("gapsize").asDouble
           projectObj.lineLength = jsonObject.get("lineLength").asDouble
           projectObj.lineLengthUnits = jsonObject.get("lineLengthUnits").toString()
           projectObj.meshType = jsonObject.get("meshType").toString()
           projectObj.plantingDirection = jsonObject.get("plantingDirection").toString()
           val jsonCoords = JsonHelper.toJSON(jsonObject.get("coordinates")) as JSONArray
           val coordinateArray = JsonHelper.toList(jsonCoords)
           val jsonBasePoints = JsonHelper.toJSON(jsonObject.get("basePoints")) as JSONArray
           val basePointsCoordinateArray = JsonHelper.toList(jsonBasePoints)
           projectObj.coordinates = coordinateArray
           projectObj.basePoints = basePointsCoordinateArray
            return projectObj
       }

   }


}