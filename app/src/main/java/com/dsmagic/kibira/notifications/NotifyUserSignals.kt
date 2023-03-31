package com.dsmagic.kibira.notifications

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

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.media.MediaPlayer
import android.os.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.dsmagic.kibira.activities.MainActivity
import com.dsmagic.kibira.R
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import java.math.RoundingMode
import java.text.DecimalFormat

class NotifyUserSignals {
    companion object {
        val decimalFormat = DecimalFormat("##.##")
        val handler = Handler(Looper.getMainLooper())

        fun pulseEffectOnUserLocationCircle(circle: Circle) {
            val runnableCode = object : Runnable {
                override fun run() {
                    var w = circle.radius
                    w += 0.4
                    if (w > 0.6) {
                        w = 0.3
                    }
                    circle.radius = w
                    handler.postDelayed(this, 50)
                }
            }

            handler.postDelayed(runnableCode, 50)
        }

        fun vibrate(activity: Activity) {
            val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect: VibrationEffect
            if (!vibrator.hasVibrator()) {
                return
            }
            //ENSURING THAT THE PHONE IS RUNNING ANDROID ABOVE OREO
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrationEffect =
                    VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.cancel()
                vibrator.vibrate(vibrationEffect)

            }

        }

        fun displayStats(
            activity: Activity,
           // size: Int,
            textView: TextView,
            list: MutableList<*>,
            distance: Float
        ) {
            decimalFormat.roundingMode = RoundingMode.DOWN
            val dist = decimalFormat.format(distance)
            val d = dist.toString()
            activity.findViewById<TextView>(R.id.distance).text = d
            activity.findViewById<TextView>(R.id.distanceUnits).text = MainActivity.gapUnits
            //activity.findViewById<TextView>(R.id.numberOfPoints).text = size.toString()
            textView.text = list.size.toString()
        }

        lateinit var proximityAnimator: ObjectAnimator
        fun flashSignal(color: String, T: TextView, activity: Activity) {
            val textViewToBlink = T
            var animationColor: Int = 0
            when (color) {
                "Green" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.GREEN
                    activity.findViewById<TextView>(R.id.plantText).text = "Mark here"
                    activity.findViewById<ImageView>(R.id.plantValue)
                        .setImageResource(R.drawable.tick)
                }
                "Orange" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.rgb(255, 215, 0)
                    activity.findViewById<TextView>(R.id.plantText).text = "slow down"
                    activity.findViewById<ImageView>(R.id.plantValue)
                        .setImageResource(R.drawable.hand)
                }

                "Stop" -> {
                    MainActivity.pointCardview.isVisible = false


                }
            }
            handler.post {
                proximityAnimator = ObjectAnimator.ofInt(
                    textViewToBlink,
                    "backgroundColor", animationColor, Color.WHITE, animationColor, animationColor
                )

                proximityAnimator.duration = 1500
                proximityAnimator.setEvaluator(ArgbEvaluator())
                proximityAnimator.repeatMode = ValueAnimator.RESTART
                proximityAnimator.repeatCount = 2
                proximityAnimator.start()
            }

        }

        fun isUserlocationOnPath(userLatLng: LatLng, line: MutableList<LatLng>): Boolean {

            return PolyUtil.isLocationOnPath(userLatLng, line, true, 0.2)
        }

        //get the straight line bearing and determine straying from that
        fun keepUserInStraightLine(
            firstPoint: Location,
            nextPoint: Location,
            currentPosition: Location
        ): String {
            var direction = ""

            val assumedStraightLineBearing = kotlin.math.abs(firstPoint.bearingTo(nextPoint))
            val userBearingAtTimeT = kotlin.math.abs(currentPosition.bearingTo(nextPoint))
            val rangeRight = 5 + userBearingAtTimeT
            val rangeLeft = userBearingAtTimeT - 5

            //too much on the left
            when {
                userBearingAtTimeT > assumedStraightLineBearing -> {
                    direction = "Left"
                }
                //too much on the right
                userBearingAtTimeT < assumedStraightLineBearing -> {
                    direction = "Right"
                }
                rangeRight > userBearingAtTimeT && userBearingAtTimeT > rangeLeft -> {
                    direction = "Stop"
                }

            }
            return direction
        }

    }
}