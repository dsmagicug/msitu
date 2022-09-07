package com.dsmagic.kibira.utils

import android.app.Activity
import androidx.core.view.isVisible
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.PlantingLine
import com.dsmagic.kibira.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ScaleLargeProjects {
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