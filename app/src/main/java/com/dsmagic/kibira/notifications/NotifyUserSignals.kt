package com.dsmagic.kibira.notifications

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.text.Layout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
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
import java.io.IOException
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

        fun startBeep() {

            try {
                mediaplayer = MediaPlayer.create(context, R.raw.longbeep)
//            mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
//                mediaplayer.prepare()
                mediaplayer.start()
                mediaplayer.isLooping = true
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

        lateinit var anim: ObjectAnimator
        fun flashPosition(color: String, T: LinearLayout) {
            val textViewToBlink = T
            var animationColor: Int = 0
            when (color) {
                "Green" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = com.google.android.libraries.places.R.color.quantum_googgreen
                    MainActivity.positionText.text = "Mark here"
                    MainActivity.positionImage.setImageResource(R.drawable.tick)
                }
                "Orange" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.rgb(255, 215, 0)
                    MainActivity.positionText.text = "slow down"
                    MainActivity.positionImage.setImageResource(R.drawable.tick)
                }
                "Red" -> {
                    MainActivity.pointCardview.isVisible = true
                    animationColor = Color.RED
                    MainActivity.positionText.text = "Away from \nPoint"
                    MainActivity.positionImage.setImageResource(R.drawable.cross)

                }
                "Cyan" -> {
                    animationColor = Color.GREEN

                }
            }
            handler.post {
                anim = ObjectAnimator.ofInt(
                    textViewToBlink,
                    "backgroundColor", animationColor, Color.WHITE, animationColor, animationColor
                )

                anim.duration = 6000
                anim.setEvaluator(ArgbEvaluator())
                anim.repeatMode = ValueAnimator.RESTART
                anim.repeatCount = 2
                anim.start()
            }

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