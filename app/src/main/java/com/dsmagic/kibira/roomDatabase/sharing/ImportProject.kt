package com.dsmagic.kibira.roomDatabase.sharing

import android.content.ContentResolver
import android.net.Uri
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject
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
           projectObj.meshType = jsonObject.get("meshType").toString()
           val jsonCoords = JsonHelper.toJSON(jsonObject.get("coordinates")) as JSONArray
           val coordinateArray = JsonHelper.toList(jsonCoords)
           projectObj.coordinates = coordinateArray

            return projectObj
       }

   }


}