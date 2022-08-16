package com.dsmagic.kibira.roomDatabase.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project

data class projectAndCoordinates(
    @Embedded val project :Project,
    @Relation(
parentColumn = "id",
        entityColumn = "projectID"
    )
    val coordinates: List<Coordinates>
)
