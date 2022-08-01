package com.dsmagic.kibira.utils

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object pointsDataserializer: Serializer<pointsDataClass> {
    override val defaultValue: pointsDataClass
        get() = pointsDataClass()

    override suspend fun readFrom(input: InputStream): pointsDataClass {
      return try{
Json.decodeFromString(deserializer = pointsDataClass.serializer(),string = input.readBytes().decodeToString())
       } catch (e:SerializationException){

           defaultValue
       }
    }

    override suspend fun writeTo(t: pointsDataClass, output: OutputStream) {
       output.write(Json.encodeToString(
           serializer = pointsDataClass.serializer(),
           value = t
       ).encodeToByteArray()
       )
    }
}