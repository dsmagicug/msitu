package com.dsmagic.kibira.roomDatabase.sharing

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.os.Environment
import android.util.Log
import com.dsmagic.kibira.activities.MainActivity.Companion.appdb
import com.dsmagic.kibira.roomDatabase.Entities.Basepoints
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.google.gson.Gson
import java.io.*
import java.nio.charset.Charset

/*
 *  This file is part of Msitu.
 *
 *  https://github.com/dsmagicug/msitu.git
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



class ExportToFile() {


    companion object{
        lateinit var project: Project;
        lateinit var projectDTO: ProjectDTO;
        lateinit var points: List<Coordinates>
        lateinit var basePoints: List<Basepoints>
        lateinit var context:Context

        suspend fun  exportProjectById(pid: Int, ctx: Context): Project {
            project = appdb.kibiraDao().getParticularProject(pid)
            context = ctx
            Log.d("Called","this has been called $pid")
            projectDTO = ProjectDTO()
            projectDTO.name = project.name
            projectDTO.gapsize = project.gapsize
            projectDTO.lineLength = project.lineLength
            projectDTO.meshType = project.MeshType
            projectDTO.gapsizeunits = project.gapsizeunits
            projectDTO.lineLengthUnits= project.lineLengthUnits
            points = getProjectCoordinates(pid)
            basePoints = getBasePoints(pid)
            projectDTO.coordinates = points
            projectDTO.basePoints = basePoints

            projectDTO.plantingDirection = project.plantingDirection

            transformToJson(projectDTO)
            return project
        }
        suspend fun getProjectCoordinates(projectId: Int): List<Coordinates> {
            return appdb.kibiraDao().getCoordinatesForProject(projectId)
        }
        suspend fun getBasePoints(projectId: Int):List<Basepoints>{
            return appdb.kibiraDao().getBasepointsForProject(projectId)
        }

        private fun transformToJson(projectDTO: ProjectDTO){
            val gson = Gson()
            val json = gson.toJson(projectDTO)

            // Download dir path
            val downloadDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            val dir =  File(downloadDirPath.toURI());
            val name = project.name.replace(" ", "_").replace("+", "").replace(":", "").replace("-","_")

            val extension = ".json"
            val fileName = "$name$extension"
            val inputStream: InputStream =
                ByteArrayInputStream(json.toByteArray(Charset.forName("UTF-8")))

            val data = ByteArray(inputStream.available())
            inputStream.read(data)
            inputStream.close()
            inputStream.read(data)

            val file = File(dir, fileName)
            val outputStream: OutputStream = FileOutputStream(file)
            outputStream.write(data)
            outputStream.close()
            val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager

            downloadManager.addCompletedDownload(file.getName(), file.getName(), true, "application/json",file.getAbsolutePath(),file.length(),true);

        }
    }

}