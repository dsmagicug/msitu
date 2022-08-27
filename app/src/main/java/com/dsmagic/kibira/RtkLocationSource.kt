package com.dsmagic.kibira

import android.location.Location
import com.google.android.gms.maps.LocationSource

interface LocationChanged {
    fun onLocationChanged(loc: Location)
}

// Used by GoogleMap API to provide location on the map...
class RtkLocationSource : LocationSource {
    var listener: LocationSource.OnLocationChangedListener? = null
    var locationChanged: LocationChanged? = null
    override fun activate(p0: LocationSource.OnLocationChangedListener) {
        listener = p0
    }

    override fun deactivate() {
        // TODO("Not yet implemented")
    }

    fun setLocationChangedTrigger(locationChanged: LocationChanged) {
        this.locationChanged = locationChanged
    }

    fun postNewLocation(l: Location) {
        listener?.onLocationChanged(l)
        locationChanged?.onLocationChanged(l) // trigger it...
    }
}