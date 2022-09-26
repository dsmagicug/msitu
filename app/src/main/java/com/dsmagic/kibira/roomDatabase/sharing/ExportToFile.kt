package com.dsmagic.kibira.roomDatabase.sharing

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.os.Environment
import android.util.Log
import com.dsmagic.kibira.MainActivity.Companion.appdb
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.google.gson.Gson
import java.io.*
import java.nio.charset.Charset


class ExportToFile() {


    companion object{
        lateinit var project: Project;
        lateinit var projectDTO: ProjectDTO;
        lateinit var points: List<Coordinates>
        lateinit var context:Context

        suspend fun  exportProjectById(pid: Int, ctx: Context): Project {
            project = appdb.kibiraDao().getParticularProject(pid)
            context = ctx
            Log.d("Called","this has been called $pid")
            projectDTO =
                ProjectDTO()
            projectDTO.name = project.name
            projectDTO.gapsize = project.gapsize
            projectDTO.lineLength = project.lineLength
            projectDTO.meshType = project.MeshType
            projectDTO.gapsizeunits = project.gapsizeunits
            projectDTO.lineLengthUnits= project.lineLengthUnits
            points = getProjectCoordinates(pid)
            projectDTO.coordinates = points

            transformToJson(projectDTO)
            return project
        }
        suspend fun getProjectCoordinates(projectId: Int): List<Coordinates> {
            return appdb.kibiraDao().getCoordinatesForProject(projectId)
        }

        fun transformToJson(projectDTO: ProjectDTO){
            val gson = Gson()
            val json = gson.toJson(projectDTO)

            // Download dir path
            val downloadDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            val dir =  File(downloadDirPath.toURI());
            val name = project.name.replace(" ", "_")
            val extension = ".json"
            var fileName = "$name$extension"
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