 package com.dsmagic.kibira.notifications

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.media.MediaPlayer
import android.os.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.MainActivity.Companion.context
import com.dsmagic.kibira.MainActivity.Companion.map
import com.dsmagic.kibira.MainActivity.Companion.thisActivity
import com.dsmagic.kibira.MainActivity.Companion.unmarkedCirclesList
import com.dsmagic.kibira.R
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import java.io.IOException
import java.lang.Math.abs
import java.math.RoundingMode
import java.text.DecimalFormat

 class NotifyUserSignals {
    companion object {
        lateinit var mediaplayer: MediaPlayer
        val vibrator = thisActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val decimalFormat = DecimalFormat("##.##")
        var circleID: String = " "        //the id is a combination of char and numbers
        var circle: Circle? = null
        val handler = Handler(Looper.getMainLooper())

       fun startBeep(scenario:String) {

            try {
                when(scenario){
                   "ShortBeep" -> {
                       mediaplayer = MediaPlayer.create(context, R.raw.shortbeep)
                   }
                    "ErrorBeep" -> {
                        mediaplayer = MediaPlayer.create(context, R.raw.errorbeep)

                    }
                }

                mediaplayer.start()
                mediaplayer.isLooping = false

            } catch (e: IOException) {
                e.printStackTrace()
            }


        }

        fun stopBeep() {
            mediaplayer = MediaPlayer.create(context, R.raw.longbeep)
            if (mediaplayer.isPlaying) {
                mediaplayer.stop()

            }

        }

        fun vibration() {
            val vibrationEffect: VibrationEffect
            if (!vibrator.hasVibrator()) {
                return
            }
            //ENSURING THAT THE PHONE IS RUNNING ANDROID ABOVE OREO
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrationEffect =
                    VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.cancel()
                vibrator.vibrate(vibrationEffect)
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
        fun flashPosition(color: String, T: TextView) {
            val textViewToBlink = T
            var animationColor: Int = 0
            when (color) {
                "Green" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.GREEN
                    MainActivity.positionText.text = "Mark here"
                    MainActivity.positionImage.setImageResource(R.drawable.tick)
                }
                "Orange" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.rgb(255, 215, 0)
                    MainActivity.positionText.text = "slow down"
                    MainActivity.positionImage.setImageResource(R.drawable.caution)
                }
                "Red" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.YELLOW
                    MainActivity.positionText.text = "Point In front or behind"
                    MainActivity.positionImage.setImageResource(R.drawable.caution)

                }
                "Yellow" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.YELLOW
                    MainActivity.positionText.text = "Point behind"
                    MainActivity.positionImage.setImageResource(R.drawable.caution)

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

        fun isUserlocationOnPath(userLatLng:LatLng,line:MutableList<LatLng>):Boolean{
            val start = System.currentTimeMillis()
                 return PolyUtil.isLocationOnPath(userLatLng,line,true,0.2)
        }
        //get the straight line bearing and determin straing form that
        fun keepUserInStraightLine(
            firstPoint: Location,
            nextPoint: Location,
            currentPosition: Location
        ):String {
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
            Toast.makeText(
                context,
                "straightLine = $assumedStraightLineBearing ours = $userBearingAtTimeT $direction",
                Toast.LENGTH_LONG
            ).show()
            return direction
        }

        fun shrinkCircle(distance: Float, pt: LatLng) {
            val firstDistance = distance
            var secondDistance: Float = 0.0f

            //ensure that we only loop through when it is necessary for us to do so.
            if (circleID.isBlank() || circle == null) {
                for (c in unmarkedCirclesList) {
                    if (c.center == pt) {
                        circleID = c.id
                        circle = c
                    }
                }
            } else {
                circleID
                circle
            }
            val diff = firstDistance - secondDistance
            when {
                diff == firstDistance -> {
                    secondDistance = firstDistance
                }
                //distance has reduced thus person is closer to point
                diff < 0 -> {

                    circle!!.remove()
                    map?.addCircle(
                        CircleOptions().radius(distance.toDouble()).center(pt).fillColor(Color.RED)
                    )
                    secondDistance = firstDistance
                }
                //person is away from point
                diff > 0 -> {
                    circle!!.remove()
                    map?.addCircle(
                        CircleOptions().radius(distance.toDouble()).fillColor(Color.RED).center(pt)
                    )
                    secondDistance = firstDistance
                }
            }


        }

    }
}