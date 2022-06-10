package com.dsmagic.kibira

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap

/**
 * This shows how to use a custom location source.
 */
class currentLOcation : AppCompatActivity() {

    private val locationSource = LongPressLocationSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        lifecycleScope.launchWhenCreated {
            val map = mapFragment.awaitMap()
            init(map = map)
        }
        lifecycle.addObserver(locationSource)
    }

    @SuppressLint("MissingPermission")
    private fun init(map: GoogleMap) {
        map.setLocationSource(locationSource)
        map.setOnMapLongClickListener(locationSource)
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        map.isMyLocationEnabled = true
    }
}

/**
 * A [LocationSource] which reports a new location whenever a user long presses the map
 * at
 * the point at which a user long pressed the map.
 */
private class LongPressLocationSource : LocationSource, OnMapLongClickListener, LifecycleObserver {

    private var listener: OnLocationChangedListener? = null

    /**
     * Flag to keep track of the activity's lifecycle. This is not strictly necessary in this
     * case because onMapLongPress events don't occur while the activity containing the map is
     * paused but is included to demonstrate best practices (e.g., if a background service were
     * to be used).
     */
    private var paused = false

    override fun activate(listener: OnLocationChangedListener) {
        this.listener = listener
    }

    override fun deactivate() {
        listener = null
    }

    override fun onMapLongClick(point: LatLng) {
        if (paused) {
            return
        }

        val location = Location("LongPressLocationProvider")
        location.latitude = point.latitude
        location.longitude = point.longitude
        location.accuracy = 100f
        listener?.onLocationChanged(location)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        paused = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        paused = false
    }
}