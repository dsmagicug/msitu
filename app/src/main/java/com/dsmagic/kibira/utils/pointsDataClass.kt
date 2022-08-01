package com.dsmagic.kibira.utils
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
data class pointsDataClass(
 var listOfPoints: PersistentList<Location> = persistentListOf()
)

@Serializable
data class Location(
    var lat:Double,
var lng:Double)
