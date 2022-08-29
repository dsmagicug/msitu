package com.dsmagic.kibira.notifications

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.TextView
import android.widget.Toast
import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.MainActivity.Companion.context
import com.dsmagic.kibira.MainActivity.Companion.thisActivity
import com.dsmagic.kibira.R
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat

class NotifyUserSignals {
    companion object {
        lateinit var mediaplayer: MediaPlayer
        val vibrator = thisActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val decimalFormat = DecimalFormat("##.##")

        fun startBeep() {

            try {
            mediaplayer = MediaPlayer.create(context, R.raw.longbeep)
//            mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
//                mediaplayer.prepare()
                mediaplayer.start()
                mediaplayer.isLooping = true
            }
            catch (e:IOException){
                e.printStackTrace()
            }


        }
        fun stopBeep(){
            if(mediaplayer.isPlaying){
                mediaplayer.stop()

            }

        }

        fun vibration() {
            val vibrationEffect:VibrationEffect
            if(!vibrator.hasVibrator()){
               return
            }
            //ENSURING THAT THE PHONE IS RUNNING ANDROID ABOVE OREO
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrationEffect = VibrationEffect.createOneShot(5000,VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.cancel()
                vibrator.vibrate(vibrationEffect)
                Toast.makeText(context,"Vibrating now", Toast.LENGTH_SHORT).show()
            }

        }

        fun statisticsWindow(size:Int,textView:TextView,list:MutableList<*>,distance:Float){
            decimalFormat.roundingMode = RoundingMode.DOWN
            val dist = decimalFormat.format(distance)
            val d = dist.toString()
            MainActivity.displayedDistance.text = d
            MainActivity.displayedDistanceUnits.text = MainActivity.gapUnits
            MainActivity.displayedPoints.text = size.toString()
            textView.text = list.size.toString()
        }
    }
}