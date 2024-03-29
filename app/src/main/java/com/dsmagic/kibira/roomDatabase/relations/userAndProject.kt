package com.dsmagic.kibira.roomDatabase.relations

/*

 *  This file is part of Msitu.

 *  https://github.com/dsmagicug/msitu.git

 *  Copyright (C) 2022 Digital Solutions

 *  Msitu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Msitu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Msitu. If not, see <http://www.gnu.org/licenses/>

 */

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
