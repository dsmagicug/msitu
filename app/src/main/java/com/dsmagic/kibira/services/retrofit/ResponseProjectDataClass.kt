package com.dsmagic.kibira.services.retrofit

//response after creation of project ...could use better naming
data class ResponseProjectDataClass(
    val createdAt: String,
    val gap_size: Int,
    val message: String,
    val name: String,
    val projectID: Int,
    val tag: String,
    val user_id: Int
)