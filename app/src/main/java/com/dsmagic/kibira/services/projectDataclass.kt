package com.dsmagic.kibira.services


data class deleteProjectDataClass(
    val project_id:Int,
)

data class deleteProjectResponse(
    val message:String,
)

data class DeleteCoords(
    val coord_id:Int,
)

data class DeleteCoordsResponse(
    val message:String
)