package com.dsmagic.kibira

import android.location.Location
import com.google.android.gms.maps.LocationSource

interface LocationChanged {
    fun onLocationChanged(loc: Location, fix: LongLat.FixType)
}

class RtkLocationSource : LocationSource {
    var listener: LocationSource.OnLocationChangedListener? = null
    var locationChanged: LocationChanged? = null
    override fun activate(p0: LocationSource.OnLocationChangedListener) {
        listener = p0
    }

    override fun deactivate() {
     deactivate()
    }

    fun setLocationChangedTrigger(locationChanged: LocationChanged) {
        this.locationChanged = locationChanged
    }

    fun postNewLocation(l: Location,fix:LongLat.FixType) {
        listener?.onLocationChanged(l)
        locationChanged?.onLocationChanged(l,fix) // trigger it...
    }
}