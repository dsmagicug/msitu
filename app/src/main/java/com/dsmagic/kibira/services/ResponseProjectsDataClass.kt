package com.dsmagic.kibira.services

//response after retrieving projects....could use better naming as well.
data class ResponseProjectsDataClass(
    val created_at: String,
    val gap_size: Int,
    val id: Int,
    val name: String,
    val updated_at: String,
    val user_id: Int,
    val mesh_size:Int

)
