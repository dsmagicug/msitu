package com.dsmagic.kibira.roomDatabase.Entities


/*
 *  This file is part of Kibira.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Kibira is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Kibira is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Kibira. If not, see <http://www.gnu.org/licenses/>
 */

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Project(
    @PrimaryKey(autoGenerate = true) val id:Int?,
    val name:String,
    val gapsize:Double,
    val lineLength:Double,
    val userID:Int,
    val MeshType:String,
    val gapsizeunits:String,
    val lineLengthUnits:String,
)
