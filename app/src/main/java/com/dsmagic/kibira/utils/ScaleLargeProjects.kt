package com.dsmagic.kibira.utils

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

import android.app.Activity
import androidx.core.view.isVisible
import com.dsmagic.kibira.activities.MainActivity
import com.dsmagic.kibira.dataReadings.PlantingLine
import com.dsmagic.kibira.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ScaleLargeProjects {
    //make sure we are loading 10 lines each time, to prevent app from crashing.
    companion object{
        fun updateProjectLines(activity: Activity): MutableList<PlantingLine> {
            val fab_moreLines = activity.findViewById<FloatingActionButton>(R.id.fab_moreLines)
            lateinit var drawPoints: MutableList<PlantingLine>
            val projectLinesSize = MainActivity.projectLines.size

            if (projectLinesSize < 10) {
                drawPoints = MainActivity.projectLines.subList(0, MainActivity.projectLines.size)
                MainActivity.projectLines = MainActivity.projectLines.subList(MainActivity.projectLines.size, MainActivity.projectLines.size)
            } else {
                drawPoints = MainActivity.projectLines.subList(0, 10)
                MainActivity.projectLines = MainActivity.projectLines.subList(10, MainActivity.projectLines.size)
            }
            fab_moreLines.isVisible = projectLinesSize != 0
            return drawPoints
        }
    }

}