package com.dsmagic.kibira.services.retrofit

data class RegisterDataclassX(
    val email: String,
    val name: String,
    val password: String,
    val password_confirmation: String
)

data class ResponseRegister(
    val message:String,
)