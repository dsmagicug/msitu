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
import com.dsmagic.kibira.R
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat

class NotifyUserSignals {
    companion object {
        lateinit var mediaPlayer: MediaPlayer
        val decimalFormat = DecimalFormat("##.##")
        val handler = Handler(Looper.getMainLooper())
        var isBeeping = false
        var reasonForBeeping = ""
        var oldScenario = ""
        fun beepingSoundForDirectionIndicator(scenario: String, context: Context): MediaPlayer {
            try {
                when (scenario) {
                    "ShortBeep" -> {
                        mediaPlayer = MediaPlayer.create(context, R.raw.signalbeepmp3)
                        startPlayer(mediaPlayer, scenario, oldScenario)
                    }
                    "ErrorBeep" -> {
                        mediaPlayer = MediaPlayer.create(context, R.raw.errorbeep)
                        startPlayer(mediaPlayer, scenario, oldScenario)
                    }
                    "Left" -> {
                        mediaPlayer = MediaPlayer.create(context, R.raw.turnleftmp3)
                        startPlayer(mediaPlayer, scenario, oldScenario)
                    }
                    "Right" -> {
                        mediaPlayer = MediaPlayer.create(context, R.raw.turnrightmp3)
                        startPlayer(mediaPlayer, scenario, oldScenario)
                    }

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return mediaPlayer
        }

        var playerThread: Thread? = null
        fun beepingSoundForMarkingPosition(scenario: String, context: Context) {
            playerThread = Thread {
                try {
                    when (scenario) {
                        "ShortBeep" -> {
                            mediaPlayer = MediaPlayer.create(context, R.raw.beepmp3)
                            //startPlayer(mediaPlayer!!, scenario, oldScenario)
                        }
                        "At Point" -> {
                            mediaPlayer = MediaPlayer.create(context, R.raw.markheremp3)
                            //startPlayer(mediaPlayer!!, scenario, oldScenario)
                        }
                        "Slow Down" -> {
                            mediaPlayer = MediaPlayer.create(context, R.raw.slowdownmp3)

                            //startPlayer(mediaPlayer!!, scenario, oldScenario)
                        }
                    }
                    handler.post {
                        mediaPlayer.start()
                        mediaPlayer.isLooping = true
                        reasonForBeeping = scenario
                        isBeeping = true
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            playerThread!!.start()

        }
//        fun startPlayer (player: MediaPlayer, s: String, os:String ):MediaPlayer{
//           player.start()
//            player.isLooping =  true
//            return player
//        }


        fun startPlayer(player: MediaPlayer, s: String, os: String): MediaPlayer {
            if (!player.isPlaying && s != os) {
                player.start()
                player.isLooping = true
            } else {
                player.stop()
            }
            oldScenario = s
            return player
        }

        fun keepPlaying() {

        }

        fun stopActivePlayer(activePlayer: MediaPlayer) {
            if (activePlayer != activePlayer && !activePlayer.isPlaying) {
                activePlayer.stop()
            }

        }

        fun stopBeep(context: Context) {
            mediaPlayer = MediaPlayer.create(context, R.raw.signalbeepmp3)
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            } else {
                //Toast.makeText(context,"stop $isBeeping $reasonForBeeping",Toast.LENGTH_SHORT).show()
            }
            isBeeping = false
            reasonForBeeping = " "
        }

        fun pulseUserLocationCircle(circle: Circle) {

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

        fun vibration(activity: Activity) {
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

        fun statisticsWindow(
            activity: Activity,
            size: Int,
            textView: TextView,
            list: MutableList<*>,
            distance: Float
        ) {
            decimalFormat.roundingMode = RoundingMode.DOWN
            val dist = decimalFormat.format(distance)
            val d = dist.toString()
            activity.findViewById<TextView>(R.id.distance).text = d
            activity.findViewById<TextView>(R.id.distanceUnits).text = MainActivity.gapUnits
            activity.findViewById<TextView>(R.id.numberOfPoints).text = size.toString()
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
                        .setImageResource(R.drawable.hand)
                }
                "Red" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.YELLOW
                    activity.findViewById<TextView>(R.id.plantText).text =
                        "Point In front or behind"
                    activity.findViewById<ImageView>(R.id.plantValue)
                        .setImageResource(R.drawable.hand)

                }
                "Yellow" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.YELLOW
                    activity.findViewById<TextView>(R.id.plantText).text =
                        "Point In front or behind"
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