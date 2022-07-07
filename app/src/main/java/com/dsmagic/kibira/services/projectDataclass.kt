package com.dsmagic.kibira.services

data class projectDataclass(
    val gap_size: Int,
    val name: String,
    val user_id: Int
)

data class deleteProjectDataClass(
    val project_id:Int,
)

data class deleteProjectResponse(
    val message:String,
)