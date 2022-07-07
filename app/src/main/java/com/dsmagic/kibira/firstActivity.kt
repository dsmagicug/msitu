package com.dsmagic.kibira


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle

import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dsmagic.kibira.R.layout
import com.dsmagic.kibira.R.string
import com.dsmagic.kibira.services.AppModule
import com.dsmagic.kibira.services.ResponseProjectDataClass
import com.dsmagic.kibira.services.createProjectDataClass
import com.google.android.gms.maps.SupportMapFragment
import l
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//Returning a layout as a dialog box
class firstActivity : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
           //l

          //  var l = MainActivity().getProjects()
            // Log.d("list","$l")
            val ar: Array<String> = MainActivity().projectList.toTypedArray()
           var selectedProject: String = ""

           //val ar = arrayOf("Project one","Project two","Project three")
           // var ar = MainActivity().l
           // val ar = emptyArray<String>()
            var checkedItemIndex = -1
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
//            val mDialogView: View = inflater.inflate(layout.activity_main, null)
//              builder.setView(mDialogView)
            if (ar.isEmpty()) {
                builder.setView(inflater.inflate(layout.activity_create_project, null))
                    // Add action buttons

                    .setNegativeButton(string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->


                        })
                    .setPositiveButton(string.create_new_project,
                        DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->
                            oncreateclick()

                        })

                builder.create()

            } else {
               // val mDialogView: View = MainActivity().setContentView(layout.activity_main)
//              builder.setView(mDialogView)
                builder.setTitle("Choose Project")
                    .setSingleChoiceItems(ar, checkedItemIndex,
                        DialogInterface.OnClickListener { dialog, which ->
                            checkedItemIndex = which
                            selectedProject = ar[which]
                        })

//                    .setNeutralButton("New Project",
//                        DialogInterface.OnClickListener { dialog, id ->
//                            dialog.dismiss()
//
//                                val builder = AlertDialog.Builder(it)
//                                val inflater = requireActivity().layoutInflater
//
//
//                                builder.setView(inflater.inflate(layout.create_project, null))
//                                    // Add action buttons
//
//                                    .setNegativeButton(string.cancel,DialogInterface.OnClickListener{ dialog, id ->
//
//
//                                    })
//                                    .setPositiveButton(string.create_new_project, DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int ->
//
//                                        oncreateclick()
//
//                                    })
//                                builder.create()
//
//                        })
                    .setNegativeButton(R.string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
                    .setPositiveButton("ok",

                        DialogInterface.OnClickListener { dialog, id ->

                            if(selectedProject == ""){
                                //MainActivity().showSnackBar(mDialogView)
                            }else
                            {
                                val displayProjectName: TextView? = activity?.findViewById(R.id.display_project_name)
                                displayProjectName?.text = selectedProject
                            }
                          crea(selectedProject)

//                            val intent = Intent(context,Geometry::class.java)
//                            intent.putExtra("values",project2)
//                            startActivity(intent)



Log.d("selected","$selectedProject")

                        })

                // Create the AlertDialog object and return it
                builder.create()
            }

        } ?: throw IllegalStateException("Activity cannot be null")
    }

fun crea(name:String) {
    val newvalues = "[\n" +
            "  [{\"lat\":8.4,\"lng\":43.9},{\"lat\":8,\"lng\":80}],\n" +
            "  {\"Base points\":{\"first\":[{\"lat\":8,\"lng\":9}],\n" +
            "    \"second\":[{\"lat\":9,\"lng\":10}]}\n" +
            "  },\n" +
            "  {\"name\":\"Project one\"},\n" +
            "  {\"gap size\":4},\n" +
            "  {\"mesh\":600.0}\n" +
            "\n" +
            "\n" +
            "]"
    val project2 =  "[\n" +
            "  [{\"lat\":8.4,\"lng\":43.9},{\"lat\":8,\"lng\":80}],\n" +
            "  {\"Base points\":{\"first\":[{\"lat\":8,\"lng\":9}],\n" +
            "    \"second\":[{\"lat\":9,\"lng\":10}]}\n" +
            "  },\n" +
            "  {\"name\":\"Project two\"},\n" +
            "  {\"gap size\":4},\n" +
            "  {\"mesh\":600.0}\n" +
            "\n" +
            "\n" +
            "]"

}

    fun oncreateclick() {

         val sharedPrefFile = "kibirasharedfile"


        val projectname = dialog?.findViewById<EditText>(R.id.projectName)
        val meshSize = dialog?.findViewById<EditText>(R.id.MeshSize)

        val gapsize = dialog?.findViewById<EditText>(R.id.gapSize)
        val displayProjectName = activity?.findViewById<TextView>(R.id.display_project_name)


        val gap_size_string = gapsize?.text.toString()

        val project_name: String = projectname?.text.toString()
        val mesh_size: String = meshSize?.text.toString()

        if(project_name =="" || gap_size_string == ""){
            alertfail("Please fill all fields")
        }
        else{

            val sharedPreferences: SharedPreferences =
                activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

            val editor = sharedPreferences.edit()
            editor.putString("size_key", gap_size_string)
            editor.putString("name_key", project_name)
            editor.putString("mesh_key", mesh_size)
            editor.apply()
            editor.commit()

            if(editor.commit()){
                val saved_project_name: String? = sharedPreferences.getString("name_key", "defaultValue")
                val gap_size: String? = sharedPreferences.getString("size_key","defaultValue")
                val saved_gap_size =  gap_size!!.toInt()
                val UID: String? = sharedPreferences.getString("userid_key", "defaultValue")
                val userID = UID!!.toInt()
                val mesh_size_string: String? = sharedPreferences.getString("mesh_key","defaultValue")
                val MeshSize = mesh_size_string!!.toInt()
                Geogmesh_size = MeshSize.toDouble()
                Geoggapsize = saved_gap_size
                val CreateProjectRetrofitObject = AppModule.retrofitInstance()
                val modal = createProjectDataClass(saved_gap_size,saved_project_name!!,userID, MeshSize)
                val retrofitData =  CreateProjectRetrofitObject.createProject(modal)
                retrofitData.enqueue(object : Callback<ResponseProjectDataClass?> {
                    override fun onResponse(
                        call: Call<ResponseProjectDataClass?>,
                        response: Response<ResponseProjectDataClass?>
                    ) {
                        if(response.isSuccessful){
                            if(response.body() != null){
                                val ProjectName = response.body()!!.name
                                val ProjectIDInt = response.body()!!.projectID
                                val ProjectID = ProjectIDInt.toString()

                                if(response.body()!!.message == "created"){

                                    editor.putString("productID_key", ProjectID)
                                    editor.apply()
                                    editor.commit()

//                                    var act = MainActivity()
//                                    val mapFragment =
//                                        act.supportFragmentManager.findFragmentById(com.dsmagic.kibira.R.id.mapFragment) as SupportMapFragment?
//                                    mapFragment?.getMapAsync(act.callback)


                                }else{

                                    alertfail("Project $ProjectName not created")
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
                //createProject(saved_project_name!!,saved_gap_size,userID)

                displayProjectName?.text = saved_project_name

                Log.d("values","Project name is: $saved_project_name")

            } else{
                Log.d("not","Not saved")
            }
        }

    }
    fun alertfail(S:String){
        this.activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Error")
                .setIcon(R.drawable.cross)
                .setMessage(S)
                .show()
        }
    }
}

