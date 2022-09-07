package com.dsmagic.kibira.notifications

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
import android.widget.Toast
import androidx.core.view.isVisible
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.MainActivity.Companion.context
import com.dsmagic.kibira.R
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat

class NotifyUserSignals {
    companion object {
        lateinit var mediaplayer: MediaPlayer

        val decimalFormat = DecimalFormat("##.##")
        var circleID: String = " "        //the id is a combination of char and numbers
        var circle: Circle? = null
        val handler = Handler(Looper.getMainLooper())

        fun startBeep(scenario: String): MediaPlayer {

            try {
                when (scenario) {
                    "ShortBeep" -> {

                        mediaplayer = MediaPlayer.create(context, R.raw.signalbeepmp3)
                    }
                    "ErrorBeep" -> {

                        mediaplayer = MediaPlayer.create(context, R.raw.errorbeep)

                    }
                    "Left" -> {

                        mediaplayer = MediaPlayer.create(context, R.raw.turnleftmp3)
                    }
                    "Right" -> {

                        mediaplayer = MediaPlayer.create(context, R.raw.turnrightmp3)
                    }
                    "At Point" -> {

                        mediaplayer = MediaPlayer.create(context, R.raw.markheremp3)
                    }
                    "Slow Down" -> {
                        mediaplayer = MediaPlayer.create(context, R.raw.slowdownmp3)
                    }
                }
                mediaplayer.start()
                mediaplayer.isLooping = true

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return mediaplayer
        }

        fun stopBeep(mediaPlayer: MediaPlayer) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()

            }

        }

        fun pulseUserLocationCircle(circle: Circle) {

            val runnableCode = object : Runnable {
                override fun run() {
                    var w = circle.radius
                    w += 0.1
                    if (w > 0.7) {
                        w = 0.4
                    }
                    circle.radius = w
                    handler.postDelayed(this, 50)
                }
            }

            handler.postDelayed(runnableCode, 50)
        }

        fun vibration(activity: Activity) {
            val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect: VibrationEffect
            if (!vibrator.hasVibrator()) {
                return
            }
            //ENSURING THAT THE PHONE IS RUNNING ANDROID ABOVE OREO
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrationEffect =
                    VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE)
//                vibrator.cancel()
//                vibrator.vibrate(vibrationEffect)
                Toast.makeText(context, "Vibrating now", Toast.LENGTH_SHORT).show()
            }

        }

        fun statisticsWindow(size: Int, textView: TextView, list: MutableList<*>, distance: Float) {
            decimalFormat.roundingMode = RoundingMode.DOWN
            val dist = decimalFormat.format(distance)
            val d = dist.toString()
            MainActivity.displayedDistance.text = d
            MainActivity.displayedDistanceUnits.text = MainActivity.gapUnits
            MainActivity.displayedPoints.text = size.toString()
            textView.text = list.size.toString()
        }

        lateinit var proximityAnimator: ObjectAnimator
        fun flashPosition(color: String, T: TextView, activity: Activity) {
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
                        .setImageResource(R.drawable.caution)
                }
                "Red" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.YELLOW
                    activity.findViewById<TextView>(R.id.plantText).text =
                        "Point In front or behind"
                    activity.findViewById<ImageView>(R.id.plantValue)
                        .setImageResource(R.drawable.caution)

                }
                "Yellow" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.YELLOW
                    activity.findViewById<TextView>(R.id.plantText).text =
                        "Point In front or behind"
                    activity.findViewById<ImageView>(R.id.plantValue)
                        .setImageResource(R.drawable.caution)

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
            val start = System.currentTimeMillis()
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

//        fun shrinkCircle(distance: Float, pt: LatLng) {
//            val firstDistance = distance
//            var secondDistance: Float = 0.0f
//
//            //ensure that we only loop through when it is necessary for us to do so.
//            if (circleID.isBlank() || circle == null) {
//                for (c in unmarkedCirclesList) {
//                    if (c.center == pt) {
//                        circleID = c.id
//                        circle = c
//                    }
//                }
//            } else {
//                circleID
//                circle
//            }
//            val diff = firstDistance - secondDistance
//            when {
//                diff == firstDistance -> {
//                    secondDistance = firstDistance
//                }
//                //distance has reduced thus person is closer to point
//                diff < 0 -> {
//
//                    circle!!.remove()
//                    map?.addCircle(
//                        CircleOptions().radius(distance.toDouble()).center(pt).fillColor(Color.RED)
//                    )
//                    secondDistance = firstDistance
//                }
//                //person is away from point
//                diff > 0 -> {
//                    circle!!.remove()
//                    map?.addCircle(
//                        CircleOptions().radius(distance.toDouble()).fillColor(Color.RED).center(pt)
//                    )
//                    secondDistance = firstDistance
//                }
//            }
//
//
//        }

    }
}