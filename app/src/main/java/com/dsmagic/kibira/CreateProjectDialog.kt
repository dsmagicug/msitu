package com.dsmagic.kibira


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.dsmagic.kibira.R.layout
import com.dsmagic.kibira.R.string
import com.dsmagic.kibira.services.RegisterDataclassX
import com.dsmagic.kibira.services.ResponseProjectDataClass
import com.dsmagic.kibira.services.apiInterface
import com.dsmagic.kibira.services.createProjectDataClass
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileWriter
import java.io.PrintWriter

//Returning a layout as a dialog box
class CreateProjectDialog : DialogFragment() {

    val sharedPrefFile = "kibirasharedfile"


    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return this.activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflate
            val inflater = requireActivity().layoutInflater
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout

            if(tag == "create"){

                builder.setView(inflater.inflate(layout.activity_create_project, null))
                    // Add action buttons

                    .setNegativeButton(string.cancel,DialogInterface.OnClickListener{ dialog, id ->


                    })
                    .setPositiveButton(string.create_new_project, DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->

                        oncreateclick()

                    })

                builder.create()

            }
            else {
                val projects = activity?.findViewById<TextView>(R.id.projectOne)

                builder.setView(inflater.inflate(layout.list_projects, null))
//
                builder.create()

            }
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    fun createProject(name:String,gap_size:Int,user_id:Int){
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(MainActivity().BaseUrl)
            .build()
            .create(apiInterface::class.java)
        val modal = createProjectDataClass(gap_size,name,user_id)
        val retrofitData =  retrofitBuilder.createProject(modal)
        retrofitData.enqueue(object : Callback<ResponseProjectDataClass?> {
            override fun onResponse(
                call: Call<ResponseProjectDataClass?>,
                response: Response<ResponseProjectDataClass?>
            ) {
if(response.isSuccessful){
    if(response.body() != null){
        val ProjectName = response.body()!!.name
        if(response.body()!!.message == "created"){

            SuccessAlert("Project $ProjectName created")

        }else{

            alertfail("Project $ProjectName created")
        }
    }else{

        alertfail("Project not created")
    }

}else{

    alertfail("Project not created")
}
            }

            override fun onFailure(call: Call<ResponseProjectDataClass?>, t: Throwable) {
                alertfail(("Response failed, invalid data"))
            }
        })
    }
    fun oncreateclick() {


        val sharedPrefFile = "kibirasharedfile"

        val projectname = dialog?.findViewById<EditText>(R.id.projectName)

        val gapsize = dialog?.findViewById<EditText>(R.id.gapSize)
        var displayProjectName = activity?.findViewById<TextView>(R.id.display_project_name)


        val gap_size_string = gapsize?.text.toString()

        val project_name: String = projectname?.text.toString()

      if(project_name =="" || gap_size_string == ""){
          alertfail("Please fill all fields")
      }
      else{
          //val gap_size = Integer.parseInt(gap_size_string)

          val sharedPreferences: SharedPreferences =
              activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

          val editor = sharedPreferences.edit()
          editor.putString("size_key", gap_size_string)
          editor.putString("name_key", project_name)
          editor.apply()
          editor.commit()

          if(editor.commit()){
              val saved_project_name: String? = sharedPreferences.getString("name_key", "defaultValue")
              var gap_size: String? = sharedPreferences.getString("size_key","defaultValue")
             // val userID = MainActivity().user_id
              var saved_gap_size =  gap_size!!.toInt()

              val UID: String? = sharedPreferences.getString("userid_key", "defaultValue")
              val userID = UID!!.toInt()

             createProject(saved_project_name!!,saved_gap_size,userID)

              displayProjectName?.text = saved_project_name

              Log.d("values","Project name is: $saved_project_name")

          } else{
              Log.d("not","Not saved")
          }
      }



//        val project = Project(
//            project_name, gap_size
//
//        )
//
//        val path = "/main/assets/project.json"
//        try {
//            PrintWriter(FileWriter(path)).use {
//                val gson = Gson()
//                val jsonString = gson.toJson(project)
//               it.write(jsonString)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

    }
val c = dialog?.context
    fun alertfail(S:String){
        AlertDialog.Builder(c!!)
                .setTitle("Error")
                .setMessage(S)
            .setIcon(R.drawable.cross)
                .show()
    }

    fun SuccessAlert(S:String){

            AlertDialog.Builder(c!!)
                .setTitle("Success")
                .setIcon(R.drawable.tick)
                .setMessage(S)
                .show()

    }



}
