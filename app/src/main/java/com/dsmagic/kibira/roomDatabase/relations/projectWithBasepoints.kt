package com.dsmagic.kibira.roomDatabase.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.dsmagic.kibira.roomDatabase.Entities.Basepoints
import com.dsmagic.kibira.roomDatabase.Entities.Coordinates
import com.dsmagic.kibira.roomDatabase.Entities.Project

data class projectWithBasepoints(
    @Embedded val project : Project,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectID"
    )
    val basepoints: List<Basepoints>

)
