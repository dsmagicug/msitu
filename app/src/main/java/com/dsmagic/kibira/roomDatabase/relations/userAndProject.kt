package com.dsmagic.kibira.roomDatabase.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.dsmagic.kibira.roomDatabase.Entities.Project
import com.dsmagic.kibira.roomDatabase.Entities.User

data class userAndProject(
    @Embedded val user :User,
    @Relation(
            parentColumn = "id",
        entityColumn = "userID"
    )
    val projects : List<Project>
)
